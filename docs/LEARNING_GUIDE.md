# Project Implementation Guide - Spring Boot Authentication Template

## 📚 Mục Lục

1. [Project Architecture Overview](#project-architecture-overview)
2. [Security Implementation Explained](#security-implementation-explained)
3. [JWT Implementation Deep Dive](#jwt-implementation-deep-dive)
4. [Email & Token Management](#email--token-management)
5. [Code Walkthrough](#code-walkthrough)
6. [Design Decisions](#design-decisions)

---

## 🏗️ Project Architecture Overview

### Tổng Quan Cấu Trúc

Project này được tổ chức theo **Domain-Driven Design**:

```
com.auth.template/
├── config/              # Cấu hình Spring & Security
├── auth/                # Domain: Authentication
├── user/                # Domain: User Management  
└── common/              # Shared utilities
```

**Tại sao tổ chức như vậy?**
- **Separation of Concerns**: Mỗi domain độc lập, dễ maintain
- **Scalability**: Dễ thêm domain mới (vd: `payment/`, `notification/`)
- **Testability**: Test từng domain riêng biệt

---

## 🔒 Security Implementation Explained

### 1. SecurityConfig.java - Trái Tim Của Security

**Location:** `config/security/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)  // ❶
            .cors(cors -> cors.configurationSource(corsConfigurationSource))  // ❷
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()  // ❸
                .anyRequest().authenticated()  // ❹
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // ❺
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // ❻
            .build();
    }
}
```

**Giải thích từng dòng:**

**❶ CSRF Disabled**
```java
.csrf(AbstractHttpConfigurer::disable)
```
- **Tại sao tắt?** REST API dùng JWT, không dùng cookies/sessions
- **CSRF chỉ cần** khi dùng session-based authentication
- **JWT trong header** không bị CSRF attack

**❷ CORS Configuration**
```java
.cors(cors -> cors.configurationSource(corsConfigurationSource))
```
- Cho phép frontend (React/Angular) từ domain khác gọi API
- Config trong `CorsConfig.java`: allowed origins, methods, headers
- **Production**: Chỉ allow specific domains

**❸ Public Endpoints**
```java
.requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
```
- `/api/auth/**`: Registration, login không cần auth
- `/swagger-ui/**`: API documentation
- **Tất cả endpoints khác** cần authentication

**❹ Authenticated Requests**
```java
.anyRequest().authenticated()
```
- Mọi request không match pattern trên **PHẢI có JWT token**
- Spring Security tự động reject nếu không có token

**❺ Stateless Sessions**
```java
.sessionManagement(session -> 
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```
- **Không tạo HTTP session** trên server
- **Mỗi request độc lập**, auth qua JWT token
- **Scalable**: Có thể deploy nhiều instances

**❻ JWT Filter**
```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```
- **Thêm custom filter** vào filter chain
- **Chạy TRƯỚC** UsernamePasswordAuthenticationFilter
- Filter này extract & validate JWT token

---

### 2. JwtAuthenticationFilter.java - JWT Validation

**Location:** `config/security/JwtAuthenticationFilter.java`

**Flow:**
```
Request → JwtAuthenticationFilter → Controller
   ↓
1. Extract token from "Authorization: Bearer xxx"
2. Validate token (signature, expiration)
3. Load user from database
4. Set SecurityContext
5. Continue to controller
```

**Code Analysis:**

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // ❶ Extract token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // Skip if no token
            return;
        }
        
        String token = authHeader.substring(7);  // Remove "Bearer "
        
        // ❷ Extract username from token
        String username = jwtService.extractUserName(token);
        
        // ❸ Validate & authenticate
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtService.validateAccessToken(token, userDetails)) {
                // ❹ Create authentication object
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                
                // ❺ Set SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**Tại sao cần filter này?**
- Spring Security **không hiểu JWT** by default
- Filter này **dạy Spring Security** cách validate JWT
- Sau khi validate, **set authentication** để controller dùng

---

### 3. CustomUserDetailsService.java - Load User

**Location:** `auth/service/CustomUserDetailsService.java`

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Cacheable(value = "users", key = "#username")  // ❶ Cache
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // ❷ Check if should unlock
        if (user.shouldUnlock()) {
            user.unlock();
            userRepository.save(user);
        }
        
        return new UserPrincipal(user);  // ❸ Wrap
    }
}
```

**❶ Caching**
```java
@Cacheable(value = "users", key = "#username")
```
- **Tại sao?** Mỗi request có JWT → load user từ DB
- **Cache** giảm database queries
- **Invalidate** khi user update profile

**❷ Auto Unlock**
```java
if (user.shouldUnlock()) {
    user.unlock();
}
```
- Account bị lock sau 5 failed attempts
- **Tự động unlock** sau 30 phút
- Check mỗi lần load user

**❸ UserPrincipal Wrapper**
- `User` entity không implement `UserDetails`
- `UserPrincipal` wrap `User` và implement `UserDetails`
- **Separation**: Entity vs Security object

---

## 🎫 JWT Implementation Deep Dive

### JWTService.java - Token Operations

**Location:** `auth/service/JWTService.java`

### 1. Generate Access Token

```java
public String generateAccessToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", userDetails.getAuthorities());  // ❶
    
    return Jwts.builder()
        .setClaims(claims)  // ❷ Custom claims
        .setSubject(userDetails.getUsername())  // ❸ Username
        .setIssuedAt(new Date())  // ❹ Issue time
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))  // ❺ Expiry
        .setIssuer(issuer)  // ❻ Issuer
        .setAudience(audience)  // ❼ Audience
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // ❽ Sign
        .compact();
}
```

**Giải thích:**

**❶ Custom Claims**
```java
claims.put("roles", userDetails.getAuthorities());
```
- Thêm **roles** vào token
- Frontend có thể **decode** (không cần gọi API) để show/hide UI
- **Không lưu sensitive data** (password, email)

**❷-❼ Standard Claims**
- `subject`: Username (required)
- `issuedAt`: Thời gian tạo token
- `expiration`: Khi nào hết hạn (1 hour)
- `issuer`: App tạo token (auth-template)
- `audience`: Ai được dùng token (auth-template-users)

**❽ Signature**
```java
.signWith(getSigningKey(), SignatureAlgorithm.HS256)
```
- **HMAC-SHA256** algorithm
- Sign với **secret key** (từ .env)
- **Verify** token không bị modify

### 2. Validate Token

```java
public Boolean validateAccessToken(String token, UserDetails userDetails) {
    final String username = extractUserName(token);
    
    return (username.equals(userDetails.getUsername())  // ❶ Username match
            && !isTokenExpired(token)  // ❷ Not expired
            && TOKEN_TYPE_ACCESS.equals(extractClaim(token, claims -> claims.get("type"))));  // ❸ Type check
}
```

**3 Checks:**
1. **Username match**: Token username = loaded user username
2. **Not expired**: Current time < expiration time
3. **Type check**: Access token (not refresh token)

### 3. Refresh Token

```java
public String generateRefreshToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", TOKEN_TYPE_REFRESH);  // ❶ Mark as refresh
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))  // ❷ 24 hours
        .signWith(getSigningKey())
        .compact();
}
```

**Tại sao cần Refresh Token?**
- **Access token** ngắn (1h) → bảo mật
- **Refresh token** dài (24h) → UX tốt
- User không cần login lại mỗi giờ

**Flow:**
```
1. Login → Access (1h) + Refresh (24h)
2. After 1h → Access expired
3. Use Refresh → Get new Access + Refresh
4. After 24h → Refresh expired → Login again
```

---

## 📧 Email & Token Management

### 1. TokenService.java - Verification & Reset Tokens

**Location:** `auth/service/TokenService.java`

```java
@Service
public class TokenService {
    
