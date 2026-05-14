package com.profit.controller;

import com.profit.application.ReceivableAppService;
import com.profit.common.api.ApiResponse;
import com.profit.dto.ReceivableDTO;
import com.profit.vo.ReceivableVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receivable")
@RequiredArgsConstructor
public class ReceivableController {

    private final ReceivableAppService appService;

    @GetMapping
    public ApiResponse<List<ReceivableVO>> list(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status) {
        return ApiResponse.ok(appService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReceivableVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(appService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<ReceivableVO> create(@Valid @RequestBody ReceivableDTO dto) {
        return ApiResponse.ok("创建成功", appService.create(dto));
    }

    @PostMapping("/{id}/payment")
    public ApiResponse<Void> recordPayment(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        appService.recordPayment(id, body.get("amount"));
        return ApiResponse.ok("登记成功", null);
    }

    @PostMapping("/{id}/overdue")
    public ApiResponse<Void> markOverdue(@PathVariable Long id) {
        appService.markOverdue(id);
        return ApiResponse.ok("已标记逾期", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
