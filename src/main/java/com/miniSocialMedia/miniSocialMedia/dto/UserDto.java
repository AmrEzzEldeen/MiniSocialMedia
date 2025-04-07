package com.miniSocialMedia.miniSocialMedia.dto;

import com.miniSocialMedia.miniSocialMedia.models.Post;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto {

    private Long id;
    private String username;
    private String bio;
    private String profilePicture;
    private List<Post> posts = new ArrayList<>();
}
