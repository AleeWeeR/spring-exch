# 🎓 User Management System - Educational Project

A comprehensive, fully-commented educational project demonstrating modern Spring Boot development with Oracle Database and two frontend options (React and Pure HTML/CSS/JS).

## 📚 Purpose

This project is designed for **educational purposes** to teach:

- **Backend**: Spring Boot 3, Spring Security, JWT Authentication, JPA/Hibernate, Oracle Database
- **Frontend**: React.js with Vite AND Pure HTML/CSS/JavaScript
- **Architecture**: REST API, Layered Architecture, DTO Pattern, Exception Handling
- **Security**: BCrypt password hashing, JWT tokens, Role-based access control
- **Best Practices**: Maximum code comments, clean code, SOLID principles

## 🌟 Features

### Backend (Spring Boot)
- ✅ RESTful API with comprehensive Swagger documentation
- ✅ JWT-based stateless authentication
- ✅ Role-based authorization (ADMIN, USER)
- ✅ Full CRUD operations for user management
- ✅ Oracle Database integration with JPA/Hibernate
- ✅ Global exception handling
- ✅ Input validation
- ✅ BCrypt password encryption
- ✅ CORS configuration for frontend access
- ✅ **MAXIMUM COMMENTS** for educational purposes

### Frontend Options

#### 1. React.js Frontend
- Modern component-based architecture
- React Router for navigation
- Axios for API calls
- Responsive design
- Admin and user dashboards

#### 2. Pure HTML/CSS/JS Frontend
- No framework dependencies
- Vanilla JavaScript
- Modern Fetch API
- Great for learning fundamentals
- Same functionality as React version

## 🏗️ Project Structure

```
spring-exch/
├── src/main/java/com/educational/usermanagement/
│   ├── config/              # Security, CORS configuration
│   ├── controller/          # REST API endpoints
│   ├── dto/                 # Data Transfer Objects
│   ├── entity/              # JPA entities
│   ├── exception/           # Custom exceptions & handler
│   ├── repository/          # Database repositories
│   ├── security/            # JWT, filters, user details service
│   └── service/             # Business logic
├── src/main/resources/
│   ├── application.properties    # Configuration
│   └── db/schema.sql            # Oracle database schema
├── frontend-react/          # React frontend
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── services/        # API services
│   │   └── styles/          # CSS files
│   ├── package.json
│   └── vite.config.js
├── frontend-html/           # Pure HTML/CSS/JS frontend
│   ├── index.html
│   ├── styles.css
│   ├── app.js
│   └── README.md
├── pom.xml                  # Maven configuration
├── README.md                # This file
└── SETUP_GUIDE.md          # Detailed setup instructions
```

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- Oracle Database 11g or higher
- Node.js 18+ (for React frontend)
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### 1. Database Setup

```sql
-- Run the schema creation script
@src/main/resources/db/schema.sql
```

### 2. Backend Setup

1. Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

2. Run the application:
```bash
mvn clean install
mvn spring-boot:run
```

3. Access Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Frontend Setup

#### Option A: React Frontend
```bash
cd frontend-react
npm install
npm run dev
```
Access at: http://localhost:3000

#### Option B: Pure HTML/CSS/JS Frontend
```bash
cd frontend-html
# Open with Live Server or:
python -m http.server 5500
```
Access at: http://localhost:5500

## 🔐 Default Login Credentials

| Role  | Username   | Password     |
|-------|------------|--------------|
| Admin | admin      | admin123     |
| User  | john_doe   | password123  |
| User  | jane_smith | password123  |

## 📖 API Endpoints

### Authentication (Public)
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/auth/health` - Health check

### User Management (Protected)
- `GET /api/users` - Get all users (Admin only)
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/me` - Get current user
- `GET /api/users/search?q=term` - Search users
- `GET /api/users/role/{role}` - Get users by role (Admin only)
- `GET /api/users/statistics` - Get user statistics (Admin only)
- `POST /api/users` - Create user (Admin only)
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (Admin only)

## 🎯 Learning Objectives

After studying this project, you will understand:

### Backend Concepts
1. **Spring Boot Architecture**
   - Layered architecture (Controller, Service, Repository)
   - Dependency injection
   - Component scanning
   - Application properties configuration

