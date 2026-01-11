package com.educational.usermanagement.dto;

import com.educational.usermanagement.entity.Role;
import com.educational.usermanagement.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Response DTO
 *
 * This class represents user data sent to the frontend.
 * It's similar to User entity but EXCLUDES the password field for security.
 *
 * Educational Purpose:
 * - Demonstrates security best practice: NEVER expose passwords
 * - Shows how to convert Entity to DTO
 * - Illustrates what data should be visible to frontend
 *
 * Security Note:
 * NEVER include password in response, even if it's hashed!
 * This prevents password hashes from being exposed to clients.
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class UserResponse {

    /**
     * User ID
     */
    private Long id;

    /**
     * Username
     */
    private String username;

    /**
     * Email address
     */
    private String email;

    /**
     * First name
     */
    private String firstName;

    /**
     * Last name
     */
    private String lastName;

    /**
     * Phone number
     */
    private String phoneNumber;

    /**
     * User role (ADMIN or USER)
     */
    private Role role;

    /**
     * Active status
     */
    private Boolean isActive;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Static factory method to convert User entity to UserResponse DTO
     *
     * This is a common pattern for converting entities to DTOs
     * It keeps conversion logic in one place
     *
     * @param user the User entity to convert
     * @return UserResponse DTO with all user data except password
     */
    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    /**
     * Get full name helper method
     *
     * @return concatenated first and last name, or username if names not available
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
}
