package com.educational.usermanagement.security;

import com.educational.usermanagement.entity.User;
import com.educational.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom User Details Service
 *
 * This class implements Spring Security's UserDetailsService interface
 * to load user-specific data during authentication.
 *
 * Educational Purpose:
 * - Demonstrates Spring Security UserDetailsService implementation
 * - Shows how to bridge application User entity with Spring Security
 * - Illustrates how Spring Security loads user during authentication
 * - Explains the role of UserDetails in Spring Security
 *
 * What is UserDetailsService?
 * It's a core Spring Security interface used to retrieve user information.
 * Spring Security calls loadUserByUsername() during authentication to:
 * 1. Find the user in database
 * 2. Load user credentials and authorities (roles)
 * 3. Create a UserDetails object for Spring Security to use
 *
 * Flow:
 * 1. User submits login credentials
 * 2. Spring Security calls loadUserByUsername()
 * 3. This method queries database for user
 * 4. Converts User entity to UserDetails object
 * 5. Spring Security compares passwords
 * 6. If match, creates Authentication object
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Service // Spring service component
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * User Repository for database access
     *
     * Injected by Spring to access user data
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Load User by Username
     *
     * This method is called by Spring Security during authentication.
     * It loads user from database and converts to UserDetails object.
     *
     * Process:
     * 1. Search for user by username or email
     * 2. If not found, throw UsernameNotFoundException
     * 3. If found, convert to UserDetails and return
     *
     * @param usernameOrEmail username or email from login request
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by username or email
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)
                );

        // Check if user account is active
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        // Convert our User entity to Spring Security's UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),              // Username
                user.getPassword(),              // Password (hashed)
                true,                            // Account is enabled
                true,                            // Account is not expired
                true,                            // Credentials are not expired
                true,                            // Account is not locked
                getAuthorities(user)             // User's authorities (roles)
        );
    }

    /**
     * Get User Authorities (Roles)
     *
     * Converts our application's Role to Spring Security's GrantedAuthority
     *
     * Spring Security uses GrantedAuthority to represent permissions
     * Each role is converted to format: "ROLE_ADMIN" or "ROLE_USER"
     *
     * @param user the user whose authorities to retrieve
     * @return Collection of GrantedAuthority objects
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Create a GrantedAuthority for the user's role
        // SimpleGrantedAuthority is Spring Security's basic implementation
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRoleName());

        // Return as singleton collection (user has only one role in our system)
        return Collections.singletonList(authority);
    }
}
