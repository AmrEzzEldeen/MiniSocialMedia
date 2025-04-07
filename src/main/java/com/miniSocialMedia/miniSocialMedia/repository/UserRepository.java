package com.miniSocialMedia.miniSocialMedia.repository;

import com.miniSocialMedia.miniSocialMedia.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
