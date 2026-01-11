package com.educational.usermanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 *
 * This filter intercepts every HTTP request and validates JWT tokens.
 * It extends OncePerRequestFilter to ensure it's executed once per request.
 *
 * Educational Purpose:
 * - Demonstrates Spring Security filter chain
 * - Shows how to extract and validate JWT from requests
 * - Illustrates how to set authentication in Security Context
 * - Explains the flow of request authentication
 *
 * What is a Filter?
 * Filters are components that intercept HTTP requests/responses.
 * They can inspect, modify, or block requests before they reach controllers.
 *
 * Filter Chain Order:
 * 1. HTTP Request arrives
 * 2. This filter executes (extracts and validates JWT)
 * 3. If valid, sets authentication in SecurityContext
 * 4. Request continues to controller
 * 5. Controller executes (with authenticated user in context)
 * 6. Response returns to client
 *
 * Request Flow:
 * Client -> Filter (JWT validation) -> Security Context -> Controller
 *
 * Authorization Header Format:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Component // Spring component - will be registered in filter chain
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT Token Provider for token operations
     */
    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Custom User Details Service to load user information
     */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Main Filter Method
     *
     * This method is called for every HTTP request.
     * It attempts to:
     * 1. Extract JWT token from Authorization header
     * 2. Validate the token
     * 3. Load user details
     * 4. Set authentication in Security Context
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 1: Extract JWT token from request
            String jwt = getJwtFromRequest(request);

            // Step 2: Check if token exists and is valid
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // Step 3: Extract username from token
                String username = tokenProvider.getUsernameFromToken(jwt);

                // Step 4: Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Create authentication token
                // UsernamePasswordAuthenticationToken is Spring Security's authentication object
                // Parameters: principal (user), credentials (null after auth), authorities (roles)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // Principal: the authenticated user
                                null,                  // Credentials: null (already authenticated)
                                userDetails.getAuthorities() // Authorities: user's roles
                        );

                // Step 6: Add request details to authentication
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 7: Set authentication in Security Context
                // This makes the user authenticated for this request
                // Controllers can now access the authenticated user via SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            // Log the error (in production, use proper logging framework)
            System.err.println("Could not set user authentication in security context: " + ex.getMessage());

            // Don't throw exception - let the request continue
            // If authentication fails, user will simply not be authenticated
            // Protected endpoints will return 401 Unauthorized
        }

        // Continue with the filter chain
        // This passes the request to the next filter or to the controller
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT Token from Request
     *
     * Reads the Authorization header and extracts the JWT token.
     *
     * Expected header format:
     * Authorization: Bearer <JWT_TOKEN>
     *
     * Process:
     * 1. Get Authorization header value
     * 2. Check if it starts with "Bearer "
     * 3. Extract token (remove "Bearer " prefix)
     * 4. Return token or null if not found
     *
     * @param request HTTP request
     * @return JWT token string or null if not present
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Get Authorization header value
        String bearerToken = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token by removing "Bearer " prefix (first 7 characters)
            return bearerToken.substring(7);
        }

        // No token found
        return null;
    }
}
