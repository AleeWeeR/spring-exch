/**
 * API Service - Centralized HTTP client
 *
 * This module provides functions to interact with the backend API.
 * It uses axios for HTTP requests and handles authentication automatically.
 *
 * Educational Purpose:
 * - Demonstrates axios configuration
 * - Shows how to attach JWT tokens to requests
 * - Illustrates centralized API error handling
 * - Explains authentication flow in frontend
 *
 * Base URL: http://localhost:8080/api
 */

import axios from 'axios';

// Base URL for API (change if backend runs on different host/port)
const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance with default configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - adds JWT token to every request
api.interceptors.request.use(
  (config) => {
    // Get token from localStorage
    const token = localStorage.getItem('token');

    // If token exists, add it to Authorization header
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handles errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // If 401 Unauthorized, clear token and redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ========== AUTH SERVICES ==========

export const authService = {
  // Login user
  login: async (usernameOrEmail, password) => {
    const response = await api.post('/auth/login', { usernameOrEmail, password });
    return response.data;
  },

  // Register new user
  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },
};

// ========== USER SERVICES ==========

export const userService = {
  // Get all users (Admin only)
  getAllUsers: async () => {
    const response = await api.get('/users');
    return response.data;
  },

  // Get user by ID
  getUserById: async (id) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },

  // Get current user
  getCurrentUser: async () => {
    const response = await api.get('/users/me');
    return response.data;
  },

  // Search users
  searchUsers: async (searchTerm) => {
    const response = await api.get(`/users/search?q=${searchTerm}`);
    return response.data;
  },

  // Create user (Admin only)
  createUser: async (userData) => {
    const response = await api.post('/users', userData);
    return response.data;
  },

  // Update user
  updateUser: async (id, userData) => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data;
  },

  // Delete user (Admin only)
  deleteUser: async (id) => {
    const response = await api.delete(`/users/${id}`);
    return response.data;
  },

  // Get user statistics
  getUserStatistics: async () => {
    const response = await api.get('/users/statistics');
    return response.data;
  },
};

export default api;
