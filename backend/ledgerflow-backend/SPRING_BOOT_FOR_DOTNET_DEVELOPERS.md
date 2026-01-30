# Spring Boot for .NET Developers

## Quick Reference: .NET vs Spring Boot

| Concept | .NET / ASP.NET Core | Spring Boot |
|---------|-------------------|-------------|
| **Main Entry Point** | `Program.cs` with `WebApplication.CreateBuilder()` | `@SpringBootApplication` class with `main()` method |
| **Dependency Injection** | `builder.Services.AddScoped<T>()` | `@Service`, `@Repository`, `@Component` annotations |
| **Controllers** | `[ApiController]` class with `[HttpGet]` | `@RestController` class with `@GetMapping` |
| **Database ORM** | Entity Framework Core | JPA / Hibernate |
| **Entities** | `DbSet<T>` in `DbContext` | `@Entity` class with `JpaRepository<T, ID>` |
| **Configuration** | `appsettings.json` | `application.yml` or `application.properties` |
| **Dependency Injection** | Constructor injection (automatic) | Constructor injection (automatic with `@Autowired` or implicit) |
| **Service Layer** | Interface + Implementation | Interface + `@Service` class (or just `@Service` class) |
| **Repository Pattern** | `IRepository<T>` with EF Core | `JpaRepository<T, ID>` interface (Spring Data JPA) |
| **HTTP Methods** | `[HttpGet]`, `[HttpPost]`, `[HttpPut]`, `[HttpDelete]` | `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` |
| **Request Body** | `[FromBody]` parameter | `@RequestBody` parameter |
| **Route Parameters** | `[FromRoute]` or `{id}` in route | `@PathVariable` parameter |
| **Query Parameters** | `[FromQuery]` parameter | `@RequestParam` parameter |
| **Validation** | `[Required]`, `[Email]` attributes | `@NotNull`, `@NotBlank`, `@Email` annotations |
| **Exception Handling** | `IExceptionHandler` or middleware | `@ControllerAdvice` with `@ExceptionHandler` |
| **Configuration** | `IConfiguration` interface | `@Value` or `@ConfigurationProperties` |
| **Auto-Configuration** | Minimal APIs / Convention-based | Spring Boot auto-configuration (magic!) |

## Key Differences

### 1. Dependency Injection

**.NET:**
```csharp
// Program.cs
builder.Services.AddScoped<IUserService, UserService>();

// Controller
public class UserController : ControllerBase
{
    private readonly IUserService _userService;
    public UserController(IUserService userService) // Constructor injection
    {
        _userService = userService;
    }
}
```

**Spring Boot:**
```java
// No registration needed! Spring finds @Service classes automatically

@Service  // This makes it a Spring-managed bean
public class UserService {
    // Spring automatically creates and manages this
}

@RestController
public class UserController {
    private final UserService userService;  // Spring injects this automatically
    
    public UserController(UserService userService) {  // Constructor injection
        this.userService = userService;
    }
}
```

**Key Difference:** In Spring Boot, you don't need to register services manually. Spring scans for `@Service`, `@Repository`, `@Component` annotations and creates beans automatically.

### 2. Database Access

**.NET (Entity Framework Core):**
```csharp
public class ApplicationDbContext : DbContext
{
    public DbSet<User> Users { get; set; }
    
    public async Task<User> GetUserById(int id)
    {
        return await Users.FindAsync(id);
    }
}
```

**Spring Boot (JPA):**
```java
// Entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}

// Repository - Spring Data JPA creates implementation automatically!
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring automatically implements: findById(), findAll(), save(), delete()
    // You can add custom methods:
    Optional<User> findByUsername(String username);  // Spring implements this too!
}
```

**Key Difference:** Spring Data JPA creates repository implementations automatically based on method names. No need to write SQL or implementation code!

### 3. Controllers

**.NET:**
```csharp
[ApiController]
[Route("api/[controller]")]
public class UsersController : ControllerBase
{
    [HttpGet("{id}")]
    public async Task<ActionResult<User>> GetUser(int id)
    {
        var user = await _userService.GetUserById(id);
        return Ok(user);
    }
}
```

