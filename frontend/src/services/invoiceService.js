import api from './api';

/**
 * Invoice Service
 * 
 * Handles all invoice-related API calls
 */

export const invoiceService = {
  /**
   * Get all invoices for a tenant
   */
  async getInvoices(tenantId) {
    const response = await api.get(`/invoices?tenantId=${tenantId}`);
    return response.data;
  },

  /**
   * Get a single invoice by ID
   */
  async getInvoice(id, tenantId) {
    const response = await api.get(`/invoices/${id}?tenantId=${tenantId}`);
    return response.data;
  },

  /**
   * Upload an invoice PDF
   */
  async uploadInvoice(file, tenantId) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('tenantId', tenantId);

    const response = await api.post('/invoices/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  /**
   * Update invoice status
   */
  async updateInvoiceStatus(id, tenantId, status) {
    const response = await api.put(
      `/invoices/${id}/status?tenantId=${tenantId}&status=${status}`
    );
    return response.data;
  },
};

