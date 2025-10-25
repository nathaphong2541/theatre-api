package com.thaitheatre.api.service;

import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return toUserDetails(u);
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        UserAccount u = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found id=" + id));
        return toUserDetails(u);
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
