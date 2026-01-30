package com.ledgerflow.controller;

import com.ledgerflow.dto.AuthResponse;
import com.ledgerflow.dto.LoginRequest;
import com.ledgerflow.dto.RegisterRequest;
import com.ledgerflow.entity.User;
import com.ledgerflow.service.AuthService;
import com.ledgerflow.util.SecurityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles user registration, login, logout, and current user info.
 * Sets httpOnly cookies for JWT tokens (similar to .NET's CookieAuthenticationOptions)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse httpResponse) {
        try {
            AuthResponse response = authService.register(request);
            
            // Set httpOnly cookie (similar to .NET's CookieAuthenticationOptions.HttpOnly = true)
            setJwtCookie(httpResponse, response.getToken());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, null, null, null, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {
        try {
            AuthResponse response = authService.login(request);
            
            // Set httpOnly cookie
            setJwtCookie(httpResponse, response.getToken());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, null, e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse httpResponse) {
        // Clear JWT cookie by setting it to expire immediately
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete cookie
        httpResponse.addCookie(cookie);
        
        return ResponseEntity.ok(new AuthResponse(null, null, null, null, "Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        try {
            // Get current user info from JWT token (via SecurityContext)
            Long userId = SecurityUtil.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, null, null, null, "Not authenticated", null));
            }
            
            // Fetch user from database
            User user = authService.getUserById(userId);
            
            AuthResponse response = new AuthResponse();
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setTenantId(user.getTenant().getId());
            response.setTenantName(user.getTenant().getName());
            response.setMessage("User information retrieved");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, null, e.getMessage(), null));
        }
    }
    
    /**
     * Helper method to set JWT cookie
     * httpOnly = true prevents JavaScript access (XSS protection)
     * secure = true in production (HTTPS only)
     */
    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true); // Prevents JavaScript access (XSS protection)
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/"); // Available for all paths
        cookie.setMaxAge(24 * 60 * 60); // 24 hours (matches JWT expiration)
        response.addCookie(cookie);
    }
}