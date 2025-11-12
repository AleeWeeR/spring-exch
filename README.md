# 🧾 PF Exchange Service

## Overview
**PF Exchange** is a Spring Boot–based backend service designed for integrating and exchanging data between internal systems and Oracle database functions/packages.  
It provides a structured way to call Oracle PL/SQL functions, convert their outputs (including CLOB-based JSON responses), and expose them via RESTful endpoints.

---

## 🧱 Architecture
The project follows a **layered architecture** for clean separation of concerns:

| Layer | Description |
|-------|--------------|
| **Controller** | Handles REST API requests and responses. Delegates logic to service layer. |
| **Service** | Contains business logic. Calls repositories and transforms raw results into DTOs. |
| **Repository** | Interacts with the Oracle database using `JdbcTemplate` and `SimpleJdbcCall`. |
| **DTO (Data Transfer Objects)** | Defines request and response models (JSON-serializable). |
| **Config** | Holds global configurations such as database, logging, and app setup. |

---

## ⚙️ Technologies Used
- **Java 17+**
- **Spring Boot 3+**
- **Spring JDBC / SimpleJdbcCall**
- **Lombok**
- **Jackson (for JSON mapping)**
- **Oracle Database**
- **Log4j2** for logging

---

## 📡 Functionality
- Calls Oracle package functions using `SimpleJdbcCall` with explicitly declared parameters.
- Converts CLOB outputs from Oracle into JSON strings.
- Deserializes JSON responses into structured DTOs (e.g., `EpChargedHistDto`).
- Provides clear logs for all function calls and exceptions.

Example Oracle integration:
```java
SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
    .withCatalogName("PF_EXCHANGES_EP_CHARGE")
    .withFunctionName("GET_CHARGED_INFO")
    .declareParameters(
        new SqlOutParameter("RETURN", Types.INTEGER),
        new SqlOutParameter("p_Out_Data", Types.CLOB),
        new SqlParameter("p_In_Data", Types.CLOB)
    );
 ```
## 📁 Example Packages
```graphql
uz.fido.pfexchange
├── controller         # REST endpoints
├── service            # Business logic interfaces
│   └── impl           # Implementations
├── repository         # Database interaction layer
│   └── mip / ep       # Function-specific repositories
├── dto                # Data Transfer Objects
│   └── ep / mip       # JSON and function models
└── config             # Configurations and utilities
 ```         
## 🧩 Example Workflow

Request received → Controller calls Service

Service calls Repository → Repository executes Oracle function

Repository returns CLOB JSON → Service parses into DTO

DTO returned as REST response

## 🪵 Logging

The application uses Log4j2 for structured logging:

Info logs for successful function executions

Error logs for Oracle or JSON parsing issues

Configurable output: console or file-based logging

## 🧠 Purpose

This project serves as a reliable middleware bridge between Oracle PL/SQL business logic and modern RESTful APIs, ensuring:

Clean separation of application layers

Consistent error handling

Easily maintainable and extendable structure for new Oracle function integrations