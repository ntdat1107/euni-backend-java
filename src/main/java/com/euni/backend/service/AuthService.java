package com.euni.backend.service;

import io.jsonwebtoken.Jwts;
import java.util.Date;

import com.euni.backend.dto.request.LoginRequest;
import com.euni.backend.dto.response.TokenResponse;
import com.euni.backend.entity.User;
import com.euni.backend.repository.UserRepository;
import com.euni.backend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public TokenResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
                
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        // 1. Generate access token with tokenVersion
        String jwt = jwtUtils.generateTokenFromUsernameAndVersion(user.getUsername(), user.getTokenVersion());

        // 2. Refresh token logic (Simple implementation using JWT with longer expiration)
        // Here we just generate a simple JWT with a longer expiration (e.g. 7 days) and include tokenVersion.
        String refreshToken = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("tokenVersion", user.getTokenVersion())
                .claim("isRefreshToken", true)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + 604800000)) // 7 days
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtUtils.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8)), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        return TokenResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .tokenVersion(user.getTokenVersion())
                .roles(roles)
                .build();
    }

    public TokenResponse refreshToken(String requestRefreshToken) {
        if (jwtUtils.validateJwtToken(requestRefreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
            Long tokenVersion = jwtUtils.getTokenVersionFromJwtToken(requestRefreshToken);
            
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.getTokenVersion().equals(tokenVersion)) {
                throw new RuntimeException("Refresh token was invalidated");
            }
            
            List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getCode())
                .collect(Collectors.toList());

            String newJwt = jwtUtils.generateTokenFromUsernameAndVersion(user.getUsername(), user.getTokenVersion());

            return TokenResponse.builder()
                .accessToken(newJwt)
                .refreshToken(requestRefreshToken) // Keep old refresh token, or rotate it
                .username(user.getUsername())
                .fullName(user.getFullName())
                .tokenVersion(user.getTokenVersion())
                .roles(roles)
                .build();
        }
        throw new RuntimeException("Refresh token is invalid!");
    }
}
