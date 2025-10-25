package com.thaitheatre.api.security;

import com.thaitheatre.api.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserDetailsServiceImpl uds;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtAuthFilter(JwtUtil jwt, UserDetailsServiceImpl uds) {
        this.jwt = jwt;
        this.uds = uds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || matcher.match("/api/auth/**", path)
                || matcher.match("/swagger-ui.html", path)
                || matcher.match("/swagger-ui/**", path)
                || matcher.match("/v3/api-docs/**", path)
                || matcher.match("/error", path)
                || matcher.match("/actuator/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String header = req.getHeader(HttpHeaders.AUTHORIZATION);

            if (StringUtils.hasText(header)) {
                String h = header.trim();
                String token = h.regionMatches(true, 0, "Bearer ", 0, 7)
                        ? h.substring(7).trim()
                        : h;

                if (StringUtils.hasText(token)) {
                    try {
                        String subject = jwt.validateAndGetSubject(token); // อาจเป็น "1" หรืออีเมล

                        UserDetails user;
                        try {
                            // กรณี sub เป็น userId (เช่น "1")
                            Long userId = Long.valueOf(subject);
                            user = uds.loadUserById(userId);   // ✅ ต้องมีเมธอดนี้ใน UDS
                        } catch (NumberFormatException nf) {
                            // กรณี sub เป็น username/email
                            user = uds.loadUserByUsername(subject);
                        }

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);

                    } catch (Exception ignored) {
                        // token ไม่ถูกต้อง/หมดอายุ/หา user ไม่พบ -> ปล่อยไปให้ entry point ตอบ 401
                    }
                }
            }
        }

        chain.doFilter(req, res);
    }
}
