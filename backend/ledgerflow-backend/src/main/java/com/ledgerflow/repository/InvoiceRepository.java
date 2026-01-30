package com.ledgerflow.repository;

import com.ledgerflow.entity.Invoice;
import com.ledgerflow.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.lineItems WHERE i.tenant.id = :tenantId ORDER BY i.createdAt DESC")
    List<Invoice> findByTenantId(@Param("tenantId") Long tenantId);
    List<Invoice> findByTenantIdAndStatus(Long tenantId, InvoiceStatus status);
    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, Long tenantId);

    @Query("SELECT i FROM Invoice i WHERE i.tenant.id = :tenantId ORDER BY i.createdAt DESC")
    List<Invoice> findRecentInvoicesByTenant(@Param("tenantId") Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, InvoiceStatus status);
}