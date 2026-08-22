# Architecture Review

## Overview

This document presents the architectural improvements recommended before releasing the next production version of the Finance application. The review focuses on maintainability, financial accuracy, security, scalability, and testability.

---

## 1. Use `BigDecimal` for Monetary Values

### Existing problem

Financial values were represented using `Double`, including transaction values.

### Why it matters

Floating-point types cannot precisely represent many decimal values. This may cause rounding errors in calculations, comparisons, and persisted financial data.

```text
0.1 + 0.2 = 0.30000000000000004
```

### Proposed solution

Use `BigDecimal` for every monetary value and explicitly define database precision:

```java
@Column(precision = 19, scale = 2, nullable = false)
private BigDecimal value;
```

Create monetary constants from strings when necessary:

```java
new BigDecimal("100.00")
```

### Expected benefits

- Accurate financial calculations.
- Consistent values between Java and the database.
- Predictable rounding behavior.
- Reduced risk of accounting errors.

---

## 2. Prefer Constructor Injection

### Existing problem

Some controllers and services used field injection with `@Autowired`.

### Why it matters

Field injection hides class dependencies, makes unit testing more difficult, and permits objects to be created without all required dependencies.

### Proposed solution

Declare dependencies as `final` fields and inject them through constructors:

```java
private final FinancialTransactionService transactionService;

public FinancialTransactionController(
        FinancialTransactionService transactionService) {
    this.transactionService = transactionService;
}
```

### Expected benefits

- Explicit dependencies.
- Easier unit testing.
- Immutable service references.
- Better support for dependency inversion.
- Valid objects immediately after construction.

---

## 3. Define Transaction Boundaries in the Service Layer

### Existing problem

Database operations did not have clearly defined transaction boundaries.

### Why it matters

Operations involving multiple database actions may be partially persisted if an exception occurs. Read operations may also keep unnecessary writable transactions open.

### Proposed solution

Use `@Transactional(readOnly = true)` at the service level and override it with `@Transactional` only for methods that modify data:

```java
@Service
@Transactional(readOnly = true)
public class FinancialTransactionService {

    @Transactional
    public FinancialTransactionResponseDto create(...) {
        // Write operation
    }
}
```

### Expected benefits

- Atomic write operations.
- Automatic rollback when failures occur.
- Clear transaction ownership.
- Improved read-operation performance.
- Reduced risk of inconsistent data.

---

## 4. Separate DTO Mapping from Business Logic

### Existing problem

Entity-to-DTO conversion was performed directly inside service methods.

### Why it matters

Mixing mapping with business logic makes services larger, increases duplication, and makes API contract changes harder to maintain.

### Proposed solution

Create dedicated mapper components, such as `FinancialTransactionMapper` and `BudgetMapper`. Mappers should handle request-to-entity conversion, entity updates, and entity-to-response conversion.

### Expected benefits

- Smaller and more focused services.
- Reusable mapping logic.
- Easier unit testing.
- Reduced duplication.
- Clear separation of responsibilities.

---

## 5. Strengthen Request Validation

### Existing problem

Some request DTO fields could receive null, blank, negative, or incorrectly formatted values.

### Why it matters

Invalid values may reach the service or database layer, resulting in inconsistent data and unclear runtime errors.

### Proposed solution

Use Jakarta Bean Validation annotations in request DTOs:

```java
@NotBlank
@Size(max = 100)
private String name;

@NotNull
@DecimalMin("0.01")
@Digits(integer = 17, fraction = 2)
private BigDecimal monthlyLimit;
```

Business validations involving multiple fields, such as ensuring that an end date is not before a start date, should remain in the service layer.

### Expected benefits

- Invalid requests are rejected early.
- Consistent validation responses.
- Cleaner service methods.
- Better API documentation.
- Improved data integrity.

---

## 6. Centralize Exception Handling

### Existing problem

Errors may be created and returned differently across controllers and services.

### Why it matters

Inconsistent error responses make the API harder to consume and may expose internal implementation details.

### Proposed solution

Introduce domain-specific exceptions and handle them through a global `@RestControllerAdvice`. Examples include:

- `ResourceNotFoundException`
- `BusinessRuleException`
- `UnauthorizedOperationException`

A standardized response should include `timestamp`, `status`, `error`, `message`, and `path`.

### Expected benefits

- Consistent API error responses.
- Less exception-handling duplication.
- Clearer service code.
- Easier monitoring and troubleshooting.
- Reduced exposure of internal details.

---

## 7. Apply Soft Delete Consistently

### Existing problem

Soft-deleted financial transactions could still be returned by some repository queries.

### Why it matters

A logically deleted record should not be visible through normal API operations. Inconsistent filtering can expose deleted or sensitive information.

### Proposed solution

Use repository methods that explicitly filter deleted records:

```java
findByIdAndUserAndDeletedAtIsNull(...)
findAllByUserAndDeletedAtIsNull(...)
```

Apply this rule consistently to every entity that supports soft deletion.

### Expected benefits

- Deleted records remain hidden.
- Consistent behavior across endpoints.
- Better auditability.
- Protection against accidental data exposure.
- Recoverability when required.

---

## 8. Enforce Resource Ownership in Repository Queries

### Existing problem

Searching only by entity ID may allow a user to access another user's resource if authorization is forgotten or applied incorrectly.

### Why it matters

This is an object-level authorization risk and may lead to insecure direct object reference vulnerabilities.

### Proposed solution

Include the authenticated user directly in repository queries:

```java
findByIdAndUserAndDeletedAtIsNull(id, currentUser)
```

