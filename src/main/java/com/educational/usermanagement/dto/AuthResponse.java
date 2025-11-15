package com.educational.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 *
 * This class represents the response sent to frontend after successful login or registration.
 * It contains the JWT token and user information.
 *
 * Educational Purpose:
 * - Demonstrates JWT-based authentication response
 * - Shows what data should be returned after successful authentication
 * - Illustrates the separation of token and user data
 *
 * Response Structure:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "type": "Bearer",
 *   "user": { ... user data ... }
 * }
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class AuthResponse {

    /**
     * JWT access token
     *
     * This token must be included in the Authorization header for subsequent requests
     * Format: Authorization: Bearer <token>
     *
     * The token contains:
     * - User ID
     * - Username
     * - Role
     * - Expiration time
     */
    private String token;

    /**
     * Token type (always "Bearer" for JWT)
     *
     * Bearer token is a security token type defined in OAuth 2.0
     * The name comes from the idea that "the bearer of the token" has access
     */
    private String type = "Bearer";

    /**
     * User information
     *
     * Contains the user details (without password)
     * This allows the frontend to display user info without making another request
     */
    private UserResponse user;

    /**
     * Constructor with token and user (type is set to "Bearer" by default)
     *
     * @param token the JWT access token
     * @param user the user information
     */
    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.type = "Bearer";
        this.user = user;
    }
}
