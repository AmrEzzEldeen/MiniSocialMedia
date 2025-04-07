package com.miniSocialMedia.miniSocialMedia.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference
    private User user;
    @NotBlank(message = "Content is required")
    @Column(nullable = false)
    private String content;
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now(); // Set timestamp before saving
    }
}
