package com.profit.domain.factevent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FactEventRepository {

    Optional<FactEvent> findById(Long id);

    List<FactEvent> search(String type, String status, LocalDate startDate, LocalDate endDate);

    FactEvent save(FactEvent event);
}
