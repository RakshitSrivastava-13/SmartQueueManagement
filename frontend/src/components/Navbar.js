import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const location = useLocation();

  const isActive = (path) => {
    return location.pathname === path ? 'nav-link active' : 'nav-link';
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          <span className="brand-icon">🏥</span>
          <span className="brand-text">SmartQueue</span>
        </Link>

        <div className="navbar-links">
          <Link to="/" className={isActive('/')}>
            Home
          </Link>
          <Link to="/register" className={isActive('/register')}>
            Get Token
          </Link>
          <Link to="/queue-board" className={isActive('/queue-board')}>
            Live Queue
          </Link>
          <Link to="/staff" className={isActive('/staff')}>
            Staff Portal
          </Link>
        </div>

        <div className="navbar-actions">
          <span className="current-time">
            {new Date().toLocaleTimeString('en-US', {
              hour: '2-digit',
              minute: '2-digit',
            })}
          </span>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
