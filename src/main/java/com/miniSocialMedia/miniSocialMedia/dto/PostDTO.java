package com.miniSocialMedia.miniSocialMedia.dto;

import com.miniSocialMedia.miniSocialMedia.models.Image;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDTO {
    private Long id;
    private String content;
    private Integer likes;
    private Integer dislikes;
    private LocalDateTime createdAt;
    private UserDTO user;
    private List<Image> images;

    @Data
    public static class UserDTO {
        private Long id;
        private String username;
        private String bio;
        private String profilePicture;
    }
}