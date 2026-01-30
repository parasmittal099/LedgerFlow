# ⚡ Quick Test Guide

## Fastest Way to Test

### 1. Start Services (4 terminals)

**Terminal 1 - Docker:**
```bash
cd /Users/parasmittal/Desktop/LedgerFlow
docker-compose up -d
```

**Terminal 2 - Backend:**
```bash
cd /Users/parasmittal/Desktop/LedgerFlow/backend/ledgerflow-backend
./mvnw spring-boot:run
```

**Terminal 3 - AI Service:**
```bash
cd /Users/parasmittal/Desktop/LedgerFlow/ai-orchestration
uvicorn main:app --reload --port 8001
```

**Terminal 4 - Frontend:**
```bash
cd /Users/parasmittal/Desktop/LedgerFlow/frontend
npm install  # First time only
npm run dev
```

### 2. Run Automated Test Script

```bash
cd /Users/parasmittal/Desktop/LedgerFlow
./test_end_to_end.sh
```

This will test:
- ✅ Service health checks
- ✅ User registration
- ✅ User login
- ✅ Get current user
- ✅ Protected endpoints
- ✅ Get invoices
- ✅ Logout

### 3. Manual Browser Test

1. **Open**: http://localhost:5173
2. **Register**: Create account
3. **Login**: Sign in
4. **Upload**: Upload a PDF invoice
5. **View**: Check invoice appears in dashboard
6. **Approve/Reject**: Test status update

### 4. Quick API Test (cURL)

```bash
# Register
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
  -c cookies.txt

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }' \
  -c cookies.txt

# Get current user
curl -X GET http://localhost:8080/api/auth/me \
  -b cookies.txt

# Get invoices (replace TENANT_ID)
curl -X GET "http://localhost:8080/api/invoices?tenantId=1" \
  -b cookies.txt
```

---

## Common Issues

**Backend won't start?**
- Check Java: `java -version` (need 17+)
- Check PostgreSQL: `docker ps`
- Check port: `lsof -i :8080`

**Frontend won't start?**
- Run `npm install` first
- Check Node version: `node -v` (need 18+)

**Can't connect to backend?**
- Verify backend is running: http://localhost:8080/actuator/health
- Check CORS in browser console

**Upload fails?**
- Check AI service: http://localhost:8001/health
- Verify OpenAI API key in `.env`

---

## Expected Results

✅ All services start without errors
✅ Registration creates user and tenant
✅ Login sets JWT cookie
✅ Dashboard shows invoices
✅ Upload processes invoice
✅ Status updates work

---

For detailed testing, see `TESTING_GUIDE.md`

