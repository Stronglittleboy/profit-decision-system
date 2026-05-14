package com.profit.infrastructure.project;

import com.profit.domain.project.Project;
import com.profit.domain.project.ProjectStatus;

public class ProjectConverter {

    private ProjectConverter() {}

    public static Project toDomain(ProjectEntity e) {
        return Project.reconstruct(
                e.getId(), e.getCode(), e.getName(),
                ProjectStatus.fromCode(e.getStatus()),
                e.getBudget(), e.getStartDate(), e.getEndDate(),
                e.getManager(), e.getDescription(), e.getEnabled(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public static ProjectEntity toEntity(Project d) {
        ProjectEntity e = new ProjectEntity();
        e.setId(d.getId());
        e.setCode(d.getCode());
        e.setName(d.getName());
        e.setStatus(d.getStatus().getCode());
        e.setBudget(d.getBudget());
        e.setStartDate(d.getStartDate());
        e.setEndDate(d.getEndDate());
        e.setManager(d.getManager());
        e.setDescription(d.getDescription());
        e.setEnabled(d.getEnabled());
        return e;
    }
}
