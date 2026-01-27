# API Examples & Usage Guide

## 📋 Table of Contents

1. [Authentication Flow](#authentication-flow)
2. [Complete Examples](#complete-examples)
3. [Error Handling](#error-handling)
4. [Best Practices](#best-practices)

---

## 🔐 Authentication Flow

### 1. Register New User

**Endpoint:** `POST /api/auth/register`

**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "imageUrl": null,
  "emailVerified": false,
  "enabled": true,
  "provider": "LOCAL",
  "role": "ROLE_USER",
  "createdAt": "2026-01-24 10:00:00",
  "lastLoginAt": null
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "fullName": "John Doe"
  }'
```

---

### 2. Verify Email

**Endpoint:** `GET /api/auth/verify-email?token={token}`

User nhận email với verification link. Click link hoặc:

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/auth/verify-email?token=abc123-def456-ghi789"
```

**Response (200 OK):**
```json
{
  "message": "Email verified successfully"
}
```

---

### 3. Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "username": "johndoe",
  "password": "SecurePass123",
  "rememberMe": false
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzA2MDg4MDAwLCJleHAiOjE3MDYwOTE2MDB9.signature",
  "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3MDYwODgwMDAsImV4cCI6MTcwNjE3NDQwMH0.signature",
  "token_type": "Bearer",
  "expires_in": 3600,
  "username": "johndoe",
  "email": "john@example.com"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

**Save tokens:**
```bash
# Extract access token
ACCESS_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"SecurePass123"}' \
  | jq -r '.access_token')

echo $ACCESS_TOKEN
```

---

### 4. Access Protected Endpoint

**Endpoint:** `GET /api/users/me`

**Headers:**
```
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "imageUrl": null,
  "emailVerified": true,
  "enabled": true,
  "provider": "LOCAL",
  "role": "ROLE_USER",
  "createdAt": "2026-01-24 10:00:00",
  "lastLoginAt": "2026-01-24 10:30:00"
}
```

**cURL:**
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

---

### 5. Refresh Access Token

**Endpoint:** `POST /api/auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK):**
```json
{
  "access_token": "new_access_token",
  "refresh_token": "new_refresh_token",
  "token_type": "Bearer",
  "expires_in": 3600,
  "username": "johndoe",
  "email": "john@example.com"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'"$REFRESH_TOKEN"'"
  }'
```

---

### 6. Forgot Password

**Endpoint:** `POST /api/auth/forgot-password`

**Request:**
```json
{
  "email": "john@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "Password reset email sent"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com"
  }'
```

---

### 7. Reset Password

**Endpoint:** `POST /api/auth/reset-password`

**Request:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePass456"
}
```

**Response (200 OK):**
```json
{
  "message": "Password reset successfully"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "abc123-def456",
    "newPassword": "NewSecurePass456"
  }'
```

---

## 👤 User Management

### 8. Update Profile

**Endpoint:** `PUT /api/users/me`

**Request:**
```json
{
  "fullName": "John Updated Doe"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Updated Doe",
  ...
}
```

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Updated Doe"
  }'
```

---

### 9. Change Password

**Endpoint:** `PUT /api/users/me/password`

**Request:**
```json
{
  "currentPassword": "SecurePass123",
  "newPassword": "NewSecurePass456"
}
```

**Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/users/me/password \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "SecurePass123",
    "newPassword": "NewSecurePass456"
  }'
```

---

### 10. Delete Account

**Endpoint:** `DELETE /api/users/me`

**Response (200 OK):**
```json
{
  "message": "Account deleted successfully"
}
```

**cURL:**
```bash
curl -X DELETE http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

---

## ❌ Error Handling

### Error Response Format

All errors follow this format:

```json
{
  "timestamp": "2026-01-24 10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/auth/login",
  "errors": [
    "Password must be at least 8 characters"
  ]
}
```

### Common Errors

#### 1. Validation Error (400)

```json
{
  "timestamp": "2026-01-24 10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/auth/register",
  "errors": [
    "Password must be at least 8 characters",
    "Email must be valid"
  ]
}
```

#### 2. User Already Exists (409)

```json
{
  "timestamp": "2026-01-24 10:00:00",
  "status": 409,
  "error": "User Already Exists",
  "message": "Username 'johndoe' is already taken",
  "path": "/api/auth/register"
}
```

#### 3. Authentication Failed (401)

```json
{
  "timestamp": "2026-01-24 10:00:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid username or password",
  "path": "/api/auth/login"
}
```

#### 4. Token Expired (401)

```json
{
  "timestamp": "2026-01-24 10:00:00",
  "status": 401,
  "error": "Token Expired",
  "message": "JWT token has expired. Please refresh your token or login again.",
  "path": "/api/users/me"
}
```

#### 5. Account Locked (403)

```json
{
  "timestamp": "2026-01-24 10:00:00",
  "status": 403,
  "error": "Account Locked",
  "message": "Account is locked due to multiple failed login attempts",
  "path": "/api/auth/login"
}
```

---

## 🔧 Complete Workflow Examples

### Example 1: Full Registration & Login Flow

```bash
#!/bin/bash

# 1. Register
echo "1. Registering user..."
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@123",
    "fullName": "Test User"
  }'

# 2. Verify email (normally from email link)
echo -e "\n2. Verifying email..."
curl -X GET "http://localhost:8080/api/auth/verify-email?token=your-token-here"

# 3. Login
echo -e "\n3. Logging in..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123"
  }')

ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.access_token')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.refresh_token')

