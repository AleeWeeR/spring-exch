package com.educational.usermanagement.controller;

import com.educational.usermanagement.dto.ApiResponse;
import com.educational.usermanagement.dto.UserRequest;
import com.educational.usermanagement.dto.UserResponse;
import com.educational.usermanagement.entity.Role;
import com.educational.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User Controller
 *
 * This REST controller handles HTTP requests for user management operations.
 * It provides endpoints for CRUD (Create, Read, Update, Delete) operations on users.
 *
 * Educational Purpose:
 * - Demonstrates REST API controller design
 * - Shows HTTP method mapping (GET, POST, PUT, DELETE)
 * - Illustrates request/response handling
 * - Explains role-based authorization with @PreAuthorize
 * - Shows validation with @Valid annotation
 * - Demonstrates Swagger/OpenAPI documentation
 *
 * REST API Principles:
 * - Stateless: Each request contains all necessary information
 * - Resource-based: URLs represent resources (users)
 * - HTTP Methods: GET (read), POST (create), PUT (update), DELETE (delete)
 * - JSON Format: Data exchanged in JSON format
 * - Status Codes: Appropriate HTTP status codes (200, 201, 404, etc.)
 *
 * Base URL: /api/users
 *
 * @author Educational Project
 * @version 1.0.0
 */
@RestController // Combines @Controller and @ResponseBody - returns JSON instead of views
@RequestMapping("/api/users") // Base URL for all endpoints in this controller
@Tag(name = "User Management", description = "APIs for managing users") // Swagger documentation
@SecurityRequirement(name = "bearer-token") // Swagger: indicates authentication required
public class UserController {

    /**
     * User Service for business logic
     */
    @Autowired
    private UserService userService;

