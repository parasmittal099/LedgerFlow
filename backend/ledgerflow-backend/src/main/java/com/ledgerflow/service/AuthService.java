package com.ledgerflow.service;

import com.ledgerflow.dto.AuthResponse;
import com.ledgerflow.dto.LoginRequest;
import com.ledgerflow.dto.RegisterRequest;
import com.ledgerflow.entity.Tenant;
import com.ledgerflow.entity.User;
import com.ledgerflow.repository.TenantRepository;
import com.ledgerflow.repository.UserRepository;
import com.ledgerflow.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * 
 * Handles user registration, login, and JWT token generation.
 * Uses BCrypt for password hashing (similar to .NET's IPasswordHasher<T>)
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       TenantService tenantService,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantService = tenantService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create tenant
        Tenant tenant = tenantService.createTenant(request.getTenantName(), request.getTenantSlug());

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        // Hash password with BCrypt (similar to .NET's PasswordHasher.HashPassword)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTenant(tenant);
        user.setActive(true);

        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), tenant.getId());

        // Return response
        AuthResponse response = new AuthResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setTenantId(tenant.getId());
        response.setTenantName(tenant.getName());
        response.setMessage("Registration successful");
        response.setToken(token); // Include token in response (cookie is primary)

        return response;
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Validate password using BCrypt (similar to .NET's PasswordHasher.VerifyHashedPassword)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Check if user is active
        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        // Check tenant if provided
        if (request.getTenantSlug() != null && !request.getTenantSlug().isEmpty()) {
            Tenant tenant = tenantService.getTenantBySlug(request.getTenantSlug());
            if (!user.getTenant().getId().equals(tenant.getId())) {
                throw new RuntimeException("User does not belong to this tenant");
            }
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getTenant().getId());

        // Return response
        AuthResponse response = new AuthResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setTenantId(user.getTenant().getId());
        response.setTenantName(user.getTenant().getName());
        response.setMessage("Login successful");
        response.setToken(token); // Include token in response (cookie is primary)

        return response;
    }
    
    /**
     * Get user by ID (for JWT token validation)
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean validateTenant(String tenantSlug) {
        return tenantRepository.existsBySlug(tenantSlug);
    }
}
