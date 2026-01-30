package com.ledgerflow.service;

import com.ledgerflow.entity.Invoice;
import com.ledgerflow.entity.InvoiceLineItem;
import com.ledgerflow.entity.InvoiceStatus;
import com.ledgerflow.entity.Tenant;
import com.ledgerflow.repository.InvoiceRepository;
import com.ledgerflow.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final TenantRepository tenantRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, TenantRepository tenantRepository) {
        this.invoiceRepository = invoiceRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Invoice createInvoice(Long tenantId, String invoiceNumber, String vendorName,
                                 LocalDate invoiceDate, LocalDate dueDate, BigDecimal totalAmount,
                                 String currency, String s3Key, String s3Url, Double confidenceScore,
                                 List<InvoiceLineItem> lineItems) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Check if invoice number already exists for this tenant
        if (invoiceRepository.findByInvoiceNumberAndTenantId(invoiceNumber, tenantId).isPresent()) {
            throw new RuntimeException("Invoice with number '" + invoiceNumber + "' already exists");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setVendorName(vendorName);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(dueDate);
        invoice.setTotalAmount(totalAmount);
        invoice.setCurrency(currency != null ? currency : "USD");
        invoice.setS3Key(s3Key);
        invoice.setS3Url(s3Url);
        invoice.setConfidenceScore(confidenceScore);
        invoice.setStatus(InvoiceStatus.EXTRACTED);
        invoice.setTenant(tenant);

        // Set line items
        if (lineItems != null) {
            for (InvoiceLineItem item : lineItems) {
                item.setInvoice(invoice);
            }
            invoice.setLineItems(lineItems);
        } else {
            invoice.setLineItems(new ArrayList<>());
        }

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoicesByTenant(Long tenantId) {
        return invoiceRepository.findByTenantId(tenantId);
    }

    public Invoice getInvoiceById(Long id, Long tenantId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Multi-tenant check
        if (!invoice.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Invoice not found");
        }

        return invoice;
    }

    @Transactional
    public Invoice updateInvoiceStatus(Long id, Long tenantId, InvoiceStatus status) {
        Invoice invoice = getInvoiceById(id, tenantId);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice createInvoiceFromExtraction(Long tenantId, Map<String, Object> extractionResult) {
        @SuppressWarnings("unchecked")
        Map<String, Object> extractedData = (Map<String, Object>) extractionResult.get("extracted_data");

        if (extractedData == null) {
            throw new RuntimeException("No extracted data found in AI response");
        }

        // Extract invoice fields
        String invoiceNumber = (String) extractedData.get("invoice_number");
        String vendorName = (String) extractedData.get("vendor_name");
        String currency = extractedData.get("currency") != null ? (String) extractedData.get("currency") : "USD";

        // Parse dates
        LocalDate invoiceDate = parseDate(extractedData.get("invoice_date"));
        LocalDate dueDate = extractedData.get("due_date") != null ? parseDate(extractedData.get("due_date")) : null;

        // Parse amounts
        BigDecimal totalAmount = parseBigDecimal(extractedData.get("total_amount"));
        BigDecimal taxAmount = extractedData.get("tax_amount") != null ? parseBigDecimal(extractedData.get("tax_amount")) : null;
        BigDecimal shippingAmount = extractedData.get("shipping_amount") != null ? parseBigDecimal(extractedData.get("shipping_amount")) : null;

        String paymentTerms = extractedData.get("payment_terms") != null ? (String) extractedData.get("payment_terms") : null;
        Double confidenceScore = extractionResult.get("confidence_score") != null ? ((Number) extractionResult.get("confidence_score")).doubleValue() : null;
        String s3Key = extractionResult.get("s3_key") != null ? (String) extractionResult.get("s3_key") : null;
        String s3Url = extractionResult.get("s3_url") != null ? (String) extractionResult.get("s3_url") : null;

        // Extract line items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineItemsData = (List<Map<String, Object>>) extractedData.get("line_items");
        List<InvoiceLineItem> lineItems = new ArrayList<>();

        if (lineItemsData != null) {
            for (Map<String, Object> itemData : lineItemsData) {
                InvoiceLineItem item = new InvoiceLineItem();
                item.setDescription((String) itemData.get("description"));
                item.setQuantity(parseBigDecimal(itemData.get("quantity")));
                item.setUnitPrice(parseBigDecimal(itemData.get("unit_price")));
                item.setAmount(parseBigDecimal(itemData.get("amount")));
                lineItems.add(item);
            }
        }

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setVendorName(vendorName);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(dueDate);
        invoice.setTotalAmount(totalAmount);
        invoice.setCurrency(currency);
        invoice.setTaxAmount(taxAmount);
        invoice.setShippingAmount(shippingAmount);
        invoice.setPaymentTerms(paymentTerms);
        invoice.setS3Key(s3Key);
        invoice.setS3Url(s3Url);
        invoice.setConfidenceScore(confidenceScore);
        invoice.setStatus(InvoiceStatus.EXTRACTED);
        invoice.setTenant(tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found")));

        // Set line items
        for (InvoiceLineItem item : lineItems) {
            item.setInvoice(invoice);
        }
        invoice.setLineItems(lineItems);

        return invoiceRepository.save(invoice);
    }

   private LocalDate parseDate(Object dateObj) {
    if (dateObj == null) return null;
    
    if (dateObj instanceof String) {
        String dateStr = (String) dateObj;
        
        try {
            // If it contains 'T', it's an ISO datetime string (e.g., "2025-12-28T00:00:00+00:00")
            if (dateStr.contains("T")) {
                // Extract just the date part (first 10 characters: YYYY-MM-DD)
                String dateOnly = dateStr.substring(0, 10);
                return LocalDate.parse(dateOnly);
            } else {
                // Simple date format (YYYY-MM-DD)
                return LocalDate.parse(dateStr);
            }
        } 
        catch (Exception e) {
                throw new RuntimeException("Cannot parse date: " + dateStr + ". Error: " + e.getMessage());
        }
    }
    
        throw new RuntimeException("Cannot parse date: " + dateObj);
    }

    private BigDecimal parseBigDecimal(Object amountObj) {
        if (amountObj == null) return BigDecimal.ZERO;
        if (amountObj instanceof Number) {
            return BigDecimal.valueOf(((Number) amountObj).doubleValue());
        }
        if (amountObj instanceof String) {
            return new BigDecimal((String) amountObj);
        }
        throw new RuntimeException("Cannot parse amount: " + amountObj);
    }
}
