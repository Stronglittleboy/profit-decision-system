package com.profit.domain.contract;

import com.profit.common.exception.BusinessException;
import com.profit.domain.counterparty.CounterpartyRepository;
import com.profit.domain.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ContractDomainService {

    private final ContractRepository contractRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final ProjectRepository projectRepository;

    public void validateCodeUnique(String code) {
        if (contractRepository.existsByCode(code)) {
            throw new BusinessException(40002, "合同编号已存在");
        }
    }

    public void validateCodeUniqueForUpdate(String code, Long id) {
        if (contractRepository.existsByCodeExcludeId(code, id)) {
            throw new BusinessException(40002, "合同编号已存在");
        }
    }

    public void validateCounterpartyExists(Long counterpartyId) {
        counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new BusinessException(40401, "往来方不存在"));
    }

    public void validateProjectExists(Long projectId) {
        if (projectId != null) {
            projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        }
    }

    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(40003, "到期日期不能早于生效日期");
        }
    }
}
