# User Management System - React Frontend

Educational React frontend for learning modern web development.

## Features

- User login and registration
- Dashboard with user info
- User list and management (CRUD)
- Role-based access control
- JWT token authentication

## Technologies

- React 18
- React Router 6
- Axios for API calls
- Vite for fast development

## Setup

1. Install dependencies:
```bash
npm install
```

2. Run development server:
```bash
npm run dev
```

3. Access at: http://localhost:3000

## Build for Production

```bash
npm run build
```

## Default Login

- **Admin**: username: `admin`, password: `admin123`
- **User**: username: `john_doe`, password: `password123`

## Project Structure

```
frontend-react/
├── src/
│   ├── components/      # React components
│   ├── services/        # API services
│   ├── styles/          # CSS styles
│   ├── App.jsx          # Main app component
│   └── main.jsx         # Entry point
├── index.html
├── package.json
└── vite.config.js
```
