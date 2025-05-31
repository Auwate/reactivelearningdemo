# Reactive AuthN/AuthZ

This project is a **Spring Boot WebFlux** application designed to explore scalable, reactive implementations of authentication and authorization. It builds upon prior work using Spring MVC by shifting to non-blocking, reactive programming for greater efficiency and performance.

The application demonstrates production-grade practices including:

- **Reactive two-factor authentication (2FA)**
- **Cookie-based sessions with CSRF protection**
- **Comprehensive testing** (unit, integration, end-to-end, and contract testing)
- **Dockerized multi-stage builds**
- **CI/CD with GitHub Actions**
- **Performance benchmarking (planned)**

> 🛠️ This project serves both as a learning tool and a solid foundation for integrating secure, scalable authN/authZ into your own applications.

---

## 📦 Components

### 🐳 Docker

The Docker configuration uses a **multi-stage build** to separate development dependencies from the final runtime image:

- **Build Stage**  
  Uses `maven:3.9.9-eclipse-temurin-21-alpine` to compile the application and resolve dependencies.

- **Runtime Stage**  
  Uses `eclipse-temurin:21.0.5_11-jre-alpine-3.21` for a lean final image containing only the compiled `.jar` and minimal runtime.

```dockerfile
# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
# ... build steps ...

# Runtime stage
FROM eclipse-temurin:21.0.5_11-jre-alpine-3.21
# ... runtime setup ...
```

### ⚙️ CI/CD

This project uses **GitHub Actions** for continuous integration and deployment. The automated pipeline performs the following steps:

1. **Run tests** using `Maven` to verify the application’s integrity.
2. **Build a Docker image** using a multi-stage Dockerfile for efficient image creation.
3. **Push the image** to [Docker Hub](https://hub.docker.com) (or your preferred container registry).

This CI/CD setup ensures:

- ✅ Reliable and repeatable builds
- 🔍 Immediate feedback when changes break the build or tests
- 🚀 Seamless deployments with minimal manual intervention

You can find the workflow configuration under `.github/workflows/ci.yml`.

---

### 🧪 Testing

This project uses a layered testing strategy to validate the application at multiple levels:

- **Unit Tests**  
  Focused on individual components like services and utilities using JUnit 5 and Mockito.

- **Integration Tests**  
  Verify interactions between layers, such as controller-service-database flows, using Spring Boot’s `@SpringBootTest`.

- **End-to-End (E2E) Tests**  
  Simulate full authentication workflows, including login, 2FA, and session management.

- **Contract Tests**  
  Ensure API compatibility with external consumers using tools like Spring Cloud Contract and OpenAPI.

These tests are automatically run during CI and can be executed locally with:

```bash
./mvnw test
