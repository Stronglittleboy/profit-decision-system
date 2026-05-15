package com.profit.controller;

import com.profit.application.BudgetAppService;
import com.profit.common.api.ApiResponse;
import com.profit.dto.BudgetDTO;
import com.profit.vo.BudgetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetAppService appService;

    @GetMapping
    public ApiResponse<List<BudgetVO>> list(@RequestParam(required = false) String period,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String status) {
        return ApiResponse.ok(appService.list(period, category, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<BudgetVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(appService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<BudgetVO> create(@Valid @RequestBody BudgetDTO dto) {
        return ApiResponse.ok("创建成功", appService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetVO> update(@PathVariable Long id, @Valid @RequestBody BudgetDTO dto) {
        return ApiResponse.ok("更新成功", appService.update(id, dto));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        appService.approve(id);
        return ApiResponse.ok("已批准", null);
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Integer>> refresh(@RequestBody Map<String, String> body) {
        int count = appService.refreshActuals(body.get("period"));
        return ApiResponse.ok("刷新完成", Map.of("affected", count));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
