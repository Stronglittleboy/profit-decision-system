package com.profit.controller;

import com.profit.application.AccountSubjectAppService;
import com.profit.common.api.ApiResponse;
import com.profit.domain.accountsubject.AccountSubjectTreeNode;
import com.profit.dto.AccountSubjectDTO;
import com.profit.dto.AccountSubjectStatusDTO;
import com.profit.vo.AccountSubjectVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-subject")
@RequiredArgsConstructor
public class AccountSubjectController {

    private final AccountSubjectAppService appService;

    @GetMapping("/tree")
    public ApiResponse<List<AccountSubjectTreeNode>> tree(
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(appService.getTree(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountSubjectVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(appService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<AccountSubjectVO> create(@Valid @RequestBody AccountSubjectDTO dto) {
        return ApiResponse.ok("保存成功", appService.createSubject(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountSubjectVO> update(@PathVariable Long id,
                                                @Valid @RequestBody AccountSubjectDTO dto) {
        return ApiResponse.ok("保存成功", appService.updateSubject(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appService.deleteSubject(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id,
                                          @Valid @RequestBody AccountSubjectStatusDTO dto) {
        appService.toggleStatus(id, dto.getEnabled());
        return ApiResponse.ok("状态已更新", null);
    }
}
