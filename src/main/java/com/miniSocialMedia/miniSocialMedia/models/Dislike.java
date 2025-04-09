package com.miniSocialMedia.miniSocialMedia.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data

public class Dislike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long postId;
    private long userId;
}