# Resource Booking System

A secure RESTful API for resource booking with JWT authentication and role-based access control.

## Technologies

- Java 17
- Spring Boot 4
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- MySQL
- Maven
- Swagger / OpenAPI
- Lombok

## Features

- JWT-based authentication
- BCrypt password encryption
- ADMIN and USER roles
- Role-based access control
- Resource CRUD operations
- Reservation management
- Reservation ownership
- Reservation status management
- Reservation filtering
- Pagination and sorting
- Input validation
- Global exception handling
- Swagger/OpenAPI documentation

## Project Structure

```text
src/main/java/com/booking/resourcebooking

├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── security
└── service
```

## Database Setup

Create a MySQL database:

```sql
CREATE DATABASE resource_booking_db;
```

Update the database configuration in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

## Run the Application

Clone the repository and navigate to the project directory.

Run:

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## Authentication

Login using:

```http
POST /auth/login
```

Request:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

The response contains a JWT token.

Use the token in protected APIs:

```text
Authorization: Bearer <JWT_TOKEN>
```

## Roles

### ADMIN

ADMIN users can:

- View resources
- Create resources
- Update resources
- Delete resources
- View all reservations
- View individual reservations
- Create reservations
- Update reservations
- Delete reservations
- Update reservation status

### USER

USER users can:

- View resources
- View individual resources
- Create reservations
- View their own reservations
- View their own reservation by ID

Users cannot access ADMIN-only operations.

## API Endpoints

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/login` | Public |

### Resources

| Method | Endpoint | Access |
|---|---|---|
| GET | `/resources` | USER / ADMIN |
| GET | `/resources/{id}` | USER / ADMIN |
| POST | `/resources` | ADMIN |
| PUT | `/resources/{id}` | ADMIN |
| DELETE | `/resources/{id}` | ADMIN |

### Reservations

| Method | Endpoint | Access |
|---|---|---|
| POST | `/reservations` | USER / ADMIN |
| GET | `/reservations/my` | USER |
| GET | `/reservations/{id}` | USER / ADMIN |
| GET | `/reservations` | ADMIN |
| PUT | `/reservations/{id}` | ADMIN |
| DELETE | `/reservations/{id}` | ADMIN |

## Reservation Filtering

Reservations can be filtered using:

```text
status
minPrice
maxPrice
```

Example:

```http
GET /reservations?status=CONFIRMED&minPrice=500&maxPrice=2000
```

USER's own reservations can also be filtered:

```http
GET /reservations/my?status=PENDING&minPrice=500&maxPrice=2000
```

## Pagination and Sorting

Supported parameters:

```text
page
size
sortBy
direction
```

Example:

```http
GET /reservations?page=0&size=5&sortBy=price&direction=desc
```

## Reservation Status

Supported statuses:

```text
PENDING
CONFIRMED
CANCELLED
```

New reservations are created with:

```text
PENDING
```

ADMIN users can update the reservation status.

## Validation

The API validates:

- Required fields
- Positive resource prices
- Valid reservation status
- Future reservation times
- End time must be after start time
- Resource existence
- Resource availability

## Error Handling

The application handles:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

Validation and application errors are returned with meaningful messages.

## Swagger Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/api-docs
```

## Test Users

### ADMIN

```text
Username: admin
Password: admin123
Role: ADMIN
```

### USER

```text
Username: user
Password: user123
Role: USER
```

These users are automatically seeded when the application starts.

## Security

The application uses:

- BCrypt for password hashing
- JWT for authentication
- Stateless session management
- Role-based authorization
- JWT identity for determining reservation ownership
- Protected REST endpoints

The application does not accept the USER identity from the reservation request body. The authenticated user is determined from the JWT.

## Author

Vaishnavi Bakal

Java Backend Developer# backend-developer-as-final-74377-vaishnavi
Final Project Assignment - This repository contains the complete final project code and documentation.
## Assignment Submission

This repository contains the completed solution for the Exelynt Backend Developer Assignment.

The implementation includes JWT authentication, role-based authorization, resource and reservation management, validation, filtering, pagination, sorting, Swagger/OpenAPI documentation, MySQL persistence, and unit tests.