# 📘 Complete Setup Guide - User Management System

This guide provides step-by-step instructions for setting up the entire project from scratch.

## Table of Contents
1. [Prerequisites Installation](#1-prerequisites-installation)
2. [Oracle Database Setup](#2-oracle-database-setup)
3. [Backend Setup](#3-backend-setup)
4. [Frontend Setup](#4-frontend-setup)
5. [Testing the Application](#5-testing-the-application)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Prerequisites Installation

### 1.1 Java 21

#### Windows:
1. Download Oracle JDK 21 or OpenJDK 21 from:
   - Oracle: https://www.oracle.com/java/technologies/downloads/#java21
   - OpenJDK: https://adoptium.net/

2. Run the installer
3. Verify installation:
```cmd
java -version
```

#### macOS:
```bash
brew install openjdk@21
java -version
```

#### Linux:
```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

### 1.2 Maven

#### Windows:
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\Maven`
3. Add to PATH:
   - `M2_HOME=C:\Program Files\Apache\Maven`
   - Add `%M2_HOME%\bin` to PATH
4. Verify:
```cmd
mvn -version
```

#### macOS/Linux:
```bash
# macOS
brew install maven

# Linux
sudo apt install maven

# Verify
mvn -version
```

### 1.3 Node.js (for React frontend)

#### All Platforms:
1. Download from: https://nodejs.org/ (LTS version)
2. Run installer
3. Verify:
```bash
node -version
npm -version
```

### 1.4 Git

#### All Platforms:
Download from: https://git-scm.com/downloads

Verify:
```bash
git --version
```

---

## 2. Oracle Database Setup

### 2.1 Install Oracle Database

#### Option A: Oracle Express Edition (XE) - Recommended for Learning

**Windows:**
1. Download Oracle XE: https://www.oracle.com/database/technologies/xe-downloads.html
2. Run installer
3. Remember the SYS/SYSTEM password you set!

**Linux:**
```bash
# Download and install Oracle XE RPM
# Follow official Oracle documentation
```

**macOS:**
Use Docker (see Option B)

#### Option B: Oracle via Docker (Cross-platform)

```bash
# Pull Oracle Express Edition image
docker pull container-registry.oracle.com/database/express:latest

# Run Oracle container
docker run -d \
  --name oracle-xe \
  -p 1521:1521 \
  -p 5500:5500 \
  -e ORACLE_PWD=YourPassword123 \
  container-registry.oracle.com/database/express:latest

# Check status
docker logs oracle-xe
```

### 2.2 Connect to Oracle

Using SQL*Plus:
```bash
sqlplus system/YourPassword@localhost:1521/XE
```

Or use SQL Developer or DBeaver (GUI tools)

### 2.3 Create Database Schema

1. Connect to Oracle as SYSTEM user

2. Create user (optional, or use SYSTEM):
```sql
CREATE USER usermanagement IDENTIFIED BY usermanagement;
GRANT CONNECT, RESOURCE TO usermanagement;
GRANT UNLIMITED TABLESPACE TO usermanagement;
```

3. Run the schema script:
```sql
-- If using SQL*Plus
@src/main/resources/db/schema.sql

-- Or copy/paste the contents of the file
```

4. Verify tables created:
```sql
SELECT table_name FROM user_tables;
SELECT * FROM USERS;
```

You should see 3 sample users (admin, john_doe, jane_smith)

---

## 3. Backend Setup

### 3.1 Clone or Download Project

```bash
git clone <repository-url>
cd spring-exch
```

### 3.2 Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
# Update these values for your Oracle setup

# For Oracle XE:
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=SYSTEM
spring.datasource.password=YourOraclePassword

# For Oracle with service name:
# spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1

# For Docker Oracle:
# spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XE
# spring.datasource.username=SYSTEM
# spring.datasource.password=YourDockerPassword
```

**Important Configuration Options:**

```properties
# For development (updates schema automatically)
spring.jpa.hibernate.ddl-auto=update

# For production (doesn't modify database)
# spring.jpa.hibernate.ddl-auto=validate

# JWT Secret (CHANGE IN PRODUCTION!)
app.jwt.secret=YourSuperSecretKeyForJWTTokenGenerationMustBeAtLeast256Bits

# Token expiration (24 hours in milliseconds)
app.jwt.expiration=86400000
```

### 3.3 Build and Run Backend

#### Using Maven Command Line:

```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run
```

#### Using IDE (IntelliJ IDEA):

1. Open project folder
2. Wait for Maven to download dependencies
3. Find `UserManagementApplication.java`
4. Right-click → Run

#### Using IDE (Eclipse):

1. Import as Maven project
2. Update project (Alt+F5)
3. Find `UserManagementApplication.java`
4. Right-click → Run As → Java Application

### 3.4 Verify Backend is Running

1. Check console output:
```
========================================
User Management System Started!
API Documentation: http://localhost:8080/swagger-ui.html
========================================
```

2. Open browser: http://localhost:8080/swagger-ui.html

3. Test health endpoint:
   - Find "Authentication" section
   - Try `GET /api/auth/health`
   - Click "Try it out" → "Execute"
   - Should return: `{"success": true, "message": "Authentication service is running"}`

---

## 4. Frontend Setup

You have **TWO options** for the frontend. Choose one or try both!

### Option A: React Frontend

#### 4.1 Navigate to React folder:
```bash
cd frontend-react
```

#### 4.2 Install dependencies:
```bash
npm install
```

This will install:
- React 18
- React Router 6
- Axios
- Vite

#### 4.3 Run development server:
```bash
npm run dev
```

#### 4.4 Access application:
Open browser: http://localhost:3000

#### 4.5 Build for production (optional):
```bash
npm run build
```

### Option B: Pure HTML/CSS/JavaScript Frontend

#### 4.1 Navigate to HTML folder:
```bash
cd frontend-html
```

#### 4.2 Run using Live Server (VS Code):
1. Install "Live Server" extension in VS Code
2. Right-click `index.html`
3. Select "Open with Live Server"

#### 4.3 Or use Python HTTP server:
```bash
python -m http.server 5500
```

#### 4.4 Or use Node.js http-server:
```bash
npx http-server -p 5500
```

#### 4.5 Access application:
Open browser: http://localhost:5500

---

## 5. Testing the Application

### 5.1 Test Login

1. Open frontend (React or HTML)
2. Use default credentials:
   - **Admin**: username: `admin`, password: `admin123`
   - **User**: username: `john_doe`, password: `password123`

3. You should be redirected to dashboard

### 5.2 Test Registration

1. Click "Register here"
2. Fill in form:
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `password123`
   - First Name: `Test`
   - Last Name: `User`

3. Click Register
4. You should be logged in automatically

### 5.3 Test User Management (Admin Only)

1. Login as admin
2. Click "Manage Users"
3. You should see all users
4. Try deleting a user (be careful!)
5. The user should be removed from the list

### 5.4 Test API with Swagger

1. Open: http://localhost:8080/swagger-ui.html
2. Test login endpoint:
   - Expand "Authentication" → "POST /api/auth/login"
   - Click "Try it out"
   - Enter credentials:
   ```json
   {
     "usernameOrEmail": "admin",
     "password": "admin123"
   }
   ```
   - Click "Execute"
   - Copy the `token` from response

3. Authorize Swagger:
   - Click "Authorize" button (top right)
   - Enter: `Bearer YOUR_TOKEN_HERE`
   - Click "Authorize" → "Close"

4. Now you can test protected endpoints!

### 5.5 Test Different User Roles

1. Login as regular user (`john_doe` / `password123`)
2. Try to access user management:
   - In React: Should see users but no delete button
   - In API: DELETE endpoints should return 403 Forbidden

3. Login as admin:
   - Should have full access to all features

---

## 6. Troubleshooting

### 6.1 Backend Issues

#### "Could not connect to database"
- Verify Oracle is running:
  ```bash
  # Check Oracle service status
  # Windows: Check Services.msc for OracleService
  # Docker: docker ps | grep oracle
  ```
- Verify connection details in `application.properties`
- Test connection with SQL*Plus or SQL Developer

#### "Port 8080 already in use"
- Change port in `application.properties`:
  ```properties
  server.port=8081
  ```
- Update frontend API URL accordingly

#### "Table or view does not exist"
- Run the database schema script
- Check user permissions
- Verify `spring.jpa.hibernate.ddl-auto=update`

#### "JWT secret key is too short"
- Ensure `app.jwt.secret` is at least 256 bits (32 characters)

### 6.2 Frontend Issues (React)

#### "npm install" fails
- Clear npm cache: `npm cache clean --force`
- Delete `node_modules` and `package-lock.json`
- Try again: `npm install`

#### "Module not found" errors
- Ensure all dependencies installed: `npm install`
- Check import paths in files
- Restart dev server: `npm run dev`

#### "CORS error" in browser console
- Verify backend CORS configuration in `SecurityConfig.java`
- Check that frontend URL is in `allowedOrigins`
- Ensure backend is running on correct port

### 6.3 Frontend Issues (HTML)

#### API calls failing
- Check browser console for errors
- Verify `API_BASE_URL` in `app.js` matches backend URL
- Ensure backend is running

#### CORS error
- Backend must allow `http://localhost:5500` (or your port)
- Check `SecurityConfig.java` CORS configuration

### 6.4 Authentication Issues

#### "Invalid username or password"
- Verify users exist in database: `SELECT * FROM USERS;`
- Check that passwords in database are BCrypt hashes
- Try re-running database schema script

#### "Token expired" or "Unauthorized"
- Tokens expire after 24 hours (default)
- Logout and login again
- Check `app.jwt.expiration` in properties

#### Cannot access protected endpoints
- Verify token is being sent in Authorization header
- Check token format: `Bearer <token>`
- Ensure user has required role (ADMIN vs USER)

### 6.5 Database Issues

#### Oracle listener not available
```bash
# Start Oracle listener
lsnrctl start

# Check status
lsnrctl status
```

#### "ORA-12154: TNS:could not resolve the connect identifier"
- Verify connection string format
- Check Oracle service name
- Ensure port 1521 is correct

#### "ORA-01017: invalid username/password"
- Verify database credentials
- Check if user account is locked
- Try connecting with SQL*Plus to confirm credentials

---

## 7. Next Steps

### Learn by Doing

1. **Modify Code**
   - Add a new field to User entity (e.g., phone number validation)
   - Create a new endpoint for user statistics
   - Add pagination to user list

2. **Enhance Security**
   - Implement password strength validation
   - Add email verification
   - Implement refresh tokens

3. **Improve UI**
   - Add more styling
   - Implement form validation feedback
   - Add loading indicators

4. **Add Features**
   - User profile editing
   - Password reset functionality
   - User activity logging

### Resources for Learning

- **Spring Boot**: https://spring.io/guides
- **Spring Security**: https://spring.io/guides/topicals/spring-security-architecture
- **JPA/Hibernate**: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
- **Oracle SQL**: https://docs.oracle.com/en/database/
- **React**: https://react.dev/learn
- **JWT**: https://jwt.io/introduction

---

## 8. Production Deployment Checklist

Before deploying to production:

- [ ] Change default passwords
- [ ] Set strong JWT secret (use environment variable)
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` or `none`
- [ ] Enable HTTPS
- [ ] Configure proper CORS origins
- [ ] Set up database backups
- [ ] Implement proper logging
- [ ] Add monitoring and health checks
- [ ] Use production-grade database connection pool
- [ ] Implement rate limiting
- [ ] Add comprehensive error handling
- [ ] Set up CI/CD pipeline
- [ ] Configure environment-specific profiles
- [ ] Review and test security measures
- [ ] Implement refresh token mechanism
- [ ] Add API versioning

---

**Congratulations!** You now have a fully functional User Management System.

For questions or issues, review:
- This SETUP_GUIDE.md
- README.md for project overview
- Code comments (every file is heavily commented)
- Swagger UI for API documentation

**Happy Coding! 🚀**
