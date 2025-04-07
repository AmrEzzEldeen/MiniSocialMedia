package com.miniSocialMedia.miniSocialMedia.controller;

import com.miniSocialMedia.miniSocialMedia.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    @PostMapping(value = "/upload/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@PathVariable Long postId,
                                              @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(postId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) throws IOException {
        imageService.deleteImageById(id);
        return ResponseEntity.noContent().build();
    }
}