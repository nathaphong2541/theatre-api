package com.thaitheatre.api.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thaitheatre.api.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
                // ✅ อนุญาตเฉพาะ public endpoints
                || matcher.match("/api/auth/register", path)
                || matcher.match("/api/auth/login", path)
                || matcher.match("/api/auth/forgot-password", path)
                || matcher.match("/api/auth/reset-password", path)
                // ✅ swagger / error / actuator
                || matcher.match("/swagger-ui.html", path)
                || matcher.match("/swagger-ui/**", path)
                || matcher.match("/v3/api-docs/**", path)
                || matcher.match("/error", path)
                || matcher.match("/actuator/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // ยังไม่ได้ auth ใน context เท่านั้นจึงตรวจ JWT
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // 1) พยายามอ่านจาก Authorization header ก่อน
            String bearer = req.getHeader("Authorization"); // case-insensitive
            String token = null;

            if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                token = bearer.substring(7);
            }

            // 2) ถ้า header ไม่มี ลองอ่านจากคุกกี้ (fallback)
            if (!StringUtils.hasText(token)) {
                token = jwt.resolveFromCookie(req, "access_token");
            }

            if (StringUtils.hasText(token)) {
                try {
                    String subject = jwt.validateAndGetSubject(token);

                    // รองรับทั้งกรณี subject เป็น userId (ตัวเลข) หรือเป็น username/email
                    UserDetails user;
                    try {
                        user = uds.loadUserById(Long.parseLong(subject));
                    } catch (NumberFormatException nfe) {
                        user = uds.loadUserByUsername(subject);
                    }

                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (Exception e) {
                    // invalid token → ไม่ตั้งค่า auth แล้วปล่อยไปให้ chain ทำงานต่อ (จะไปเจอ 401 ตาม rule)
                    // ถ้าต้องการ log ให้ debug: logger.debug("Invalid JWT", e);
                }
            }
        }

        chain.doFilter(req, res);
    }
}
