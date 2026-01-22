import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { doctorAPI, queueAPI } from '../services/api';
import './DoctorDashboard.css';

const DoctorDashboard = () => {
  const { doctorId } = useParams();
  const [doctor, setDoctor] = useState(null);
  const [currentPatient, setCurrentPatient] = useState(null);
  const [waitingQueue, setWaitingQueue] = useState([]);
  const [stats, setStats] = useState({
    waiting: 0,
    completed: 0,
    averageTime: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [consultationStartTime, setConsultationStartTime] = useState(null);
  const [elapsedTime, setElapsedTime] = useState(0);

  const fetchDoctorDetails = useCallback(async () => {
    try {
      const response = await doctorAPI.getById(doctorId);
      setDoctor(response.data.data);
    } catch (err) {
      setError('Failed to load doctor details');
    }
  }, [doctorId]);

  const fetchQueueData = useCallback(async () => {
    try {
      const [queueResponse, statsResponse] = await Promise.all([
        queueAPI.getByDoctor(doctorId),
        queueAPI.getStats()
      ]);
      
      const queue = queueResponse.data.data || [];
      const waiting = queue.filter(t => t.status === 'WAITING');
      const inProgress = queue.find(t => t.status === 'IN_CONSULTATION' || t.status === 'CALLED');
      
      setWaitingQueue(waiting);
      setCurrentPatient(inProgress || null);
      
      // Calculate stats
      const completed = queue.filter(t => t.status === 'COMPLETED');
      setStats({
        waiting: waiting.length,
        completed: completed.length,
        averageTime: statsResponse.data.data?.averageWaitTime || 15
      });
    } catch (err) {
      console.error('Failed to fetch queue data', err);
    } finally {
      setLoading(false);
    }
  }, [doctorId]);

  useEffect(() => {
    fetchDoctorDetails();
    fetchQueueData();
    const interval = setInterval(fetchQueueData, 5000);
    return () => clearInterval(interval);
  }, [fetchDoctorDetails, fetchQueueData]);

  // Timer for current consultation
  useEffect(() => {
    let timer;
    if (currentPatient?.status === 'IN_CONSULTATION' && consultationStartTime) {
      timer = setInterval(() => {
        setElapsedTime(Math.floor((Date.now() - consultationStartTime) / 1000));
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [currentPatient, consultationStartTime]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCallNext = async () => {
    try {
      const response = await queueAPI.callNext(doctorId);
      setCurrentPatient(response.data.data);
      await fetchQueueData();
    } catch (err) {
      setError(err.response?.data?.message || 'No patients in queue');
      setTimeout(() => setError(''), 3000);
    }
  };

  const handleStartConsultation = async () => {
    if (!currentPatient) return;
    try {
      await queueAPI.startConsultation(currentPatient.id);
      setConsultationStartTime(Date.now());
      setCurrentPatient(prev => ({ ...prev, status: 'IN_CONSULTATION' }));
      await fetchQueueData();
    } catch (err) {
      setError('Failed to start consultation');
      setTimeout(() => setError(''), 3000);
    }
  };

  const handleCompleteConsultation = async () => {
    if (!currentPatient) return;
    try {
      await queueAPI.complete(currentPatient.id);
      setCurrentPatient(null);
      setConsultationStartTime(null);
      setElapsedTime(0);
      await fetchQueueData();
    } catch (err) {
      setError('Failed to complete consultation');
      setTimeout(() => setError(''), 3000);
    }
  };

  const handleSkipPatient = async () => {
    if (!currentPatient) return;
    try {
      await queueAPI.skip(currentPatient.id);
      setCurrentPatient(null);
      setConsultationStartTime(null);
      setElapsedTime(0);
      await fetchQueueData();
    } catch (err) {
      setError('Failed to skip patient');
      setTimeout(() => setError(''), 3000);
    }
  };

  const getPriorityIcon = (priority) => {
    switch (priority) {
      case 'EMERGENCY': return '🚨';
      case 'PREGNANT': return '🤰';
      case 'SENIOR_CITIZEN': return '👴';
      default: return '';
    }
  };

  if (loading) {
    return (
      <div className="doctor-dashboard">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="doctor-dashboard">
      {/* Header */}
      <div className="dashboard-header">
        <div className="doctor-info">
          <div className="doctor-avatar">
            {doctor?.name?.charAt(0) || 'D'}
          </div>
          <div className="doctor-details">
            <h1>Dr. {doctor?.name}</h1>
            <p>{doctor?.specialization} • {doctor?.departmentName}</p>
          </div>
        </div>
        <div className="dashboard-stats">
          <div className="stat-item">
            <span className="stat-value waiting">{stats.waiting}</span>
            <span className="stat-label">Waiting</span>
          </div>
          <div className="stat-item">
            <span className="stat-value completed">{stats.completed}</span>
            <span className="stat-label">Completed</span>
          </div>
          <div className="stat-item">
            <span className="stat-value avg">{stats.averageTime}m</span>
            <span className="stat-label">Avg Time</span>
          </div>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="dashboard-content">
        {/* Current Patient Section */}
        <div className="current-section">
          <h2>Current Patient</h2>
          {currentPatient ? (
            <div className="current-patient-card">
              <div className="token-display">
                <span className="token-prefix">Token</span>
                <span className="token-number">{currentPatient.tokenNumber}</span>
                {currentPatient.priority !== 'NORMAL' && (
                  <span className={`priority-indicator ${currentPatient.priority.toLowerCase()}`}>
                    {getPriorityIcon(currentPatient.priority)} {currentPatient.priority}
                  </span>
                )}
              </div>

              <div className="patient-info">
                <h3>{currentPatient.patientName}</h3>
                <p className="patient-phone">📱 {currentPatient.patientPhone}</p>
                {currentPatient.notes && (
                  <p className="patient-notes">📝 {currentPatient.notes}</p>
                )}
              </div>

              <div className="status-section">
                <span className={`status-badge ${currentPatient.status.toLowerCase().replace('_', '-')}`}>
                  {currentPatient.status.replace('_', ' ')}
                </span>
                {currentPatient.status === 'IN_CONSULTATION' && (
                  <div className="timer">
                    <span className="timer-icon">⏱️</span>
                    <span className="timer-value">{formatTime(elapsedTime)}</span>
                  </div>
                )}
              </div>

              <div className="action-buttons">
                {currentPatient.status === 'CALLED' && (
                  <button 
                    className="btn btn-primary btn-large"
                    onClick={handleStartConsultation}
                  >
                    ▶️ Start Consultation
                  </button>
                )}
                {currentPatient.status === 'IN_CONSULTATION' && (
                  <button 
                    className="btn btn-success btn-large"
                    onClick={handleCompleteConsultation}
                  >
                    ✅ Complete
                  </button>
                )}
                <button 
                  className="btn btn-secondary"
                  onClick={handleSkipPatient}
                >
                  ⏭️ Skip
                </button>
              </div>
            </div>
          ) : (
            <div className="no-patient">
              <div className="no-patient-icon">👨‍⚕️</div>
              <h3>No Patient Currently</h3>
              <p>Call the next patient from the queue</p>
              <button 
                className="btn btn-primary btn-large"
                onClick={handleCallNext}
                disabled={stats.waiting === 0}
              >
                📢 Call Next Patient
              </button>
            </div>
          )}
        </div>

        {/* Queue Section */}
        <div className="queue-section">
          <div className="queue-header">
            <h2>Waiting Queue</h2>
            <span className="queue-count">{stats.waiting} patients</span>
          </div>

          {waitingQueue.length > 0 ? (
            <div className="queue-list">
              {waitingQueue.map((token, index) => (
                <div key={token.id} className="queue-item">
                  <div className="queue-position">{index + 1}</div>
                  <div className="queue-token-info">
                    <span className="queue-token-number">{token.tokenNumber}</span>
                    <span className="queue-patient-name">{token.patientName}</span>
                  </div>
                  {token.priority !== 'NORMAL' && (
                    <span className={`priority-badge ${token.priority.toLowerCase()}`}>
                      {getPriorityIcon(token.priority)}
                    </span>
                  )}
                  <span className="wait-time">
                    {token.waitTime || '~'} min
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-queue">
              <p>🎉 No patients waiting!</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DoctorDashboard;
