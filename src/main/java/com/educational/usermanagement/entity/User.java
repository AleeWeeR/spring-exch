package com.educational.usermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User Entity Class
 *
 * This class represents a User in our database.
 * It uses JPA (Java Persistence API) annotations to map Java objects to database tables.
 *
 * Educational Purpose:
 * - Demonstrates JPA entity mapping
 * - Shows how to use Lombok for reducing boilerplate code
 * - Illustrates validation annotations
 * - Demonstrates audit fields (createdAt, updatedAt)
 *
 * Database Table: users
 * Primary Key: id (auto-generated sequence)
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Entity // Marks this class as a JPA entity (database table)
@Table(name = "users") // Specifies the table name in the database
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok: Generates a no-argument constructor (required by JPA)
@AllArgsConstructor // Lombok: Generates a constructor with all arguments
public class User {

    /**
     * Primary Key - Unique identifier for each user
     *
     * @Id - Marks this field as the primary key
     * @GeneratedValue - Specifies how the primary key should be generated
     * @SequenceGenerator - Defines a sequence generator for Oracle database
     *
     * Oracle uses sequences for auto-incrementing IDs
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "USER_SEQ", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Username - Unique username for login
     *
     * @NotBlank - Validation: Cannot be null or empty
     * @Size - Validation: Must be between 3 and 50 characters
     * @Column(unique = true) - Database constraint: Must be unique
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Email - User's email address
     *
     * @Email - Validation: Must be a valid email format
     * @NotBlank - Validation: Cannot be null or empty
     * @Column(unique = true) - Database constraint: Must be unique
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * Password - Encrypted password (stored as BCrypt hash)
     *
     * IMPORTANT: Never store passwords in plain text!
     * This field stores the BCrypt hash of the password.
     *
     * @NotBlank - Validation: Cannot be null or empty
     */
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * First Name - User's first name
     *
     * @Size - Validation: Maximum 50 characters
     */
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(name = "first_name", length = 50)
    private String firstName;

    /**
     * Last Name - User's last name
     *
     * @Size - Validation: Maximum 50 characters
     */
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", length = 50)
    private String lastName;

    /**
     * Phone Number - User's contact phone number
     *
     * @Size - Validation: Maximum 20 characters
     */
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Role - User's role in the system (ADMIN or USER)
     *
     * @Enumerated - Tells JPA to store enum as string in database
     * EnumType.STRING - Stores the enum name (recommended over ORDINAL)
     *
     * Default value is USER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

    /**
     * Active Status - Whether the user account is active
     *
     * Inactive users cannot log in to the system
     * Default value is true (active)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Created At - Timestamp when the user was created
     *
     * @CreationTimestamp - Automatically sets this field to current timestamp on insert
     * updatable = false - This field cannot be updated after creation
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated At - Timestamp when the user was last updated
     *
     * @UpdateTimestamp - Automatically updates this field to current timestamp on update
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to get full name
     *
     * @return concatenated first name and last name
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

    /**
     * Check if user is an administrator
     *
     * @return true if user has ADMIN role, false otherwise
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}
