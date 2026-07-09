package com.daisy.health.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig {
    @Value("${daisy.upload-dir:uploads}")
    private String uploadDir;

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, PermissionService permissionService, ObjectMapper objectMapper) {
        return new JwtAuthFilter(jwtService, permissionService, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<JwtAuthFilter>();
        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/api/v1/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
                if (!uploadPath.endsWith("/")) {
                    uploadPath = uploadPath + "/";
                }
                registry.addResourceHandler("/uploads/**").addResourceLocations(uploadPath);
            }
        };
    }
}