echo "Access Token: $ACCESS_TOKEN"

# 4. Get user profile
echo -e "\n4. Getting user profile..."
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# 5. Update profile
echo -e "\n5. Updating profile..."
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User Updated"
  }'

# 6. Refresh token
echo -e "\n6. Refreshing token..."
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'"$REFRESH_TOKEN"'"
  }'
```

---

### Example 2: Password Reset Flow

```bash
#!/bin/bash

# 1. Request password reset
echo "1. Requesting password reset..."
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'

# 2. User receives email with token
# 3. Reset password with token
echo -e "\n2. Resetting password..."
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "reset-token-from-email",
    "newPassword": "NewTest@456"
  }'

# 4. Login with new password
echo -e "\n3. Logging in with new password..."
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "NewTest@456"
  }'
```

---

## 💡 Best Practices

### 1. Token Storage (Frontend)

**✅ Recommended:**
```javascript
// Store in memory (most secure for SPA)
let accessToken = null;
let refreshToken = null;

// Or use httpOnly cookies (set by backend)
```

**❌ Avoid:**
```javascript
// Don't store in localStorage (XSS vulnerable)
localStorage.setItem('token', accessToken);
```

### 2. Token Refresh Strategy

```javascript
// Refresh token before expiration
const REFRESH_BEFORE_EXPIRY = 5 * 60 * 1000; // 5 minutes

setInterval(async () => {
  const newTokens = await refreshAccessToken(refreshToken);
  accessToken = newTokens.access_token;
  refreshToken = newTokens.refresh_token;
}, expiresIn - REFRESH_BEFORE_EXPIRY);
```

### 3. Error Handling

```javascript
async function apiCall(url, options) {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
        ...options.headers
      }
    });

    if (response.status === 401) {
      // Token expired, try refresh
      await refreshAccessToken();
      // Retry original request
      return apiCall(url, options);
    }

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }

    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
```

### 4. Rate Limiting

Implement exponential backoff for failed requests:

```javascript
async function loginWithRetry(credentials, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await login(credentials);
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => 
        setTimeout(resolve, Math.pow(2, i) * 1000)
      );
    }
  }
}
```

---

## 📱 Integration Examples

### React Example

```javascript
import { useState, useEffect } from 'react';

function useAuth() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in
    const token = getAccessToken();
    if (token) {
      fetchUserProfile(token)
        .then(setUser)
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (credentials) => {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    
    const data = await response.json();
    setAccessToken(data.access_token);
    setRefreshToken(data.refresh_token);
    setUser(data);
  };

  const logout = () => {
    clearTokens();
    setUser(null);
  };

  return { user, loading, login, logout };
}
```

### Angular Example

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private accessToken$ = new BehaviorSubject<string | null>(null);
  
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials)
      .pipe(
        tap(response => {
          this.accessToken$.next(response.access_token);
          localStorage.setItem('refresh_token', response.refresh_token);
        })
      );
  }

  getAccessToken(): string | null {
    return this.accessToken$.value;
  }
}
```

---

**For more examples, see the Swagger UI at http://localhost:8080/swagger-ui.html**
