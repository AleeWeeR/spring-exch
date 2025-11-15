package com.educational.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API Response DTO
 *
 * This class provides a standard structure for API responses.
 * It wraps the actual data with additional metadata like success status and message.
 *
 * Educational Purpose:
 * - Demonstrates consistent API response structure
 * - Shows how to use generic types in Java
 * - Illustrates best practice for API design
 *
 * Response Structure:
 * {
 *   "success": true,
 *   "message": "Operation successful",
 *   "data": { ... actual data ... }
 * }
 *
 * Benefits:
 * 1. Consistency: All endpoints return the same structure
 * 2. Clarity: Frontend always knows if operation succeeded
 * 3. User feedback: Message can be displayed to user
 * 4. Flexibility: Data can be any type (using generics)
 *
 * @param <T> the type of data in the response
 * @author Educational Project
 * @version 1.0.0
 */
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok: Generates no-argument constructor
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class ApiResponse<T> {

    /**
     * Success indicator
     *
     * true: Operation completed successfully
     * false: Operation failed (error occurred)
     */
    private boolean success;

    /**
     * Human-readable message
     *
     * Success example: "User created successfully"
     * Error example: "Username already exists"
     */
    private String message;

    /**
     * Actual response data (generic type)
     *
     * Can be:
     * - A single object (UserResponse)
     * - A list of objects (List<UserResponse>)
     * - null (for operations that don't return data)
     */
    private T data;

    /**
     * Create a success response with data and message
     *
     * @param message success message
     * @param data response data
     * @param <T> type of data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Create a success response with only message (no data)
     *
     * Useful for operations like delete that don't return data
     *
     * @param message success message
     * @param <T> type of data (will be null)
     * @return ApiResponse with success=true and data=null
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Create an error response with message
     *
     * @param message error message
     * @param <T> type of data (will be null)
     * @return ApiResponse with success=false and data=null
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Create an error response with message and error data
     *
     * Useful for returning validation errors or detailed error info
     *
     * @param message error message
     * @param data error details
     * @param <T> type of error data
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
