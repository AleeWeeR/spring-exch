package com.educational.usermanagement.repository;

import com.educational.usermanagement.entity.Role;
import com.educational.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 *
 * This interface provides database access methods for User entity.
 * It extends JpaRepository which provides built-in CRUD methods.
 *
 * Educational Purpose:
 * - Demonstrates Spring Data JPA repository pattern
 * - Shows how to use built-in CRUD methods
 * - Illustrates custom query methods
 * - Demonstrates JPQL (Java Persistence Query Language) queries
 *
 * JpaRepository provides methods like:
 * - save() - Insert or update a user
 * - findById() - Find user by ID
 * - findAll() - Get all users
 * - deleteById() - Delete user by ID
 * - count() - Count total users
 * - existsById() - Check if user exists
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Repository // Marks this interface as a Spring Data repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<User, Long> means:
    // - User: The entity type we're working with
    // - Long: The type of the entity's primary key (id)

    /**
     * Find user by username
     *
     * Spring Data JPA automatically implements this method based on the method name.
     * Method naming convention: findBy + FieldName
     *
     * @param username the username to search for
     * @return Optional<User> - may contain user if found, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     *
     * Automatically implemented by Spring Data JPA
     * Useful for checking if email already exists during registration
     *
     * @param email the email to search for
     * @return Optional<User> - may contain user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     *
     * More efficient than findByUsername when you only need to check existence
     * Returns boolean instead of fetching the entire entity
     *
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     *
     * More efficient than findByEmail when you only need to check existence
     * Useful for validation during user registration
     *
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by role
     *
     * Spring Data JPA automatically implements this method
     * Useful for getting all admins or all regular users
     *
     * @param role the role to filter by (ADMIN or USER)
     * @return List of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Find all active users
     *
     * Returns only users where isActive = true
     * Useful for excluding deactivated users from lists
     *
     * @param isActive the active status to filter by
     * @return List of active/inactive users
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * Find users by role and active status
     *
     * Combines two filters: role and active status
     * Example: Get all active admins
     *
     * @param role the role to filter by
     * @param isActive the active status to filter by
     * @return List of users matching both criteria
     */
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    /**
     * Search users by username or email (case-insensitive)
     *
     * This is a custom JPQL query using @Query annotation
     * JPQL is similar to SQL but works with entity objects instead of tables
     *
     * LOWER() - Converts strings to lowercase for case-insensitive search
     * LIKE - SQL pattern matching operator
     * % - Wildcard that matches any sequence of characters
     *
     * @param searchTerm the term to search for in username or email
     * @return List of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Count users by role
     *
     * Returns the number of users with a specific role
     * Useful for statistics (e.g., "You have 5 admins and 42 users")
     *
     * @param role the role to count
     * @return number of users with the specified role
     */
    long countByRole(Role role);

    /**
     * Count active users
     *
     * Returns the number of active users
     * Useful for dashboard statistics
     *
     * @param isActive the active status
     * @return number of active/inactive users
     */
    long countByIsActive(Boolean isActive);

    /**
     * Find user by username or email
     *
     * Useful for login where user can use either username or email
     *
     * @param username the username to search for
     * @param email the email to search for
     * @return Optional<User> - may contain user if found, or empty if not found
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
}
