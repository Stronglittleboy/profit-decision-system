package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.accountsubject.AccountSubject;
import com.profit.domain.accountsubject.AccountSubjectRepository;
import com.profit.domain.counterparty.Counterparty;
import com.profit.domain.counterparty.CounterpartyRepository;
import com.profit.domain.factevent.*;
import com.profit.dto.FactEventDTO;
import com.profit.vo.AmortizationEntryVO;
import com.profit.vo.FactEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactEventAppService {

    private final FactEventRepository repository;
    private final FactEventDomainService domainService;
    private final AmortizationEntryRepository amortizationRepo;
    private final AccountSubjectRepository subjectRepository;
    private final CounterpartyRepository counterpartyRepository;

    @Transactional
    public FactEventVO create(FactEventDTO dto) {
        domainService.validateSubjectExists(dto.getSubjectId());
        domainService.validateCounterpartyExists(dto.getCounterpartyId());

        FactType type = FactType.fromCode(dto.getType());
        CostCategory costCategory = CostCategory.fromCode(dto.getCostCategory());

        if (dto.getAmortizeStart() != null && dto.getAmortizeEnd() != null) {
            if (dto.getAmortizeEnd().isBefore(dto.getAmortizeStart())) {
                throw new BusinessException(400, "分摊结束日期不能早于开始日期");
            }
        }

        FactEvent event = FactEvent.create(
                type, dto.getAmount(),
                dto.getBusinessDate(), dto.getAccountingDate(),
                dto.getSubjectId(), dto.getCounterpartyId(),
                costCategory,
                dto.getAmortizeStart(), dto.getAmortizeEnd(),
                dto.getAmortizeMethod() != null ? dto.getAmortizeMethod() : (dto.getAmortizeStart() != null ? "linear" : null),
                dto.getInvoiceNo(), dto.getRemark()
        );

        FactEvent saved = repository.save(event);

        if (saved.isAmortizable()) {
            generateAmortizationEntries(saved);
        }

        return enrichVO(FactEventVO.from(saved));
    }

    @Transactional
    public void reverse(Long id) {
        FactEvent event = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "收支记录不存在"));
        domainService.validateNotAlreadyReversed(event);
        event.reverse();
        repository.save(event);
        amortizationRepo.deleteByFactEventId(id);
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

    public List<AmortizationEntryVO> getAmortizationEntries(Long factEventId) {
        return amortizationRepo.findByFactEventId(factEventId).stream()
                .map(AmortizationEntryVO::from).collect(Collectors.toList());
    }

    private void generateAmortizationEntries(FactEvent event) {
        int months = event.getAmortizeMonths();
        if (months <= 0) return;

        BigDecimal monthly = event.getAmount().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        BigDecimal totalAllocated = monthly.multiply(BigDecimal.valueOf(months - 1));
        BigDecimal lastMonth = event.getAmount().subtract(totalAllocated);

        List<AmortizationEntry> entries = new ArrayList<>();
        YearMonth cursor = YearMonth.from(event.getAmortizeStart());
        for (int i = 0; i < months; i++) {
            BigDecimal amt = (i == months - 1) ? lastMonth : monthly;
            entries.add(AmortizationEntry.create(event.getId(), cursor.toString(), amt));
            cursor = cursor.plusMonths(1);
        }
        amortizationRepo.saveAll(entries);
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
            if (s != null) { vo.setSubjectCode(s.getCode()); vo.setSubjectName(s.getName()); }
            Counterparty c = cpMap.get(vo.getCounterpartyId());
            if (c != null) { vo.setCounterpartyName(c.getName()); }
        }
        return vos;
    }
}
