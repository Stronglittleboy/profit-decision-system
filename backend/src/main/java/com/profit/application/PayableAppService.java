package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.common.PaymentRecord;
import com.profit.domain.common.PaymentRecordRepository;
import com.profit.domain.common.PaymentStatus;
import com.profit.domain.contract.ContractRepository;
import com.profit.domain.counterparty.CounterpartyRepository;
import com.profit.domain.payable.Payable;
import com.profit.domain.payable.PayableRepository;
import com.profit.dto.PayableDTO;
import com.profit.dto.PaymentRecordDTO;
import com.profit.vo.PayableVO;
import com.profit.vo.PaymentRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayableAppService {

    private final PayableRepository repository;
    private final CounterpartyRepository counterpartyRepository;
    private final ContractRepository contractRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    public List<PayableVO> list(String keyword, String status) {
        return repository.search(keyword, status).stream().map(this::enrichVO).collect(Collectors.toList());
    }

    public PayableVO getDetail(Long id) { return enrichVO(findOrThrow(id)); }

    @Transactional
    public PayableVO create(PayableDTO dto) {
        if (repository.existsByCode(dto.getCode())) throw new BusinessException(40002, "单据编号已存在");
        counterpartyRepository.findById(dto.getCounterpartyId())
                .orElseThrow(() -> new BusinessException(40401, "供应商不存在"));
        if (dto.getContractId() != null) contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new BusinessException(40401, "合同不存在"));
        Payable p = Payable.create(dto.getCode(), dto.getCounterpartyId(), dto.getContractId(),
                dto.getAmount(), dto.getDueDate(), dto.getRemark());
        return enrichVO(repository.save(p));
    }

    @Transactional
    public void recordPayment(Long id, PaymentRecordDTO dto) {
        Payable p = findOrThrow(id);
        p.recordPayment(dto.getAmount());
        repository.save(p);
        paymentRecordRepository.save(PaymentRecord.create("payable", id, dto.getAmount(), dto.getPayDate(), dto.getRemark()));
    }

    public List<PaymentRecordVO> getPaymentRecords(Long id) {
        return paymentRecordRepository.findByBiz("payable", id).stream()
                .map(PaymentRecordVO::from).collect(Collectors.toList());
    }

    @Transactional
    public void markOverdue(Long id) {
        Payable p = findOrThrow(id); p.markOverdue(); repository.save(p);
    }

    @Transactional
    public int batchMarkOverdue() {
        LocalDate today = LocalDate.now();
        List<Payable> candidates = repository.search(null, null).stream()
                .filter(p -> p.getStatus() != PaymentStatus.PAID && p.getStatus() != PaymentStatus.OVERDUE)
                .filter(p -> p.getDueDate().isBefore(today))
                .collect(Collectors.toList());
        for (Payable p : candidates) { p.markOverdue(); repository.save(p); }
        return candidates.size();
    }

    @Transactional
    public void delete(Long id) { findOrThrow(id); repository.deleteById(id); }

    private Payable findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(40401, "应付记录不存在"));
    }

    private PayableVO enrichVO(Payable p) {
        PayableVO vo = PayableVO.from(p);
        counterpartyRepository.findById(p.getCounterpartyId()).ifPresent(c -> vo.setCounterpartyName(c.getName()));
        if (p.getContractId() != null) contractRepository.findById(p.getContractId())
                .ifPresent(c -> vo.setContractName(c.getName()));
        long aging = ChronoUnit.DAYS.between(p.getCreatedAt().toLocalDate(), LocalDate.now());
        vo.setAgingDays(aging);
        return vo;
    }
}
