package com.educational.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for User Management System
 *
 * This is the starting point of our Spring Boot application.
 * The @SpringBootApplication annotation combines three annotations:
 * 1. @Configuration - Tags the class as a source of bean definitions
 * 2. @EnableAutoConfiguration - Tells Spring Boot to start adding beans based on classpath settings
 * 3. @ComponentScan - Tells Spring to look for components, configurations, and services in this package
 *
 * Educational Purpose:
 * This application demonstrates:
 * - Spring Boot REST API development
 * - Oracle Database integration
 * - JWT-based authentication
 * - Role-based authorization (Admin/User)
 * - Full CRUD operations
 *
 * @author Educational Project
 * @version 1.0.0
 */
@SpringBootApplication
public class UserManagementApplication {

    /**
     * Main method - entry point of the application
     *
     * @param args command line arguments (not used in this application)
     */
    public static void main(String[] args) {
        // SpringApplication.run() starts the Spring Boot application
        // It creates the ApplicationContext, starts the embedded server (Tomcat by default)
        // and initializes all the beans
        SpringApplication.run(UserManagementApplication.class, args);

        System.out.println("========================================");
        System.out.println("User Management System Started!");
        System.out.println("API Documentation: http://localhost:8080/swagger-ui.html");
        System.out.println("H2 Console (if enabled): http://localhost:8080/h2-console");
        System.out.println("========================================");
    }
}
