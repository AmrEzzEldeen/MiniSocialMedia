package com.miniSocialMedia.miniSocialMedia.service;

import com.miniSocialMedia.miniSocialMedia.exception.ResourceNotFoundException;
import com.miniSocialMedia.miniSocialMedia.models.Image;
import com.miniSocialMedia.miniSocialMedia.models.Post;
import com.miniSocialMedia.miniSocialMedia.models.User;
import com.miniSocialMedia.miniSocialMedia.repository.ImageRepository;
import com.miniSocialMedia.miniSocialMedia.repository.PostRepository;
import com.miniSocialMedia.miniSocialMedia.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String UPLOAD_DIR;

    @PostConstruct
    public void init() throws IOException {
        if (!UPLOAD_DIR.endsWith("/")) {
            UPLOAD_DIR = UPLOAD_DIR + "/";
        }
        Files.createDirectories(Paths.get(UPLOAD_DIR));
    }

    public String uploadImage(Long postId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with ID " + postId + " does not exist"));

        // Authorization: Check if the authenticated user owns this post
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!post.getUser().getId().equals(authenticatedUserId)) {
            throw new SecurityException("You are not authorized to upload images for this post");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String uniqueFilename = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        Files.write(filePath, file.getBytes());

        Image image = new Image();
        image.setPost(post);
        image.setUrl("/" + UPLOAD_DIR + uniqueFilename);
        imageRepository.save(image);

        return image.getUrl();
    }

    public void deleteImage(String imageUrl) throws IOException {
        String expectedPrefix = "/" + UPLOAD_DIR;
        if (imageUrl == null || !imageUrl.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("Invalid image URL: " + imageUrl + ". Expected to start with " + expectedPrefix);
        }

        String relativePath = imageUrl.substring(1);
        Path filePath = Paths.get(relativePath);
        Files.deleteIfExists(filePath);
    }

    public void deleteImageById(Long id) throws IOException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image with ID " + id + " does not exist"));

        // Authorization: Check if the authenticated user owns the post this image belongs to
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!image.getPost().getUser().getId().equals(authenticatedUserId)) {
            throw new SecurityException("You are not authorized to delete this image");
        }

        deleteImage(image.getUrl());
        imageRepository.delete(image);
    }

    private Long getAuthenticatedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + username));
        return user.getId();
    }
}