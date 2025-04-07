package com.miniSocialMedia.miniSocialMedia.service;

import com.miniSocialMedia.miniSocialMedia.exception.ResourceNotFoundException;
import com.miniSocialMedia.miniSocialMedia.models.User;
import com.miniSocialMedia.miniSocialMedia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String signUp(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        userRepository.save(user);
        return "User registered successfully";
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " does not exist"));
    }
}

