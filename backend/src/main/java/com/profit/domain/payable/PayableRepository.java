package com.profit.domain.payable;

import java.util.List;
import java.util.Optional;

public interface PayableRepository {
    Optional<Payable> findById(Long id);
    List<Payable> search(String keyword, String status);
    Payable save(Payable p);
    void deleteById(Long id);
    boolean existsByCode(String code);
}
