import React, { useState, useEffect, useCallback } from 'react';
import { queueAPI, departmentAPI } from '../services/api';
import './LiveQueueBoard.css';

const LiveQueueBoard = () => {
  const [queues, setQueues] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [selectedDepartment, setSelectedDepartment] = useState('all');
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(new Date());

  const fetchQueues = useCallback(async () => {
    try {
      const response = await queueAPI.getAll();
      let queueData = response.data.data || [];
      
      if (selectedDepartment !== 'all') {
        queueData = queueData.filter(q => q.departmentId === parseInt(selectedDepartment));
      }
      
      setQueues(queueData);
      setLastUpdated(new Date());
    } catch (err) {
      console.error('Failed to fetch queues:', err);
    } finally {
      setLoading(false);
    }
  }, [selectedDepartment]);

  const fetchDepartments = async () => {
    try {
      const response = await departmentAPI.getAll();
      setDepartments(response.data.data || []);
    } catch (err) {
      console.error('Failed to fetch departments:', err);
    }
  };

  useEffect(() => {
    fetchDepartments();
  }, []);

  useEffect(() => {
    fetchQueues();
    const interval = setInterval(fetchQueues, 10000); // Refresh every 10 seconds
    return () => clearInterval(interval);
  }, [fetchQueues]);

  const getPriorityClass = (priority) => {
    switch (priority) {
      case 'EMERGENCY': return 'priority-emergency';
      case 'PREGNANT': return 'priority-pregnant';
      case 'SENIOR_CITIZEN': return 'priority-senior';
      default: return 'priority-normal';
    }
  };

  if (loading) {
    return (
      <div className="live-queue-board">
        <div className="loading">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="live-queue-board">
      {/* Header */}
      <div className="board-header">
        <div className="header-content">
          <h1>🏥 Live Queue Status</h1>
          <p>Real-time queue information for all departments</p>
        </div>
        <div className="header-controls">
          <select
            className="department-filter"
            value={selectedDepartment}
            onChange={(e) => setSelectedDepartment(e.target.value)}
          >
            <option value="all">All Departments</option>
            {departments.map((dept) => (
              <option key={dept.id} value={dept.id}>
                {dept.name}
              </option>
            ))}
          </select>
          <div className="last-updated">
            Last updated: {lastUpdated.toLocaleTimeString()}
            <span className="pulse-dot"></span>
          </div>
        </div>
      </div>

      {/* Queue Board */}
      {queues.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📋</div>
          <h3>No Active Queues</h3>
          <p>There are no patients in queue at the moment.</p>
        </div>
      ) : (
        <div className="queue-board">
          {queues.map((queue) => (
            <div key={queue.doctorId} className="doctor-queue-card">
              {/* Doctor Header */}
              <div className="doctor-queue-header">
                <div className="doctor-info">
                  <h3>Dr. {queue.doctorName}</h3>
                  <span className="department">{queue.departmentName}</span>
                </div>
                <div className="room-info">
                  <span className="room-label">Room</span>
                  <span className="room-number">{queue.roomNumber}</span>
                </div>
              </div>

              {/* Current Token */}
              <div className="current-section">
                <div className="section-label">Now Serving</div>
                {queue.currentToken ? (
                  <div className="current-token active">
                    <span className="token-number">{queue.currentToken.tokenNumber}</span>
                    <span className="patient-name">{queue.currentToken.patientName}</span>
                    {queue.currentToken.priority !== 'NORMAL' && (
                      <span className={`priority-tag ${getPriorityClass(queue.currentToken.priority)}`}>
                        {queue.currentToken.priority}
                      </span>
                    )}
                  </div>
                ) : (
                  <div className="current-token empty">
                    <span className="no-patient">No patient being served</span>
                  </div>
                )}
              </div>

              {/* Queue Stats */}
              <div className="queue-stats-bar">
                <div className="stat-item">
                  <span className="stat-value">{queue.totalWaiting}</span>
                  <span className="stat-label">Waiting</span>
                </div>
                <div className="stat-item">
                  <span className="stat-value">~{queue.averageWaitTimeMinutes}</span>
                  <span className="stat-label">Avg. Wait (min)</span>
                </div>
              </div>

              {/* Waiting List */}
              <div className="waiting-section">
                <div className="section-label">Up Next</div>
                {queue.waitingTokens && queue.waitingTokens.length > 0 ? (
                  <div className="waiting-list">
                    {queue.waitingTokens.slice(0, 5).map((token, index) => (
                      <div key={token.id} className="waiting-item">
                        <div className="waiting-position">{index + 1}</div>
                        <div className="waiting-details">
                          <span className="waiting-token">{token.tokenNumber}</span>
                          <span className="waiting-name">{token.patientName}</span>
                        </div>
                        {token.priority !== 'NORMAL' && (
                          <span className={`priority-badge ${getPriorityClass(token.priority)}`}>
                            {token.priority === 'EMERGENCY' ? '🚨' : 
                             token.priority === 'PREGNANT' ? '🤰' : '👴'}
                          </span>
                        )}
                      </div>
                    ))}
                    {queue.waitingTokens.length > 5 && (
                      <div className="more-patients">
                        +{queue.waitingTokens.length - 5} more patients
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="no-waiting">No patients waiting</div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Legend */}
      <div className="board-legend">
        <div className="legend-title">Priority Legend:</div>
        <div className="legend-items">
          <span className="legend-item">
            <span className="legend-dot emergency"></span> Emergency
          </span>
          <span className="legend-item">
            <span className="legend-dot pregnant"></span> Pregnant
          </span>
          <span className="legend-item">
            <span className="legend-dot senior"></span> Senior Citizen
          </span>
          <span className="legend-item">
            <span className="legend-dot normal"></span> Normal
          </span>
        </div>
      </div>
    </div>
  );
};

export default LiveQueueBoard;
