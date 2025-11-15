import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { userService } from '../services/api';

export default function UserList({ user }) {
  const [users, setUsers] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      const response = await userService.getAllUsers();
      setUsers(response.data);
    } catch (error) {
      console.error('Failed to load users:', error);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        await userService.deleteUser(id);
        loadUsers();
      } catch (error) {
        alert('Failed to delete user');
      }
    }
  };

  return (
    <div>
      <div className="navbar">
        <h2>User Management</h2>
        <button onClick={() => navigate('/dashboard')} className="btn btn-secondary">Back to Dashboard</button>
      </div>
      <div className="container">
        <div className="card">
          <h3>All Users</h3>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                {user?.role === 'ADMIN' && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.username}</td>
                  <td>{u.email}</td>
                  <td>{u.role}</td>
                  <td>{u.isActive ? 'Active' : 'Inactive'}</td>
                  {user?.role === 'ADMIN' && (
                    <td>
                      <button onClick={() => handleDelete(u.id)} className="btn btn-danger" style={{ padding: '6px 12px' }}>
                        Delete
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
