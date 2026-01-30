package com.ledgerflow.security;

import com.ledgerflow.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * 
 * This filter intercepts HTTP requests and validates JWT tokens.
 * Similar to .NET's JwtBearerAuthenticationHandler or custom middleware
 * 
 * How it works:
 * 1. Extracts JWT token from httpOnly cookie or Authorization header
 * 2. Validates the token
 * 3. Sets authentication in Spring Security context
 * 4. Allows request to proceed to controller
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            Long tenantId = jwtUtil.extractTenantId(token);
            
            // Create authentication object
            // Similar to .NET's ClaimsPrincipal with claims
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            
            // Store additional user info in details
            UserDetails userDetails = new UserDetails(userId, tenantId, username);
            authentication.setDetails(userDetails);
            
            // Set authentication in Spring Security context
            // This makes the user "authenticated" for this request
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from request
     * Checks both httpOnly cookie and Authorization header
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // First, try to get from httpOnly cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // Fallback: try Authorization header (Bearer token)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
    
    /**
     * Simple class to store user details in authentication
     */
    public static class UserDetails {
        private final Long userId;
        private final Long tenantId;
        private final String username;
        
        public UserDetails(Long userId, Long tenantId, String username) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.username = username;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public Long getTenantId() {
            return tenantId;
        }
        
        public String getUsername() {
            return username;
        }
    }
}

