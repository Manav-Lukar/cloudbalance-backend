package com.cloudbalance.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    // Secret key for signing JWT tokens (use a secure key in production)
    private final String SECRET_KEY = "cloudbalancewebprojecttrainingmanavlukarfullstackdevelopjasjajsjaskasaker";

    // Secure key for signing JWT
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private final TokenBlacklistService tokenBlacklistService;

    public JwtService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // ✅ Validate refresh token and return email (subject)
    public String validateRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(refreshToken)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null; // Invalid token
        }
    }

    // ✅ Generate access token (valid for 15 minutes)
    public String generateAccessToken(String email) {
        long now = System.currentTimeMillis();
        long expirationMillis = 1000 * 60 * 15; // 15 minutes expiration

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username (email) from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //  Generate a custom JWT token with additional claims
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities()); // Store roles in the token
        return createToken(claims, userDetails.getUsername());
    }

    // Core method to create a token with claims and subject (email)
    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        long expirationMillis = 1000 * 60 * 15; // 15 minutes expiration

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256) // Secure key for signing
                .compact();
    }
    // Refresh token generator
    public String generateRefreshToken(String username) {
        long now = System.currentTimeMillis();
        long expirationMillis = 1000 * 60 * 60;
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256) // Secure key for signing
                .compact();
    }

    // Validate if token is valid (not expired, matching user, and not blacklisted)
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !tokenBlacklistService.isTokenBlacklisted(token); // Check blacklist
    }
    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Parse and extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key) // Use the secure key for parsing
                .parseClaimsJws(token)
                .getBody();
    }


}