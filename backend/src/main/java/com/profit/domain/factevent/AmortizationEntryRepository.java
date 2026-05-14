package com.profit.domain.factevent;

import java.util.List;

public interface AmortizationEntryRepository {
    void saveAll(List<AmortizationEntry> entries);
    List<AmortizationEntry> findByFactEventId(Long factEventId);
    void deleteByFactEventId(Long factEventId);
}
