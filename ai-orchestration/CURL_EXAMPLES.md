# cURL Examples for LedgerFlow AI Orchestration API

## Prerequisites
- Server must be running: `uvicorn main:app --reload --port 8001`
- Have a PDF invoice file ready for testing

---

## 1. Health Check

Test if the service is running:

```bash
curl -X GET http://localhost:8001/health
```

**Expected Response:**
```json
{
  "status": "healthy",
  "service": "ai-orchestration",
  "version": "1.0.0"
}
```

---

## 2. Extract Invoice (Basic)

Extract data from an invoice PDF:

```bash
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenant_id=test-tenant-123"
```

**Replace:**
- `/path/to/your/invoice.pdf` with your actual PDF file path
- `test-tenant-123` with your tenant ID

---

## 3. Extract Invoice (Pretty Print JSON)

Same as above, but with formatted JSON output:

```bash
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenant_id=test-tenant-123" \
  | python -m json.tool
```

Or using `jq` (if installed):
```bash
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenant_id=test-tenant-123" \
  | jq .
```

---

## 4. Extract Invoice (Save Response to File)

Save the response to a file:

```bash
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenant_id=test-tenant-123" \
  -o response.json
```

---

## 5. Extract Invoice (Verbose Output)

See detailed request/response information:

```bash
curl -v -X POST http://localhost:8001/extract-invoice \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenant_id=test-tenant-123"
```

---

## 6. Extract Invoice (With Error Handling)

Show HTTP status code and handle errors:

```bash
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenant_id=test-tenant-123" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s -S
```

---

## 7. Extract Invoice (Relative Path)

If your PDF is in the current directory:

```bash
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@invoice.pdf" \
  -F "tenant_id=test-tenant-123"
```

---

## 8. Extract Invoice (Windows PowerShell)

For Windows PowerShell users:

```powershell
$filePath = "C:\path\to\invoice.pdf"
$tenantId = "test-tenant-123"

curl.exe -X POST http://localhost:8001/extract-invoice `
  -F "file=@$filePath" `
  -F "tenant_id=$tenantId"
```

---

## 9. Extract Invoice (Windows CMD)

For Windows Command Prompt:

```cmd
curl -X POST http://localhost:8001/extract-invoice ^
  -F "file=@C:\path\to\invoice.pdf" ^
  -F "tenant_id=test-tenant-123"
```

---

## Common Issues & Solutions

### Issue: "Connection refused"
**Solution:** Make sure the server is running:
```bash
cd ai-orchestration
uvicorn main:app --reload --port 8001
```

### Issue: "File not found"
**Solution:** Use absolute path or check file exists:
```bash
# Check if file exists
ls -la /path/to/invoice.pdf

# Use absolute path in curl
curl -X POST ... -F "file=@/absolute/path/to/invoice.pdf" ...
```

### Issue: "Invalid API key"
**Solution:** Check your `.env` file has valid API keys:
```bash
# Check .env file
cat .env | grep OPENAI_API_KEY
```

### Issue: "No such file or directory"
**Solution:** Make sure you're using `@` before the file path:
```bash
# ✅ Correct
-F "file=@invoice.pdf"

# ❌ Wrong (missing @)
-F "file=invoice.pdf"
```

---

## Expected Response Format

Successful response:
```json
{
  "status": "success",
  "filename": "invoice.pdf",
  "extracted_data": {
    "vendor_name": "Acme Corp",
    "invoice_number": "INV-2024-001",
    "invoice_date": "2024-01-15",
    "total_amount": 1500.00,
    "currency": "USD",
    "line_items": [
      {
        "description": "Software License",
        "quantity": 10,
        "unit_price": 150.00,
        "amount": 1500.00
      }
    ]
  },
  "confidence_score": 0.95,
  "raw_text_length": 2500,
  "extraction_method": "agentic_llm"
}
```

Error response:
```json
{
  "detail": "Error message here"
}
```

---

## Quick Test Command

Copy and paste this (replace the file path):

```bash
curl -X POST http://localhost:8001/extract-invoice \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenant_id=test-tenant-123" \
  | python -m json.tool
```



