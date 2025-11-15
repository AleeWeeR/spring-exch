package com.educational.usermanagement.exception;

import com.educational.usermanagement.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * This class handles exceptions thrown by controllers across the entire application.
 * It provides centralized exception handling and converts exceptions to appropriate HTTP responses.
 *
 * Educational Purpose:
 * - Demonstrates @RestControllerAdvice for global exception handling
 * - Shows how to convert exceptions to HTTP responses
 * - Illustrates handling of different exception types
 * - Shows validation error handling
 *
 * How it works:
 * 1. Exception is thrown anywhere in the application
 * 2. Spring catches the exception
 * 3. Spring finds matching @ExceptionHandler method
 * 4. Handler method creates appropriate HTTP response
 * 5. Response is sent to client
 *
 * Benefits:
 * - Centralized error handling (DRY principle)
 * - Consistent error response format
 * - No try-catch blocks needed in controllers
 * - Easy to maintain and extend
 *
 * @author Educational Project
 * @version 1.0.0
 */
@RestControllerAdvice // Combines @ControllerAdvice and @ResponseBody for REST APIs
public class GlobalExceptionHandler {

    /**
     * Handle Resource Not Found Exception
     *
     * Returns 404 NOT_FOUND status when requested resource doesn't exist
     *
     * Example: User with ID 123 not found
     *
     * @param ex the ResourceNotFoundException
     * @return ResponseEntity with 404 status and error message
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // Create error response with exception message
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());

        // Return 404 NOT FOUND status
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Duplicate Resource Exception
     *
     * Returns 409 CONFLICT status when trying to create duplicate resource
     *
     * Example: Username "john_doe" already exists
     *
     * @param ex the DuplicateResourceException
     * @return ResponseEntity with 409 status and error message
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex) {
        // Create error response with exception message
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());

        // Return 409 CONFLICT status
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle Unauthorized Exception
     *
     * Returns 403 FORBIDDEN status when user doesn't have required permissions
     *
     * Example: Regular user trying to access admin-only endpoint
     *
     * @param ex the UnauthorizedException
     * @return ResponseEntity with 403 status and error message
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        // Create error response with exception message
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());

        // Return 403 FORBIDDEN status
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Bad Credentials Exception
     *
     * Returns 401 UNAUTHORIZED status for invalid login credentials
     * Thrown by Spring Security when username or password is incorrect
     *
     * Example: Wrong password during login
     *
     * @param ex the BadCredentialsException
     * @return ResponseEntity with 401 status and error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        // Create generic error message (don't reveal whether username or password is wrong)
        ApiResponse<Void> response = ApiResponse.error("Invalid username or password");

        // Return 401 UNAUTHORIZED status
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Username Not Found Exception
     *
     * Returns 401 UNAUTHORIZED status when username doesn't exist
     * Thrown by Spring Security's UserDetailsService
     *
     * @param ex the UsernameNotFoundException
     * @return ResponseEntity with 401 status and error message
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        // Create generic error message (don't reveal that username doesn't exist)
        ApiResponse<Void> response = ApiResponse.error("Invalid username or password");

        // Return 401 UNAUTHORIZED status
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Validation Errors
     *
     * Returns 400 BAD_REQUEST status for validation failures
     * Automatically triggered when @Valid or @Validated annotation fails
     *
     * Example: Email format is invalid, username too short, etc.
     *
     * @param ex the MethodArgumentNotValidException containing all validation errors
     * @return ResponseEntity with 400 status and all validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        // Create a map to store field-specific errors
        // Key: field name (e.g., "username", "email")
        // Value: error message (e.g., "Username is required")
        Map<String, String> errors = new HashMap<>();

        // Extract all field errors from the exception
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Create error response with all validation errors
        ApiResponse<Map<String, String>> response = ApiResponse.error("Validation failed", errors);

        // Return 400 BAD REQUEST status
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle All Other Exceptions
     *
     * Catches any exception not handled by specific handlers above
     * Returns 500 INTERNAL_SERVER_ERROR status
     *
     * This is a fallback handler for unexpected errors
     *
     * @param ex the generic Exception
     * @return ResponseEntity with 500 status and error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        // Log the exception for debugging (in production, use proper logging)
        ex.printStackTrace();

        // Create generic error response (don't expose internal error details)
        ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred: " + ex.getMessage());

        // Return 500 INTERNAL SERVER ERROR status
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
