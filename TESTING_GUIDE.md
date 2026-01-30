# 🧪 LedgerFlow Testing Guide

## Prerequisites Check

Before testing, ensure you have:
- ✅ Java 17+ installed
- ✅ Python 3.11+ installed
- ✅ Node.js 18+ installed
- ✅ Docker Desktop running
- ✅ PostgreSQL and Redis running (via Docker)

---

## Step 1: Start All Services

### Terminal 1: Start Docker Services
```bash
cd /Users/parasmittal/Desktop/LedgerFlow
docker-compose up -d

# Verify containers are running
docker ps
```

Expected output should show:
- `postgres` (port 5432)
- `redis` (port 6379)
- `chromadb` (if configured)

### Terminal 2: Start Backend (Spring Boot)
```bash
cd /Users/parasmittal/Desktop/LedgerFlow/backend/ledgerflow-backend
./mvnw spring-boot:run
```

Wait for: `Started LedgerFlowApplication in X.XXX seconds`

**Verify**: Open http://localhost:8080/actuator/health
Expected: `{"status":"UP"}`

### Terminal 3: Start AI Orchestration Service
```bash
cd /Users/parasmittal/Desktop/LedgerFlow/ai-orchestration
source venv/bin/activate  # If using virtual environment
uvicorn main:app --reload --port 8001
```

Wait for: `Uvicorn running on http://127.0.0.1:8001`

**Verify**: Open http://localhost:8001/health
Expected: `{"status":"healthy"}`

### Terminal 4: Start Frontend
```bash
cd /Users/parasmittal/Desktop/LedgerFlow/frontend
npm install  # First time only
npm run dev
```

Wait for: `Local: http://localhost:5173/`

**Verify**: Open http://localhost:5173
Expected: Login page should load

---

## Step 2: Test Authentication Flow

### 2.1 Register a New User

**Using Browser**:
1. Go to http://localhost:5173
2. Click "create a new account" or go to http://localhost:5173/register
3. Fill in the form:
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `password123`
   - First Name: `Test`
   - Last Name: `User`
   - Company Name: `Test Company`
   - Company Slug: `test-company`
4. Click "Create account"

**Expected Result**:
- ✅ Redirected to dashboard
- ✅ See "Welcome, testuser" in header
- ✅ Cookie is set (check DevTools → Application → Cookies)

**Using cURL**:
```bash
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
```

**Check**:
- Status code: `201 Created`
- Response contains: `username`, `email`, `tenantId`, `tenantName`
- Cookie file created: `cookies.txt`

### 2.2 Login

**Using Browser**:
1. Logout (if logged in)
2. Go to http://localhost:5173/login
3. Enter credentials:
   - Username: `testuser`
   - Password: `password123`
4. Click "Sign in"

**Expected Result**:
- ✅ Redirected to dashboard
- ✅ See user info in header

**Using cURL**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }' \
  -c cookies.txt \
  -v
```

**Check**:
- Status code: `200 OK`
- Cookie `jwt` is set

### 2.3 Get Current User

**Using cURL**:
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -b cookies.txt \
  -v
```

**Expected Result**:
- Status code: `200 OK`
- Response contains user information

### 2.4 Test Protected Endpoint (Without Auth)

```bash
curl -X GET http://localhost:8080/api/invoices?tenantId=1 \
  -v
```

**Expected Result**:
- Status code: `401 Unauthorized` or `403 Forbidden`
- Error message about authentication

---

## Step 3: Test Invoice Upload

### 3.1 Create a Test PDF Invoice

Create a simple PDF invoice or use an existing one.

**Quick Test** (if you have a PDF):
```bash
# Save your invoice PDF as test_invoice.pdf in the project root
```

### 3.2 Upload Invoice via Frontend

1. Go to http://localhost:5173/dashboard
2. Click "Upload Invoice" button
3. Select a PDF file
4. Click "Upload"

**Expected Result**:
- ✅ Upload progress bar appears
- ✅ Success message or invoice appears in list
- ✅ Invoice status shows as `EXTRACTING` then `EXTRACTED`

### 3.3 Upload Invoice via API

**First, get tenantId from registration response or database**

```bash
# Get tenantId (replace 1 with actual tenantId)
TENANT_ID=1

curl -X POST http://localhost:8080/api/invoices/upload \
  -b cookies.txt \
  -F "file=@/path/to/your/invoice.pdf" \
  -F "tenantId=$TENANT_ID" \
  -v
```

**Expected Result**:
- Status code: `201 Created`
- Response contains invoice data:
  - `invoiceNumber`
  - `vendorName`
  - `totalAmount`
  - `status`: `EXTRACTED` or `EXTRACTING`
  - `lineItems`: array of line items

**Check Database**:
```sql
-- Connect to PostgreSQL
docker exec -it ledgerflow-postgres psql -U ledgerflow -d ledgerflow

-- Check invoices
SELECT id, invoice_number, vendor_name, total_amount, status, tenant_id FROM invoices;

-- Check line items
SELECT * FROM invoice_line_items;
```

---

## Step 4: Test Invoice List

### 4.1 Get Invoices via Frontend

1. Go to http://localhost:5173/dashboard
2. Verify invoices are displayed in a table

**Expected Result**:
- ✅ Table shows all invoices
- ✅ Columns: Invoice Number, Vendor, Date, Amount, Status
- ✅ Status badges are colored correctly
- ✅ Approve/Reject buttons appear for `EXTRACTED` invoices

### 4.2 Get Invoices via API

