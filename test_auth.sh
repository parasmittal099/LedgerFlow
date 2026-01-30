#!/bin/bash

echo "=== Step 1: Register a new user ==="
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "tenantName": "Test Company",
    "tenantSlug": "test-company"
  }' \
  -c cookies.txt \
  -v

echo -e "\n\n=== Step 2: Check cookies.txt file ==="
cat cookies.txt

echo -e "\n\n=== Step 3: Get current user (using cookies) ==="
curl -X GET http://localhost:8080/api/auth/me \
  -b cookies.txt \
  -v

echo -e "\n\n=== Step 4: Try without cookies (should fail) ==="
curl -X GET http://localhost:8080/api/auth/me \
  -v
