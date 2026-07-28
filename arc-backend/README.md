# ARC Backend Service

Enterprise Spring Boot backend for the ARC Production Suite & Industrial Line Operating System.

## Technology Stack
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security)
- **Database**: PostgreSQL (Production) / H2 (Development & Testing)
- **Security**: JWT Authentication (JJWT 0.12.x) & BCrypt Password Encoding
- **Build Tool**: Maven 3.9.x

## Project Structure

```text
arc-backend/
├── .gitignore
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
│
├── src/
│   ├── main/
│   │   ├── java/com/arc/login/
│   │   │   ├── LoginApplication.java         # Application Entry Point
│   │   │   ├── config/                       # Security, CORS & Seeding Configurations
│   │   │   ├── controller/                   # REST API Controllers
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   ├── entity/                       # JPA Entities (User, Role)
│   │   │   ├── repository/                   # Spring Data JPA Repositories
│   │   │   ├── security/                     # JWT & UserDetailsService Security
│   │   │   ├── service/                      # Business Logic Interfaces
│   │   │   │   └── impl/                     # Service Implementations
│   │   │   ├── exception/                    # Global & Custom Exceptions
│   │   │   └── util/                         # Utility Helper Classes
│   │   └── resources/
│   │       ├── application.properties        # Main Configuration
│   │       ├── application-dev.properties    # H2 Development Profile
│   │       └── application-prod.properties   # PostgreSQL Production Profile
│   └── test/
│       └── java/com/arc/login/
│           └── LoginApplicationTests.java
└── target/                                   # Maven Build Output (Git Ignored)
```

## Getting Started

### Prerequisites
- Java 21 LTS
- Maven 3.9+

### Running Locally (Development Mode)
```bash
mvn spring-boot:run
```

By default, the application runs on **port 8080** with an embedded H2 database.
- **H2 Console**: `http://localhost:8080/h2-console`
- **Default Users**:
  - Manager: `arc_manager` / `Manager@123`
  - Operator: `arc_operator` / `Operator@123`
