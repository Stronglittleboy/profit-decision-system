package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.contract.*;
import com.profit.domain.counterparty.Counterparty;
import com.profit.domain.counterparty.CounterpartyRepository;
import com.profit.domain.project.Project;
import com.profit.domain.project.ProjectRepository;
import com.profit.dto.ContractDTO;
import com.profit.vo.ContractVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractAppService {

    private final ContractRepository repository;
    private final ContractDomainService domainService;
    private final CounterpartyRepository counterpartyRepository;
    private final ProjectRepository projectRepository;

    public List<ContractVO> list(String keyword, String type, String status) {
        List<Contract> contracts = repository.search(keyword, type, status);
        List<ContractVO> vos = contracts.stream().map(ContractVO::from).collect(Collectors.toList());
        return enrichVOs(vos);
    }

    public ContractVO getDetail(Long id) {
        Contract c = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "合同不存在"));
        return enrichVO(ContractVO.from(c));
    }

    @Transactional
    public ContractVO create(ContractDTO dto) {
        domainService.validateCodeUnique(dto.getCode());
        domainService.validateCounterpartyExists(dto.getCounterpartyId());
        domainService.validateProjectExists(dto.getProjectId());
        domainService.validateDateRange(dto.getStartDate(), dto.getEndDate());

        Contract contract = Contract.create(
                dto.getCode(), dto.getName(), dto.getCounterpartyId(), dto.getProjectId(),
                ContractType.fromCode(dto.getType()), dto.getAmount(),
                dto.getSignDate(), dto.getStartDate(), dto.getEndDate(), dto.getRemark()
        );
        Contract saved = repository.save(contract);
        return enrichVO(ContractVO.from(saved));
    }

    @Transactional
    public ContractVO update(Long id, ContractDTO dto) {
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "合同不存在"));
        domainService.validateCodeUniqueForUpdate(dto.getCode(), id);
        domainService.validateCounterpartyExists(dto.getCounterpartyId());
        domainService.validateProjectExists(dto.getProjectId());
        domainService.validateDateRange(dto.getStartDate(), dto.getEndDate());

        contract.update(dto.getName(), dto.getCounterpartyId(), dto.getProjectId(),
                ContractType.fromCode(dto.getType()), dto.getAmount(),
                dto.getSignDate(), dto.getStartDate(), dto.getEndDate(), dto.getRemark());
        Contract saved = repository.save(contract);
        return enrichVO(ContractVO.from(saved));
    }

    @Transactional
    public void delete(Long id) {
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "合同不存在"));
        if (!contract.isDeletable()) {
            throw new BusinessException(40005, "当前状态不允许删除");
        }
        repository.deleteById(id);
    }

    @Transactional
    public void transition(Long id, String action) {
        Contract contract = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "合同不存在"));
        switch (action) {
            case "activate" -> contract.activate();
            case "complete" -> contract.complete();
            case "terminate" -> contract.terminate();
            default -> throw new BusinessException(400, "无效的操作: " + action);
        }
        repository.save(contract);
    }

    private ContractVO enrichVO(ContractVO vo) {
        counterpartyRepository.findById(vo.getCounterpartyId())
                .ifPresent(c -> vo.setCounterpartyName(c.getName()));
        if (vo.getProjectId() != null) {
            projectRepository.findById(vo.getProjectId())
                    .ifPresent(p -> vo.setProjectName(p.getName()));
        }
        return vo;
    }

    private List<ContractVO> enrichVOs(List<ContractVO> vos) {
        if (vos.isEmpty()) return vos;

        Set<Long> cpIds = vos.stream().map(ContractVO::getCounterpartyId).collect(Collectors.toSet());
        Set<Long> pjIds = vos.stream().map(ContractVO::getProjectId)
                .filter(id -> id != null).collect(Collectors.toSet());

        Map<Long, Counterparty> cpMap = cpIds.stream()
                .map(id -> counterpartyRepository.findById(id).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toMap(Counterparty::getId, c -> c));

        Map<Long, Project> pjMap = pjIds.isEmpty() ? Map.of() : pjIds.stream()
                .map(id -> projectRepository.findById(id).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(Project::getId, p -> p));

        for (ContractVO vo : vos) {
            Counterparty cp = cpMap.get(vo.getCounterpartyId());
            if (cp != null) vo.setCounterpartyName(cp.getName());
            if (vo.getProjectId() != null) {
                Project pj = pjMap.get(vo.getProjectId());
                if (pj != null) vo.setProjectName(pj.getName());
            }
        }
        return vos;
    }
}
