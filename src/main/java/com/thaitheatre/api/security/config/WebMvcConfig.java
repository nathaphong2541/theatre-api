package com.thaitheatre.api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.files.profile-dir}")
    private String profileDir;

    @Value("${upload.script.dir}")
    private String scriptDir;

    @Value("${upload.script.pdf.dir}")
    private String scriptPdfDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // profile
        registry.addResourceHandler("/files/profile/**")
                .addResourceLocations("file:" + profileDir + "/");

        // images (scripts)
        registry.addResourceHandler("/files/scripts/**")
                .addResourceLocations("file:" + scriptDir + "/");

        // pdf
        registry.addResourceHandler("/files/scripts/pdf/**")
                .addResourceLocations("file:" + scriptPdfDir + "/");
      
    }
}
