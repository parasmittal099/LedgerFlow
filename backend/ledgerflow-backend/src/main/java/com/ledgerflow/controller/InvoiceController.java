package com.ledgerflow.controller;

import com.ledgerflow.dto.ErrorResponse;
import com.ledgerflow.entity.Invoice;
import com.ledgerflow.entity.InvoiceStatus;
import com.ledgerflow.service.AiOrchestrationService;
import com.ledgerflow.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final AiOrchestrationService aiOrchestrationService;

    public InvoiceController(InvoiceService invoiceService,AiOrchestrationService aiOrchestrationService) {
        this.invoiceService = invoiceService;
        this.aiOrchestrationService = aiOrchestrationService;
    }

    @GetMapping
    public ResponseEntity<?> getInvoices(@RequestParam(required = false) Long tenantId) {
        // Handle missing tenantId parameter
        if (tenantId == null) {
            ErrorResponse error = new ErrorResponse(
                "Required parameter 'tenantId' is missing. Please provide it as a query parameter: ?tenantId=<value>",
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        try {
            List<Invoice> invoices = invoiceService.getInvoicesByTenant(tenantId);
            return ResponseEntity.ok(invoices);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id, @RequestParam Long tenantId) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(id, tenantId);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Map<String, Object> invoiceData) {
        // TODO: Create proper DTO for invoice creation
        // For now, this is a placeholder
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Invoice> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam Long tenantId,
            @RequestParam InvoiceStatus status) {
        try {
            Invoice invoice = invoiceService.updateInvoiceStatus(id, tenantId, status);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadInvoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tenantId") Long tenantId) {

        if (file.isEmpty()) {
            ErrorResponse error = new ErrorResponse("File is empty", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            // Call AI orchestration service
            Map<String, Object> extractionResult = aiOrchestrationService.extractInvoice(file, tenantId.toString());

            // Create invoice from extraction result
            Invoice invoice = invoiceService.createInvoiceFromExtraction(tenantId, extractionResult);

            return ResponseEntity.status(HttpStatus.CREATED).body(invoice);

        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                    "Failed to process invoice: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}