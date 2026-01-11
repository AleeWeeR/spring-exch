package com.educational.usermanagement.exception;

/**
 * Unauthorized Exception
 *
 * This custom exception is thrown when a user tries to perform an action
 * they don't have permission for.
 *
 * Educational Purpose:
 * - Demonstrates authorization exception handling
 * - Shows separation between authentication and authorization
 * - Illustrates role-based access control errors
 *
 * Difference from Authentication:
 * - Authentication: "Who are you?" (Login verification)
 * - Authorization: "What are you allowed to do?" (Permission verification)
 *
 * When to throw:
 * - Regular user trying to access admin-only endpoint
 * - User trying to modify another user's data (when not admin)
 * - User trying to delete their own account (when not allowed)
 * - Any other permission violation
 *
 * Example Usage:
 * throw new UnauthorizedException("You don't have permission to delete users");
 * throw new UnauthorizedException("Only administrators can access this resource");
 *
 * @author Educational Project
 * @version 1.0.0
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructor with message
     *
     * @param message the error message explaining why access was denied
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Default constructor
     *
     * Uses a generic message
     */
    public UnauthorizedException() {
        super("You are not authorized to perform this action");
    }
}