    /**
     * Get All Users
     *
     * Endpoint: GET /api/users
     * Access: ADMIN only
     *
     * Returns a list of all users in the system.
     * Only administrators can view all users.
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Users retrieved successfully",
     *   "data": [{ user1 }, { user2 }, ...]
     * }
     *
     * @return ResponseEntity with list of users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only users with ADMIN role can access
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        // Call service to get all users
        List<UserResponse> users = userService.getAllUsers();

        // Create success response
        ApiResponse<List<UserResponse>> response = ApiResponse.success(
                "Users retrieved successfully",
                users
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * Get User by ID
     *
     * Endpoint: GET /api/users/{id}
     * Access: Authenticated users (can view own data), Admins (can view any data)
     *
     * Returns a specific user by their ID.
     * Regular users can only view their own data.
     * Admins can view any user's data.
     *
     * @param id the user ID from URL path
     * @return ResponseEntity with user data
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        // Call service to get user
        // Service will check if current user has permission
        UserResponse user = userService.getUserById(id);

        // Create success response
        ApiResponse<UserResponse> response = ApiResponse.success(
                "User retrieved successfully",
                user
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * Get Current User
     *
     * Endpoint: GET /api/users/me
     * Access: Authenticated users
     *
     * Returns the currently logged-in user's information.
     * Useful for "My Profile" page.
     *
     * @return ResponseEntity with current user data
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        // Call service to get current user
        UserResponse user = userService.getCurrentUser();

        // Create success response
        ApiResponse<UserResponse> response = ApiResponse.success(
                "Current user retrieved successfully",
                user
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * Search Users
     *
     * Endpoint: GET /api/users/search?q=searchTerm
     * Access: Authenticated users
     *
     * Searches users by username, email, first name, or last name.
     * Search is case-insensitive and partial match.
     *
     * Example: GET /api/users/search?q=john
     *
     * @param searchTerm the search query parameter
     * @return ResponseEntity with list of matching users
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by username, email, or name")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam("q") String searchTerm) {

        // Call service to search users
        List<UserResponse> users = userService.searchUsers(searchTerm);

        // Create success response
        ApiResponse<List<UserResponse>> response = ApiResponse.success(
                "Search completed successfully",
                users
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * Get Users by Role
     *
     * Endpoint: GET /api/users/role/{role}
     * Access: ADMIN only
     *
     * Returns all users with a specific role (ADMIN or USER).
     *
     * Example: GET /api/users/role/ADMIN
     *
     * @param role the role from URL path
     * @return ResponseEntity with list of users with specified role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can filter by role
    @Operation(summary = "Get users by role", description = "Retrieve users with specific role (Admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable Role role) {
        // Call service to get users by role
        List<UserResponse> users = userService.getUsersByRole(role);

        // Create success response
        ApiResponse<List<UserResponse>> response = ApiResponse.success(
                "Users retrieved successfully",
                users
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * Get User Statistics
     *
     * Endpoint: GET /api/users/statistics
     * Access: ADMIN only
     *
     * Returns statistics about users (total count, count by role, etc.).
     * Useful for admin dashboard.
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Statistics retrieved successfully",
     *   "data": {
     *     "totalUsers": 42,
     *     "adminCount": 3,
     *     "userCount": 39,
     *     "activeUsers": 40,
     *     "inactiveUsers": 2
     *   }
     * }
     *
     * @return ResponseEntity with user statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can view statistics
    @Operation(summary = "Get user statistics", description = "Retrieve user count statistics (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUserStatistics() {
        // Call service to get statistics
        Map<String, Long> statistics = userService.getUserStatistics();

        // Create success response
        ApiResponse<Map<String, Long>> response = ApiResponse.success(
                "Statistics retrieved successfully",
                statistics
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * Create User
     *
     * Endpoint: POST /api/users
     * Access: ADMIN only
     *
     * Creates a new user account.
     * Admin can specify role and active status.
     *
     * Request Body:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "password123",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "role": "USER",
     *   "isActive": true
     * }
     *
     * @Valid annotation triggers validation of UserRequest fields
     *
     * @param request the user data from request body
     * @return ResponseEntity with created user data
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Only admins can create users
    @Operation(summary = "Create user", description = "Create a new user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        // Call service to create user
        UserResponse createdUser = userService.createUser(request);

        // Create success response
        ApiResponse<UserResponse> response = ApiResponse.success(
                "User created successfully",
                createdUser
        );

        // Return 201 CREATED with response
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update User
     *
     * Endpoint: PUT /api/users/{id}
     * Access: Authenticated users (can update own data), Admins (can update any data)
     *
     * Updates an existing user.
     * Regular users can update their own data (except role and active status).
     * Admins can update any user's data including role and active status.
     *
     * Request Body (all fields optional):
     * {
     *   "username": "new_username",
     *   "email": "new_email@example.com",
     *   "password": "new_password",
     *   "firstName": "NewFirstName",
     *   "lastName": "NewLastName",
     *   "role": "ADMIN",          // Admin only
     *   "isActive": false         // Admin only
     * }
     *
     * @param id the user ID from URL path
     * @param request the updated user data from request body
     * @return ResponseEntity with updated user data
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {

        // Call service to update user
        // Service will check permissions
        UserResponse updatedUser = userService.updateUser(id, request);

        // Create success response
        ApiResponse<UserResponse> response = ApiResponse.success(
                "User updated successfully",
                updatedUser
        );

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }

    /**
     * Delete User
     *
     * Endpoint: DELETE /api/users/{id}
     * Access: ADMIN only
     *
     * Deletes a user from the system.
     * This is a permanent operation.
     *
     * Alternative approach: Instead of deleting, consider setting isActive=false
     * to deactivate users rather than removing them from database.
     *
     * @param id the user ID from URL path
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can delete users
    @Operation(summary = "Delete user", description = "Delete a user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        // Call service to delete user
        userService.deleteUser(id);

        // Create success response
        ApiResponse<Void> response = ApiResponse.success("User deleted successfully");

        // Return 200 OK with response
        return ResponseEntity.ok(response);
    }
}
