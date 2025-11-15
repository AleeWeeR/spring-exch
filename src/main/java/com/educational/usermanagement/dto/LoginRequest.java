package com.educational.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO (Data Transfer Object)
 *
 * This class represents the data sent from frontend when user tries to log in.
 * DTOs are used to transfer data between layers and decouple API from entity structure.
 *
 * Educational Purpose:
 * - Demonstrates the DTO pattern
 * - Shows input validation annotations
 * - Illustrates separation of concerns (API vs Database entities)
 *
 * Why use DTOs?
 * 1. Security: Don't expose internal entity structure
 * 2. Flexibility: API can differ from database schema
 * 3. Validation: Can have different validation rules than entities
 * 4. Performance: Only transfer necessary data
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class LoginRequest {

    /**
     * Username or Email for login
     *
     * User can log in with either username or email
     * @NotBlank - Validation: Field cannot be empty or null
     */
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    /**
     * Password for login
     *
     * This is the plain text password sent from frontend
     * It will be compared with the hashed password in database
     *
     * @NotBlank - Validation: Field cannot be empty or null
     */
    @NotBlank(message = "Password is required")
    private String password;
}
