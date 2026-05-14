package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.accountsubject.AccountSubject;
import com.profit.domain.accountsubject.AccountSubjectRepository;
import com.profit.domain.counterparty.Counterparty;
import com.profit.domain.counterparty.CounterpartyRepository;
import com.profit.domain.factevent.*;
import com.profit.dto.FactEventDTO;
import com.profit.vo.FactEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactEventAppService {

    private final FactEventRepository repository;
    private final FactEventDomainService domainService;
    private final AccountSubjectRepository subjectRepository;
    private final CounterpartyRepository counterpartyRepository;

    @Transactional
    public FactEventVO create(FactEventDTO dto) {
        domainService.validateSubjectExists(dto.getSubjectId());
        domainService.validateCounterpartyExists(dto.getCounterpartyId());

        FactType type = FactType.fromCode(dto.getType());
        CostCategory costCategory = CostCategory.fromCode(dto.getCostCategory());

        FactEvent event = FactEvent.create(
                type, dto.getAmount(),
                dto.getBusinessDate(), dto.getAccountingDate(),
                dto.getSubjectId(), dto.getCounterpartyId(),
                costCategory, dto.getInvoiceNo(), dto.getRemark()
        );

        FactEvent saved = repository.save(event);
        return enrichVO(FactEventVO.from(saved));
    }

    @Transactional
    public void reverse(Long id) {
        FactEvent event = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "收支记录不存在"));
        domainService.validateNotAlreadyReversed(event);
        event.reverse();
        repository.save(event);
    }

    public List<FactEventVO> list(String type, String status, LocalDate startDate, LocalDate endDate) {
        List<FactEvent> events = repository.search(type, status, startDate, endDate);
        List<FactEventVO> vos = events.stream().map(FactEventVO::from).collect(Collectors.toList());
        return enrichVOs(vos);
    }

    public FactEventVO getDetail(Long id) {
        FactEvent event = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "收支记录不存在"));
        return enrichVO(FactEventVO.from(event));
    }

    private FactEventVO enrichVO(FactEventVO vo) {
        subjectRepository.findById(vo.getSubjectId()).ifPresent(s -> {
            vo.setSubjectCode(s.getCode());
            vo.setSubjectName(s.getName());
        });
        counterpartyRepository.findById(vo.getCounterpartyId()).ifPresent(c -> {
            vo.setCounterpartyName(c.getName());
        });
        return vo;
    }

    private List<FactEventVO> enrichVOs(List<FactEventVO> vos) {
        if (vos.isEmpty()) return vos;

        Set<Long> subjectIds = vos.stream().map(FactEventVO::getSubjectId).collect(Collectors.toSet());
        Set<Long> cpIds = vos.stream().map(FactEventVO::getCounterpartyId).collect(Collectors.toSet());

        Map<Long, AccountSubject> subjectMap = subjectIds.stream()
                .map(id -> subjectRepository.findById(id).orElse(null))
                .filter(s -> s != null)
                .collect(Collectors.toMap(AccountSubject::getId, s -> s));

        Map<Long, Counterparty> cpMap = cpIds.stream()
                .map(id -> counterpartyRepository.findById(id).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toMap(Counterparty::getId, c -> c));

        for (FactEventVO vo : vos) {
            AccountSubject s = subjectMap.get(vo.getSubjectId());
            if (s != null) {
                vo.setSubjectCode(s.getCode());
                vo.setSubjectName(s.getName());
            }
            Counterparty c = cpMap.get(vo.getCounterpartyId());
            if (c != null) {
                vo.setCounterpartyName(c.getName());
            }
        }
        return vos;
    }
}
