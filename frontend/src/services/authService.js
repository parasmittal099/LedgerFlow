import api from './api';

/**
 * Authentication Service
 * 
 * Handles all authentication-related API calls
 */

export const authService = {
  /**
   * Register a new user and tenant
   */
  async register(data) {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  /**
   * Login user
   */
  async login(data) {
    const response = await api.post('/auth/login', data);
    return response.data;
  },

  /**
   * Logout user
   */
  async logout() {
    const response = await api.post('/auth/logout');
    return response.data;
  },

  /**
   * Get current authenticated user
   */
  async getCurrentUser() {
    const response = await api.get('/auth/me');
    return response.data;
  },
};

