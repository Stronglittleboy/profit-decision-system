package com.profit.controller;

import com.profit.application.CounterpartyAppService;
import com.profit.common.api.ApiResponse;
import com.profit.dto.AccountSubjectStatusDTO;
import com.profit.dto.CounterpartyDTO;
import com.profit.vo.CounterpartyVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counterparty")
@RequiredArgsConstructor
public class CounterpartyController {

    private final CounterpartyAppService appService;

    @GetMapping
    public ApiResponse<List<CounterpartyVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        return ApiResponse.ok(appService.list(keyword, type));
    }

    @GetMapping("/{id}")
    public ApiResponse<CounterpartyVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(appService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<CounterpartyVO> create(@Valid @RequestBody CounterpartyDTO dto) {
        return ApiResponse.ok("保存成功", appService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<CounterpartyVO> update(@PathVariable Long id,
                                              @Valid @RequestBody CounterpartyDTO dto) {
        return ApiResponse.ok("保存成功", appService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id,
                                          @Valid @RequestBody AccountSubjectStatusDTO dto) {
        appService.toggleStatus(id, dto.getEnabled());
        return ApiResponse.ok("状态已更新", null);
    }
}