    public String createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();  // ❶ Random token
        
        VerificationToken verificationToken = VerificationToken.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusSeconds(verificationExpiration / 1000))  // ❷ 24h
            .build();
        
        verificationTokenRepository.save(verificationToken);  // ❸ Save to DB
        return token;
    }
    
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenException("Invalid token"));
        
        if (verificationToken.getUsed()) {  // ❹ Check used
            throw new TokenException("Token already used");
        }
        
        if (verificationToken.isExpired()) {  // ❺ Check expired
            throw new TokenException("Token expired");
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);  // ❻ Mark verified
        userRepository.save(user);
        
        verificationToken.setUsed(true);  // ❼ Mark used
        verificationTokenRepository.save(verificationToken);
    }
}
```

**Tại sao lưu token vào database?**
- **JWT** không thể revoke (stateless)
- **Verification token** cần revoke sau dùng
- **Database** cho phép check used/expired

**Security:**
- ❶ UUID random → không đoán được
- ❷ Expiry 24h → giới hạn thời gian
- ❹ One-time use → không reuse
- ❺ Check expired → không dùng old token

### 2. EmailService.java - Send Emails

**Location:** `auth/service/EmailService.java`

```java
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;  // ❶ Spring Mail
    
    @Autowired
    private TemplateEngine templateEngine;  // ❷ Thymeleaf
    
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;  // ❸ Frontend URL
        
        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);  // ❹ Template variable
        
        String htmlContent = templateEngine.process("email/verification-email", context);  // ❺ Render
        
        sendHtmlEmail(to, "Verify your email", htmlContent);  // ❻ Send
    }
}
```

**❶ JavaMailSender**
- Spring Boot auto-config từ `spring.mail.*` properties
- Support SMTP (Gmail, SendGrid, etc.)

**❷ Thymeleaf**
- Template engine cho HTML emails
- Variables: `${verificationUrl}`
- Professional-looking emails

**❸ Frontend URL**
- Link về **frontend** (React/Angular)
- Frontend handle verification, call API
- **Không link trực tiếp** về backend API

---

## 🔍 Code Walkthrough

### Authentication Flow - Step by Step

#### 1. User Registration

**Request:**
```http
POST /api/auth/register
{
  "username": "john",
  "email": "john@example.com",
  "password": "Pass@123"
}
```

**Code Flow:**

```java
// ❶ AuthController.java
@PostMapping("/register")
public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    UserResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

