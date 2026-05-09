package com.euni.backend.constant;

public class AppConstants {
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // JWT Constants (Should ideally be in application.yml)
    public static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days
    
    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_LECTURER = "LECTURER";
    
    // Permissions
    public static final String PERM_MAKER = "MAKER";
    public static final String PERM_APPROVER = "APPROVER";
    public static final String PERM_VIEWER = "VIEWER";

    private AppConstants() {}
}
