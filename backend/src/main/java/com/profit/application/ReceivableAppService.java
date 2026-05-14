package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.common.PaymentRecord;
import com.profit.domain.common.PaymentRecordRepository;
import com.profit.domain.common.PaymentStatus;
import com.profit.domain.contract.ContractRepository;
import com.profit.domain.counterparty.CounterpartyRepository;
import com.profit.domain.receivable.Receivable;
import com.profit.domain.receivable.ReceivableRepository;
import com.profit.dto.PaymentRecordDTO;
import com.profit.dto.ReceivableDTO;
import com.profit.vo.PaymentRecordVO;
import com.profit.vo.ReceivableVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceivableAppService {

    private final ReceivableRepository repository;
    private final CounterpartyRepository counterpartyRepository;
    private final ContractRepository contractRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    public List<ReceivableVO> list(String keyword, String status) {
        List<Receivable> items = repository.search(keyword, status);
        return items.stream().map(this::enrichVO).collect(Collectors.toList());
    }

    public ReceivableVO getDetail(Long id) {
        return enrichVO(findOrThrow(id));
    }

    @Transactional
    public ReceivableVO create(ReceivableDTO dto) {
        if (repository.existsByCode(dto.getCode())) throw new BusinessException(40002, "单据编号已存在");
        counterpartyRepository.findById(dto.getCounterpartyId())
                .orElseThrow(() -> new BusinessException(40401, "客户不存在"));
        if (dto.getContractId() != null) contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new BusinessException(40401, "合同不存在"));

        Receivable r = Receivable.create(dto.getCode(), dto.getCounterpartyId(), dto.getContractId(),
                dto.getAmount(), dto.getDueDate(), dto.getRemark());
        return enrichVO(repository.save(r));
    }

    @Transactional
    public void recordPayment(Long id, PaymentRecordDTO dto) {
        Receivable r = findOrThrow(id);
        r.recordPayment(dto.getAmount());
        repository.save(r);
        paymentRecordRepository.save(PaymentRecord.create("receivable", id, dto.getAmount(), dto.getPayDate(), dto.getRemark()));
    }

    public List<PaymentRecordVO> getPaymentRecords(Long id) {
        return paymentRecordRepository.findByBiz("receivable", id).stream()
                .map(PaymentRecordVO::from).collect(Collectors.toList());
    }

    @Transactional
    public void markOverdue(Long id) {
        Receivable r = findOrThrow(id);
        r.markOverdue();
        repository.save(r);
    }

    @Transactional
    public int batchMarkOverdue() {
        LocalDate today = LocalDate.now();
        List<Receivable> candidates = repository.search(null, null).stream()
                .filter(r -> r.getStatus() != PaymentStatus.PAID && r.getStatus() != PaymentStatus.OVERDUE)
                .filter(r -> r.getDueDate().isBefore(today))
                .collect(Collectors.toList());
        for (Receivable r : candidates) {
            r.markOverdue();
            repository.save(r);
        }
        return candidates.size();
    }

    @Transactional
    public void delete(Long id) {
        findOrThrow(id);
        repository.deleteById(id);
    }

    private Receivable findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(40401, "应收记录不存在"));
    }

    private ReceivableVO enrichVO(Receivable r) {
        ReceivableVO vo = ReceivableVO.from(r);
        counterpartyRepository.findById(r.getCounterpartyId()).ifPresent(c -> vo.setCounterpartyName(c.getName()));
        if (r.getContractId() != null) contractRepository.findById(r.getContractId())
                .ifPresent(c -> vo.setContractName(c.getName()));
        long aging = ChronoUnit.DAYS.between(r.getCreatedAt().toLocalDate(), LocalDate.now());
        vo.setAgingDays(aging);
        return vo;
    }
}
