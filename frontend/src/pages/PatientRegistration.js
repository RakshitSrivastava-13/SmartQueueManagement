import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { departmentAPI, doctorAPI, patientAPI, tokenAPI } from '../services/api';
import './PatientRegistration.css';

const PatientRegistration = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Form data
  const [patientData, setPatientData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    dateOfBirth: '',
    gender: '',
    isSeniorCitizen: false,
    isPregnant: false,
  });

  const [departments, setDepartments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [selectedDoctor, setSelectedDoctor] = useState('');
  const [priority, setPriority] = useState('NORMAL');
  const [notes, setNotes] = useState('');

  const [existingPatient, setExistingPatient] = useState(null);
  const [generatedToken, setGeneratedToken] = useState(null);

  useEffect(() => {
    fetchDepartments();
  }, []);

  useEffect(() => {
    if (selectedDepartment) {
      fetchDoctors(selectedDepartment);
    }
  }, [selectedDepartment]);

  const fetchDepartments = async () => {
    try {
      const response = await departmentAPI.getAll();
      setDepartments(response.data.data || []);
    } catch (err) {
      setError('Failed to load departments');
    }
  };

  const fetchDoctors = async (deptId) => {
    try {
      const response = await doctorAPI.getByDepartment(deptId);
      setDoctors(response.data.data || []);
    } catch (err) {
      setError('Failed to load doctors');
    }
  };

  const handlePatientInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setPatientData({
      ...patientData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const checkExistingPatient = async () => {
    if (!patientData.phone || patientData.phone.length !== 10) {
      return;
    }

    try {
      const response = await patientAPI.getByPhone(patientData.phone);
      if (response.data.data) {
        setExistingPatient(response.data.data);
        setPatientData({
          ...patientData,
          firstName: response.data.data.firstName,
          lastName: response.data.data.lastName,
          email: response.data.data.email || '',
          dateOfBirth: response.data.data.dateOfBirth || '',
          gender: response.data.data.gender || '',
          isSeniorCitizen: response.data.data.isSeniorCitizen || false,
          isPregnant: response.data.data.isPregnant || false,
        });
      }
    } catch (err) {
      // Patient not found - that's okay
      setExistingPatient(null);
    }
  };

  const handleStep1Submit = async (e) => {
    e.preventDefault();
    setError('');

    if (!patientData.firstName || !patientData.lastName || !patientData.phone) {
      setError('Please fill in all required fields');
      return;
    }

    if (patientData.phone.length !== 10) {
      setError('Phone number must be 10 digits');
      return;
    }

    setStep(2);
  };

  const handleStep2Submit = async (e) => {
    e.preventDefault();
    setError('');

    if (!selectedDepartment) {
      setError('Please select a department');
      return;
    }

    setLoading(true);

    try {
      // Register or find patient
      let patientId;
      if (existingPatient) {
        patientId = existingPatient.id;
      } else {
        const patientResponse = await patientAPI.register(patientData);
        patientId = patientResponse.data.data.id;
      }

      // Generate token
      const tokenRequest = {
        patientId,
        departmentId: parseInt(selectedDepartment),
        doctorId: selectedDoctor ? parseInt(selectedDoctor) : null,
        priority,
        notes,
      };

      const tokenResponse = await tokenAPI.generate(tokenRequest);
      setGeneratedToken(tokenResponse.data.data);
      setSuccess('Token generated successfully!');
      setStep(3);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate token');
    } finally {
      setLoading(false);
    }
  };

  const renderStep1 = () => (
    <form onSubmit={handleStep1Submit} className="registration-form">
      <h2>Patient Information</h2>
      <p className="form-subtitle">Enter your details to get started</p>

      <div className="form-group">
        <label className="form-label">Phone Number *</label>
        <input
          type="tel"
          name="phone"
          className="form-input"
          placeholder="Enter 10-digit phone number"
          value={patientData.phone}
          onChange={handlePatientInputChange}
          onBlur={checkExistingPatient}
          maxLength={10}
          required
        />
        {existingPatient && (
          <p className="form-hint success">
            ✓ Welcome back, {existingPatient.firstName}!
          </p>
        )}
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">First Name *</label>
          <input
            type="text"
            name="firstName"
            className="form-input"
            placeholder="First name"
            value={patientData.firstName}
            onChange={handlePatientInputChange}
            required
          />
        </div>
        <div className="form-group">
          <label className="form-label">Last Name *</label>
          <input
            type="text"
            name="lastName"
            className="form-input"
            placeholder="Last name"
            value={patientData.lastName}
            onChange={handlePatientInputChange}
            required
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Email</label>
          <input
            type="email"
            name="email"
            className="form-input"
            placeholder="Email address"
            value={patientData.email}
            onChange={handlePatientInputChange}
          />
        </div>
        <div className="form-group">
          <label className="form-label">Date of Birth</label>
          <input
            type="date"
            name="dateOfBirth"
            className="form-input"
            value={patientData.dateOfBirth}
            onChange={handlePatientInputChange}
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Gender</label>
          <select
            name="gender"
            className="form-select"
            value={patientData.gender}
            onChange={handlePatientInputChange}
          >
            <option value="">Select gender</option>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
      </div>

      <div className="checkbox-group">
        <label className="checkbox-label">
          <input
            type="checkbox"
            name="isSeniorCitizen"
            checked={patientData.isSeniorCitizen}
            onChange={handlePatientInputChange}
          />
          <span>Senior Citizen (60+ years)</span>
        </label>
        <label className="checkbox-label">
          <input
            type="checkbox"
            name="isPregnant"
            checked={patientData.isPregnant}
            onChange={handlePatientInputChange}
          />
          <span>Pregnant</span>
        </label>
      </div>

      <button type="submit" className="btn btn-primary btn-lg full-width">
        Continue to Department Selection
      </button>
    </form>
  );

  const renderStep2 = () => (
    <form onSubmit={handleStep2Submit} className="registration-form">
      <h2>Select Department & Doctor</h2>
      <p className="form-subtitle">Choose where you want to consult</p>

      <div className="form-group">
        <label className="form-label">Department *</label>
        <select
          className="form-select"
          value={selectedDepartment}
          onChange={(e) => {
            setSelectedDepartment(e.target.value);
            setSelectedDoctor('');
          }}
          required
        >
          <option value="">Select a department</option>
          {departments.map((dept) => (
            <option key={dept.id} value={dept.id}>
              {dept.name} ({dept.code})
            </option>
          ))}
        </select>
      </div>

      {doctors.length > 0 && (
        <div className="form-group">
          <label className="form-label">Doctor (Optional)</label>
          <div className="doctor-grid">
            {doctors.map((doctor) => (
              <div
                key={doctor.id}
                className={`doctor-card ${selectedDoctor === String(doctor.id) ? 'selected' : ''}`}
                onClick={() => setSelectedDoctor(String(doctor.id))}
              >
                <div className="doctor-avatar">👨‍⚕️</div>
                <div className="doctor-info">
                  <h4>Dr. {doctor.fullName}</h4>
                  <p>{doctor.specialization}</p>
                  <p className="room">Room: {doctor.roomNumber}</p>
                  <div className="doctor-stats">
                    <span className="wait-badge">
                      ~{doctor.estimatedWaitTime} min wait
                    </span>
                    <span className="queue-badge">
                      {doctor.currentQueueLength} in queue
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="form-group">
        <label className="form-label">Priority</label>
        <select
          className="form-select"
          value={priority}
          onChange={(e) => setPriority(e.target.value)}
        >
          <option value="NORMAL">Normal</option>
          <option value="EMERGENCY">Emergency</option>
        </select>
        <p className="form-hint">
          Note: Senior citizens and pregnant women automatically get priority
        </p>
      </div>

      <div className="form-group">
        <label className="form-label">Notes (Optional)</label>
        <textarea
          className="form-input"
          placeholder="Any specific concerns or symptoms..."
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          rows={3}
        />
      </div>

      <div className="form-actions">
        <button
          type="button"
          className="btn btn-secondary"
          onClick={() => setStep(1)}
        >
          Back
        </button>
        <button
          type="submit"
          className="btn btn-primary btn-lg"
          disabled={loading}
        >
          {loading ? 'Generating Token...' : 'Get Token'}
        </button>
      </div>
    </form>
  );

  const renderStep3 = () => (
    <div className="token-success">
      <div className="success-icon">✅</div>
      <h2>Token Generated Successfully!</h2>

      <div className="token-display">
        <div className="token-label">Your Token Number</div>
        <div className="token-number-large">{generatedToken?.tokenNumber}</div>
      </div>

      <div className="token-details">
        <div className="detail-item">
          <span className="detail-label">Department</span>
          <span className="detail-value">{generatedToken?.departmentName}</span>
        </div>
        {generatedToken?.doctorName && (
          <div className="detail-item">
            <span className="detail-label">Doctor</span>
            <span className="detail-value">Dr. {generatedToken?.doctorName}</span>
          </div>
        )}
        {generatedToken?.roomNumber && (
          <div className="detail-item">
            <span className="detail-label">Room</span>
            <span className="detail-value">{generatedToken?.roomNumber}</span>
          </div>
        )}
        <div className="detail-item">
          <span className="detail-label">Priority</span>
          <span className={`badge badge-${generatedToken?.priority?.toLowerCase()}`}>
            {generatedToken?.priority}
          </span>
        </div>
      </div>

      <div className="token-actions">
        <button
          className="btn btn-primary btn-lg"
          onClick={() => navigate(`/token/${generatedToken?.tokenNumber}`)}
        >
          Track Your Token
        </button>
        <button
          className="btn btn-secondary"
          onClick={() => {
            setStep(1);
            setPatientData({
              firstName: '',
              lastName: '',
              phone: '',
              email: '',
              dateOfBirth: '',
              gender: '',
              isSeniorCitizen: false,
              isPregnant: false,
            });
            setSelectedDepartment('');
            setSelectedDoctor('');
            setGeneratedToken(null);
            setExistingPatient(null);
          }}
        >
          Generate Another Token
        </button>
      </div>
    </div>
  );

  return (
    <div className="patient-registration">
      <div className="registration-container">
        {/* Progress Steps */}
        <div className="progress-steps">
          <div className={`step ${step >= 1 ? 'active' : ''} ${step > 1 ? 'completed' : ''}`}>
            <div className="step-number">1</div>
            <div className="step-label">Patient Info</div>
          </div>
          <div className="step-connector"></div>
          <div className={`step ${step >= 2 ? 'active' : ''} ${step > 2 ? 'completed' : ''}`}>
            <div className="step-number">2</div>
            <div className="step-label">Department</div>
          </div>
          <div className="step-connector"></div>
          <div className={`step ${step >= 3 ? 'active' : ''}`}>
            <div className="step-number">3</div>
            <div className="step-label">Token</div>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div className="registration-content">
          {step === 1 && renderStep1()}
          {step === 2 && renderStep2()}
          {step === 3 && renderStep3()}
        </div>
      </div>
    </div>
  );
};

export default PatientRegistration;
