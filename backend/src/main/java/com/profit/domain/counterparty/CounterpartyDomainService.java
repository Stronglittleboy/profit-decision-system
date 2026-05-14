package com.profit.domain.counterparty;

import com.profit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CounterpartyDomainService {

    private final CounterpartyRepository repository;

    public void validateNameUnique(String name) {
        if (repository.existsByName(name)) {
            throw new BusinessException(40901, "往来方名称已存在");
        }
    }

    public void validateNameUniqueForUpdate(String name, Long id) {
        if (repository.existsByNameAndIdNot(name, id)) {
            throw new BusinessException(40901, "往来方名称已存在");
        }
    }
}
