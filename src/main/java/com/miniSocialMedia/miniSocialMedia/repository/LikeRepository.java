package com.miniSocialMedia.miniSocialMedia.repository;

import com.miniSocialMedia.miniSocialMedia.models.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByPostId(Long postId);

    boolean existsByPostIdAndUserId(long postId, long userId);

    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    void deleteAllByPostId(Long postId);
}
