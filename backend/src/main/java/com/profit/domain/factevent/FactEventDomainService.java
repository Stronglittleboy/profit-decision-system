package com.profit.domain.factevent;

import com.profit.common.exception.BusinessException;
import com.profit.domain.accountsubject.AccountSubjectRepository;
import com.profit.domain.counterparty.CounterpartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactEventDomainService {

    private final AccountSubjectRepository subjectRepository;
    private final CounterpartyRepository counterpartyRepository;

    public void validateSubjectExists(Long subjectId) {
        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new BusinessException(40401, "会计科目不存在"));
    }

    public void validateCounterpartyExists(Long counterpartyId) {
        counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new BusinessException(40401, "往来方不存在"));
    }

    public void validateNotAlreadyReversed(FactEvent event) {
        if (event.isReversed()) {
            throw new BusinessException(40001, "该记录已冲正");
        }
    }
}
