package com.profit.controller;

import com.profit.application.FactEventAppService;
import com.profit.common.api.ApiResponse;
import com.profit.dto.FactEventDTO;
import com.profit.vo.AmortizationEntryVO;
import com.profit.vo.FactEventVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fact-event")
@RequiredArgsConstructor
public class FactEventController {

    private final FactEventAppService appService;

    @GetMapping
    public ApiResponse<List<FactEventVO>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(appService.list(type, status, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<FactEventVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(appService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<FactEventVO> create(@Valid @RequestBody FactEventDTO dto) {
        return ApiResponse.ok("保存成功", appService.create(dto));
    }

    @PostMapping("/{id}/reverse")
    public ApiResponse<Void> reverse(@PathVariable Long id) {
        appService.reverse(id);
        return ApiResponse.ok("冲正成功", null);
    }

    @GetMapping("/{id}/amortization")
    public ApiResponse<List<AmortizationEntryVO>> amortization(@PathVariable Long id) {
        return ApiResponse.ok(appService.getAmortizationEntries(id));
    }
}
