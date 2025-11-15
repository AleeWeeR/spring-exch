package com.educational.usermanagement.exception;

/**
 * Duplicate Resource Exception
 *
 * This custom exception is thrown when trying to create a resource that already exists.
 * Common scenarios: duplicate username, duplicate email, etc.
 *
 * Educational Purpose:
 * - Demonstrates handling of unique constraint violations
 * - Shows how to provide meaningful error messages
 * - Illustrates data validation at service layer
 *
 * When to throw:
 * - Attempting to register with existing username
 * - Attempting to register with existing email
 * - Any other unique constraint violation
 *
 * Example Usage:
 * throw new DuplicateResourceException("Username", username);
 * throw new DuplicateResourceException("Email", email);
 *
 * @author Educational Project
 * @version 1.0.0
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Name of the field that has duplicate value
     * Example: "Username", "Email"
     */
    private String fieldName;

    /**
     * The duplicate value
     * Example: "john_doe", "john@example.com"
     */
    private Object fieldValue;

    /**
     * Constructor with field and value details
     *
     * Automatically formats the error message
     * Format: "Username already exists: john_doe"
     *
     * @param fieldName the name of the field with duplicate value
     * @param fieldValue the duplicate value
     */
    public DuplicateResourceException(String fieldName, Object fieldValue) {
        super(String.format("%s already exists: '%s'", fieldName, fieldValue));
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
    public DuplicateResourceException(String message) {
        super(message);
    }

    // Getters for exception details
    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
