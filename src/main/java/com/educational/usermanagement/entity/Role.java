package com.educational.usermanagement.entity;

/**
 * Role Enumeration
 *
 * This enum defines the different roles a user can have in the system.
 * Enums are special Java types used to define collections of constants.
 *
 * Educational Purpose:
 * - Demonstrates how to use enums for fixed sets of values
 * - Shows role-based access control (RBAC) implementation
 * - Used in Spring Security for authorization
 *
 * Usage:
 * - ADMIN: Has full access to all operations (Create, Read, Update, Delete all users)
 * - USER: Limited access (Can only view and update their own profile)
 *
 * @author Educational Project
 * @version 1.0.0
 */
public enum Role {
    /**
     * Administrator role - has full system access
     * Can perform all CRUD operations on any user
     */
    ADMIN,

    /**
     * Regular user role - has limited access
     * Can only view and modify their own data
     */
    USER;

    /**
     * Get role name with ROLE_ prefix (Spring Security convention)
     *
     * Spring Security expects role names to start with "ROLE_" prefix
     * For example: ROLE_ADMIN, ROLE_USER
     *
     * @return role name with ROLE_ prefix
     */
    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
