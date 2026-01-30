package com.ledgerflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application entry point for LedgerFlow Backend
 * 
 * This service handles:
 * - Multi-tenant authentication and authorization
 * - Invoice management and lifecycle
 * - Integration with AI Orchestration service
 * - Real-time updates via WebSocket
 */
@SpringBootApplication
public class LedgerFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerFlowApplication.class, args);
    }
}

