import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { staffAPI, doctorAPI, setStaffCredentials, clearStaffCredentials, isAuthenticated } from '../services/api';
import './StaffDashboard.css';

const StaffDashboard = () => {
  const navigate = useNavigate();
  const [authenticated, setAuthenticated] = useState(isAuthenticated());
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [loginError, setLoginError] = useState('');
  const [loading, setLoading] = useState(false);

  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [queue, setQueue] = useState([]);
  const [stats, setStats] = useState(null);
  const [activeConsultations, setActiveConsultations] = useState([]);

  const fetchDoctors = async () => {
    try {
      const response = await doctorAPI.getAvailable();
      setDoctors(response.data.data || []);
      if (response.data.data?.length > 0 && !selectedDoctor) {
        setSelectedDoctor(response.data.data[0]);
      }
    } catch (err) {
      console.error('Failed to fetch doctors:', err);
    }
  };

  const fetchQueue = useCallback(async () => {
    if (!selectedDoctor) return;
    try {
      const response = await staffAPI.getDoctorQueue(selectedDoctor.id);
      setQueue(response.data.data || []);
    } catch (err) {
      console.error('Failed to fetch queue:', err);
    }
  }, [selectedDoctor]);

  const fetchStats = async () => {
    try {
      const response = await staffAPI.getDashboard();
      setStats(response.data.data);
    } catch (err) {
      console.error('Failed to fetch stats:', err);
    }
  };

  const fetchActiveConsultations = async () => {
    try {
      const response = await staffAPI.getActiveConsultations();
      setActiveConsultations(response.data.data || []);
    } catch (err) {
      console.error('Failed to fetch active consultations:', err);
    }
  };

  useEffect(() => {
    if (authenticated) {
      fetchDoctors();
      fetchStats();
      fetchActiveConsultations();
    }
  }, [authenticated]);

  useEffect(() => {
    if (authenticated && selectedDoctor) {
      fetchQueue();
      const interval = setInterval(fetchQueue, 5000);
      return () => clearInterval(interval);
    }
  }, [authenticated, selectedDoctor, fetchQueue]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setLoginError('');

    try {
      setStaffCredentials(loginForm.username, loginForm.password);
      // Test the credentials
      await staffAPI.getDashboard();
      setAuthenticated(true);
    } catch (err) {
      clearStaffCredentials();
      setLoginError('Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    clearStaffCredentials();
    setAuthenticated(false);
    setSelectedDoctor(null);
    setQueue([]);
    setStats(null);
  };

  const handleCallNext = async () => {
    if (!selectedDoctor) return;
    try {
      await staffAPI.callNext(selectedDoctor.id);
      fetchQueue();
      fetchStats();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to call next patient');
    }
  };

  const handleStartConsultation = async (tokenId) => {
    try {
      await staffAPI.startConsultation(tokenId);
      fetchQueue();
      fetchStats();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to start consultation');
    }
  };

  const handleEndConsultation = async (tokenId) => {
    try {
      await staffAPI.endConsultation(tokenId);
      fetchQueue();
      fetchStats();
      fetchActiveConsultations();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to end consultation');
    }
  };

  const handleCancelConsultation = async (tokenId) => {
    if (window.confirm('Are you sure you want to cancel this consultation?')) {
      try {
        await staffAPI.cancelConsultation(tokenId);
        fetchQueue();
        fetchStats();
        fetchActiveConsultations();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to cancel consultation');
      }
    }
  };

  const handleNoShow = async (tokenId) => {
    if (window.confirm('Mark this patient as no-show?')) {
      try {
        await staffAPI.markNoShow(tokenId);
        fetchQueue();
        fetchStats();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to mark no-show');
      }
    }
  };

  const handleSkip = async (tokenId) => {
    try {
      await staffAPI.skip(tokenId);
      fetchQueue();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to skip patient');
    }
  };

  const getStatusBadge = (status) => {
    const badges = {
      'WAITING': 'badge-waiting',
      'CALLED': 'badge-called',
      'IN_CONSULTATION': 'badge-in-consultation',
      'COMPLETED': 'badge-completed',
      'CANCELLED': 'badge-cancelled',
      'NO_SHOW': 'badge-cancelled'
    };
    return badges[status] || '';
  };

  // Login Form
  if (!authenticated) {
    return (
      <div className="staff-dashboard">
        <div className="login-container">
          <div className="login-card">
            <div className="login-header">
              <h2>👨‍⚕️ Staff Portal</h2>
              <p>Login to access the dashboard</p>
            </div>
            <form onSubmit={handleLogin} className="login-form">
              {loginError && <div className="alert alert-error">{loginError}</div>}
              <div className="form-group">
                <label className="form-label">Username</label>
                <input
                  type="text"
                  className="form-input"
                  value={loginForm.username}
                  onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })}
                  placeholder="Enter username"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Password</label>
                <input
                  type="password"
                  className="form-input"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                  placeholder="Enter password"
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary btn-lg full-width" disabled={loading}>
                {loading ? 'Logging in...' : 'Login'}
              </button>
            </form>
            <div className="login-hint">
              <p>Demo credentials:</p>
              <p><strong>admin</strong> / admin123</p>
              <p><strong>doctor1</strong> / doctor123</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const currentToken = queue.find(t => t.status === 'IN_CONSULTATION' || t.status === 'CALLED');
  const waitingTokens = queue.filter(t => t.status === 'WAITING');

  return (
    <div className="staff-dashboard">
      {/* Dashboard Header */}
      <div className="dashboard-header">
        <div className="header-left">
          <h1>Staff Dashboard</h1>
          <p>Manage patient queues and consultations</p>
        </div>
        <div className="header-right">
          <select
            className="doctor-select"
            value={selectedDoctor?.id || ''}
            onChange={(e) => {
              const doctor = doctors.find(d => d.id === parseInt(e.target.value));
              setSelectedDoctor(doctor);
            }}
          >
            {doctors.map((doctor) => (
              <option key={doctor.id} value={doctor.id}>
                Dr. {doctor.fullName} - {doctor.departmentName}
              </option>
            ))}
          </select>
          <button className="btn btn-secondary" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-value">{stats.totalPatientsToday}</div>
            <div className="stat-label">Total Today</div>
          </div>
          <div className="stat-card waiting">
            <div className="stat-value">{stats.totalWaiting}</div>
            <div className="stat-label">Waiting</div>
          </div>
          <div className="stat-card in-progress">
            <div className="stat-value">{stats.totalInConsultation}</div>
            <div className="stat-label">In Consultation</div>
          </div>
          <div className="stat-card completed">
            <div className="stat-value">{stats.totalCompleted}</div>
            <div className="stat-label">Completed</div>
          </div>
        </div>
      )}

      {/* Active Consultations Banner - Shows when there are active consultations */}
      {activeConsultations.length > 0 && (
        <div className="active-consultations-panel">
          <div className="panel-header">
            <h3>🔴 Active Consultations ({activeConsultations.length})</h3>
            <p>These consultations must be ended before calling new patients</p>
          </div>
          <div className="active-consultations-list">
            {activeConsultations.map((token) => (
              <div key={token.id} className="active-consultation-item">
                <div className="consultation-info">
                  <div className="consultation-token">{token.tokenNumber}</div>
                  <div className="consultation-details">
                    <span className="patient-name">{token.patientName}</span>
                    <span className="doctor-name">Dr. {token.doctorName}</span>
                    <span className={`status-tag ${token.status.toLowerCase()}`}>
                      {token.status.replace('_', ' ')}
                    </span>
                  </div>
                </div>
                <div className="consultation-actions">
                  <button
                    className="btn btn-success btn-sm"
                    onClick={() => handleEndConsultation(token.id)}
                  >
                    ✓ End Consultation
                  </button>
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={() => handleCancelConsultation(token.id)}
                  >
                    ✕ Cancel
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="dashboard-content">
        {/* Current Patient Panel */}
        <div className="current-patient-panel">
          <h2>Current Patient</h2>
          {currentToken ? (
            <div className="current-patient-card">
              <div className="patient-token">{currentToken.tokenNumber}</div>
              <div className="patient-details">
                <h3>{currentToken.patientName}</h3>
                <p>📞 {currentToken.patientPhone}</p>
                <p>🏥 {currentToken.departmentName}</p>
                {currentToken.priority !== 'NORMAL' && (
                  <span className={`priority-tag priority-${currentToken.priority.toLowerCase()}`}>
                    {currentToken.priority}
                  </span>
                )}
              </div>
              <div className={`patient-status ${getStatusBadge(currentToken.status)}`}>
                {currentToken.status.replace('_', ' ')}
              </div>
              <div className="patient-actions">
                {currentToken.status === 'CALLED' && (
                  <>
                    <button
                      className="btn btn-success"
                      onClick={() => handleStartConsultation(currentToken.id)}
                    >
                      Start Consultation
                    </button>
                    <button
                      className="btn btn-warning"
                      onClick={() => handleNoShow(currentToken.id)}
                    >
                      No Show
                    </button>
                    <button
                      className="btn btn-secondary"
                      onClick={() => handleSkip(currentToken.id)}
                    >
                      Skip
                    </button>
                  </>
                )}
                {currentToken.status === 'IN_CONSULTATION' && (
                  <button
                    className="btn btn-primary btn-lg"
                    onClick={() => handleEndConsultation(currentToken.id)}
                  >
                    End Consultation
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="no-current-patient">
              <div className="empty-icon">👤</div>
              <p>No patient being served</p>
              <button className="btn btn-primary btn-lg" onClick={handleCallNext}>
                Call Next Patient
              </button>
            </div>
          )}
        </div>

        {/* Waiting Queue Panel */}
        <div className="waiting-queue-panel">
          <div className="panel-header">
            <h2>Waiting Queue ({waitingTokens.length})</h2>
            {!currentToken && waitingTokens.length > 0 && (
              <button className="btn btn-primary" onClick={handleCallNext}>
                Call Next
              </button>
            )}
          </div>
          {waitingTokens.length === 0 ? (
            <div className="empty-queue">
              <p>No patients waiting</p>
            </div>
          ) : (
            <div className="queue-list">
              {waitingTokens.map((token, index) => (
                <div key={token.id} className="queue-item">
                  <div className="queue-position">{index + 1}</div>
                  <div className="queue-info">
                    <div className="queue-token">{token.tokenNumber}</div>
                    <div className="queue-patient">{token.patientName}</div>
                    <div className="queue-time">
                      {new Date(token.generatedAt).toLocaleTimeString()}
                    </div>
                  </div>
                  {token.priority !== 'NORMAL' && (
                    <span className={`priority-badge priority-${token.priority.toLowerCase()}`}>
                      {token.priority === 'EMERGENCY' ? '🚨' : 
                       token.priority === 'PREGNANT' ? '🤰' : '👴'}
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default StaffDashboard;
