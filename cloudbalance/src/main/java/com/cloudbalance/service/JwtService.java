package com.cloudbalance.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY = "cloudbalancewebprojecttrainingmanavlukarfullstackdevelopjasjajsjaskasaker";

    private final TokenBlacklistService tokenBlacklistService;

    public JwtService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }
    // Method to validate the refresh token and extract the user's email
    public String validateRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(refreshToken)
                    .getBody();

            return claims.getSubject(); // The email of the user
        } catch (Exception e) {
            return null; // Invalid refresh token
        }
    }

    // Method to generate a new access token for the user
    public String generateAccessToken(String email) {
        long now = System.currentTimeMillis();
        long expirationMillis = 1000 * 60 * 15; // Access token valid for 15 minutes

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Use a secure key
                .compact();
    }

    // ✅ Extract email from token (used as username/subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ✅ Extract a specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ✅ Generate token with custom claims (including roles)
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Include roles in claims
        claims.put("roles", userDetails.getAuthorities());

        return createToken(claims, userDetails.getUsername());
    }

    // ✅ Core token creation logic
    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        long expirationMillis = 1000 * 60 * 15; // 15 minutes

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // email
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // ✅ Validate token

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !tokenBlacklistService.isTokenBlacklisted(token); // ✅ Check blacklist
    }

    // ✅ Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ✅ Get expiration date from token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ✅ Parse all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

//    public String generateRefreshToken(UserDetails userDetails){
//        long now = System.currentTimeMillis();
//        long expirationMillis = 1000 * 60 ;
//        return Jwts.builder()
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(now))
//                .setExpiration(new Date(now + expirationMillis))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }


}
