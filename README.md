Authentication System (Spring Boot)

Overview

This project is a backend authentication and authorization system built using Spring Boot. It implements secure user authentication using JSON Web Tokens (JWT) along with refresh token support and role-based access control.

The system follows production-level practices such as stateless authentication, secure token handling, and centralized exception management.

⸻

Features
•	User registration and login
•	JWT-based authentication (stateless)
•	Refresh token mechanism with rotation
•	HttpOnly cookie-based refresh token storage
•	Role-based authorization using Spring Security
•	Global exception handling
•	Input validation using Jakarta Validation
•	Clean layered architecture (Controller, Service, Repository)

⸻

Tech Stack
•	Java 21
•	Spring Boot
•	Spring Security
•	Spring Data JPA
•	PostgreSQL
•	JWT (io.jsonwebtoken)
•	Maven

⸻

Architecture

The application follows a layered architecture:
•	Controller: Handles HTTP requests and responses
•	Service: Contains business logic
•	Repository: Handles database interactions
•	Security: JWT filter and authentication configuration

Authentication is handled using a custom filter that validates JWT tokens before requests reach the controller.

⸻

Authentication Flow
1.	User registers with email and password
2.	On login:
•	Access token (JWT) is returned in the response body
•	Refresh token is stored in an HttpOnly cookie
3.	Access token is used for protected API calls
4.	When access token expires:
•	Refresh endpoint issues a new access token using the refresh token
5.	On logout:
•	Refresh token is invalidated and cookie is cleared

⸻

API Endpoints

Authentication
•	POST /api/v1/auth/register
•	POST /api/v1/auth/login
•	POST /api/v1/auth/refresh
•	POST /api/v1/auth/logout

Authorization
•	POST /api/v1/auth/admin/assign-role (requires ADMIN role)

⸻

Configuration

Sensitive values such as JWT secret are externalized.

Example configuration:
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

Set environment variable:
export JWT_SECRET=your-secret-key

Database Design

Key entities:
•	User
•	Role
•	RefreshToken

Users can have multiple roles, and refresh tokens are mapped to users with expiry tracking.

⸻

Security Considerations
•	Passwords are encrypted using a password encoder
•	JWT tokens are signed and validated using a secret key
•	Refresh tokens are securely stored and rotated
•	HttpOnly cookies prevent access from client-side scripts
•	Role-based access control enforced using annotations

⸻

Running the Application
1.	Clone the repository
2.	Configure database in application.properties
3.	Set environment variables
4.	Run the application

    mvn spring-boot:run


Future Improvements
•	Token revocation or blacklist strategy
•	Role hierarchy and permission-based access control
•	Rate limiting and request throttling
•	Redis integration for caching
•	Audit logging


Author
Abhilash 