2. **Spring Security**
   - JWT authentication
   - Security filter chain
   - Password encoding with BCrypt
   - Role-based authorization
   - Stateless session management

3. **JPA/Hibernate**
   - Entity mapping
   - Relationships
   - Queries (JPQL)
   - Transactions
   - Oracle-specific configurations

4. **REST API Design**
   - HTTP methods (GET, POST, PUT, DELETE)
   - Request/Response DTOs
   - Status codes
   - Exception handling
   - API documentation with Swagger

### Frontend Concepts
1. **React.js**
   - Component-based architecture
   - State management
   - Routing
   - HTTP client (Axios)
   - Modern build tools (Vite)

2. **Vanilla JavaScript**
   - DOM manipulation
   - Fetch API
   - Event handling
   - LocalStorage
   - Async/await patterns

### Security Concepts
1. JWT tokens and how they work
2. Password hashing (never store plain text!)
3. Authorization headers
4. CORS and why it matters
5. Role-based access control

## 📝 Code Comments

This project contains **MAXIMUM COMMENTS** for educational purposes. Every file includes:
- Purpose explanation
- Detailed method documentation
- Inline comments explaining complex logic
- Educational notes on best practices
- Links between related components

## 🔧 Technologies Used

### Backend
- **Spring Boot 3.5.5** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **Hibernate** - ORM framework
- **Oracle JDBC Driver** - Database connectivity
- **JWT (jjwt 0.11.5)** - Token generation
- **Lombok** - Reduce boilerplate code
- **Swagger/OpenAPI** - API documentation
- **Maven** - Build tool

### Frontend (React)
- **React 18** - UI framework
- **React Router 6** - Navigation
- **Axios** - HTTP client
- **Vite** - Build tool

### Frontend (HTML)
- **Pure HTML5** - Markup
- **CSS3** - Styling
- **JavaScript (ES6+)** - Logic
- **Fetch API** - HTTP requests

## 📚 Additional Documentation

- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - Detailed setup instructions for Oracle, Java, and all tools
- **[frontend-react/README.md](frontend-react/README.md)** - React frontend documentation
- **[frontend-html/README.md](frontend-html/README.md)** - HTML frontend documentation
- **Swagger UI** - http://localhost:8080/swagger-ui.html (when backend running)

## 🎓 Recommended Learning Path

1. **Start with Backend**
   - Read entity classes to understand data model
   - Study repository interfaces
   - Examine service layer for business logic
   - Review controllers for API endpoints
   - Understand security configuration

2. **Then Frontend (Choose One)**
   - **For Framework Learners**: Study React frontend
   - **For Fundamentals**: Study HTML/JS frontend
   - Both frontends demonstrate the same functionality

3. **Test and Experiment**
   - Use Swagger UI to test API endpoints
   - Try different user roles
   - Modify code and see the effects
   - Add new features as practice

## 🤝 Contributing

This is an educational project. Feel free to:
- Fork and experiment
- Add more features for practice
- Improve comments and documentation
- Share with other learners

## ⚠️ Important Notes

### For Production Use
This project is **educational**. Before using in production:

1. **Security**
   - Change default passwords
   - Use environment variables for secrets
   - Set `app.jwt.secret` to a strong random value
   - Enable HTTPS
   - Add rate limiting
   - Implement refresh tokens

2. **Database**
   - Set `spring.jpa.hibernate.ddl-auto=validate` or `none`
   - Use database migrations (Flyway/Liquibase)
   - Implement proper backup strategy

3. **Configuration**
   - Use profiles for different environments
   - Externalize configuration
   - Set up proper logging
   - Configure monitoring

4. **Code**
   - Remove excessive comments (they're for learning!)
   - Add proper error tracking
   - Implement comprehensive tests
   - Set up CI/CD pipeline

## 📧 Support

For questions or issues:
- Review the comprehensive code comments
- Check SETUP_GUIDE.md for detailed instructions
- Consult Swagger UI for API documentation
- Read inline documentation in the code

## 📄 License

This is an educational project created for learning purposes. Feel free to use, modify, and learn from it!

## 🌟 Acknowledgments

Created as a comprehensive educational resource for learning:
- Modern Spring Boot development
- Security best practices
- RESTful API design
- Frontend-backend integration
- Database integration with Oracle

**Happy Learning! 🎓**
