package com.thaitheatre.api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.files.profile-dir}")
    private String profileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ==========================
        // 1) เปิดให้โหลดรูปโปรไฟล์
        // ==========================
        registry.addResourceHandler("/files/profile/**")
                .addResourceLocations("file:" + profileDir + "/");

        // ==========================
        // 2) เปิดให้โหลดไฟล์ uploads (รูปบทละคร, pdf ฯลฯ)
        // ==========================
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
