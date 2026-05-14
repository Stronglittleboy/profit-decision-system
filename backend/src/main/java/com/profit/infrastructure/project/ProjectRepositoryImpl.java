package com.profit.infrastructure.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.profit.domain.project.Project;
import com.profit.domain.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepository {

    private final ProjectMapper mapper;

    @Override
    public Optional<Project> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(ProjectConverter::toDomain);
    }

    @Override
    public Optional<Project> findByCode(String code) {
        LambdaQueryWrapper<ProjectEntity> w = new LambdaQueryWrapper<>();
        w.eq(ProjectEntity::getCode, code);
        return Optional.ofNullable(mapper.selectOne(w))
                .map(ProjectConverter::toDomain);
    }

    @Override
    public List<Project> search(String keyword, String status) {
        LambdaQueryWrapper<ProjectEntity> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(ProjectEntity::getCode, keyword)
                    .or().like(ProjectEntity::getName, keyword));
        }
        if (status != null && !status.isBlank()) {
            w.eq(ProjectEntity::getStatus, status);
        }
        w.orderByDesc(ProjectEntity::getCreatedAt);
        return mapper.selectList(w).stream()
                .map(ProjectConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = ProjectConverter.toEntity(project);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return ProjectConverter.toDomain(mapper.selectById(entity.getId()));
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<ProjectEntity> w = new LambdaQueryWrapper<>();
        w.eq(ProjectEntity::getCode, code);
        return mapper.selectCount(w) > 0;
    }

    @Override
    public boolean existsByCodeExcludeId(String code, Long id) {
        LambdaQueryWrapper<ProjectEntity> w = new LambdaQueryWrapper<>();
        w.eq(ProjectEntity::getCode, code).ne(ProjectEntity::getId, id);
        return mapper.selectCount(w) > 0;
    }
}
