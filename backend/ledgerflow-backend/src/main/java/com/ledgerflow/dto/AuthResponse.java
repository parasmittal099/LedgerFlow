package com.ledgerflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String email;
    private Long tenantId;
    private String tenantName;
    private String message;
    private String token; // JWT token (optional, mainly for debugging - cookie is primary)
}