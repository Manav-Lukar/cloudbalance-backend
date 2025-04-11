package com.cloudbalance.service;

import com.cloudbalance.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // ✅ Return user role as a GrantedAuthority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the getter method to access the role name
        String role = "ROLE_" + user.getRole().getName();
        return Collections.singleton(new SimpleGrantedAuthority(role));
    }

    // ✅ Return hashed password
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // ✅ Return email as username for authentication
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Add custom logic if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add custom logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add custom logic if needed
    }

    @Override
    public boolean isEnabled() {
        return true; // Return user.isActive() if you track this in your entity
    }

    // ✅ Helpful to access original User entity
    public User getUser() {
        return this.user;
    }
}
