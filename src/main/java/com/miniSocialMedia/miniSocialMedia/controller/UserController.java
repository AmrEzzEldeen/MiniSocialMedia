package com.miniSocialMedia.miniSocialMedia.controller;

import com.miniSocialMedia.miniSocialMedia.dto.UserDto;
import com.miniSocialMedia.miniSocialMedia.models.Post;
import com.miniSocialMedia.miniSocialMedia.models.User;
import com.miniSocialMedia.miniSocialMedia.service.PostService;
import com.miniSocialMedia.miniSocialMedia.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final PostService postService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp (@RequestBody User user) {
        return ResponseEntity.ok(userService.signUp(user));
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }


    @GetMapping({"", "/"})
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
}


