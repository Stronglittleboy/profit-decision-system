package com.profit.domain.contract;

import java.util.List;
import java.util.Optional;

public interface ContractRepository {

    Optional<Contract> findById(Long id);

    List<Contract> search(String keyword, String type, String status);

    Contract save(Contract contract);

    void deleteById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeExcludeId(String code, Long id);
}
