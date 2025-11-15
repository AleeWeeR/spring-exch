package com.educational.usermanagement.service;

import com.educational.usermanagement.dto.AuthResponse;
import com.educational.usermanagement.dto.LoginRequest;
import com.educational.usermanagement.dto.RegisterRequest;
import com.educational.usermanagement.dto.UserResponse;
import com.educational.usermanagement.entity.Role;
import com.educational.usermanagement.entity.User;
import com.educational.usermanagement.exception.DuplicateResourceException;
import com.educational.usermanagement.repository.UserRepository;
import com.educational.usermanagement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 *
 * This service handles authentication operations:
 * - User registration (signup)
 * - User login (signin)
 * - JWT token generation
 *
 * Educational Purpose:
 * - Demonstrates authentication flow in Spring Security
 * - Shows how to use AuthenticationManager
 * - Illustrates JWT token generation after authentication
 * - Explains registration process with password hashing
 *
 * Authentication vs Authorization:
 * - Authentication: Verifying WHO the user is (this service)
 * - Authorization: Verifying WHAT the user can do (handled by Security Config)
 *
 * Login Flow:
 * 1. User submits credentials (username/email + password)
 * 2. AuthenticationManager verifies credentials
 * 3. If valid, creates Authentication object
 * 4. Generate JWT token from authentication
 * 5. Return token and user data to client
 *
 * Registration Flow:
 * 1. User submits registration data
 * 2. Validate username and email are unique
 * 3. Hash password with BCrypt
 * 4. Save user to database
 * 5. Automatically log in user (generate token)
 * 6. Return token and user data to client
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Service // Marks this as a Spring service component
@Transactional // All methods run in transactions
public class AuthService {

    /**
     * Authentication Manager - handles authentication process
     * Configured in SecurityConfig
     */
    @Autowired
    private AuthenticationManager authenticationManager;

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
     * JWT Token Provider for generating tokens
     */
    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * User Login
     *
     * Authenticates user and returns JWT token.
     *
     * Process:
     * 1. Create authentication request with credentials
     * 2. AuthenticationManager validates credentials:
     *    - Calls CustomUserDetailsService to load user
     *    - Compares password hash with stored hash
     *    - If match, creates Authentication object
     * 3. Set authentication in Security Context
     * 4. Generate JWT token
     * 5. Load user data and return with token
     *
     * @param loginRequest login credentials (username/email and password)
     * @return AuthResponse with JWT token and user data
     * @throws BadCredentialsException if credentials are invalid (handled by GlobalExceptionHandler)
     */
    public AuthResponse login(LoginRequest loginRequest) {
        // Create authentication token with user credentials
        // At this point, credentials are NOT verified yet
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(),  // Principal (username or email)
                loginRequest.getPassword()          // Credentials (plain text password)
        );

        // Authenticate user
        // This internally:
        // 1. Calls CustomUserDetailsService.loadUserByUsername()
        // 2. Gets user from database
        // 3. Compares passwords using PasswordEncoder
        // 4. If match, returns Authentication object
        // 5. If no match, throws BadCredentialsException
        Authentication authentication = authenticationManager.authenticate(authToken);

        // Set authentication in Security Context
        // This makes the user authenticated for the rest of this request
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // Get username from authentication
        String username = authentication.getName();

        // Load full user data from database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Convert user to DTO (excludes password)
        UserResponse userResponse = UserResponse.fromEntity(user);

        // Create and return authentication response
        return new AuthResponse(jwt, userResponse);
    }

    /**
     * User Registration
     *
     * Creates a new user account and automatically logs them in.
     *
     * Process:
     * 1. Validate username is unique
     * 2. Validate email is unique
     * 3. Create new User entity
     * 4. Hash password with BCrypt
     * 5. Set default role (USER) and active status (true)
     * 6. Save user to database
     * 7. Generate JWT token for the new user
     * 8. Return token and user data
     *
     * Security Notes:
     * - Password is hashed before storing (NEVER store plain text!)
     * - New users get USER role by default (not ADMIN)
     * - Username and email must be unique
     *
     * @param registerRequest registration data
     * @return AuthResponse with JWT token and user data
     * @throws DuplicateResourceException if username or email already exists
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        // Validate username is unique
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username", registerRequest.getUsername());
        }

        // Validate email is unique
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email", registerRequest.getEmail());
        }

        // Create new User entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());

        // Hash password before saving
        // BCrypt automatically generates a random salt and hashes the password
        // The hash includes: algorithm + cost + salt + hash
        // Example hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());

        // Set default role as USER
        // New users cannot register as ADMIN
        // Only existing admins can create admin users
        user.setRole(Role.USER);

        // Set account as active
        user.setIsActive(true);

        // Save user to database
        // JPA automatically sets createdAt and updatedAt fields
        User savedUser = userRepository.save(user);

        // Generate JWT token for the new user
        // This automatically logs in the user after registration
        String jwt = tokenProvider.generateTokenFromUsername(savedUser.getUsername());

        // Convert user to DTO
        UserResponse userResponse = UserResponse.fromEntity(savedUser);

        // Return authentication response
        return new AuthResponse(jwt, userResponse);
    }
}
