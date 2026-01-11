package com.educational.usermanagement.service;

import com.educational.usermanagement.dto.UserRequest;
import com.educational.usermanagement.dto.UserResponse;
import com.educational.usermanagement.entity.Role;
import com.educational.usermanagement.entity.User;
import com.educational.usermanagement.exception.DuplicateResourceException;
import com.educational.usermanagement.exception.ResourceNotFoundException;
import com.educational.usermanagement.exception.UnauthorizedException;
import com.educational.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service
 *
 * This service contains business logic for user management operations.
 * It handles all CRUD operations and enforces business rules.
 *
 * Educational Purpose:
 * - Demonstrates the Service layer pattern
 * - Shows business logic separation from controllers
 * - Illustrates transaction management
 * - Explains security context usage
 * - Shows DTO to Entity conversion
 *
 * Service Layer Responsibilities:
 * 1. Business Logic: Enforce rules (e.g., username must be unique)
 * 2. Data Validation: Validate data before saving
 * 3. Authorization: Check if user has permission for operation
 * 4. Transaction Management: Ensure data consistency
 * 5. DTO Conversion: Convert between DTOs and Entities
 *
 * @Transactional:
 * - Ensures database operations are atomic (all or nothing)
 * - Auto-commits if method completes successfully
 * - Auto-rollbacks if exception is thrown
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Service // Marks this as a Spring service component
@Transactional // All methods run in transactions (can be overridden per method)
public class UserService {

    /**
     * User Repository for database access
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Password Encoder for hashing passwords
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get All Users
     *
     * Retrieves all users from database and converts them to DTOs.
     * Accessible by ADMIN only (enforced in controller).
     *
     * @return List of UserResponse DTOs
     */
    @Transactional(readOnly = true) // Optimization for read-only operations
    public List<UserResponse> getAllUsers() {
        // Fetch all users from database
        List<User> users = userRepository.findAll();

        // Convert User entities to UserResponse DTOs
        // stream() - Converts list to stream for functional operations
        // map() - Transforms each User to UserResponse
        // collect() - Collects results back to list
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get User by ID
     *
     * Retrieves a single user by their ID.
     * Users can get their own data, admins can get any user's data.
     *
     * @param id the user ID
     * @return UserResponse DTO
     * @throws ResourceNotFoundException if user not found
     * @throws UnauthorizedException if user tries to access another user's data
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        // Find user by ID or throw exception
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check if current user has permission to view this user
        checkUserAccess(user);

        // Convert to DTO and return
        return UserResponse.fromEntity(user);
    }

    /**
     * Search Users
     *
     * Searches users by username, email, first name, or last name.
     * Search is case-insensitive and partial match.
     *
     * @param searchTerm the search term
     * @return List of matching users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String searchTerm) {
        // Call repository search method
        List<User> users = userRepository.searchUsers(searchTerm);

        // Convert to DTOs
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get Users by Role
     *
     * Retrieves all users with a specific role.
     * Admin-only operation.
     *
     * @param role the role to filter by
     * @return List of users with specified role
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);

        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create User
     *
     * Creates a new user account.
     * Admin-only operation - allows setting role and active status.
     *
     * Validations:
     * - Username must be unique
     * - Email must be unique
     * - Password must meet minimum requirements
     *
     * @param request UserRequest DTO with user data
     * @return UserResponse DTO of created user
     * @throws DuplicateResourceException if username or email already exists
     */
    public UserResponse createUser(UserRequest request) {
        // Validate username is unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username", request.getUsername());
        }

        // Validate email is unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email", request.getEmail());
        }

        // Create new User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Hash password before saving (NEVER store plain text passwords!)
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        // Set role (default to USER if not specified)
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        // Set active status (default to true if not specified)
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Save to database
        User savedUser = userRepository.save(user);

        // Convert to DTO and return
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Update User
     *
     * Updates an existing user.
     * Users can update themselves, admins can update anyone.
     *
     * Business Rules:
     * - Regular users cannot change their role
     * - Regular users cannot change their active status
     * - Username/email must remain unique
     * - Password is optional (only updated if provided)
     *
     * @param id the user ID to update
     * @param request UserRequest DTO with updated data
     * @return UserResponse DTO of updated user
     * @throws ResourceNotFoundException if user not found
     * @throws DuplicateResourceException if new username/email already exists
     * @throws UnauthorizedException if user tries to update another user
     */
    public UserResponse updateUser(Long id, UserRequest request) {
        // Find existing user
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check if current user has permission to update this user
        boolean isAdmin = isCurrentUserAdmin();
        if (!isAdmin) {
            checkUserAccess(user);
        }

        // Update username if provided and different
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            // Check if new username is unique
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateResourceException("Username", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if new email is unique
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Update password if provided (hash it first!)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Update other fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Only admin can change role and active status
        if (isAdmin) {
            if (request.getRole() != null) {
                user.setRole(request.getRole());
            }
            if (request.getIsActive() != null) {
                user.setIsActive(request.getIsActive());
            }
        }

        // Save updated user
        User updatedUser = userRepository.save(user);

        // Convert to DTO and return
        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Delete User
     *
     * Deletes a user from the system.
     * Admin-only operation.
     *
     * @param id the user ID to delete
     * @throws ResourceNotFoundException if user not found
     */
    public void deleteUser(Long id) {
        // Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Delete the user
        userRepository.delete(user);
    }

    /**
     * Get Current User
     *
     * Returns the currently authenticated user's information.
     * Useful for "My Profile" functionality.
     *
     * @return UserResponse DTO of current user
     * @throws ResourceNotFoundException if current user not found in database
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        // Get current username from Security Context
        String username = getCurrentUsername();

        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Convert to DTO and return
        return UserResponse.fromEntity(user);
    }

    /**
     * Get User Statistics
     *
     * Returns count of users by role and active status.
     * Admin-only operation, useful for dashboard.
     *
     * @return Map with statistics
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getUserStatistics() {
        return java.util.Map.of(
                "totalUsers", userRepository.count(),
                "adminCount", userRepository.countByRole(Role.ADMIN),
                "userCount", userRepository.countByRole(Role.USER),
                "activeUsers", userRepository.countByIsActive(true),
                "inactiveUsers", userRepository.countByIsActive(false)
        );
    }

    // ========== HELPER METHODS ==========

    /**
     * Get Current Username from Security Context
     *
     * Extracts the username of the currently authenticated user.
     *
     * @return username of current user
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Check if Current User is Admin
     *
     * @return true if current user has ADMIN role, false otherwise
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /**
     * Check User Access
     *
     * Verifies if current user can access the specified user's data.
     * Users can access their own data, admins can access anyone's data.
     *
     * @param user the user being accessed
     * @throws UnauthorizedException if access is not allowed
     */
    private void checkUserAccess(User user) {
        // Get current username
        String currentUsername = getCurrentUsername();

        // Check if current user is admin
        boolean isAdmin = isCurrentUserAdmin();

        // Allow access if user is admin or accessing their own data
        if (!isAdmin && !user.getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("You can only access your own data");
        }
    }
}
