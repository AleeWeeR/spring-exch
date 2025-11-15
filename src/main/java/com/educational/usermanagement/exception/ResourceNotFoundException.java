package com.educational.usermanagement.exception;

/**
 * Resource Not Found Exception
 *
 * This custom exception is thrown when a requested resource (e.g., User) is not found.
 * It extends RuntimeException, which is an unchecked exception.
 *
 * Educational Purpose:
 * - Demonstrates custom exception creation
 * - Shows how to extend RuntimeException
 * - Illustrates exception handling best practices
 *
 * When to throw:
 * - User with specific ID not found
 * - User with specific username not found
 * - Any other resource lookup that fails
 *
 * Example Usage:
 * throw new ResourceNotFoundException("User", "id", userId);
 * throw new ResourceNotFoundException("User", "username", username);
 *
 * @author Educational Project
 * @version 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Name of the resource that was not found
     * Example: "User", "Role", "Organization"
     */
    private String resourceName;

    /**
     * Name of the field used for searching
     * Example: "id", "username", "email"
     */
    private String fieldName;

    /**
     * Value of the field that was searched for
     * Example: 123, "john_doe", "john@example.com"
     */
    private Object fieldValue;

    /**
     * Constructor with resource, field, and value details
     *
     * Automatically formats the error message
     * Format: "User not found with id: 123"
     *
     * @param resourceName the name of the resource
     * @param fieldName the name of the field
     * @param fieldValue the value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Constructor with custom message
     *
     * Use when you need a specific error message
     *
     * @param message the custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Getters for exception details
    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
