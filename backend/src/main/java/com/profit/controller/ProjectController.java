package com.profit.controller;

import com.profit.application.ProjectAppService;
import com.profit.common.api.ApiResponse;
import com.profit.dto.ProjectDTO;
import com.profit.vo.ProjectPnlVO;
import com.profit.vo.ProjectVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectAppService appService;

    @GetMapping
    public ApiResponse<List<ProjectVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(appService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(appService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<ProjectVO> create(@Valid @RequestBody ProjectDTO dto) {
        return ApiResponse.ok("创建成功", appService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectVO> update(@PathVariable Long id, @Valid @RequestBody ProjectDTO dto) {
        return ApiResponse.ok("更新成功", appService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    @GetMapping("/{id}/pnl")
    public ApiResponse<ProjectPnlVO> pnl(@PathVariable Long id) {
        return ApiResponse.ok(appService.getPnl(id));
    }

    @PostMapping("/{id}/transition")
    public ApiResponse<Void> transition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        appService.transition(id, body.get("action"));
        return ApiResponse.ok("操作成功", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> toggleEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        appService.toggleEnabled(id, body.getOrDefault("enabled", true));
        return ApiResponse.ok("操作成功", null);
    }
}
