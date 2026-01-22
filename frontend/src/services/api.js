import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth token
api.interceptors.request.use(
  (config) => {
    const credentials = localStorage.getItem('staffCredentials');
    if (credentials) {
      config.headers.Authorization = `Basic ${credentials}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for handling errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('staffCredentials');
    }
    return Promise.reject(error);
  }
);

// Department APIs
export const departmentAPI = {
  getAll: () => api.get('/departments'),
  getById: (id) => api.get(`/departments/${id}`),
  getDoctors: (id) => api.get(`/departments/${id}/doctors`),
  getAllWithDoctors: () => api.get('/departments/with-doctors'),
};

// Doctor APIs
export const doctorAPI = {
  getAll: () => api.get('/doctors'),
  getById: (id) => api.get(`/doctors/${id}`),
  getAvailable: () => api.get('/doctors/available'),
  getByDepartment: (deptId) => api.get(`/doctors/department/${deptId}`),
  updateAvailability: (id, available) => api.patch(`/doctors/${id}/availability?available=${available}`),
};

// Patient APIs
export const patientAPI = {
  getById: (id) => api.get(`/patients/${id}`),
  getByPhone: (phone) => api.get(`/patients/phone/${phone}`),
  register: (data) => api.post('/patients', data),
  findOrRegister: (data) => api.post('/patients/find-or-register', data),
  update: (id, data) => api.put(`/patients/${id}`, data),
  search: (query) => api.get(`/patients/search?query=${query}`),
};

// Token APIs
export const tokenAPI = {
  generate: (data) => api.post('/tokens', data),
  getById: (id) => api.get(`/tokens/${id}`),
  getByNumber: (tokenNumber) => api.get(`/tokens/number/${tokenNumber}`),
  getPatientTokens: (patientId) => api.get(`/tokens/patient/${patientId}`),
  getQueuePosition: (tokenNumber) => api.get(`/tokens/queue-position/${tokenNumber}`),
  getWaitingTime: (tokenId) => api.get(`/tokens/waiting-time/${tokenId}`),
  cancel: (id) => api.post(`/tokens/${id}/cancel`),
  getToday: () => api.get('/tokens/today'),
};

// Queue APIs
export const queueAPI = {
  getByDoctor: (doctorId) => api.get(`/queue/doctor/${doctorId}`),
  getByDepartment: (deptId) => api.get(`/queue/department/${deptId}`),
  getCurrent: (doctorId) => api.get(`/queue/current/${doctorId}`),
  getWaiting: (doctorId) => api.get(`/queue/waiting/${doctorId}`),
  getAll: () => api.get('/queue/all'),
  getLiveBoard: () => api.get('/queue/live-board'),
};

// Staff APIs (requires authentication)
export const staffAPI = {
  callNext: (doctorId) => api.post(`/staff/call-next/${doctorId}`),
  startConsultation: (tokenId) => api.post(`/staff/start-consultation/${tokenId}`),
  endConsultation: (tokenId) => api.post(`/staff/end-consultation/${tokenId}`),
  cancelConsultation: (tokenId) => api.post(`/staff/cancel-consultation/${tokenId}`),
  getActiveConsultations: () => api.get('/staff/active-consultations'),
  markNoShow: (tokenId) => api.post(`/staff/no-show/${tokenId}`),
  markPriority: (tokenId, priority) => api.post(`/staff/mark-priority/${tokenId}?priority=${priority}`),
  skip: (tokenId) => api.post(`/staff/skip/${tokenId}`),
  getDashboard: () => api.get('/staff/dashboard'),
  getDoctorQueue: (doctorId) => api.get(`/staff/doctor-queue/${doctorId}`),
};

// Auth helper
export const setStaffCredentials = (username, password) => {
  const credentials = btoa(`${username}:${password}`);
  localStorage.setItem('staffCredentials', credentials);
};

export const clearStaffCredentials = () => {
  localStorage.removeItem('staffCredentials');
};

export const isAuthenticated = () => {
  return !!localStorage.getItem('staffCredentials');
};

export default api;
