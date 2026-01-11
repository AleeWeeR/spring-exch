/**
 * Main Entry Point for React Application
 *
 * This file initializes the React application and renders it to the DOM.
 *
 * Educational Purpose:
 * - Demonstrates React 18 createRoot API
 * - Shows how to mount React app to DOM
 * - Illustrates React Router setup
 */

import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './styles/global.css'

// Create root and render app
// React 18+ uses createRoot instead of render
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
