/**
 * Pure JavaScript Frontend - User Management System
 *
 * This file contains all the JavaScript logic for the application.
 * It uses vanilla JavaScript (no frameworks) to demonstrate core concepts.
 */

// API Base URL - change if backend runs on different host/port
const API_BASE_URL = 'http://localhost:8080/api';

// Current user data
let currentUser = null;

// ========== UTILITY FUNCTIONS ==========

function showPage(pageId) {
  document.querySelectorAll('.page').forEach(page => page.style.display = 'none');
  document.getElementById(pageId).style.display = 'block';
}

function getToken() {
  return localStorage.getItem('token');
}

function setToken(token) {
  localStorage.setItem('token', token);
}

function removeToken() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}

async function apiCall(endpoint, method = 'GET', body = null) {
  const headers = {
    'Content-Type': 'application/json',
  };

  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const options = {
    method,
    headers,
  };

  if (body) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || 'Request failed');
  }

  return data;
}

// ========== AUTHENTICATION ==========

document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const usernameOrEmail = document.getElementById('loginUsername').value;
  const password = document.getElementById('loginPassword').value;

  try {
    const response = await apiCall('/auth/login', 'POST', { usernameOrEmail, password });
    setToken(response.data.token);
    currentUser = response.data.user;
    localStorage.setItem('user', JSON.stringify(currentUser));
    loadDashboard();
  } catch (error) {
    document.getElementById('loginError').textContent = error.message;
    document.getElementById('loginError').style.display = 'block';
  }
});

document.getElementById('registerForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const userData = {
    username: document.getElementById('regUsername').value,
    email: document.getElementById('regEmail').value,
    password: document.getElementById('regPassword').value,
    firstName: document.getElementById('regFirstName').value,
    lastName: document.getElementById('regLastName').value,
  };

  try {
    const response = await apiCall('/auth/register', 'POST', userData);
    setToken(response.data.token);
    currentUser = response.data.user;
    localStorage.setItem('user', JSON.stringify(currentUser));
    loadDashboard();
  } catch (error) {
    document.getElementById('registerError').textContent = error.message;
    document.getElementById('registerError').style.display = 'block';
  }
});

function logout() {
  removeToken();
  currentUser = null;
  showPage('loginPage');
}

// ========== DASHBOARD ==========

async function loadDashboard() {
  showPage('dashboardPage');
  document.getElementById('welcomeText').textContent = `Welcome, ${currentUser.username}`;

  let details = `<p><strong>Role:</strong> ${currentUser.role}</p>`;
  details += `<p><strong>Email:</strong> ${currentUser.email}</p>`;
  if (currentUser.firstName) {
    details += `<p><strong>Name:</strong> ${currentUser.firstName} ${currentUser.lastName || ''}</p>`;
  }
  document.getElementById('userDetails').innerHTML = details;

  if (currentUser.role === 'ADMIN') {
    try {
      const response = await apiCall('/users/statistics');
      const stats = response.data;
      document.getElementById('statsCard').style.display = 'block';
      document.getElementById('stats').innerHTML = `
        <p>Total Users: ${stats.totalUsers}</p>
        <p>Admins: ${stats.adminCount}</p>
        <p>Regular Users: ${stats.userCount}</p>
        <p>Active Users: ${stats.activeUsers}</p>
      `;
    } catch (error) {
      console.error('Failed to load statistics:', error);
    }
  }
}

// ========== USERS LIST ==========

async function loadUsers() {
  try {
    const response = await apiCall('/users');
    const users = response.data;
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '';

    if (currentUser.role === 'ADMIN') {
      document.getElementById('actionsHeader').style.display = '';
    }

    users.forEach(user => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${user.id}</td>
        <td>${user.username}</td>
        <td>${user.email}</td>
        <td>${user.role}</td>
        <td>${user.isActive ? 'Active' : 'Inactive'}</td>
        ${currentUser.role === 'ADMIN' ? `
          <td>
            <button onclick="deleteUser(${user.id})" class="btn btn-danger">Delete</button>
          </td>
        ` : ''}
      `;
      tbody.appendChild(tr);
    });
  } catch (error) {
    console.error('Failed to load users:', error);
  }
}

async function deleteUser(id) {
  if (confirm('Are you sure you want to delete this user?')) {
    try {
      await apiCall(`/users/${id}`, 'DELETE');
      loadUsers();
    } catch (error) {
      alert('Failed to delete user: ' + error.message);
    }
  }
}

// ========== INITIALIZATION ==========

window.addEventListener('DOMContentLoaded', () => {
  const token = getToken();
  const savedUser = localStorage.getItem('user');

  if (token && savedUser) {
    currentUser = JSON.parse(savedUser);
    loadDashboard();
  } else {
    showPage('loginPage');
  }
});
