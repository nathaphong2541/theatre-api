package com.thaitheatre.api.security.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import com.thaitheatre.api.security.JwtAuthFilter;
import com.thaitheatre.api.security.JwtUtil;
import com.thaitheatre.api.service.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.exp-minutes}")
    private long expMinutes;

    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret, expMinutes);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            UserDetailsServiceImpl uds,
            JwtUtil jwt,
            BCryptPasswordEncoder encoder) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration c = new CorsConfiguration();
                c.setAllowedOriginPatterns(List.of("*"));
                c.setAllowCredentials(true);
                c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                c.setAllowedHeaders(List.of("*"));
                c.setExposedHeaders(List.of("Authorization"));
                c.setMaxAge(3600L);
                return c;
            }))
            .sessionManagement(sm -> sm.sessionCreationPolicy(
                org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/api/profiles/**",
                    "/files/**",    
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/error"
                ).permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            .logout(l -> l.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                res.setStatus(401);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"message\":\"Unauthorized\"}");
            }))
            // ✅ บอก Spring ให้ใช้ DaoAuthenticationProvider ที่เรากำหนด
            .authenticationProvider(authProvider(uds, encoder))
            // ✅ แทรก JWT filter ให้ตั้ง Authentication จาก Bearer token
            .addFilterBefore(new JwtAuthFilter(jwt, uds),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(UserDetailsServiceImpl uds, BCryptPasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
