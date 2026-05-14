package com.profit.domain.counterparty;

import java.util.List;
import java.util.Optional;

public interface CounterpartyRepository {

    Optional<Counterparty> findById(Long id);

    List<Counterparty> findAll();

    List<Counterparty> search(String keyword, String type);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Counterparty save(Counterparty counterparty);

    void deleteById(Long id);
}
