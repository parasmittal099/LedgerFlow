import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import { invoiceService } from '../services/invoiceService';
import InvoiceUpload from '../components/InvoiceUpload';
import InvoiceList from '../components/InvoiceList';

/**
 * Dashboard Page
 * 
 * Main page after login - shows invoice list and upload functionality
 */
export default function Dashboard() {
  const navigate = useNavigate();
  const { user, logout, isAuthenticated } = useAuthStore();
  const [invoices, setInvoices] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showUpload, setShowUpload] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
      return;
    }

    // Wait for user to be loaded before loading invoices
    if (user?.tenantId) {
      loadInvoices();
    }
  }, [isAuthenticated, navigate, user]);

  const loadInvoices = async () => {
    if (!user?.tenantId) {
      console.error('Cannot load invoices: tenantId is missing', user);
      setError('User tenant information is missing. Please log in again.');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      console.log('Loading invoices for tenant:', user.tenantId);
      const data = await invoiceService.getInvoices(user.tenantId);
      console.log('Invoices loaded:', data);
      setInvoices(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error loading invoices:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Failed to load invoices';
      setError(errorMessage);
      setInvoices([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const handleUploadSuccess = () => {
    setShowUpload(false);
    loadInvoices(); // Reload invoices after successful upload
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-lg mb-2">Loading user information...</div>
          <div className="text-sm text-gray-500">If this persists, please try logging in again.</div>
        </div>
      </div>
    );
  }

  // Debug: Log user info
  console.log('Dashboard - User:', user);
  console.log('Dashboard - isAuthenticated:', isAuthenticated);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">LedgerFlow</h1>
              <p className="text-sm text-gray-600">{user.tenantName}</p>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-700">Welcome, {user.username}</span>
              <button
                onClick={handleLogout}
                className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6 flex justify-between items-center">
          <h2 className="text-xl font-semibold text-gray-900">Invoices</h2>
          <button
            onClick={() => setShowUpload(!showUpload)}
            className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            {showUpload ? 'Cancel' : 'Upload Invoice'}
          </button>
        </div>

        {showUpload && (
          <div className="mb-6">
            <InvoiceUpload
              tenantId={user.tenantId}
              onSuccess={handleUploadSuccess}
              onCancel={() => setShowUpload(false)}
            />
          </div>
        )}

        {error && (
          <div className="mb-4 rounded-md bg-red-50 p-4 border border-red-200">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <div className="text-sm text-red-800 font-medium">Error loading invoices</div>
                <div className="text-sm text-red-700 mt-1">{error}</div>
                <button
                  onClick={loadInvoices}
                  className="mt-2 text-sm text-red-600 hover:text-red-800 underline"
                >
                  Try again
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Debug info - remove in production */}
        {process.env.NODE_ENV === 'development' && (
          <div className="mb-4 rounded-md bg-blue-50 p-4 border border-blue-200 text-xs">
            <div className="font-semibold text-blue-800 mb-1">Debug Info:</div>
            <div className="text-blue-700">
              <div>Tenant ID: {user?.tenantId || 'N/A'}</div>
              <div>Invoices Count: {invoices.length}</div>
              <div>Loading: {isLoading ? 'Yes' : 'No'}</div>
              <div>Error: {error || 'None'}</div>
            </div>
          </div>
        )}

        <InvoiceList
          invoices={invoices}
          isLoading={isLoading}
          onRefresh={loadInvoices}
        />
      </main>
    </div>
  );
}

