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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = String.valueOf(user.getRole());
        // Ensure "ROLE_" prefix is used if Spring expects that format
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        return Collections.singleton(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Hashed (BCrypt) password
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Used for authentication
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Add logic here if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add logic here if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add logic here if needed
    }

    @Override
    public boolean isEnabled() {
        return true; // Could check user.isActive() if such a field exists
    }

    public User getUser() {
        return user; // Helpful if you want to access full user info later
    }
}
