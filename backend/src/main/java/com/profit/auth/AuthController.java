package com.profit.auth;

import com.profit.common.api.ApiResponse;
import com.profit.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthTokenResolver tokenResolver;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUser> me(HttpServletRequest request) {
        CurrentUser currentUser = (CurrentUser) request.getAttribute(AuthConstants.REQUEST_USER_ATTR);
        if (currentUser == null) {
            throw new BusinessException(401, "未获取到登录信息");
        }
        return ApiResponse.ok(currentUser);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(tokenResolver.resolve(request));
        return ApiResponse.ok("退出成功", (Void) null);
    }
}
