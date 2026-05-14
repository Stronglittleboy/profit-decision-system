package com.profit.controller;

import com.profit.application.ContractAppService;
import com.profit.common.api.ApiResponse;
import com.profit.dto.ContractDTO;
import com.profit.vo.ContractVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractAppService appService;

    @GetMapping
    public ApiResponse<List<ContractVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(appService.list(keyword, type, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ContractVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(appService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<ContractVO> create(@Valid @RequestBody ContractDTO dto) {
        return ApiResponse.ok("创建成功", appService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ContractVO> update(@PathVariable Long id, @Valid @RequestBody ContractDTO dto) {
        return ApiResponse.ok("更新成功", appService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/{id}/transition")
    public ApiResponse<Void> transition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        appService.transition(id, body.get("action"));
        return ApiResponse.ok("操作成功", null);
    }
}
