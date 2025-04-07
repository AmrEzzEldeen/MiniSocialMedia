package com.miniSocialMedia.miniSocialMedia.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "postId", nullable = false)
    @JsonBackReference
    private Post post;
    @Column(nullable = false)
    private String url;
}