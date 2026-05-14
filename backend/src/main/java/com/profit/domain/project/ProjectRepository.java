package com.profit.domain.project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {

    Optional<Project> findById(Long id);

    Optional<Project> findByCode(String code);

    List<Project> search(String keyword, String status);

    Project save(Project project);

    void deleteById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeExcludeId(String code, Long id);
}
