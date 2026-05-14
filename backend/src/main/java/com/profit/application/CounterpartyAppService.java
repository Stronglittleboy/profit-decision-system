package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.counterparty.*;
import com.profit.dto.CounterpartyDTO;
import com.profit.vo.CounterpartyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounterpartyAppService {

    private final CounterpartyRepository repository;
    private final CounterpartyDomainService domainService;

    @Transactional
    public CounterpartyVO create(CounterpartyDTO dto) {
        domainService.validateNameUnique(dto.getName());

        CounterpartyType type = CounterpartyType.fromCode(dto.getType());
        CreditLevel creditLevel = CreditLevel.fromCode(dto.getCreditLevel());

        Counterparty cp = Counterparty.create(
                dto.getName(), type, dto.getContact(), dto.getPhone(),
                dto.getAddress(), dto.getTaxNo(), creditLevel, dto.getRemark()
        );

        return CounterpartyVO.from(repository.save(cp));
    }

    @Transactional
    public CounterpartyVO update(Long id, CounterpartyDTO dto) {
        Counterparty cp = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "往来方不存在"));

        domainService.validateNameUniqueForUpdate(dto.getName(), id);

        CounterpartyType type = CounterpartyType.fromCode(dto.getType());
        CreditLevel creditLevel = CreditLevel.fromCode(dto.getCreditLevel());

        cp.update(dto.getName(), type, dto.getContact(), dto.getPhone(),
                dto.getAddress(), dto.getTaxNo(), creditLevel, dto.getRemark());

        return CounterpartyVO.from(repository.save(cp));
    }

    @Transactional
    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "往来方不存在"));
        repository.deleteById(id);
    }

    @Transactional
    public void toggleStatus(Long id, boolean enabled) {
        Counterparty cp = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "往来方不存在"));
        if (enabled) {
            cp.enable();
        } else {
            cp.disable();
        }
        repository.save(cp);
    }

    public List<CounterpartyVO> list(String keyword, String type) {
        return repository.search(keyword, type).stream()
                .map(CounterpartyVO::from)
                .collect(Collectors.toList());
    }

    public CounterpartyVO getDetail(Long id) {
        Counterparty cp = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "往来方不存在"));
        return CounterpartyVO.from(cp);
    }
}
