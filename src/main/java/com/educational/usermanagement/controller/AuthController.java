package com.educational.usermanagement.controller;

import com.educational.usermanagement.dto.ApiResponse;
import com.educational.usermanagement.dto.AuthResponse;
import com.educational.usermanagement.dto.LoginRequest;
import com.educational.usermanagement.dto.RegisterRequest;
import com.educational.usermanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * This REST controller handles authentication-related HTTP requests.
 * It provides endpoints for user login and registration.
 *
 * Educational Purpose:
 * - Demonstrates authentication endpoints in REST API
 * - Shows how login and registration differ from other endpoints
 * - Illustrates public endpoint configuration (no authentication required)
 * - Explains JWT token generation and response
 *
 * Key Differences from UserController:
 * - These endpoints are PUBLIC (no authentication required)
 * - Returns JWT token for successful authentication
 * - Handles new user registration
 *
 * Security Notes:
 * - Login endpoint: Validates credentials, returns token if valid
 * - Register endpoint: Creates new user, automatically logs them in
 * - Both endpoints return JWT token that client must store
 * - Client must include token in subsequent requests
 *
 * Base URL: /api/auth
 *
 * @author Educational Project
 * @version 1.0.0
 */
@RestController // Marks this as a REST controller - returns JSON
@RequestMapping("/api/auth") // Base URL for all endpoints in this controller
@Tag(name = "Authentication", description = "APIs for user authentication and registration") // Swagger documentation
public class AuthController {

    /**
     * Authentication Service for login/register logic
     */
    @Autowired
    private AuthService authService;

    /**
     * User Login
     *
     * Endpoint: POST /api/auth/login
     * Access: PUBLIC (no authentication required)
     *
     * Authenticates a user and returns a JWT token.
     * User can log in with either username or email.
     *
     * Request Body:
     * {
     *   "usernameOrEmail": "john_doe",  // or "john@example.com"
     *   "password": "password123"
     * }
     *
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "type": "Bearer",
     *     "user": {
     *       "id": 1,
     *       "username": "john_doe",
     *       "email": "john@example.com",
     *       "role": "USER",
     *       ...
     *     }
     *   }
     * }
     *
     * Error Response (401 Unauthorized):
     * {
     *   "success": false,
     *   "message": "Invalid username or password",
     *   "data": null
     * }
     *
     * How to use the token:
     * 1. Extract token from response
     * 2. Store token in client (localStorage, sessionStorage, or memory)
     * 3. Include token in Authorization header for subsequent requests:
     *    Authorization: Bearer <token>
     *
     * @Valid annotation triggers validation:
     * - usernameOrEmail must not be blank
     * - password must not be blank
     *
     * @param loginRequest login credentials from request body
     * @return ResponseEntity with JWT token and user data
     */
    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT token"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Call service to authenticate user
        // Service will:
        // 1. Verify credentials using AuthenticationManager
        // 2. Generate JWT token
        // 3. Return token and user data
        AuthResponse authResponse = authService.login(loginRequest);

        // Wrap in ApiResponse format
        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Login successful",
                authResponse
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * User Registration
     *
     * Endpoint: POST /api/auth/register
     * Access: PUBLIC (no authentication required)
     *
     * Registers a new user and automatically logs them in.
     * Returns a JWT token so user doesn't need to log in separately.
     *
     * Request Body:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "password123",
     *   "firstName": "John",          // Optional
     *   "lastName": "Doe",             // Optional
     *   "phoneNumber": "+1234567890"   // Optional
     * }
     *
     * Validation Rules:
     * - username: 3-50 characters, must be unique
     * - email: valid email format, must be unique
     * - password: minimum 6 characters
     * - firstName, lastName: optional, max 50 characters
     * - phoneNumber: optional, max 20 characters
     *
     * Success Response (201 Created):
     * {
     *   "success": true,
     *   "message": "Registration successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "type": "Bearer",
     *     "user": {
     *       "id": 2,
     *       "username": "john_doe",
     *       "email": "john@example.com",
     *       "role": "USER",
     *       "isActive": true,
     *       ...
     *     }
     *   }
     * }
     *
     * Error Response (409 Conflict):
     * {
     *   "success": false,
     *   "message": "Username already exists: 'john_doe'",
     *   "data": null
     * }
     *
     * Error Response (400 Bad Request):
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": {
     *     "username": "Username must be between 3 and 50 characters",
     *     "email": "Email must be valid"
     *   }
     * }
     *
     * Security Notes:
     * - Password is automatically hashed before storing (BCrypt)
     * - New users always get USER role (not ADMIN)
     * - New users are active by default
     * - Username and email uniqueness is enforced
     *
     * @Valid annotation triggers validation of all fields
     *
     * @param registerRequest registration data from request body
     * @return ResponseEntity with JWT token and user data
     */
    @PostMapping("/register")
    @Operation(
            summary = "User registration",
            description = "Register a new user and return JWT token"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        // Call service to register user
        // Service will:
        // 1. Validate username and email are unique
        // 2. Hash password
        // 3. Create user with USER role
        // 4. Save user to database
        // 5. Generate JWT token
        // 6. Return token and user data
        AuthResponse authResponse = authService.register(registerRequest);

        // Wrap in ApiResponse format
        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Registration successful",
                authResponse
        );

        // Return 201 CREATED with response
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Health Check Endpoint
     *
     * Endpoint: GET /api/auth/health
     * Access: PUBLIC
     *
     * Simple endpoint to check if the authentication service is running.
     * Useful for monitoring and debugging.
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Authentication service is running",
     *   "data": null
     * }
     *
     * @return ResponseEntity with health status
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if authentication service is running"
    )
    public ResponseEntity<ApiResponse<Void>> health() {
        ApiResponse<Void> response = ApiResponse.success("Authentication service is running");
        return ResponseEntity.ok(response);
    }
}
