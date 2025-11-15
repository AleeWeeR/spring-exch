# User Management System - Pure HTML/CSS/JS Frontend

Simple, educational frontend using vanilla JavaScript, HTML, and CSS.

## Features

- Login and registration
- User dashboard
- User list and management
- No dependencies - pure JavaScript!
- Modern, clean design

## How to Run

### Option 1: Live Server (VS Code)

1. Install "Live Server" extension in VS Code
2. Right-click on `index.html`
3. Select "Open with Live Server"
4. Access at: http://localhost:5500

### Option 2: Python HTTP Server

```bash
python -m http.server 5500
```

Then open: http://localhost:5500

### Option 3: Node.js HTTP Server

```bash
npx http-server -p 5500
```

Then open: http://localhost:5500

## Default Login Credentials

- **Admin**: username: `admin`, password: `admin123`
- **User**: username: `john_doe`, password: `password123`

## Project Structure

```
frontend-html/
├── index.html    # Main HTML file
├── styles.css    # CSS styling
├── app.js        # JavaScript logic
└── README.md     # This file
```

## CORS Configuration

Make sure the Spring Boot backend has CORS configured to allow:
- Origin: http://localhost:5500 (or your server port)

This is already configured in the backend SecurityConfig.

## Learning Points

- **Vanilla JavaScript**: No frameworks, pure JS
- **Fetch API**: Modern way to make HTTP requests
- **Local Storage**: Client-side storage for tokens
- **JWT Authentication**: Token-based auth
- **REST API Integration**: Calling backend endpoints
- **DOM Manipulation**: Dynamically updating UI
- **Event Handling**: Form submissions and user interactions
