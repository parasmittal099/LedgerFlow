#!/bin/bash

# LedgerFlow End-to-End Test Script
# This script tests the complete flow of the application

set -e  # Exit on error

echo "🧪 LedgerFlow End-to-End Test"
echo "=============================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BACKEND_URL="http://localhost:8080"
AI_URL="http://localhost:8001"
FRONTEND_URL="http://localhost:5173"
COOKIE_FILE="test_cookies.txt"

# Test counter
PASSED=0
FAILED=0

# Helper functions
test_step() {
    echo -e "${YELLOW}Testing: $1${NC}"
}

test_pass() {
    echo -e "${GREEN}✅ PASS: $1${NC}"
    ((PASSED++))
}

test_fail() {
    echo -e "${RED}❌ FAIL: $1${NC}"
    ((FAILED++))
}

check_service() {
    local service=$1
    local url=$2
    
    if curl -s -f "$url" > /dev/null 2>&1; then
        test_pass "$service is running"
        return 0
    else
        test_fail "$service is not running at $url"
        return 1
    fi
}

# Step 1: Check Services
echo "Step 1: Checking Services"
echo "-------------------------"
check_service "Backend" "$BACKEND_URL/actuator/health"
check_service "AI Orchestration" "$AI_URL/health"
check_service "Frontend" "$FRONTEND_URL"

echo ""

# Step 2: Test Registration
echo "Step 2: Testing User Registration"
echo "---------------------------------"
test_step "Register new user"

REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BACKEND_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_'$(date +%s)'",
    "password": "password123",
    "email": "test_'$(date +%s)'@example.com",
    "firstName": "Test",
    "lastName": "User",
    "tenantName": "Test Company",
    "tenantSlug": "test-company-'$(date +%s)'"
  }' \
  -c "$COOKIE_FILE")

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
BODY=$(echo "$REGISTER_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 201 ]; then
    test_pass "User registration successful"
    TENANT_ID=$(echo "$BODY" | grep -o '"tenantId":[0-9]*' | grep -o '[0-9]*' | head -1)
    echo "   Tenant ID: $TENANT_ID"
else
    test_fail "User registration failed (HTTP $HTTP_CODE)"
    echo "   Response: $BODY"
fi

echo ""

# Step 3: Test Login
echo "Step 3: Testing User Login"
echo "--------------------------"
test_step "Login with credentials"

# Get username from registration
USERNAME=$(echo "$BODY" | grep -o '"username":"[^"]*' | cut -d'"' -f4)

LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BACKEND_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"password123\"
  }" \
  -c "$COOKIE_FILE")

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    test_pass "User login successful"
else
    test_fail "User login failed (HTTP $HTTP_CODE)"
    echo "   Response: $BODY"
fi

echo ""

# Step 4: Test Get Current User
echo "Step 4: Testing Get Current User"
echo "--------------------------------"
test_step "Get authenticated user info"

ME_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BACKEND_URL/api/auth/me" \
  -b "$COOKIE_FILE")

HTTP_CODE=$(echo "$ME_RESPONSE" | tail -n1)
BODY=$(echo "$ME_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    test_pass "Get current user successful"
    echo "   User: $(echo "$BODY" | grep -o '"username":"[^"]*' | cut -d'"' -f4)"
else
    test_fail "Get current user failed (HTTP $HTTP_CODE)"
    echo "   Response: $BODY"
fi

echo ""

# Step 5: Test Protected Endpoint (Without Auth)
echo "Step 5: Testing Protected Endpoint (No Auth)"
echo "----------------------------------------------"
test_step "Access protected endpoint without authentication"

PROTECTED_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BACKEND_URL/api/invoices?tenantId=1")

HTTP_CODE=$(echo "$PROTECTED_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 401 ] || [ "$HTTP_CODE" -eq 403 ]; then
    test_pass "Protected endpoint correctly rejects unauthenticated requests"
else
    test_fail "Protected endpoint should return 401/403, got $HTTP_CODE"
fi

echo ""

# Step 6: Test Get Invoices (With Auth)
echo "Step 6: Testing Get Invoices"
echo "----------------------------"
test_step "Get invoices list (authenticated)"

if [ -z "$TENANT_ID" ]; then
    TENANT_ID=1  # Fallback
fi

INVOICES_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BACKEND_URL/api/invoices?tenantId=$TENANT_ID" \
  -b "$COOKIE_FILE")

HTTP_CODE=$(echo "$INVOICES_RESPONSE" | tail -n1)
BODY=$(echo "$INVOICES_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    test_pass "Get invoices successful"
    INVOICE_COUNT=$(echo "$BODY" | grep -o '"id":[0-9]*' | wc -l | tr -d ' ')
    echo "   Found $INVOICE_COUNT invoice(s)"
else
    test_fail "Get invoices failed (HTTP $HTTP_CODE)"
    echo "   Response: $BODY"
fi

echo ""

# Step 7: Test Logout
echo "Step 7: Testing Logout"
echo "----------------------"
test_step "Logout user"

LOGOUT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BACKEND_URL/api/auth/logout" \
  -b "$COOKIE_FILE" \
  -c "$COOKIE_FILE")

HTTP_CODE=$(echo "$LOGOUT_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 200 ]; then
    test_pass "Logout successful"
    
    # Check if cookie was cleared
    if ! grep -q "jwt" "$COOKIE_FILE" 2>/dev/null || grep -q "#HttpOnly" "$COOKIE_FILE" 2>/dev/null; then
        test_pass "Cookie cleared after logout"
    else
        test_fail "Cookie not cleared after logout"
    fi
else
    test_fail "Logout failed (HTTP $HTTP_CODE)"
fi

echo ""

# Summary
echo "=============================="
echo "Test Summary"
echo "=============================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the output above.${NC}"
    exit 1
fi

