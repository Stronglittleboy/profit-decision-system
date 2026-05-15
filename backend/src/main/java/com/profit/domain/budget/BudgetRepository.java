package com.profit.domain.budget;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository {
    Optional<Budget> findById(Long id);
    List<Budget> findByPeriod(String period);
    List<Budget> findAll(String period, String category, String status);
    boolean existsByPeriodAndCategory(String period, String category);
    Budget save(Budget budget);
    void deleteById(Long id);
}
