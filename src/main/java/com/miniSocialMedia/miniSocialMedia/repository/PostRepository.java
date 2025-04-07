package com.miniSocialMedia.miniSocialMedia.repository;

import com.miniSocialMedia.miniSocialMedia.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    // Custom query to fetch posts with images
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.images WHERE p.id IN :ids")
    List<Post> findByIdsWithImages(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Post> findByIdWithImages(@Param("id") Long id);
}
