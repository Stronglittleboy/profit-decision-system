package com.profit.application;

import com.profit.common.exception.BusinessException;
import com.profit.domain.project.Project;
import com.profit.domain.project.ProjectDomainService;
import com.profit.domain.project.ProjectRepository;
import com.profit.domain.project.ProjectStatus;
import com.profit.dto.ProjectDTO;
import com.profit.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectAppService {

    private final ProjectRepository repository;
    private final ProjectDomainService domainService;

    public List<ProjectVO> list(String keyword, String status) {
        return repository.search(keyword, status).stream()
                .map(ProjectVO::from)
                .collect(Collectors.toList());
    }

    public ProjectVO getDetail(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        return ProjectVO.from(project);
    }

    @Transactional
    public ProjectVO create(ProjectDTO dto) {
        domainService.validateCodeUnique(dto.getCode());
        domainService.validateDateRange(dto.getStartDate(), dto.getEndDate());

        Project project = Project.create(
                dto.getCode(), dto.getName(), dto.getBudget(),
                dto.getStartDate(), dto.getEndDate(),
                dto.getManager(), dto.getDescription()
        );
        Project saved = repository.save(project);
        return ProjectVO.from(saved);
    }

    @Transactional
    public ProjectVO update(Long id, ProjectDTO dto) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        domainService.validateCodeUniqueForUpdate(dto.getCode(), id);
        domainService.validateDateRange(dto.getStartDate(), dto.getEndDate());

        project.update(dto.getName(), dto.getBudget(),
                dto.getStartDate(), dto.getEndDate(),
                dto.getManager(), dto.getDescription());
        Project saved = repository.save(project);
        return ProjectVO.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new BusinessException(40005, "已完成的项目不能删除");
        }
        repository.deleteById(id);
    }

    @Transactional
    public void transition(Long id, String action) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        switch (action) {
            case "start" -> project.start();
            case "complete" -> project.complete();
            case "suspend" -> project.suspend();
            case "resume" -> project.resume();
            default -> throw new BusinessException(400, "无效的操作: " + action);
        }
        repository.save(project);
    }

    @Transactional
    public void toggleEnabled(Long id, boolean enabled) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40401, "项目不存在"));
        if (enabled) {
            project.enable();
        } else {
            project.disable();
        }
        repository.save(project);
    }
}
