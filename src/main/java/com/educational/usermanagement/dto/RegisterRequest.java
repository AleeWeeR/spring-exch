package com.educational.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Register Request DTO
 *
 * This class represents the data sent from frontend when a new user registers.
 * It contains all necessary fields for creating a new user account.
 *
 * Educational Purpose:
 * - Demonstrates input validation for user registration
 * - Shows how to use multiple validation annotations
 * - Illustrates best practices for password handling
 *
 * Validation Rules:
 * - Username: 3-50 characters, required, unique (checked in service layer)
 * - Email: Valid email format, required, unique (checked in service layer)
 * - Password: Minimum 6 characters, required
 * - First/Last Name: Optional, max 50 characters
 * - Phone: Optional, max 20 characters
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class RegisterRequest {

    /**
     * Desired username for the new account
     *
     * @NotBlank - Cannot be null or empty
     * @Size - Must be between 3 and 50 characters
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Email address for the new account
     *
     * @NotBlank - Cannot be null or empty
     * @Email - Must be a valid email format
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Password for the new account
     *
     * This will be hashed before storing in database
     * NEVER store passwords in plain text!
     *
     * @NotBlank - Cannot be null or empty
     * @Size - Minimum 6 characters (for security)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * First name (optional)
     *
     * @Size - Maximum 50 characters
     */
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    /**
     * Last name (optional)
     *
     * @Size - Maximum 50 characters
     */
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    /**
     * Phone number (optional)
     *
     * @Size - Maximum 20 characters
     */
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;
}
