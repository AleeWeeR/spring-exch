package com.educational.usermanagement.config;

import com.educational.usermanagement.security.CustomUserDetailsService;
import com.educational.usermanagement.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration
 *
 * This class configures Spring Security for the application.
 * It defines:
 * - Authentication mechanism (JWT)
 * - Authorization rules (who can access what)
 * - Password encoding (BCrypt)
 * - CORS settings (for frontend communication)
 * - Filter chain (request processing order)
 *
 * Educational Purpose:
 * - Demonstrates modern Spring Security configuration (Spring Boot 3)
 * - Shows JWT-based stateless authentication
 * - Illustrates role-based access control
 * - Explains CORS configuration
 * - Shows password hashing with BCrypt
 *
 * Key Concepts:
 * 1. Authentication: Verifying WHO the user is (login)
 * 2. Authorization: Verifying WHAT the user can do (permissions)
 * 3. Stateless: No server-side session, all state in JWT token
 * 4. BCrypt: Strong password hashing algorithm
 * 5. CORS: Allows frontend (different origin) to call API
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Configuration // Marks this as a Spring configuration class
@EnableWebSecurity // Enables Spring Security
@EnableMethodSecurity // Enables @PreAuthorize, @Secured annotations on methods
public class SecurityConfig {

    /**
     * Custom User Details Service for loading user from database
     */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * JWT Authentication Filter for token validation
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password Encoder Bean
     *
     * BCryptPasswordEncoder is a strong password hashing function.
     * It automatically handles:
     * - Salt generation (random data added to passwords)
     * - Multiple hashing rounds (slow computation to resist brute force)
     *
     * Why BCrypt?
     * 1. One-way function: Cannot reverse hash to get original password
     * 2. Adaptive: Can increase cost factor as hardware improves
     * 3. Salt: Prevents rainbow table attacks
     * 4. Industry standard: Widely trusted and tested
     *
     * @return BCryptPasswordEncoder for password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder with default strength (10 rounds)
        // Each round doubles the computation time
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider Bean
     *
     * DaoAuthenticationProvider is Spring Security's authentication mechanism
     * that uses a UserDetailsService and PasswordEncoder.
     *
     * How it works:
     * 1. User submits login credentials
     * 2. Provider loads user via UserDetailsService
     * 3. Provider compares submitted password with stored hash
     * 4. If match, authentication succeeds
     *
     * @return configured authentication provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Set our custom UserDetailsService to load users from database
        authProvider.setUserDetailsService(userDetailsService);

        // Set password encoder to verify passwords
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Authentication Manager Bean
     *
     * AuthenticationManager is the main Spring Security interface for authentication.
     * We expose it as a bean so we can inject it into services (for login logic).
     *
     * @param authConfig authentication configuration
     * @return AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Security Filter Chain
     *
     * This is the core of Spring Security configuration.
     * It defines the security rules for HTTP requests.
     *
     * Configuration:
     * 1. Disable CSRF (not needed for stateless JWT)
     * 2. Configure CORS (allow frontend access)
     * 3. Set session to STATELESS (no server-side sessions)
     * 4. Define authorization rules (what URLs require what roles)
     * 5. Add JWT filter to filter chain
     *
     * @param http HttpSecurity configuration object
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery) protection
                // Not needed for stateless APIs using JWT
                // CSRF protection is for session-based authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS (Cross-Origin Resource Sharing)
                // Allows frontend running on different domain to call our API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - anyone can access without authentication
                        .requestMatchers("/api/auth/**").permitAll()           // Login, register
                        .requestMatchers("/swagger-ui/**").permitAll()          // Swagger UI
                        .requestMatchers("/v3/api-docs/**").permitAll()         // API docs
                        .requestMatchers("/h2-console/**").permitAll()          // H2 console (dev only)

                        // Admin-only endpoints - only users with ADMIN role
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Set session management to STATELESS
                // No HTTP sessions will be created or used
                // All authentication state is in the JWT token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Set authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                // This ensures JWT validation happens first
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration
     *
     * CORS (Cross-Origin Resource Sharing) allows frontend applications
     * running on different domains to call our API.
     *
     * Why needed?
     * Browsers block cross-origin requests by default for security.
     * We need to explicitly allow our frontend to call the API.
     *
     * Example:
     * - Backend API: http://localhost:8080
     * - React Frontend: http://localhost:3000
     * - HTML Frontend: http://localhost:5500
     * Without CORS, browser would block these requests.
     *
     * @return CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from these origins (frontend URLs)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",    // React dev server
                "http://localhost:5500",    // Live Server (VS Code extension)
                "http://127.0.0.1:5500",    // Alternative Live Server URL
                "http://localhost:8080"     // Same origin (for testing)
        ));

        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET",      // Read operations
                "POST",     // Create operations
                "PUT",      // Update operations
                "DELETE",   // Delete operations
                "OPTIONS"   // Preflight requests
        ));

        // Allow these headers in requests
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",   // For JWT token
                "Content-Type",    // For JSON payload
                "Accept"           // For response type
        ));

        // Expose these headers in responses
        configuration.setExposedHeaders(List.of("Authorization"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