**Spring Boot:**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
```

**Key Difference:** Very similar! Spring uses annotations instead of attributes, and `ResponseEntity<T>` is like `ActionResult<T>`.

### 4. Configuration

**.NET:**
```json
// appsettings.json
{
  "Database": {
    "ConnectionString": "Server=localhost;Database=mydb;"
  }
}
```

```csharp
// Program.cs
builder.Configuration.GetConnectionString("Database:ConnectionString");
```

**Spring Boot:**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
```

```java
@Value("${spring.datasource.url}")
private String databaseUrl;
```

**Key Difference:** Spring Boot uses YAML (more readable) and `@Value` annotation for injection.

## Spring Boot Annotations Cheat Sheet

| Annotation | Purpose | .NET Equivalent |
|------------|---------|----------------|
| `@SpringBootApplication` | Main application class | `WebApplication.CreateBuilder()` |
| `@RestController` | REST API controller | `[ApiController]` |
| `@Service` | Business logic service | `AddScoped<IService, Service>()` |
| `@Repository` | Data access layer | `DbContext` or Repository pattern |
| `@Entity` | Database entity | Entity Framework entity class |
| `@Component` | Generic Spring component | Any registered service |
| `@Autowired` | Dependency injection | Constructor injection (automatic in .NET) |
| `@GetMapping` | HTTP GET endpoint | `[HttpGet]` |
| `@PostMapping` | HTTP POST endpoint | `[HttpPost]` |
| `@RequestBody` | Request body parameter | `[FromBody]` |
| `@PathVariable` | URL path parameter | `{id}` in route |
| `@RequestParam` | Query parameter | `[FromQuery]` |
| `@Valid` | Validate request | `[ApiController]` does this automatically |
| `@Transactional` | Database transaction | `DbContext.SaveChanges()` is transactional |
| `@Configuration` | Configuration class | `Program.cs` or `Startup.cs` |
| `@Bean` | Create a Spring bean | `builder.Services.AddSingleton<T>()` |

## Spring Boot Magic (Auto-Configuration)

Spring Boot automatically configures:
- **Database connections** - Just add `spring-boot-starter-data-jpa` and configure `application.yml`
- **Web server** - Embedded Tomcat (like Kestrel in .NET)
- **JSON serialization** - Jackson (like System.Text.Json)
- **Health checks** - Actuator (like health checks in .NET)
- **CORS** - Can be configured in `@Configuration` class

**In .NET**, you configure everything manually in `Program.cs`.  
**In Spring Boot**, most things work out of the box!

## Project Structure Comparison

**.NET:**
```
Project/
├── Controllers/
│   └── UsersController.cs
├── Services/
│   └── UserService.cs
├── Models/
│   └── User.cs
├── Data/
│   └── ApplicationDbContext.cs
└── Program.cs
```

**Spring Boot:**
```
src/main/java/com/ledgerflow/
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── entity/
│   └── User.java
├── repository/
│   └── UserRepository.java
└── LedgerFlowApplication.java
```

**Key Difference:** Spring Boot uses packages (folders) for organization, similar to .NET namespaces.

## Common Patterns

### 1. Service Layer Pattern
Both .NET and Spring Boot use the same pattern:
- **Controller** → Handles HTTP requests
- **Service** → Business logic
- **Repository** → Data access

### 2. Dependency Injection
Both use constructor injection (preferred method).

### 3. Exception Handling
- **.NET**: `IExceptionHandler` or middleware
- **Spring Boot**: `@ControllerAdvice` with `@ExceptionHandler`

### 4. Validation
- **.NET**: Data annotations (`[Required]`, `[Email]`)
- **Spring Boot**: Bean Validation (`@NotNull`, `@NotBlank`, `@Email`)

## Next Steps

Now that you understand the basics, we'll build:
1. **Entities** - Like EF Core entities
2. **Repositories** - Like DbContext, but Spring creates implementations automatically
3. **Services** - Like your service classes in .NET
4. **Controllers** - Like API controllers in .NET
5. **DTOs** - Like DTOs in .NET (separate from entities)

Let's start building!

