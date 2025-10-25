package com.thaitheatre.api.security.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Thai Theatre API",
                version = "1.0.0",
                description = "ระบบสมัครสมาชิก / ล็อกอิน (JWT + Swagger UI)"
        ),
        security = {
            @SecurityRequirement(name = "bearerAuth") // ทำให้ทุก endpoint ต้องการ bearer โดยค่าเริ่มต้น
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT" // แค่ label ให้ UI
)
public class OpenApiConfig {
    // ไม่ต้องมี @Bean อะไรเพิ่ม
}
