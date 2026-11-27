package com.findit.common;

import com.findit.item.ItemType;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom Micrometer counters. Prometheus exposes them as findit_items_created_total{type},
 * findit_claims_total{status} and findit_matches_found_total.
 */
@Component
public class FindItMetrics {

    private final MeterRegistry registry;

    public FindItMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void itemCreated(ItemType type) {
        registry.counter("findit.items.created", "type", type.name()).increment();
    }

    public void claimRecorded(String status) {
        registry.counter("findit.claims", "status", status).increment();
    }

    public void matchFound() {
        registry.counter("findit.matches.found").increment();
    }
}
