/*
 * Vite Configuration
 *
 * Vite is a modern build tool for frontend development.
 * It provides fast development server and optimized production builds.
 *
 * Educational Purpose:
 * - Shows how to configure Vite for React
 * - Demonstrates proxy setup for API calls
 * - Explains development server configuration
 */

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  // React plugin for Vite
  plugins: [react()],

  // Development server configuration
  server: {
    port: 3000,  // Run dev server on port 3000

    // Proxy API requests to backend
    // This avoids CORS issues during development
    // Requests to /api/* will be forwarded to http://localhost:8080
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
