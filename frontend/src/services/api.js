import axios from 'axios';

/**
 * API Service Configuration
 * 
 * Axios instance configured to:
 * - Send cookies automatically (withCredentials: true)
 * - Handle base URL
 * - Handle errors globally
 */

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true, // Important: This sends httpOnly cookies automatically
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor (runs before every request)
api.interceptors.request.use(
  (config) => {
    // You can add auth tokens here if needed (but we use httpOnly cookies)
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor (runs after every response)
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle 401 Unauthorized - redirect to login
    if (error.response?.status === 401) {
      // Clear any local state if needed
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

