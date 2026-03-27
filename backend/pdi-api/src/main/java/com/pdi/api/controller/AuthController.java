package com.pdi.api.controller;

import com.pdi.api.aspect.OperationLog;
import com.pdi.api.dto.CaptchaVO;
import com.pdi.api.dto.LoginDTO;
import com.pdi.api.dto.RefreshTokenDTO;
import com.pdi.api.vo.LoginVO;
import com.pdi.api.vo.UserVO;
import com.pdi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户登录、登出、Token刷新等接口")
public class AuthController {

    // TODO: 注入AuthService
    // private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        // TODO: 调用authService.login(loginDTO)
        return Result.success();
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @OperationLog(module = "认证管理", operation = "用户登出")
    @Operation(summary = "用户登出", description = "用户登出，使当前Token失效")
    public Result<Void> logout() {
        log.info("用户登出");
        // TODO: 调用authService.logout()
        return Result.success();
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        log.info("刷新Token");
        // TODO: 调用authService.refreshToken(refreshTokenDTO)
        return Result.success();
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取图形验证码")
    public Result<CaptchaVO> getCaptcha() {
        log.info("获取验证码");
        // TODO: 调用authService.generateCaptcha()
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    public Result<UserVO> getUserInfo() {
        log.info("获取当前用户信息");
        // TODO: 调用authService.getCurrentUserInfo()
        return Result.success();
    }
}
