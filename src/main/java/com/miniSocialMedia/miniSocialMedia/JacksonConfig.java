package com.miniSocialMedia.miniSocialMedia;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {
    private static final Logger logger = LoggerFactory.getLogger(JacksonConfig.class);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        logger.info("Configuring Jackson2ObjectMapperBuilderCustomizer to disable WRITE_DATES_AS_TIMESTAMPS");
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            builder.modules(javaTimeModule);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}