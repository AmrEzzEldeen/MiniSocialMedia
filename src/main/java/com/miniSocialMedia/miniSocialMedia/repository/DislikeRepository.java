package com.miniSocialMedia.miniSocialMedia.repository;

import com.miniSocialMedia.miniSocialMedia.models.Dislike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DislikeRepository extends JpaRepository<Dislike, Long> {
    long countByPostId(Long postId);

    boolean existsByPostIdAndUserId(long postId, long userId);


    Optional<Dislike> findByPostIdAndUserId(Long postId, Long userId);
    void deleteAllByPostId(Long postId);
}
