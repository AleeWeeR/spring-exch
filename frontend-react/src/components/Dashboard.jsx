import { useNavigate, Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { userService } from '../services/api';

export default function Dashboard({ user }) {
  const [stats, setStats] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      userService.getUserStatistics()
        .then(res => setStats(res.data))
        .catch(err => console.error(err));
    }
  }, [user]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div>
      <div className="navbar">
        <h2>User Management System</h2>
        <div className="user-info">
          <span>Welcome, {user?.username || 'User'}</span>
          <button onClick={handleLogout} className="btn btn-secondary">Logout</button>
        </div>
      </div>
      <div className="container">
        <div className="card">
          <h3>Dashboard</h3>
          <p><strong>Role:</strong> {user?.role}</p>
          <p><strong>Email:</strong> {user?.email}</p>
          {user?.firstName && <p><strong>Name:</strong> {user.firstName} {user.lastName}</p>}
          <div className="mt-20">
            <Link to="/users" className="btn btn-primary">Manage Users</Link>
          </div>
        </div>
        {user?.role === 'ADMIN' && stats && (
          <div className="card">
            <h3>User Statistics</h3>
            <p>Total Users: {stats.totalUsers}</p>
            <p>Admins: {stats.adminCount}</p>
            <p>Regular Users: {stats.userCount}</p>
            <p>Active Users: {stats.activeUsers}</p>
          </div>
        )}
      </div>
    </div>
  );
}
