import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import PatientRegistration from './pages/PatientRegistration';
import TokenDetails from './pages/TokenDetails';
import LiveQueueBoard from './pages/LiveQueueBoard';
import StaffDashboard from './pages/StaffDashboard';
import DoctorDashboard from './pages/DoctorDashboard';
import Home from './pages/Home';

function App() {
  return (
    <Router>
      <div className="app">
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/register" element={<PatientRegistration />} />
            <Route path="/token/:tokenNumber" element={<TokenDetails />} />
            <Route path="/queue-board" element={<LiveQueueBoard />} />
            <Route path="/staff" element={<StaffDashboard />} />
            <Route path="/doctor/:doctorId" element={<DoctorDashboard />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
