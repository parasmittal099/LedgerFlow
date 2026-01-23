#!/bin/bash

# Test script for LedgerFlow AI Orchestration API
# Make sure the server is running: uvicorn main:app --reload --port 8001

echo "Testing Health Endpoint..."
curl -X GET http://localhost:8001/health
echo -e "\n\n"

echo "Testing Extract Invoice Endpoint..."
echo "Replace 'sample_invoice.pdf' with your actual PDF file path"
echo -e "\n"

# Basic curl command for extract-invoice
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@sample_invoice.pdf" \
  -F "tenant_id=test-tenant-123" \
  -H "Content-Type: multipart/form-data"

echo -e "\n"



