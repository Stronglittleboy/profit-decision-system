package com.profit.domain.project;

import com.profit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProjectDomainService {

    private final ProjectRepository repository;

    public void validateCodeUnique(String code) {
        if (repository.existsByCode(code)) {
            throw new BusinessException(40002, "项目编号已存在");
        }
    }

    public void validateCodeUniqueForUpdate(String code, Long id) {
        if (repository.existsByCodeExcludeId(code, id)) {
            throw new BusinessException(40002, "项目编号已存在");
        }
    }

    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(40003, "结束日期不能早于开始日期");
        }
    }
}
