package com.miniSocialMedia.miniSocialMedia.service;

import com.miniSocialMedia.miniSocialMedia.dto.PostDTO;
import com.miniSocialMedia.miniSocialMedia.exception.NotAuthorizedException;
import com.miniSocialMedia.miniSocialMedia.exception.ResourceNotFoundException;
import com.miniSocialMedia.miniSocialMedia.models.*;
import com.miniSocialMedia.miniSocialMedia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final LikeRepository likeRepository;
    private final DislikeRepository dislikeRepository;

    public PostDTO updatePost(Long id, String content, List<MultipartFile> newImages) throws IOException {
        Post post = postRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with ID " + id + " does not exist"));

        // Authorization: Check if the authenticated user owns this post
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!post.getUser().getId().equals(authenticatedUserId)) {
            throw new SecurityException("You are not authorized to update this post");
        }

        if (content != null && !content.trim().isEmpty()) {
            post.setContent(content);
        }

        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = imageService.uploadImage(post.getId(), file);
                }
            }
        }

        postRepository.save(post);
        return getPostById(id);
    }

    public Long createPostWithImages(Long userId, String content, List<MultipartFile> files) throws IOException {
        // Authorization: Ensure the userId matches the authenticated user
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!userId.equals(authenticatedUserId)) {
            throw new SecurityException("You are not authorized to create a post for another user");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with ID " + userId + " does not exist");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be null or empty");
        }

        Post post = new Post();
        post.setUser(userRepository.findById(userId).get());
        post.setContent(content);
        post.setLikes(0);
        post.setDislikes(0);
        Post savedPost = postRepository.save(post);

        List<String> imageUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = imageService.uploadImage(savedPost.getId(), file);
                    imageUrls.add(imageUrl);
                }
            }
        }

        return savedPost.getId();
    }

    public List<Post> getPostsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with ID " + userId + " does not exist");
        }
        return postRepository.findByUserId(userId);
    }

    public List<PostDTO> getAllPosts(Pageable pageable) {
        Page<Post> postPage = postRepository.findAll(pageable);
        List<Post> posts = postPage.getContent();
        if (!posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
            List<Post> postsWithImages = postRepository.findByIdsWithImages(postIds);
            Map<Long, Post> postMap = postsWithImages.stream().collect(Collectors.toMap(Post::getId, post -> post));
            List<Post> orderedPosts = postIds.stream().map(postMap::get).filter(Objects::nonNull).collect(Collectors.toList());

            return orderedPosts.stream().map(this::mapToPostDTO).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public PostDTO getPostById(Long id) {
        Post post = postRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with ID " + id + " does not exist"));
        return mapToPostDTO(post);
    }



    @Transactional
    public void deletePost(Long id) throws IOException {
        Post post = postRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with ID " + id + " does not exist"));

        // Authorization: Check if the authenticated user owns this post
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!post.getUser().getId().equals(authenticatedUserId)) {
            throw new SecurityException("You are not authorized to delete this post");
        }
        likeRepository.deleteAllByPostId(id);
        dislikeRepository.deleteAllByPostId(id);
        for (Image image : post.getImages()) {
            imageService.deleteImage(image.getUrl());
        }

        postRepository.delete(post);
    }

    private PostDTO mapToPostDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setImages(post.getImages());
        dto.setLikes(post.getLikes());
        dto.setDislikes(post.getDislikes());

        PostDTO.UserDTO userDTO = new PostDTO.UserDTO();
        userDTO.setId(post.getUser().getId());
        userDTO.setUsername(post.getUser().getUsername());
        userDTO.setBio(post.getUser().getBio());
        userDTO.setProfilePicture(post.getUser().getProfilePicture());
        dto.setUser(userDTO);

        return dto;
    }

    private Long getAuthenticatedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + username));
        return user.getId();
    }

    public String like(Long postId, Long userId) {
        // Fetch authenticated user's ID from the security context (JWT)
        Long authenticatedUserId = getAuthenticatedUserId();
        // Verify that the provided userId matches the authenticated user's ID
        if (!authenticatedUserId.equals(userId)) {
            throw new NotAuthorizedException("Unauthorized action: You can only like posts as yourself.");
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new NotAuthorizedException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NotAuthorizedException("User not found"));

        // Check if the user has already disliked the post
        if (dislikeRepository.existsByPostIdAndUserId(postId, userId)) {
            // If the user has disliked the post, remove the dislike first
            Dislike existingDislike = dislikeRepository.findByPostIdAndUserId(postId, userId)
                    .orElseThrow(() -> new NotAuthorizedException("Dislike not found"));
            dislikeRepository.delete(existingDislike);
            post.setDislikes(post.getDislikes() - 1);
        }

        // Check if the user has already liked the post
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return "User already liked this post";
        }

        // Proceed to add the like
        Like like = new Like();
        like.setPostId(post.getId());
        like.setUserId(authenticatedUserId);
        likeRepository.save(like);
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);

        return "Post liked successfully!";
    }


    public String dislike(Long postId, Long userId) {
        // Fetch authenticated user's ID from the security context (JWT)
        Long authenticatedUserId = getAuthenticatedUserId();
        // Verify that the provided userId matches the authenticated user's ID
        if (!authenticatedUserId.equals(userId)) {
            throw new NotAuthorizedException("Unauthorized action: You can only dislike posts as yourself.");
        }
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotAuthorizedException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NotAuthorizedException("User not found"));

        // Check if the user has already liked the post
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            // If the user has liked the post, remove the like first
            Like existingLike = likeRepository.findByPostIdAndUserId(postId, userId)
                    .orElseThrow(() -> new NotAuthorizedException("Like not found"));
            likeRepository.delete(existingLike);
            post.setLikes(post.getLikes() - 1);
        }

        // Check if the user has already disliked the post
        if (dislikeRepository.existsByPostIdAndUserId(postId, userId)) {
            return "User already disliked this post";
        }

        // Proceed to add the dislike
        Dislike dislike = new Dislike();
        dislike.setPostId(post.getId());
        dislike.setUserId(authenticatedUserId);
        dislikeRepository.save(dislike);
        post.setDislikes(post.getDislikes() + 1);
        postRepository.save(post);

        return "Post disliked successfully!";
    }


    public String unlikePost(Long postId, Long userId) {
        // Fetch authenticated user's ID from the security context (JWT)
        Long authenticatedUserId = getAuthenticatedUserId();
        // Verify that the provided userId matches the authenticated user's ID
        if (!authenticatedUserId.equals(userId)) {
            throw new NotAuthorizedException("Unauthorized action: You can only remove like from posts as yourself.");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotAuthorizedException("Post not found"));

        Like like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new NotAuthorizedException("Like not found"));

        likeRepository.delete(like);

        // Decrement the like count
        post.setLikes(post.getLikes() - 1);
        postRepository.save(post);

        return "Like removed successfully!";

    }

    public String undoDislike(Long postId, Long userId) {
        // Fetch authenticated user's ID from the security context (JWT)
        Long authenticatedUserId = getAuthenticatedUserId();
        // Verify that the provided userId matches the authenticated user's ID
        if (!authenticatedUserId.equals(userId)) {
            throw new NotAuthorizedException("Unauthorized action: You can only remove dislike from posts as yourself.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotAuthorizedException("Post not found"));

        Dislike dislike = dislikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new NotAuthorizedException("Dislike not found"));

        dislikeRepository.delete(dislike);

        // Decrement the dislike count
        post.setDislikes(post.getDislikes() - 1);
        postRepository.save(post);

        return "Dislike removed successfully!";

    }
}