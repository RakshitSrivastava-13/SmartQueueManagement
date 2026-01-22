import React from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
  return (
    <div className="home">
      <section className="hero">
        <div className="hero-content">
          <h1>Smart Hospital Queue Management</h1>
          <p>
            Skip the long wait times. Get your token digitally and track your
            queue position in real-time.
          </p>
          <div className="hero-actions">
            <Link to="/register" className="btn btn-primary btn-lg">
              Get Your Token
            </Link>
            <Link to="/queue-board" className="btn btn-secondary btn-lg">
              View Live Queue
            </Link>
          </div>
        </div>
      </section>

      <section className="features">
        <h2>How It Works</h2>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon">📝</div>
            <h3>1. Register</h3>
            <p>Enter your details and select your department or doctor.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">🎫</div>
            <h3>2. Get Token</h3>
            <p>Receive a unique token number with estimated wait time.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">📊</div>
            <h3>3. Track Queue</h3>
            <p>Monitor your position in real-time from anywhere.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">🔔</div>
            <h3>4. Get Called</h3>
            <p>Arrive when it's your turn - no more waiting in lines!</p>
          </div>
        </div>
      </section>

      <section className="benefits">
        <div className="benefits-content">
          <h2>Benefits</h2>
          <div className="benefits-grid">
            <div className="benefit-item">
              <span className="benefit-icon">⏱️</span>
              <div>
                <h4>Reduced Wait Times</h4>
                <p>Spend less time in hospital waiting rooms</p>
              </div>
            </div>
            <div className="benefit-item">
              <span className="benefit-icon">📱</span>
              <div>
                <h4>Real-Time Updates</h4>
                <p>Track your queue position from your phone</p>
              </div>
            </div>
            <div className="benefit-item">
              <span className="benefit-icon">🏥</span>
              <div>
                <h4>Less Crowding</h4>
                <p>Virtual queues reduce OPD congestion</p>
              </div>
            </div>
            <div className="benefit-item">
              <span className="benefit-icon">⚡</span>
              <div>
                <h4>Priority Care</h4>
                <p>Emergency and priority cases handled first</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="quick-access">
        <h2>Quick Access</h2>
        <div className="quick-access-grid">
          <Link to="/register" className="quick-card patient">
            <div className="quick-icon">👤</div>
            <h3>Patient</h3>
            <p>Register and get your token</p>
          </Link>
          <Link to="/queue-board" className="quick-card queue">
            <div className="quick-icon">📺</div>
            <h3>Queue Board</h3>
            <p>View live queue status</p>
          </Link>
          <Link to="/staff" className="quick-card staff">
            <div className="quick-icon">👨‍⚕️</div>
            <h3>Staff Portal</h3>
            <p>Manage patients and queues</p>
          </Link>
        </div>
      </section>
    </div>
  );
};

export default Home;