// ❷ AuthService.java
public UserResponse register(RegisterRequest request) {
    // Check username exists
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new UserAlreadyExistsException(request.getUsername());
    }
    
    // Create user
    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))  // ❸ Hash password
        .emailVerified(false)
        .enabled(true)
        .build();
    
    User savedUser = userRepository.save(user);  // ❹ Save to DB
    
    // Send verification email
    String token = tokenService.createVerificationToken(savedUser);  // ❺ Create token
    emailService.sendVerificationEmail(savedUser.getEmail(), token);  // ❻ Send email
    
    return mapToUserResponse(savedUser);
}
```

**❸ Password Hashing**
```java
passwordEncoder.encode(request.getPassword())
```
- **BCrypt** với strength 12
- **Never** lưu plain password
- **One-way** hash, không decode được

#### 2. User Login

**Request:**
```http
POST /api/auth/login
{
  "username": "john",
  "password": "Pass@123"
}
```

**Code Flow:**

```java
// ❶ AuthController.java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
}

// ❷ AuthService.java
public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // ❸ Check account locked
    if (user.getAccountLocked()) {
        if (user.shouldUnlock()) {
            user.unlock();
            userRepository.save(user);
        } else {
            throw new AccountLockedException("Account locked");
        }
    }
    
    try {
        // ❹ Authenticate
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // ❺ Success - reset failed attempts
        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // ❻ Generate tokens
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails.getUsername());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
            .username(user.getUsername())
            .email(user.getEmail())
            .build();
            
    } catch (Exception e) {
        // ❼ Failed - increment attempts
        user.incrementFailedAttempts();
        userRepository.save(user);
        throw e;
    }
}
```

**❹ AuthenticationManager**
- Spring Security component
- Gọi `CustomUserDetailsService.loadUserByUsername()`
- Gọi `PasswordEncoder.matches()` để verify password
- **Throw exception** nếu sai

**❼ Account Lockout**
```java
user.incrementFailedAttempts();
```
- Increment counter mỗi lần fail
- **Lock account** sau 5 lần
- **Auto unlock** sau 30 phút

#### 3. Access Protected Endpoint

**Request:**
```http
GET /api/users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Code Flow:**

