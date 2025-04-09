package com.miniSocialMedia.miniSocialMedia.controller;

import com.miniSocialMedia.miniSocialMedia.dto.PostDTO;
import com.miniSocialMedia.miniSocialMedia.repository.LikeRepository;
import com.miniSocialMedia.miniSocialMedia.repository.PostRepository;
import com.miniSocialMedia.miniSocialMedia.repository.UserRepository;
import com.miniSocialMedia.miniSocialMedia.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Long id,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages) throws IOException {
        return ResponseEntity.ok(postService.updatePost(id, content, newImages));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) throws IOException {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createPostWithImages(
            @RequestParam("userId") Long userId,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request) throws IOException {
        logger.info("Entering createPostWithImages");
        logger.info("Received Content-Type: {}", request.getContentType());
        logger.info("UserId: {}, Content: {}, Files: {}", userId, content, files != null ? files.size() : 0);
        return ResponseEntity.ok(postService.createPostWithImages(userId, content, files));
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort sorting = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            sorting = sorting.descending();
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        return ResponseEntity.ok(postService.like(postId, userId));
    }
    @PostMapping("/{postId}/dislike")
    public ResponseEntity<String> dislikePost(@PathVariable Long postId, @RequestParam Long userId) {
        return ResponseEntity.ok(postService.dislike(postId, userId));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<String> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        return ResponseEntity.ok(postService.unlikePost(postId, userId));
    }

    @DeleteMapping("/{postId}/dislike")
    public ResponseEntity<String> undoDislike(@PathVariable Long postId, @RequestParam Long userId) {
        return ResponseEntity.ok(postService.undoDislike(postId, userId));
    }

}