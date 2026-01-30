# 🔍 Debugging Blank Dashboard

## Quick Debugging Steps

### 1. Open Browser Console
Press `F12` or `Cmd+Option+I` (Mac) / `Ctrl+Shift+I` (Windows) and check the Console tab for errors.

### 2. Check Network Tab
In DevTools, go to Network tab and look for:
- Failed API calls (red status)
- 401/403 errors (authentication issues)
- CORS errors
- Missing responses

### 3. Check Application Tab
In DevTools, go to Application → Cookies:
- Verify `jwt` cookie exists
- Check cookie domain and path are correct

### 4. Common Issues & Solutions

#### Issue: "Loading user information..." forever
**Cause**: User data not being loaded from API
**Solution**:
1. Check if backend is running: http://localhost:8080/actuator/health
2. Check browser console for API errors
3. Try logging out and logging in again

#### Issue: "Error loading invoices"
**Cause**: API call to get invoices failed
**Solution**:
1. Check backend logs for errors
2. Verify tenantId is correct
3. Check database has invoices
4. Verify CORS is configured correctly

#### Issue: Blank white screen
**Cause**: JavaScript error preventing render
**Solution**:
1. Check browser console for errors
2. Check if all dependencies are installed: `npm install`
3. Clear browser cache and reload

#### Issue: Redirects to login immediately
**Cause**: Authentication check failing
**Solution**:
1. Check if JWT cookie exists
2. Verify backend authentication endpoint: http://localhost:8080/api/auth/me
3. Check CORS allows credentials

### 5. Manual API Test

Test the API endpoints directly:

```bash
# Test authentication
curl -X GET http://localhost:8080/api/auth/me \
  -b cookies.txt \
  -v

# Test invoices (replace TENANT_ID)
curl -X GET "http://localhost:8080/api/invoices?tenantId=1" \
  -b cookies.txt \
  -v
```

### 6. Check React DevTools

Install React DevTools browser extension and check:
- Component state
- Props being passed
- Store state (Zustand)

### 7. Enable Debug Logging

The updated Dashboard component now includes console.log statements. Check the browser console for:
- "Dashboard - User: ..."
- "Dashboard - isAuthenticated: ..."
- "Loading invoices for tenant: ..."
- "Invoices loaded: ..."

### 8. Verify Services Are Running

```bash
# Check backend
curl http://localhost:8080/actuator/health

# Check AI service
curl http://localhost:8001/health

# Check frontend
curl http://localhost:5173
```

### 9. Common Console Errors

**"Failed to fetch"**
- Backend not running
- CORS issue
- Network error

**"401 Unauthorized"**
- JWT cookie missing or expired
- Authentication failed
- Cookie not being sent

**"Cannot read property 'tenantId' of null"**
- User object not loaded
- Authentication check failed

### 10. Reset and Retry

If nothing works:
1. Clear browser cookies
2. Clear browser cache
3. Restart all services
4. Register a new user
5. Try again

---

## What to Share for Help

If you need help, share:
1. Browser console errors (screenshot or copy)
2. Network tab errors (screenshot)
3. Backend logs
4. Steps to reproduce