```
1. Request → JwtAuthenticationFilter
2. Extract token from header
3. Validate token (JWTService)
4. Load user (CustomUserDetailsService)
5. Set SecurityContext
6. → UserController.getCurrentUser()
7. @AuthenticationPrincipal inject UserDetails
8. Return user data
```

```java
// UserController.java
@GetMapping("/me")
public ResponseEntity<UserResponse> getCurrentUser(
        @AuthenticationPrincipal UserDetails userDetails) {  // ❶ Auto-inject
    
    UserResponse response = userService.getCurrentUser(userDetails.getUsername());
    return ResponseEntity.ok(response);
}
```

**❶ @AuthenticationPrincipal**
- Spring Security annotation
- **Auto-inject** authenticated user
- Lấy từ `SecurityContextHolder.getContext().getAuthentication()`

---

## 🎯 Design Decisions

### 1. Tại sao dùng JWT thay vì Sessions?

**Sessions (Traditional):**
```
✗ Server lưu session state
✗ Không scale horizontal dễ
✗ Cần sticky sessions hoặc shared storage
```

**JWT (This Project):**
```
✓ Stateless - không lưu gì trên server
✓ Scale dễ - deploy nhiều instances
✓ Mobile-friendly - không cần cookies
✓ Microservices-ready
```

### 2. Tại sao có cả Access & Refresh Token?

**Chỉ Access Token:**
```
✗ Ngắn (1h) → User login lại nhiều → UX xấu
✗ Dài (24h) → Bị đánh cắp → Nguy hiểm
```

**Access + Refresh:**
```
✓ Access ngắn (1h) → Bảo mật
✓ Refresh dài (24h) → UX tốt
✓ Refresh có thể revoke → Kiểm soát
```

### 3. Tại sao Email Verification?

```
✓ Verify email thật
✓ Prevent spam accounts
✓ Enable password reset
✓ Professional app standard
```

### 4. Tại sao Account Lockout?

```
✓ Prevent brute-force attacks
✓ Auto-unlock → UX tốt
✓ Security best practice
```

### 5. Tại sao Domain-Driven Design?

```
✓ Separation of concerns
✓ Easy to test
✓ Easy to extend
✓ Team collaboration
```

---

## 📝 Summary

### Project Highlights

1. **Stateless JWT Authentication** - Scalable, mobile-friendly
2. **Dual Token System** - Security + UX
3. **Email Verification** - Professional standard
4. **Account Lockout** - Brute-force protection
5. **Domain-Driven Design** - Maintainable architecture
6. **Production-Ready** - Error handling, logging, documentation

### Key Files to Understand

| File | Purpose | Priority |
|------|---------|----------|
| SecurityConfig.java | Security configuration | ⭐⭐⭐ |
| JwtAuthenticationFilter.java | JWT validation | ⭐⭐⭐ |
| JWTService.java | Token operations | ⭐⭐⭐ |
| AuthService.java | Auth business logic | ⭐⭐⭐ |
| CustomUserDetailsService.java | Load user | ⭐⭐ |
| TokenService.java | Email tokens | ⭐⭐ |
| EmailService.java | Send emails | ⭐ |

### Next Steps

1. **Đọc code** theo flow: Registration → Login → Protected endpoint
2. **Debug** với breakpoints để xem flow
3. **Modify** để hiểu: Thay đổi expiration, thêm claims, etc.
4. **Extend**: Thêm OAuth2, 2FA, etc.

---

**Bây giờ bạn hiểu rõ project implement như thế nào! 🚀**
