# Development Guide

## 📋 Table of Contents

1. [Setup Development Environment](#setup-development-environment)
2. [Project Structure](#project-structure)
3. [Development Workflow](#development-workflow)
4. [Testing](#testing)
5. [Debugging](#debugging)
6. [Code Style](#code-style)

---

## 🛠️ Setup Development Environment

### Prerequisites

- **Java 21** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 15+** - [Download](https://www.postgresql.org/download/)
- **IDE** - IntelliJ IDEA (recommended) or VS Code
- **Git** - [Download](https://git-scm.com/)
- **Docker** (optional) - [Download](https://www.docker.com/)

### IDE Setup

#### IntelliJ IDEA

1. **Install Plugins:**
   - Lombok
   - Spring Boot
   - Database Tools

2. **Enable Annotation Processing:**
   - Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"

3. **Configure Code Style:**
   - Settings → Editor → Code Style → Java
   - Import `codestyle.xml` (if provided)

4. **Configure Run Configuration:**
   - Run → Edit Configurations
   - Add new Spring Boot configuration
   - Main class: `com.auth.template.AuthTemplateApplication`
   - VM options: `-Dspring.profiles.active=dev`

#### VS Code

1. **Install Extensions:**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Configure launch.json:**
```json
{
  "type": "java",
  "name": "Spring Boot App",
  "request": "launch",
  "mainClass": "com.auth.template.AuthTemplateApplication",
  "projectName": "spring-boot-auth-template",
  "args": "--spring.profiles.active=dev"
}
```

---

## 📁 Project Structure

```
src/main/java/com/auth/template/
├── config/                    # Configuration classes
│   ├── security/              # Security-related configs
│   │   ├── JwtProperties      # JWT configuration properties
│   │   ├── SecurityConfig     # Spring Security configuration
│   │   └── JwtAuthenticationFilter  # JWT filter
│   ├── CorsConfig             # CORS configuration
│   └── SwaggerConfig          # API documentation config
│
├── auth/                      # Authentication domain
│   ├── controller/
│   │   └── AuthController     # Auth endpoints (register, login, etc.)
│   ├── service/
│   │   ├── AuthService        # Auth business logic
│   │   ├── JWTService         # JWT token operations
│   │   ├── TokenService       # Verification/reset tokens
│   │   ├── EmailService       # Email sending
│   │   └── CustomUserDetailsService  # Load user for auth
│   ├── dto/
│   │   ├── request/           # Request DTOs
│   │   │   ├── LoginRequest
│   │   │   ├── RegisterRequest
│   │   │   ├── RefreshTokenRequest
│   │   │   ├── ForgotPasswordRequest
│   │   │   ├── ResetPasswordRequest
│   │   │   └── ChangePasswordRequest
│   │   └── response/          # Response DTOs
│   │       ├── AuthResponse
│   │       ├── UserResponse
│   │       └── ErrorResponse
│   └── entity/
│       ├── User               # User entity
│       ├── UserPrincipal      # Spring Security UserDetails
│       ├── VerificationToken  # Email verification token
│       └── PasswordResetToken # Password reset token
│
├── user/                      # User management domain
│   ├── controller/
│   │   └── UserController     # User profile endpoints
│   ├── service/
│   │   └── UserService        # User business logic
│   └── repository/
│       ├── UserRepository
│       ├── VerificationTokenRepository
│       └── PasswordResetTokenRepository
│
├── common/                    # Shared utilities
│   ├── exception/             # Exception handling
│   │   ├── GlobalExceptionHandler
│   │   ├── JwtAuthenticationException
│   │   ├── UserAlreadyExistsException
│   │   ├── TokenException
│   │   └── AccountLockedException
│   ├── constant/              # Constants
│   │   ├── AppConstants
│   │   └── AuthProvider (enum)
│   └── util/                  # Utility classes (future)
│
└── AuthTemplateApplication    # Main application class
```

### Key Design Patterns

1. **Domain-Driven Design (DDD)**
   - Organized by domain (auth, user, common)
   - Each domain has its own controllers, services, repositories

2. **DTO Pattern**
   - Separate request/response DTOs
   - Never expose entities directly

3. **Service Layer Pattern**
   - Business logic in services
   - Controllers are thin, just routing

4. **Repository Pattern**
   - Data access abstraction
   - Spring Data JPA repositories

---

## 🔄 Development Workflow

### 1. Create New Feature

```bash
# 1. Create feature branch
git checkout -b feature/add-two-factor-auth

# 2. Make changes
# 3. Test locally
./mvnw test

# 4. Commit
git add .
git commit -m "feat: add two-factor authentication"

# 5. Push
git push origin feature/add-two-factor-auth

# 6. Create pull request
```

### 2. Add New Endpoint

**Example: Add "Get All Users" endpoint for admin**

#### Step 1: Create DTO (if needed)

```java
// src/main/java/com/auth/template/user/dto/response/UserListResponse.java
@Data
@Builder
public class UserListResponse {
    private List<UserResponse> users;
    private int totalPages;
    private long totalElements;
}
```

#### Step 2: Add Service Method

```java
// src/main/java/com/auth/template/user/service/UserService.java
public Page<UserResponse> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable)
            .map(this::mapToUserResponse);
}
```

#### Step 3: Add Controller Endpoint

```java
// src/main/java/com/auth/template/user/controller/UserController.java
@GetMapping("/all")
@PreAuthorize("hasRole('ADMIN')")
@Operation(summary = "Get all users (Admin only)")
public ResponseEntity<Page<UserResponse>> getAllUsers(
        @PageableDefault(size = 20) Pageable pageable) {
    Page<UserResponse> users = userService.getAllUsers(pageable);
    return ResponseEntity.ok(users);
}
```

#### Step 4: Test

```bash
# Run tests
./mvnw test

# Test manually
curl -X GET http://localhost:8080/api/users/all \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 3. Add New Entity

**Example: Add "Role" entity for RBAC**

#### Step 1: Create Entity

```java
// src/main/java/com/auth/template/auth/entity/Role.java
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;  // ROLE_USER, ROLE_ADMIN
    
    private String description;
}
```

#### Step 2: Update User Entity

```java
// Add to User.java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

#### Step 3: Create Repository

```java
// src/main/java/com/auth/template/user/repository/RoleRepository.java
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
```

#### Step 4: Update UserPrincipal

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
}
```

---

## 🧪 Testing

### Unit Tests

```java
// src/test/java/com/auth/template/auth/service/AuthServiceTest.java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void register_ShouldCreateUser_WhenValidRequest() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser", "test@example.com", "Test@123", "Test User"
        );
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(new User());
        
        // When
        UserResponse response = authService.register(request);
        
        // Then
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }
}
```

### Integration Tests

```java
// src/test/java/com/auth/template/auth/controller/AuthControllerIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void register_ShouldReturn201_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "newuser", "new@example.com", "Test@123", "New User"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }
}
```

### Run Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthServiceTest

# Run with coverage
./mvnw test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## 🐛 Debugging

### Enable Debug Logging

```yaml
# application-dev.yml
logging:
  level:
    com.auth.template: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Debug JWT Issues

```java
// Add to JWTService
private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

public String generateAccessToken(UserDetails userDetails) {
    logger.debug("Generating access token for user: {}", userDetails.getUsername());
    // ... rest of code
}
```

### Debug SQL Queries

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### Remote Debugging

```bash
# Run with debug enabled
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# In IDE, attach debugger to localhost:5005
```

---

## 📝 Code Style

### Naming Conventions

- **Classes**: PascalCase (`UserService`, `AuthController`)
- **Methods**: camelCase (`getUserById`, `validateToken`)
- **Variables**: camelCase (`accessToken`, `userRepository`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_FAILED_ATTEMPTS`)
- **Packages**: lowercase (`com.auth.template.modules.auth.service`)

### Code Organization

```java
// 1. Package declaration
package com.auth.template.modules.auth.service;

// 2. Imports (organized)

import com.auth.template...;
import org.springframework...;
import java.util...;

// 3. Class JavaDoc

/**
 * Service for handling authentication operations
 */
@Service
public class AuthService {

    // 4. Constants
    private static final Logger logger = ...;

    // 5. Dependencies (final, injected via constructor)
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 6. Constructor
    @Autowired
    public AuthService(...) {
        this.userRepository = userRepository;
        ...
    }

    // 7. Public methods
    public UserResponse register(...) {
        ...
    }

    // 8. Private methods
    private UserResponse mapToUserResponse(...) {
        ...
    }
}
```

### Best Practices

1. **Use Constructor Injection**
   ```java
   // ✅ Good
   @Autowired
   public AuthService(UserRepository repo) {
       this.repo = repo;
   }
   
   // ❌ Bad
   @Autowired
   private UserRepository repo;
   ```

2. **Use Lombok Wisely**
   ```java
   // ✅ Good - DTOs
   @Data
   @Builder
   public class UserResponse { }
   
   // ❌ Bad - Entities (can cause issues)
   @Data  // Avoid on entities
   @Entity
   public class User { }
   ```

3. **Handle Exceptions Properly**
   ```java
   // ✅ Good
   try {
       emailService.send(email);
   } catch (Exception e) {
       logger.error("Failed to send email", e);
       throw new EmailException("Failed to send email", e);
   }
   
   // ❌ Bad
   try {
       emailService.send(email);
   } catch (Exception e) {
       e.printStackTrace();  // Don't do this
   }
   ```

4. **Use Meaningful Variable Names**
   ```java
   // ✅ Good
   User authenticatedUser = userRepository.findByUsername(username);
   
   // ❌ Bad
   User u = userRepository.findByUsername(username);
   ```

---

## 🔧 Useful Commands

```bash
# Clean and build
./mvnw clean install

# Run without tests
./mvnw spring-boot:run -DskipTests

# Package as JAR
./mvnw clean package

# Run specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Check for dependency updates
./mvnw versions:display-dependency-updates

# Format code
./mvnw spotless:apply

# Generate documentation
./mvnw javadoc:javadoc
```

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Lombok Features](https://projectlombok.org/features/)
- [JPA Best Practices](https://vladmihalcea.com/tutorials/hibernate/)