The application must not trust a user ID received from the client to determine ownership.

### Expected benefits

- Strong user data isolation.
- Reduced authorization complexity.
- Protection against horizontal privilege escalation.
- Security rules enforced close to data access.

---

## 9. Improve JWT Security and Configuration

### Existing problem

Authentication behavior depends on configuration values such as JWT expiration and secrets. Incorrect test or production configuration can create security problems or unstable tests.

### Why it matters

Hard-coded or weak secrets, overly long token lifetimes, and inconsistent expiration rules increase the risk of unauthorized access.

### Proposed solution

- Store JWT secrets in environment variables or a secrets manager.
- Use separate configurations for development, testing, and production.
- Define a reasonable access-token lifetime.
- Validate signatures, expiration, and expected claims.
- Avoid tests that depend on long `Thread.sleep` calls.
- Test unauthorized access deterministically with invalid tokens.

### Expected benefits

- Stronger authentication security.
- Safer secret management.
- Faster and more reliable tests.
- Easier environment configuration.
- Reduced risk of token misuse.

---

## 10. Use Structured and Contextual Logging

### Existing problem

Some logs do not provide enough context for production troubleshooting, while others may expose user information unnecessarily.

### Why it matters

Unstructured logs are difficult to search and correlate. Sensitive information in logs may create privacy and security risks.

### Proposed solution

Use structured logging with contextual identifiers such as `requestId`, `userId`, `operation`, `resourceId`, `result`, and `duration`.

Avoid logging passwords, JWT tokens, financial account secrets, or unnecessary personal information. Apply `INFO`, `WARN`, `ERROR`, and `DEBUG` levels consistently.

### Expected benefits

- Faster production troubleshooting.
- Better observability.
- Easier log aggregation.
- Improved security and privacy.
- Support for metrics and alerting.

---

## 11. Introduce Pagination and Filtering

### Existing problem

List endpoints return every record belonging to the current user.

### Why it matters

As the number of transactions and budgets grows, loading all records increases database load, memory consumption, network traffic, and response time.

### Proposed solution

Use Spring Data pagination:

```java
Page<FinancialTransaction> findAllByUserAndDeletedAtIsNull(
        User user,
        Pageable pageable);
```

Expose query parameters such as `page`, `size`, `sort`, `category`, `status`, `startDate`, and `endDate`. Define a maximum page size to prevent excessively large requests.

### Expected benefits

- Predictable API response sizes.
- Lower memory and database usage.
- Better response times.
- Improved user experience.
- Greater scalability.

---

## 12. Improve Test Architecture

### Existing problem

Some integration tests depended on long waits and environment-specific behavior. The project also forced an outdated Surefire version while running on Java 25.

### Why it matters

Slow or unstable tests reduce developer confidence and make continuous integration unreliable.

### Proposed solution

- Cover business rules with fast unit tests.
- Use integration tests where framework and database behavior must be validated.
- Use Testcontainers for an isolated PostgreSQL environment.
- Avoid fixed waits such as `Thread.sleep`.
- Test invalid or expired tokens deterministically.
- Use a modern Maven Surefire version compatible with Java 25.
- Separate unit and integration tests as the suite grows.
- Run the complete test suite in continuous integration.

### Expected benefits

- Faster feedback.
- Deterministic test execution.
- Fewer environment-related failures.
- Safer refactoring.
- More reliable releases.

---

## 13. Organize Packages by Business Feature

### Existing problem

The project is primarily organized by technical layer, such as controllers, services, repositories, DTOs, and models.

### Why it matters

As the system grows, a layer-based structure spreads each feature across many directories. This increases navigation effort and coupling between unrelated domains.

### Proposed solution

Gradually organize the application by business feature:

```text
finances/
  shared/
    config/
    security/
    exception/
  transaction/
    controller/
    service/
    repository/
    dto/
    mapper/
    model/
  budget/
    controller/
    service/
    repository/
    dto/
    mapper/
    model/
  user/
    controller/
    service/
    repository/
    dto/
    model/
```

The migration should be incremental to avoid unnecessary disruption.

### Expected benefits

- Better feature isolation.
- Easier navigation.
- Clear ownership boundaries.
- Reduced coupling.
- Improved maintainability as the project grows.

---

## 14. Add Database Migrations and Constraints

### Existing problem

Relying only on automatic schema generation makes database changes harder to track and reproduce across environments.

### Why it matters

Production databases require controlled, versioned, and repeatable changes. Application validation alone is not sufficient to guarantee integrity.

### Proposed solution

Use Flyway or Liquibase to version schema changes. Add database constraints for required fields, monetary precision, unique user identifiers, foreign keys, and indexes on frequently queried ownership and soft-delete columns.

Example composite index:

```text
(user_id, deleted_at)
```

### Expected benefits

- Repeatable deployments.
- Auditable schema evolution.
- Consistent environments.
- Improved query performance.
- Stronger data integrity.

---

## Conclusion

The proposed improvements prioritize financial correctness, security, maintainability, scalability, and test reliability.

Several improvements were applied during the assessment:

- Migration from `Double` to `BigDecimal`.
- Constructor-based dependency injection.
- Explicit transaction boundaries.
- Dedicated DTO mappers.
- Stronger request validation.
- Consistent soft-delete filtering.
- User ownership checks.
- REST status-code corrections.
- Deterministic authentication tests.
- Modern Maven test execution configuration.
- Implementation of the Budget Management domain.
- Unit and integration test coverage.

The remaining recommendations provide a roadmap for evolving the application safely as its user base and business requirements grow.
