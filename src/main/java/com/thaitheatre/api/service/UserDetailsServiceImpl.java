package com.thaitheatre.api.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.model.entity.UserAccount;
import com.thaitheatre.api.repository.UserRepository;
import com.thaitheatre.api.security.CustomUserDetails;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toUserDetails(u);
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        UserAccount u = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toUserDetails(u);
    }

    private UserDetails toUserDetails(UserAccount u) {
        // ✅ ตอนนี้คุณ fix role เป็น USER อยู่
        // ถ้าในอนาคตมี role จริงใน DB ค่อย map เพิ่ม
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new CustomUserDetails(
                u.getId(), // ✅ สำคัญ: userId เก็บตรงนี้
                u.getEmail() != null ? u.getEmail() : String.valueOf(u.getId()),
                u.getPasswordHash(),
                authorities);
    }
}
