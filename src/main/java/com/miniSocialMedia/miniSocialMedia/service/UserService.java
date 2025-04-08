package com.miniSocialMedia.miniSocialMedia.service;

import com.miniSocialMedia.miniSocialMedia.config.RequestLoggingFilter;
import com.miniSocialMedia.miniSocialMedia.dto.PostDTO;
import com.miniSocialMedia.miniSocialMedia.dto.UserDto;
import com.miniSocialMedia.miniSocialMedia.exception.ResourceNotFoundException;
import com.miniSocialMedia.miniSocialMedia.models.Post;
import com.miniSocialMedia.miniSocialMedia.models.User;
import com.miniSocialMedia.miniSocialMedia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private UserDto toUserDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setProfilePicture(user.getProfilePicture());

        // Map the posts (assuming Post is the same in both User and UserDto for now)
//        List<Post> posts = user.getPosts() != null ? new ArrayList<>(user.getPosts()) : new ArrayList<>();
//        dto.setPosts(posts);

        return dto;
    }


    public String signUp(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        logger.info("User registered : {}", user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    public List<UserDto> getAllUsers() {

        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toUserDto) // Map each User to UserDto
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " does not exist"));
        return toUserDto(user);
    }
}

