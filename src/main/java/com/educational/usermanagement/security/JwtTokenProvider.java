package com.educational.usermanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider
 *
 * This class handles JWT (JSON Web Token) operations:
 * - Generating tokens after successful login
 * - Validating tokens from incoming requests
 * - Extracting user information from tokens
 *
 * Educational Purpose:
 * - Demonstrates JWT implementation in Spring Boot
 * - Shows how to use io.jsonwebtoken library
 * - Illustrates stateless authentication (no session storage)
 * - Explains JWT structure and security
 *
 * What is JWT?
 * JWT is a compact, URL-safe token format for securely transmitting information.
 * Structure: header.payload.signature
 * - Header: Token type and hashing algorithm
 * - Payload: User data (claims) like username, role, expiration
 * - Signature: Verifies token hasn't been tampered with
 *
 * Why use JWT?
 * 1. Stateless: Server doesn't need to store session data
 * 2. Scalable: Works well with microservices
 * 3. Cross-domain: Can be used across different domains
 * 4. Self-contained: Contains all necessary user information
 *
 * Security Notes:
 * - JWT secret must be strong and kept secure
 * - Tokens should have expiration time
 * - Use HTTPS in production to prevent token interception
 * - Store tokens securely on client side (not in localStorage if XSS risk)
 *
 * @author Educational Project
 * @version 1.0.0
 */
@Component // Spring component - can be injected into other classes
public class JwtTokenProvider {

    /**
     * JWT Secret Key
     *
     * This secret is used to sign and verify JWT tokens
     * Loaded from application.properties: app.jwt.secret
     *
     * IMPORTANT: In production:
     * - Use a strong, random secret (at least 256 bits)
     * - Store in environment variables, not in code
     * - Never commit secrets to version control
     * - Rotate secrets periodically
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * JWT Token Expiration Time (in milliseconds)
     *
     * Loaded from application.properties: app.jwt.expiration
     * Default: 86400000ms = 24 hours
     *
     * Trade-off:
     * - Short expiration: More secure, but users must re-login frequently
     * - Long expiration: More convenient, but higher security risk if token stolen
     *
     * Best practice: Use refresh tokens for long-term sessions
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate JWT Token from Authentication
     *
     * Called after successful login to create a token for the user
     *
     * Token contains (claims):
     * - sub: Subject (username)
     * - iat: Issued at timestamp
     * - exp: Expiration timestamp
     *
     * @param authentication Spring Security Authentication object
     * @return JWT token as String
     */
    public String generateToken(Authentication authentication) {
        // Get username from authentication
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Get current date for issuedAt time
        Date now = new Date();

        // Calculate expiration date (now + expiration time)
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // Create secret key for signing
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Build and return the JWT token
        return Jwts.builder()
                .setSubject(username)                    // Set username as subject
                .setIssuedAt(now)                        // Set issue time
                .setExpiration(expiryDate)               // Set expiration time
                .signWith(key, SignatureAlgorithm.HS512) // Sign with secret key using HS512 algorithm
                .compact();                              // Build the token
    }

    /**
     * Generate JWT Token from Username
     *
     * Alternative method when you only have the username
     * Useful for registration or other scenarios
     *
     * @param username the username to create token for
     * @return JWT token as String
     */
    public String generateTokenFromUsername(String username) {
        // Get current date
        Date now = new Date();

        // Calculate expiration date
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // Create secret key
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Build and return the JWT token
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract Username from JWT Token
     *
     * Parses the token and extracts the username (subject claim)
     *
     * @param token JWT token
     * @return username from token
     */
    public String getUsernameFromToken(String token) {
        // Create secret key for parsing
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Parse the token and extract claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Return the subject (username)
        return claims.getSubject();
    }

    /**
     * Validate JWT Token
     *
     * Checks if the token is valid:
     * 1. Signature is correct (not tampered with)
     * 2. Not expired
     * 3. Properly formatted
     *
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Create secret key for parsing
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            // Try to parse the token
            // If any validation fails, an exception will be thrown
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            // If no exception, token is valid
            return true;

        } catch (SecurityException ex) {
            // Invalid JWT signature
            System.err.println("Invalid JWT signature: " + ex.getMessage());
        } catch (MalformedJwtException ex) {
            // Invalid JWT token format
            System.err.println("Invalid JWT token: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // JWT token is expired
            System.err.println("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // JWT token is unsupported
            System.err.println("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // JWT claims string is empty
            System.err.println("JWT claims string is empty: " + ex.getMessage());
        }

        // If any exception occurred, token is invalid
        return false;
    }
}
