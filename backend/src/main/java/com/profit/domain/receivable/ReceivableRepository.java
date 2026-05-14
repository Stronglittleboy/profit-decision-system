package com.profit.domain.receivable;

import java.util.List;
import java.util.Optional;

public interface ReceivableRepository {
    Optional<Receivable> findById(Long id);
    List<Receivable> search(String keyword, String status);
    Receivable save(Receivable r);
    void deleteById(Long id);
    boolean existsByCode(String code);
}
