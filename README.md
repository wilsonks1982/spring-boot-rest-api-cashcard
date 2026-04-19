# CashCard API - A Test-Driven Development Journey with Spring Boot

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.14.4-02303A?style=flat-square&logo=gradle)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

A production-ready RESTful API built with **Spring Boot** using **Test-Driven Development (TDD)** principles. This project demonstrates best practices in REST API design, multi-tenant architecture, and comprehensive testing strategies.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Building & Running](#building--running)
- [API Documentation](#api-documentation)
- [Testing Strategy](#testing-strategy)
- [Authentication & Authorization](#authentication--authorization)
- [Code Quality & Metrics](#code-quality--metrics)
- [Author](#author)
- [Acknowledgments](#acknowledgments)

---

## Features

### Core Functionality
- ✅ **CRUD Operations** - Create, Read, Update, Delete CashCards
- ✅ **Pagination & Sorting** - Retrieve cards with configurable pagination
- ✅ **Card Search** - Search cards by card number
- ✅ **Input Validation** - Comprehensive validation with custom error messages
- ✅ **HTTP Status Codes** - Proper REST status codes (201, 400, 401, 403, 404, etc.)

### Security & Multi-Tenancy
- 🔐 **HTTP Basic Authentication** - Secure API with username/password authentication
- 👥 **Multi-Tenant Architecture** - Complete data isolation between users
- 🛡️ **Authorization Checks** - Users can only access their own resources
- 🔒 **Server-Generated Fields** - ID and owner auto-managed by server

### Data Management
- 💾 **In-Memory H2 Database** - Perfect for development and testing
- 💰 **BigDecimal Precision** - Accurate financial calculations with 2 decimal places
- 🔄 **Automatic Rounding** - HALF_UP rounding strategy for all monetary values
- 🗂️ **Spring Data JDBC** - Lightweight, simple data access layer

### Testing & Quality
- 🧪 **Comprehensive Test Suite** - 100+ unit and integration tests
- 📊 **Code Coverage Reporting** - JaCoCo integration with coverage verification
- 📝 **Test Documentation** - Detailed test specifications and examples
- ✔️ **Continuous Validation** - All tests pass with every build


## Architecture

### Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Web Framework** | Spring Boot 3.5.13 | REST API and MVC support |
| **Data Access** | Spring Data JDBC | Database operations |
| **Security** | Spring Security 6.5.9 | Authentication & Authorization |
| **Validation** | Jakarta Bean Validation | Input constraint validation |
| **Database** | H2 (In-Memory) | Development/Testing database |
| **Build Tool** | Gradle 8.14.4 | Project build and dependency management |
| **Testing** | JUnit 5, AssertJ | Unit and integration tests |
| **Code Quality** | JaCoCo | Code coverage analysis |
| **Utilities** | Lombok | Reduced boilerplate code |

### Architectural Diagram
```plaintext
+-------------------+       +-------------------+       +-------------------+
|   Client (HTTP)   | <---> |  Spring Boot API  | <---> |   H2 Database    |
+-------------------+       +-------------------+       +-------------------+
        |                       |                       |
        |                       |                       |
        v                       v                       v
  Authentication         Business Logic           Data Persistence
  & Authorization       & Validation             & Repository Layer
```

## Prerequisites

### System Requirements
- **Java**: JDK 17 or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Gradle**: 8.14.4 or higher ([Download](https://gradle.org/install/))
- **Git**: 2.30+ ([Download](https://git-scm.com/downloads))
- **IDE**: IntelliJ IDEA or VS Code (recommended)

### Optional Tools
- **Postman**: For API testing ([Download](https://www.postman.com/downloads/))
- **cURL**: Command-line API testing (`brew install curl` on macOS)
- **Docker**: For containerization ([Download](https://www.docker.com/products/docker-desktop))

## Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/wilsonks1982/cashcard2.git
cd cashcard2
```

### 2. Verify Java Installation

```bash
java -version
```

### 3. Verify Gradle Installation

```bash
./gradlew -v
```

### 4. Build the Project

```bash
./gradlew clean build
```
### 5. Run the Tests

#### Run All Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests CashCardTest
./gradlew test --tests CashCardJsonTest
./gradlew test --tests CashCardControllerTest
./gradlew test --tests ApplicationTests

# Run nested test class
./gradlew test --tests 'CashCardControllerTest$GetSingleCashCardTests'
./gradlew test --tests 'CashCardControllerTest$GetAllCashCardsTests'
./gradlew test --tests 'CashCardControllerTest$CreateCashCardTests'
./gradlew test --tests 'CashCardControllerTest$UpdateCashCardTests'
./gradlew test --tests 'CashCardControllerTest$DeleteCashCardTests'

# Run with coverage report
./gradlew test jacocoTestReport

```

## Building & Running

### Build the Application

```bash
# Build JAR file
./gradlew clean build

# Build without tests
./gradlew clean build -x test

```
Output: build/libs/cashcard2-0.0.1-SNAPSHOT.jar

### Run the Application

```bash
# Run with Gradle
./gradlew bootRun

# Run the JAR file
java -jar build/libs/cashcard2-0.0.1-SNAPSHOT.jar
```

### Access the Application
- API Base URL: `http://localhost:8080/cashcard`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`, Password: ``)
- API Testing: Use Postman or cURL to interact with the endpoints
- Authentication: Use HTTP Basic Auth with username/password (e.g., `user1`/`user@1`)
- Example cURL Command:
```bash
curl -u user1:user@1 -X POST http://localhost:8080/cashcard \
     -H "Content-Type: application/json" \
     -d '{"cardNumber":"1234567890123456","balance":100.00}'
     
curl -u user1:user@1 -X GET http://localhost:8080/cashcard
curl -u user1:user@1 -X GET http://localhost:8080/cashcard/1
     
curl -u user1:user@1 -X PUT http://localhost:8080/cashcard/1 \
     -H "Content-Type: application/json" \
     -d '{"balance":150.00}'
curl -u user1:user@1 -X DELETE http://localhost:8080/cashcard/1
          
```


## API Documentation

### Quick API Reference
| Endpoint                | Method | Description                          | Authentication Required |
|-------------------------|--------|--------------------------------------|------------------------|
| `/cashcard`             | POST   | Create a new CashCard                | Yes                    |
| `/cashcard`             | GET    | Retrieve all CashCards for the user   | Yes                    |
| `/cashcard/{id}`        | GET    | Retrieve a single CashCard by ID      | Yes                    |
| `/cashcard/{id}`        | PUT    | Update an existing CashCard           | Yes                    |
| `/cashcard/{id}`        | DELETE | Delete a CashCard by ID               | Yes                    |

### Detailed API Documentation
For comprehensive API documentation, including request/response examples, error codes, and authentication details, 
please refer to the [API Documentation](docs/API_CONTRACT.md) file.

## Testing Strategy

### Three-Tier Testing Approach
1. **Unit Tests(CashCardTest)**: 
   - Focus: CashCard record validation and behavior
   - Scope: Data validation, rounding, immutability, and edge cases
   - Tools: JUnit 5, AssertJ
   - Coverage: 100% for CashCard class
   - Tests: 20+ tests covering all validation rules and scenarios
   - Location: `src/test/java/com/wilsonks1982/cashcard2/data_transfer/CashCardTest.java`
2. **Json Serialization Tests(CashCardJsonTest)**: 
   - Focus: JSON serialization/deserialization of CashCard
   - Scope: Field mapping, date formatting, and error handling
   - Tools: JUnit 5, AssertJ, Jackson
   - Coverage: 100% for JSON processing of CashCard
   - Tests: 10+ tests covering all serialization scenarios
   - Location: `src/test/java/com/wilsonks1982/cashcard2/data_transfer/CashCardJsonTest.java`
3. **Integration Tests(CashCardControllerTest)**:
   - Focus: End-to-end testing of CashCardController
   - Scope: API endpoints, authentication, multi-tenancy, and error handling
   - Tools: JUnit 5, AssertJ, Spring Boot Test
   - Coverage: 100% for all controller endpoints and scenarios
   - Tests: 70+ tests covering all CRUD operations, security, and edge cases
   - Location: `src/test/java/com/wilsonks1982/cashcard2/CashCardControllerTest.java`

### Code Coverage
- **Tool**: JaCoCo

```bash
# Generate coverage report
./gradlew test jacocoTestReport

# View HTML report
open build/reports/jacoco/test/html/index.html
```

### Test Documentation
- Unit Tests: [UNIT_TEST.md](docs/UNIT_TEST.md)
- Integration Tests: [INTEGRATION_TEST.md](docs/INTEGRATION_TEST.md)


## Authentication & Authorization

### Default Test Credentials
| Username | Password  | Role  |
|----------|-----------|-------|
| user1    | user@1    | USER  |
| user2    | user@22   | USER  |
| admin    | admin@123 | ADMIN |

### Authentication Method
- **HTTP Basic Authentication**: Clients must provide a valid username and password with each request to access protected endpoints.
- Example cURL Command:
```bash
curl -u user1:user@1 -X GET http://localhost:8080/cashcard
```

## Code Quality & Metrics

### Generate Coverage Report
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```
## Author
### Wilson K. Sam. (wilsonks1982@gmail.com)

## Acknowledgments
- Spring Boot team for the excellent framework
- JUnit and AssertJ communities for their powerful testing libraries
- The TDD community for best practices and patterns