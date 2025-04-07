package com.miniSocialMedia.miniSocialMedia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.miniSocialMedia.miniSocialMedia.exception.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        logger.info("ObjectMapper WRITE_DATES_AS_TIMESTAMPS enabled: {}", objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        // Test serialization of LocalDateTime directly
        try {
            LocalDateTime testDateTime = LocalDateTime.now();
            String serialized = objectMapper.writeValueAsString(testDateTime);
            logger.info("Direct serialization of LocalDateTime: {}", serialized);
        } catch (Exception e) {
            logger.error("Error during direct serialization test", e);
        }
    }
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        logger.warn("Authentication failed for request to {}: {}", request.getRequestURI(), authException.getMessage());

        // Set the response status and headers
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");

        // Create the error response
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid username or password",
                LocalDateTime.now()
        );

        // Configure ObjectMapper to handle LocalDateTime
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule to handle LocalDateTime

        // Write the response
        mapper.writeValue(response.getWriter(), errorResponse);

        // Ensure the response is committed
        response.getWriter().flush();
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("miniSocialMedia");
        super.afterPropertiesSet();
    }
}