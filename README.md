# Workflow Approval System

A backend-focused full-stack workflow approval system. Users can submit Leave or Budget requests, and Approvers can approve or reject them with comments.

## Tech Stack

- Java 21 / Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA + H2 (in-memory)
- Lombok, Maven

## Setup

1. Copy the example properties file:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```

2. Replace `YOUR_SECRET_HERE` in `application.properties` with a real secret:
   ```bash
   openssl rand -hex 32
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The app starts on `http://localhost:8080`. The H2 console is available at `http://localhost:8080/h2-console`.

Two test users are created automatically on startup:
- **Requester** — `requester@test.com` / `password123`
- **Approver** — `approver@test.com` / `password123`
