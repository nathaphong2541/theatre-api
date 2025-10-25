package com.thaitheatre.api.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles("USER")
                .build();
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        var u = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles("USER")
                .build();
    }

    private UserDetails toUserDetails(UserAccount u) {
        // 🟢 default role เป็น USER (หรือ ADMIN ตามที่ต้องการ)
        String role = "USER";

        return User.builder()
                .username(u.getEmail() != null ? u.getEmail() : String.valueOf(u.getId()))
                .password(u.getPasswordHash())
                .roles(role)
                .disabled(false)
                .build();
    }
}
