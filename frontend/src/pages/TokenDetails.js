import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { tokenAPI } from '../services/api';
import './TokenDetails.css';

const TokenDetails = () => {
  const { tokenNumber } = useParams();
  const navigate = useNavigate();
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentTime, setCurrentTime] = useState(new Date());
  const [lastUpdated, setLastUpdated] = useState(null);

  const fetchTokenDetails = useCallback(async () => {
    try {
      const response = await tokenAPI.getQueuePosition(tokenNumber);
      setToken(response.data.data);
      setLastUpdated(new Date());
      setError('');
    } catch (err) {
      setError('Token not found or expired');
    } finally {
      setLoading(false);
    }
  }, [tokenNumber]);

  useEffect(() => {
    fetchTokenDetails();
    
    // Auto-refresh every 5 seconds for real-time updates (like train tracking)
    const interval = setInterval(fetchTokenDetails, 5000);
    return () => clearInterval(interval);
  }, [fetchTokenDetails]);

  // Update current time every second for live countdown
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // Format estimated service time
  const formatServiceTime = (estimatedServiceTime) => {
    if (!estimatedServiceTime) return null;
    const date = new Date(estimatedServiceTime);
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit', 
      second: '2-digit',
      hour12: true 
    });
  };

  // Calculate time remaining until service
  const getTimeRemaining = (estimatedServiceTime) => {
    if (!estimatedServiceTime) return null;
    const serviceTime = new Date(estimatedServiceTime);
    const diffMs = serviceTime - currentTime;
    
    if (diffMs <= 0) return { text: 'Any moment now!', urgent: true };
    
    const diffMins = Math.floor(diffMs / 60000);
    const diffSecs = Math.floor((diffMs % 60000) / 1000);
    
    if (diffMins < 1) {
      return { text: `${diffSecs} seconds`, urgent: true };
    } else if (diffMins < 5) {
      return { text: `${diffMins}m ${diffSecs}s`, urgent: true };
    } else {
      return { text: `${diffMins} min ${diffSecs} sec`, urgent: false };
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'WAITING': return 'status-waiting';
      case 'CALLED': return 'status-called';
      case 'IN_CONSULTATION': return 'status-in-consultation';
      case 'COMPLETED': return 'status-completed';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  };

  const getStatusMessage = (status) => {
    switch (status) {
      case 'WAITING':
        return 'Please wait. We will call you when it\'s your turn.';
      case 'CALLED':
        return '🔔 Your turn! Please proceed to the consultation room.';
      case 'IN_CONSULTATION':
        return 'Consultation in progress...';
      case 'COMPLETED':
        return 'Consultation completed. Thank you for visiting!';
      case 'CANCELLED':
        return 'This token has been cancelled.';
      default:
        return '';
    }
  };

  const handleCancelToken = async () => {
    if (window.confirm('Are you sure you want to cancel this token?')) {
      try {
        await tokenAPI.cancel(token.id);
        fetchTokenDetails();
      } catch (err) {
        setError('Failed to cancel token');
      }
    }
  };

  if (loading) {
    return (
      <div className="token-details-page">
        <div className="loading">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="token-details-page">
        <div className="error-container">
          <div className="error-icon">❌</div>
          <h2>Token Not Found</h2>
          <p>{error}</p>
          <button className="btn btn-primary" onClick={() => navigate('/register')}>
            Get New Token
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="token-details-page">
      <div className="token-card">
        {/* Token Header */}
        <div className={`token-header ${getStatusClass(token?.status)}`}>
          <div className="token-status">
            <span className={`status-badge ${getStatusClass(token?.status)}`}>
              {token?.status?.replace('_', ' ')}
            </span>
          </div>
          <div className="token-number-display">
            <span className="label">Token Number</span>
            <span className="number">{token?.tokenNumber}</span>
          </div>
        </div>

        {/* Status Message */}
        <div className={`status-message ${getStatusClass(token?.status)}`}>
          {getStatusMessage(token?.status)}
        </div>

        {/* Queue Position - Only show for waiting tokens */}
        {token?.status === 'WAITING' && (
          <div className="queue-info">
            <div className="queue-position-card">
              <span className="position-label">Your Position</span>
              <span className="position-number">{token?.queuePosition || '-'}</span>
            </div>
            
            {/* Estimated Service Time - Like train tracking */}
            {token?.estimatedServiceTime && (
              <div className="estimated-service-time">
                <div className="service-time-header">
                  <span className="service-time-icon">🕐</span>
                  <span className="service-time-label">Expected Service Time</span>
                </div>
                <div className="service-time-value">
                  {formatServiceTime(token.estimatedServiceTime)}
                </div>
                <div className={`time-remaining ${getTimeRemaining(token.estimatedServiceTime)?.urgent ? 'urgent' : ''}`}>
                  <span className="countdown-label">Time Remaining:</span>
                  <span className="countdown-value">{getTimeRemaining(token.estimatedServiceTime)?.text}</span>
                </div>
              </div>
            )}
            
            <div className="queue-stats">
              <div className="stat">
                <span className="stat-value">{token?.patientsAhead || 0}</span>
                <span className="stat-label">Patients Ahead</span>
              </div>
              <div className="stat">
                <span className="stat-value">~{token?.estimatedWaitMinutes || 0} min</span>
                <span className="stat-label">Est. Wait</span>
              </div>
            </div>
          </div>
        )}

        {/* Token Details */}
        <div className="token-info-grid">
          <div className="info-item">
            <span className="info-label">Department</span>
            <span className="info-value">{token?.departmentName}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Doctor</span>
            <span className="info-value">
              {token?.doctorName ? `Dr. ${token.doctorName}` : 'Any Available'}
            </span>
          </div>
          <div className="info-item">
            <span className="info-label">Room</span>
            <span className="info-value">{token?.roomNumber || '-'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Priority</span>
            <span className={`priority-badge priority-${token?.priority?.toLowerCase()}`}>
              {token?.priority}
            </span>
          </div>
          <div className="info-item">
            <span className="info-label">Patient</span>
            <span className="info-value">{token?.patientName}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Generated At</span>
            <span className="info-value">
              {token?.generatedAt && new Date(token.generatedAt).toLocaleTimeString()}
            </span>
          </div>
        </div>

        {/* Timestamps */}
        {(token?.calledAt || token?.consultationStartedAt || token?.consultationEndedAt) && (
          <div className="timestamps">
            {token?.calledAt && (
              <div className="timestamp-item">
                <span className="ts-label">Called At:</span>
                <span className="ts-value">{new Date(token.calledAt).toLocaleTimeString()}</span>
              </div>
            )}
            {token?.consultationStartedAt && (
              <div className="timestamp-item">
                <span className="ts-label">Consultation Started:</span>
                <span className="ts-value">{new Date(token.consultationStartedAt).toLocaleTimeString()}</span>
              </div>
            )}
            {token?.consultationEndedAt && (
              <div className="timestamp-item">
                <span className="ts-label">Consultation Ended:</span>
                <span className="ts-value">{new Date(token.consultationEndedAt).toLocaleTimeString()}</span>
              </div>
            )}
          </div>
        )}

        {/* Actions */}
        <div className="token-actions">
          {token?.status === 'WAITING' && (
            <button className="btn btn-danger" onClick={handleCancelToken}>
              Cancel Token
            </button>
          )}
          <button className="btn btn-secondary" onClick={() => navigate('/queue-board')}>
            View Live Queue
          </button>
          <button className="btn btn-primary" onClick={() => navigate('/register')}>
            Get Another Token
          </button>
        </div>

        {/* Auto-refresh indicator */}
        <div className="refresh-indicator">
          <span className="pulse-dot"></span>
          <span>Live tracking • Updates every 5 seconds</span>
          {lastUpdated && (
            <span className="last-updated">
              Last updated: {lastUpdated.toLocaleTimeString()}
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default TokenDetails;
