-- ============================================
-- User Management System - Oracle Database Schema
-- ============================================
--
-- This SQL script creates the database schema for the User Management System.
-- It includes:
-- - User table for storing user information
-- - Sequence for auto-incrementing user IDs
-- - Indexes for optimizing queries
-- - Sample data for testing
--
-- Educational Purpose:
-- - Demonstrates Oracle-specific SQL syntax
-- - Shows how to create sequences for auto-increment
-- - Illustrates table creation with constraints
-- - Explains indexing for performance
-- - Provides sample data for initial testing
--
-- How to use:
-- 1. Connect to Oracle database using SQL*Plus or SQL Developer
-- 2. Run this script: @schema.sql
-- 3. Verify tables created: SELECT * FROM user_tab_columns WHERE table_name = 'USERS';
--
-- ============================================

-- ============================================
-- DROP EXISTING OBJECTS (for clean re-creation)
-- ============================================

-- Drop table if exists (ignore error if doesn't exist)
-- This ensures clean state when re-running the script
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE USERS CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

-- Drop sequence if exists
BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE USER_SEQ';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -2289 THEN
         RAISE;
      END IF;
END;
/

-- ============================================
-- CREATE SEQUENCE FOR USER IDs
-- ============================================

-- Sequence generates unique ID numbers for each new user
-- START WITH 1: First ID will be 1
-- INCREMENT BY 1: Each new ID increments by 1 (1, 2, 3, ...)
-- NOCACHE: Don't cache sequence values (safer but slightly slower)
-- NOCYCLE: Don't restart sequence after reaching max value
CREATE SEQUENCE USER_SEQ
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;

-- ============================================
-- CREATE USERS TABLE
-- ============================================

-- Main table for storing user information
CREATE TABLE USERS (
    -- Primary Key: Unique identifier for each user
    -- Generated automatically using USER_SEQ sequence
    ID NUMBER(19) PRIMARY KEY,

    -- Username: Unique login name
    -- VARCHAR2(50): Variable-length string, max 50 characters
    -- UNIQUE: No two users can have the same username
    -- NOT NULL: Field must have a value
    USERNAME VARCHAR2(50) UNIQUE NOT NULL,

    -- Email: User's email address
    -- VARCHAR2(100): Allows longer email addresses
    -- UNIQUE: No two users can have the same email
    -- NOT NULL: Field must have a value
    EMAIL VARCHAR2(100) UNIQUE NOT NULL,

    -- Password: Encrypted password hash
    -- VARCHAR2(255): BCrypt hashes are 60 chars, but allow 255 for flexibility
    -- NOT NULL: User must have a password
    -- IMPORTANT: This stores BCrypt hash, never plain text!
    PASSWORD VARCHAR2(255) NOT NULL,

    -- First Name: Optional first name
    -- VARCHAR2(50): Max 50 characters
    -- NULL allowed: User doesn't have to provide first name
    FIRST_NAME VARCHAR2(50),

    -- Last Name: Optional last name
    -- VARCHAR2(50): Max 50 characters
    -- NULL allowed: User doesn't have to provide last name
    LAST_NAME VARCHAR2(50),

    -- Phone Number: Optional contact number
    -- VARCHAR2(20): Max 20 characters (allows international formats)
    PHONE_NUMBER VARCHAR2(20),

    -- Role: User's role in the system (ADMIN or USER)
    -- VARCHAR2(20): Store enum value as string
    -- NOT NULL: Every user must have a role
    -- CHECK: Ensures only valid values are stored
    ROLE VARCHAR2(20) NOT NULL CHECK (ROLE IN ('ADMIN', 'USER')),

    -- Active Status: Whether account is active
    -- NUMBER(1): 1 for true, 0 for false
    -- DEFAULT 1: New users are active by default
    -- NOT NULL: Must always have a value
    IS_ACTIVE NUMBER(1) DEFAULT 1 NOT NULL CHECK (IS_ACTIVE IN (0, 1)),

    -- Created At: Timestamp when user was created
    -- TIMESTAMP: Stores date and time
    -- DEFAULT CURRENT_TIMESTAMP: Automatically set to current time on insert
    -- NOT NULL: Must have a value
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Updated At: Timestamp when user was last updated
    -- TIMESTAMP: Stores date and time
    -- DEFAULT CURRENT_TIMESTAMP: Automatically set to current time on insert
    -- Updated automatically by trigger (see below)
    -- NOT NULL: Must have a value
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================
-- CREATE INDEXES FOR PERFORMANCE
-- ============================================

-- Index on USERNAME for faster lookups during login
-- Unique index also enforces uniqueness constraint
-- CREATE UNIQUE INDEX is redundant since we have UNIQUE constraint
-- But shown for educational purposes
-- CREATE UNIQUE INDEX IDX_USERS_USERNAME ON USERS(USERNAME);

-- Index on EMAIL for faster lookups
-- Useful when checking if email exists during registration
-- CREATE UNIQUE INDEX IDX_USERS_EMAIL ON USERS(EMAIL);

-- Index on ROLE for faster filtering by role
-- Useful for queries like "get all admins"
CREATE INDEX IDX_USERS_ROLE ON USERS(ROLE);

-- Index on IS_ACTIVE for faster filtering by active status
-- Useful for queries like "get all active users"
CREATE INDEX IDX_USERS_IS_ACTIVE ON USERS(IS_ACTIVE);

-- Composite index on ROLE and IS_ACTIVE
-- Optimizes queries like "get all active admins"
CREATE INDEX IDX_USERS_ROLE_ACTIVE ON USERS(ROLE, IS_ACTIVE);

-- ============================================
-- CREATE TRIGGER FOR UPDATED_AT
-- ============================================

-- This trigger automatically updates UPDATED_AT timestamp
-- whenever a row is modified
-- BEFORE UPDATE: Executes before the UPDATE operation
-- FOR EACH ROW: Executes for each row being updated
CREATE OR REPLACE TRIGGER TRG_USERS_UPDATED_AT
BEFORE UPDATE ON USERS
FOR EACH ROW
BEGIN
    -- Set UPDATED_AT to current timestamp
    :NEW.UPDATED_AT := CURRENT_TIMESTAMP;
END;
/

-- ============================================
-- INSERT SAMPLE DATA FOR TESTING
-- ============================================

-- Note: In real application, Spring Boot with ddl-auto=update
-- will create the table. This sample data is for manual testing.

-- Insert Admin User
-- Username: admin
-- Password: admin123 (BCrypt hash shown below)
-- Role: ADMIN
INSERT INTO USERS (
    ID, USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE_NUMBER,
    ROLE, IS_ACTIVE, CREATED_AT, UPDATED_AT
) VALUES (
    USER_SEQ.NEXTVAL,
    'admin',
    'admin@example.com',
    -- BCrypt hash of 'admin123'
    -- To generate BCrypt hash in Java: BCryptPasswordEncoder.encode("admin123")
    -- This is an example hash - replace with actual hash from your application
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'User',
    '+1234567890',
    'ADMIN',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert Regular User 1
-- Username: john_doe
-- Password: password123
-- Role: USER
INSERT INTO USERS (
    ID, USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE_NUMBER,
    ROLE, IS_ACTIVE, CREATED_AT, UPDATED_AT
) VALUES (
    USER_SEQ.NEXTVAL,
    'john_doe',
    'john@example.com',
    -- BCrypt hash of 'password123'
    '$2a$10$eOXCNT5r4N5zLpnlqe9BzuZF7kRKS.iFOxCvPwN.kJ2h3FZPqC0L.',
    'John',
    'Doe',
    '+1234567891',
    'USER',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert Regular User 2
-- Username: jane_smith
-- Password: password123
-- Role: USER
INSERT INTO USERS (
    ID, USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, PHONE_NUMBER,
    ROLE, IS_ACTIVE, CREATED_AT, UPDATED_AT
) VALUES (
    USER_SEQ.NEXTVAL,
    'jane_smith',
    'jane@example.com',
    -- BCrypt hash of 'password123'
    '$2a$10$eOXCNT5r4N5zLpnlqe9BzuZF7kRKS.iFOxCvPwN.kJ2h3FZPqC0L.',
    'Jane',
    'Smith',
    '+1234567892',
    'USER',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Commit the sample data
COMMIT;

-- ============================================
-- VERIFY INSTALLATION
-- ============================================

-- Count total users (should be 3)
SELECT COUNT(*) AS TOTAL_USERS FROM USERS;

-- Show all users
SELECT
    ID,
    USERNAME,
    EMAIL,
    FIRST_NAME,
    LAST_NAME,
    ROLE,
    IS_ACTIVE,
    CREATED_AT
FROM USERS
ORDER BY ID;

-- Show user statistics
SELECT
    ROLE,
    COUNT(*) AS COUNT,
    SUM(CASE WHEN IS_ACTIVE = 1 THEN 1 ELSE 0 END) AS ACTIVE_COUNT,
    SUM(CASE WHEN IS_ACTIVE = 0 THEN 1 ELSE 0 END) AS INACTIVE_COUNT
FROM USERS
GROUP BY ROLE;

-- ============================================
-- NOTES
-- ============================================

-- 1. Password Hashes:
--    The BCrypt hashes shown above are examples.
--    To get actual hashes for your passwords:
--    - Use the /api/auth/register endpoint
--    - Or use BCryptPasswordEncoder in Java code
--    - Or use online BCrypt generator (not recommended for production)

-- 2. Production Considerations:
--    - Don't commit passwords (even hashed) to version control
--    - Use strong passwords for default admin account
--    - Change or delete default accounts after deployment
--    - Consider adding more fields: email_verified, last_login, etc.
--    - Add audit fields: created_by, updated_by
--    - Consider soft delete instead of hard delete

-- 3. Performance Tuning:
--    - Adjust index strategy based on query patterns
--    - Consider partitioning for very large user tables
--    - Monitor and optimize slow queries
--    - Use connection pooling in application

-- 4. Security:
--    - Never store passwords in plain text
--    - Always use parameterized queries to prevent SQL injection
--    - Implement rate limiting on authentication endpoints
--    - Log security events (failed logins, password changes, etc.)
--    - Consider adding fields for password reset tokens, MFA, etc.

-- End of schema.sql
