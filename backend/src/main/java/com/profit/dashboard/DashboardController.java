package com.profit.dashboard;

import com.profit.common.api.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/summary")
    public ApiResponse<DashboardSummary> summary() {
        DashboardSummary summary = new DashboardSummary(
                "基础框架已就绪",
                List.of(
                        new DashboardMetric("后端", "Spring Boot + JDK 21", "Maven / MyBatis-Plus / Lombok / Hutool"),
                        new DashboardMetric("前端", "Vue 3 + Router + Element Plus", "Vite 构建，布局已接入"),
                        new DashboardMetric("鉴权", "Token 会话", "登录后才能访问受保护接口")),
                List.of(
                        "后端基础工程和统一返回值已落地",
                        "前端登录页与主布局已搭建",
                        "后续业务模块可直接挂载到现有骨架"));
        return ApiResponse.ok(summary);
    }
}
