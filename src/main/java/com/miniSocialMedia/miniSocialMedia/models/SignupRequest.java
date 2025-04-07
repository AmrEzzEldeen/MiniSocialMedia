package com.miniSocialMedia.miniSocialMedia.models;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SignupRequest {
    private String username;
    private String password;
}
