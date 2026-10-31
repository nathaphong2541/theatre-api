package com.thaitheatre.api.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.thaitheatre.api.common.DelFlag;
import com.thaitheatre.api.common.RecordStatus;
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
        // Deleted / deactivated accounts must not authenticate (login OR via an
        // existing cookie). Treat them as "not found" so no account info leaks.
        if (u.getDelFlag() == DelFlag.Y || u.getRecordStatus() == RecordStatus.I) {
            throw new UsernameNotFoundException("User not found");
        }

        // Map the real role from the DB so hasRole('ADMIN') works. Spring's
        // hasRole adds the "ROLE_" prefix, so the authority must be ROLE_<ROLE>.
        String roleName = u.getRole() != null ? u.getRole().name() : "USER";
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        return new CustomUserDetails(
                u.getId(), // ✅ สำคัญ: userId เก็บตรงนี้
                u.getEmail() != null ? u.getEmail() : String.valueOf(u.getId()),
                u.getPasswordHash(),
                authorities);
    }
}
