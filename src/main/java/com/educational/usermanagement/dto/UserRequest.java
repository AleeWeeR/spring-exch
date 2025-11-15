package com.educational.usermanagement.dto;

import com.educational.usermanagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Request DTO
 *
 * This class represents the data sent when creating or updating a user.
 * It's similar to RegisterRequest but includes additional fields like role and isActive.
 *
 * Educational Purpose:
 * - Demonstrates DTO for update operations
 * - Shows how admin can set role and active status
 * - Illustrates optional password field for updates
 *
 * Usage:
 * - Create User: Admin can create user with specific role
 * - Update User: Admin can update any user, users can update themselves
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class UserRequest {

    /**
     * Username for the account
     *
     * @Size - Must be between 3 and 50 characters (if provided)
     * Can be null when updating (means don't change username)
     */
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Email address
     *
     * @Email - Must be a valid email format (if provided)
     * Can be null when updating (means don't change email)
     */
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Password (optional for updates)
     *
     * - For creating user: Required (validated in service layer)
     * - For updating user: Optional (if null, password won't change)
     *
     * @Size - Minimum 6 characters (if provided)
     */
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

    /**
     * User role (ADMIN or USER)
     *
     * Only admins can set this field
     * Regular users cannot change their own role
     */
    private Role role;

    /**
     * Active status
     *
     * Only admins can activate/deactivate users
     * Deactivated users cannot log in
     */
    private Boolean isActive;
}
