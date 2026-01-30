import { create } from 'zustand';
import { authService } from '../services/authService';

/**
 * Authentication Store (Zustand)
 * 
 * Manages authentication state globally
 * Similar to Redux or Context API, but simpler
 */

const useAuthStore = create((set, get) => ({
  // State
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  // Actions
  /**
   * Login user
   */
  login: async (username, password) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.login({ username, password });
      set({
        user: {
          username: response.username,
          email: response.email,
          tenantId: response.tenantId,
          tenantName: response.tenantName,
        },
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || 'Login failed';
      set({
        error: errorMessage,
        isLoading: false,
        isAuthenticated: false,
        user: null,
      });
      return { success: false, error: errorMessage };
    }
  },

  /**
   * Register new user
   */
  register: async (registerData) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.register(registerData);
      set({
        user: {
          username: response.username,
          email: response.email,
          tenantId: response.tenantId,
          tenantName: response.tenantName,
        },
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || 'Registration failed';
      set({
        error: errorMessage,
        isLoading: false,
        isAuthenticated: false,
        user: null,
      });
      return { success: false, error: errorMessage };
    }
  },

  /**
   * Logout user
   */
  logout: async () => {
    set({ isLoading: true });
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
    }
  },

  /**
   * Check if user is authenticated (get current user from server)
   */
  checkAuth: async () => {
    set({ isLoading: true });
    try {
      const response = await authService.getCurrentUser();
      set({
        user: {
          username: response.username,
          email: response.email,
          tenantId: response.tenantId,
          tenantName: response.tenantName,
        },
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
      return { success: true };
    } catch (error) {
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
      return { success: false };
    }
  },

  /**
   * Clear error
   */
  clearError: () => set({ error: null }),
}));

export default useAuthStore;

