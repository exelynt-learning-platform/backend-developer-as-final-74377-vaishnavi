# Resource Booking System - Backend API

A robust, production-ready Spring Boot REST API for managing resources, user reservations, authentication, and Role-Based Access Control (RBAC).

---

## 🛠️ Tech Stack
* **Java 17+**
* **Spring Boot**
* **Spring Security & JWT** (JSON Web Tokens for stateless authentication)
* **Spring Data JPA / Hibernate** (Database persistence and specifications for dynamic filtering)
* **MySQL** (Relational database)
* **SpringDoc OpenAPI / Swagger** (API documentation)

---

## 🔐 Environment Variables
The application supports the following environment variables. Fallback defaults are provided in local development, but they should be explicitly configured for production environments:

| Variable Name | Description | Default (if omitted) |
| :--- | :--- | :--- |
| `DB_PASSWORD` | MySQL database password | `root` |
| `JWT_SECRET` | Base64-encoded secret key for signing JWT tokens | Built-in test secret |

---

## 🚀 Getting Started & Local Setup

### Prerequisites
* Java Development Kit (JDK 17 or higher)
* Maven (or use the included `./mvnw wrapper`)
* MySQL Server running locally on port `3306`

### 1. Configure the Database
Create a MySQL database named `resource_booking_db`:
```sql
CREATE DATABASE resource_booking_db;