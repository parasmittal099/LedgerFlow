package com.ledgerflow.service;

import com.ledgerflow.entity.Tenant;
import com.ledgerflow.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant createTenant(String name, String slug) {
        // Check if slug already exists
        if (tenantRepository.existsBySlug(slug)) {
            throw new RuntimeException("Tenant with slug '" + slug + "' already exists");
        }

        // Check if name already exists
        if (tenantRepository.existsByName(name)) {
            throw new RuntimeException("Tenant with name '" + name + "' already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setSlug(slug);
        tenant.setActive(true);

        return tenantRepository.save(tenant);
    }

    public Tenant getTenantBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Tenant not found with slug: " + slug));
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
    }
}