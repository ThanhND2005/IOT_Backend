package com.app.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC configuration:
 * 1. Automatically prefixes all @RestController routes with /api/v1 (configurable in application.yml)
 * 2. Configures CORS mappings
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.api.prefix:/api/v1}")
    private String apiPrefix;

    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(apiPrefix, 
                HandlerTypePredicate.builder()
                        .basePackage("com.app")
                        .annotation(RestController.class)
                        .build()
        );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
