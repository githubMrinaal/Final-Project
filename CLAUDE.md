# Workflow Approval System — Claude Code Context

## Project Overview
A backend-focused full-stack workflow approval system built as a university Individual Study course final project. Deadline: May 1.

The system replaces email/WhatsApp approvals with a structured request and approval workflow. Users can submit Leave or Budget requests, and Approvers can approve or reject them with comments.

---

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.5.13
- Spring MVC (REST API only — no Thymeleaf, no server-rendered HTML)
- Spring Data JPA + Hibernate
- Spring Security + JWT
- H2 (in-memory database for development)
- Maven
- Lombok

### Frontend (built separately after backend is complete)
- Next.js with TypeScript
- Fetches JSON from the Spring Boot REST API

### CI/CD
- GitHub Actions (backend: mvn test + mvn package, frontend: npm install + npm run build)

---

## Package Structure

```
com.app.workflow_app
├── controller       → REST controllers (HTTP layer only, no business logic)
├── service          → Business logic and rules
├── repository       → Spring Data JPA repositories
├── model            → JPA entities
├── dto              → Data Transfer Objects (request/response shapes)
├── exception        → Custom exceptions and global exception handler
└── config           → Security config, JWT config
```

---

## Entities

### User
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;           // unique, used for login
    private String password;        // BCrypt hashed
    @Enumerated(EnumType.STRING)
    private Role role;              // REQUESTER or APPROVER
}
```

### Request
```java
@Entity
@Table(name = "requests")
public class Request {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private RequestType type;       // LEAVE or BUDGET

    @Enumerated(EnumType.STRING)
    private RequestStatus status;   // PENDING, APPROVED, REJECTED

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    // Decision fields
    private String decisionComment;
    private LocalDateTime decidedAt;

    @ManyToOne
    @JoinColumn(name = "decided_by_id")
    private User decidedBy;

    // Leave-specific fields (null for BUDGET)
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;

    // Budget-specific fields (null for LEAVE)
    private BigDecimal amount;
    private String currency;
}
```

### AuditLog
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @Enumerated(EnumType.STRING)
    private AuditAction action;     // CREATED, APPROVED, REJECTED

    @ManyToOne
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    private String comment;
    private LocalDateTime timestamp;
}
```

---

## Enums

```java
public enum Role {
    REQUESTER, APPROVER
}

public enum RequestType {
    LEAVE, BUDGET
}

public enum RequestStatus {
    PENDING, APPROVED, REJECTED
}

public enum AuditAction {
    CREATED, APPROVED, REJECTED
}
```

---

## API Endpoints

### Auth (public — no token required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register a new user |
| POST | /api/auth/login | Login, returns JWT token |

### Requests — Requester (requires REQUESTER role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/requests | Create a new request (LEAVE or BUDGET) |
| GET | /api/requests/mine | Get all requests by the logged-in user |
| GET | /api/requests/{id} | Get a single request by ID |

### Requests — Approver (requires APPROVER role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/requests/pending | Get all pending requests |
| POST | /api/requests/{id}/approve | Approve a request with optional comment |
| POST | /api/requests/{id}/reject | Reject a request with optional comment |

---

## Business Rules (enforced in the Service layer — never in the Controller)

1. Only users with REQUESTER role can create requests
2. Only users with APPROVER role can approve or reject requests
3. Only requests with status PENDING can be approved or rejected
4. A requester can only view their own requests
5. Every action (create, approve, reject) must create an AuditLog entry
6. Status is always set automatically — never trust the client to send a status
7. createdAt and timestamp fields are always set server-side
8. decidedAt is set at the time of the approval or rejection decision

---

## DTOs

### Auth
- `RegisterRequest` — name, email, password, role
- `LoginRequest` — email, password
- `AuthResponse` — token (JWT string)

### Requests
- `CreateRequestDTO` — title, description, type, and type-specific fields (startDate/endDate/numberOfDays for LEAVE, amount/currency for BUDGET)
- `RequestResponseDTO` — full request details returned to client
- `DecisionDTO` — comment (optional, used for approve/reject)

---

## Security

- Spring Security with JWT (stateless — no sessions)
- Passwords hashed with BCrypt
- JWT token issued on login, required on all protected endpoints
- Token passed as: `Authorization: Bearer <token>`
- Endpoints protected by role using `@PreAuthorize` or `SecurityConfig`
- `/api/auth/**` is public (no token required)

---

## Database

- H2 in-memory database for development
- `spring.jpa.hibernate.ddl-auto=create-drop` — tables created from entities on startup, dropped on shutdown
- H2 console enabled at `/h2-console` for debugging
- Single-table approach for Request — LEAVE and BUDGET share one table, type-specific fields are null when not applicable

### application.properties (development)
```properties
spring.datasource.url=jdbc:h2:mem:workflowdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

---

## Architecture Rules (always follow these)

1. **Controllers** — receive HTTP requests, call service methods, return ResponseEntity. No business logic.
2. **Services** — contain all business rules. Call repositories. Create audit logs. Annotate modifying methods with @Transactional.
3. **Repositories** — extend JpaRepository. Add custom query methods as needed.
4. **DTOs** — always use DTOs for request/response. Never expose entities directly to the client.
5. **Exceptions** — throw custom exceptions from the service layer. Handle them in a @ControllerAdvice global exception handler.

---

## Error Handling

Use a global `@ControllerAdvice` exception handler that returns:
```json
{
  "status": 400,
  "message": "Only PENDING requests can be approved",
  "timestamp": "2025-04-09T10:30:00"
}
```

Custom exceptions to implement:
- `ResourceNotFoundException` — 404 when entity not found
- `UnauthorizedActionException` — 403 when role doesn't permit action
- `InvalidRequestStateException` — 400 when trying to approve/reject a non-PENDING request

---

## What NOT to do

- Do NOT use Thymeleaf or any server-side HTML rendering
- Do NOT put business logic in controllers
- Do NOT expose JPA entities directly as API responses — always use DTOs
- Do NOT use field injection (@Autowired on fields) — use constructor injection
- Do NOT use ddl-auto=create in production
- Do NOT store plain-text passwords — always BCrypt
