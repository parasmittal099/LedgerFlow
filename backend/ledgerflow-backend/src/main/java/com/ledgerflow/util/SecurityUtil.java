package com.ledgerflow.util;

import com.ledgerflow.security.JwtAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security Utility Class
 * 
 * Helper class to get current authenticated user information
 * Similar to .NET's HttpContext.User or IHttpContextAccessor
 */
public class SecurityUtil {
    
    /**
     * Get current authenticated username
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * Get current authenticated user ID from JWT token
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.UserDetails) {
            JwtAuthenticationFilter.UserDetails userDetails = 
                (JwtAuthenticationFilter.UserDetails) authentication.getDetails();
            return userDetails.getUserId();
        }
        return null;
    }
    
    /**
     * Get current authenticated tenant ID from JWT token
     */
    public static Long getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.UserDetails) {
            JwtAuthenticationFilter.UserDetails userDetails = 
                (JwtAuthenticationFilter.UserDetails) authentication.getDetails();
            return userDetails.getTenantId();
        }
        return null;
    }
    
    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}