```bash
# Replace TENANT_ID with actual tenantId
TENANT_ID=1

curl -X GET "http://localhost:8080/api/invoices?tenantId=$TENANT_ID" \
  -b cookies.txt \
  -v
```

**Expected Result**:
- Status code: `200 OK`
- Response is an array of invoice objects
- Each invoice has: `id`, `invoiceNumber`, `vendorName`, `totalAmount`, `status`, etc.

---

## Step 5: Test Invoice Status Update

### 5.1 Update Status via Frontend

1. Go to dashboard
2. Find an invoice with status `EXTRACTED`
3. Click "Approve" or "Reject"

**Expected Result**:
- ✅ Status changes to `APPROVED` or `REJECTED`
- ✅ UI updates immediately

### 5.2 Update Status via API

```bash
# Replace INVOICE_ID and TENANT_ID
INVOICE_ID=1
TENANT_ID=1

curl -X PUT "http://localhost:8080/api/invoices/$INVOICE_ID/status?tenantId=$TENANT_ID&status=APPROVED" \
  -b cookies.txt \
  -v
```

**Expected Result**:
- Status code: `200 OK`
- Response shows updated invoice with new status

---

## Step 6: Test Error Scenarios

### 6.1 Invalid Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "wronguser",
    "password": "wrongpass"
  }' \
  -v
```

**Expected**: `401 Unauthorized` with error message

### 6.2 Upload Non-PDF File
```bash
# Try uploading a text file
curl -X POST http://localhost:8080/api/invoices/upload \
  -b cookies.txt \
  -F "file=@/path/to/file.txt" \
  -F "tenantId=1" \
  -v
```

**Expected**: Error message about file type

### 6.3 Access Protected Endpoint Without Auth
```bash
curl -X GET http://localhost:8080/api/invoices?tenantId=1 \
  -v
```

**Expected**: `401 Unauthorized` or `403 Forbidden`

### 6.4 Invalid Tenant ID
```bash
curl -X GET "http://localhost:8080/api/invoices?tenantId=99999" \
  -b cookies.txt \
  -v
```

**Expected**: Empty array or error (depending on implementation)

---

## Step 7: Verify Database State

### 7.1 Check Users Table
```sql
SELECT id, username, email, tenant_id, active FROM users;
```

### 7.2 Check Tenants Table
```sql
SELECT id, name, slug FROM tenants;
```

### 7.3 Check Invoices Table
```sql
SELECT id, invoice_number, vendor_name, total_amount, status, tenant_id, created_at 
FROM invoices 
ORDER BY created_at DESC;
```

### 7.4 Check Invoice Line Items
```sql
SELECT 
  ili.id,
  ili.description,
  ili.quantity,
  ili.unit_price,
  ili.amount,
  i.invoice_number
FROM invoice_line_items ili
JOIN invoices i ON ili.invoice_id = i.id;
```

---

## Step 8: Test Cookie Handling

### 8.1 Verify Cookie is Set
```bash
# After login, check cookies.txt
cat cookies.txt
```

**Expected**: Contains `jwt` cookie with token value

### 8.2 Test Cookie Expiration
- Wait 24 hours (or modify JWT expiration in `application.yml`)
- Try accessing protected endpoint
- Should get `401 Unauthorized`

### 8.3 Test Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt \
  -c cookies.txt \
  -v
```

**Expected**: Cookie is cleared (check `cookies.txt`)

---

## Common Issues & Solutions

### Issue: Backend won't start
**Solution**:
- Check Java version: `java -version` (should be 17+)
- Check PostgreSQL is running: `docker ps`
- Check port 8080 is not in use: `lsof -i :8080`

### Issue: AI service returns 404
**Solution**:
- Verify service is running on port 8001
- Check `AI_ORCHESTRATION_URL` in `application.yml`
- Verify OpenAI API key is set in `.env`

### Issue: Frontend can't connect to backend
**Solution**:
- Check CORS configuration in `SecurityConfig.java`
- Verify backend is running
- Check browser console for CORS errors

### Issue: Cookies not being sent
**Solution**:
- Verify `withCredentials: true` in `api.js`
- Check CORS allows credentials
- Use browser DevTools to inspect cookies

### Issue: Invoice upload fails
**Solution**:
- Check file size (max 10MB)
- Verify file is PDF
- Check AI service is running
- Check S3 credentials (if using S3)

---

## Test Checklist

- [ ] All services start successfully
- [ ] User registration works
- [ ] User login works
- [ ] JWT cookie is set correctly
- [ ] Protected endpoints require authentication
- [ ] Invoice upload works
- [ ] Invoice list displays correctly
- [ ] Invoice status update works
- [ ] Line items are saved correctly
- [ ] Error handling works
- [ ] Logout clears cookie
- [ ] Database state is correct

---

## Performance Testing (Optional)

### Load Test with Multiple Invoices
```bash
# Upload multiple invoices
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/invoices/upload \
    -b cookies.txt \
    -F "file=@invoice_$i.pdf" \
    -F "tenantId=1"
done
```

### Concurrent Requests
Use a tool like Apache Bench or `wrk`:
```bash
# Install wrk: brew install wrk
wrk -t4 -c100 -d30s http://localhost:8080/api/invoices?tenantId=1
```

---

## Next Steps After Testing

1. ✅ Fix any issues found
2. ✅ Implement WebSocket for real-time updates
3. ✅ Add invoice detail view
4. ✅ Improve error handling
5. ✅ Add filtering and search

---

**Happy Testing! 🚀**

