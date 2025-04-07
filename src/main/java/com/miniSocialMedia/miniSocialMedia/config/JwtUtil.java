package com.miniSocialMedia.miniSocialMedia.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    // Use a secure key for signing the JWT (in production, store this in a configuration file or environment variable)
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // Generate a JWT with a 30-day expiration
    public String generateToken(String username) {
        Instant now = Instant.now();
//        Instant expiration = now.plus(30, ChronoUnit.DAYS); // Token expires in 30 days
        Instant expiration = now.plus(1, ChronoUnit.MINUTES); // Token expires in 1 minute
        byte[] keyBytes = key.getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        System.out.println("THE KEY (Base64): " + base64Key);
        System.out.println("THE KEY (Raw Bytes Length): " + keyBytes.length);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    // Validate the JWT and extract the username
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Check if the token is expired
    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().before(new Date());
    }
}