package com.pdi.api.controller;

import com.pdi.api.vo.AlarmVO;
import com.pdi.api.vo.DashboardStatisticsVO;
import com.pdi.api.vo.OnlineRateVO;
import com.pdi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作台控制器
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "工作台", description = "工作台统计数据接口")
public class DashboardController {

    // TODO: 注入DashboardService
    // private final DashboardService dashboardService;

    /**
     * 获取今日统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "获取工作台今日统计概览")
    public Result<DashboardStatisticsVO> getStatistics() {
        log.info("获取今日统计数据");
        // TODO: 调用dashboardService.getStatistics()
        return Result.success();
    }

    /**
     * 获取设备在线率
     */
    @GetMapping("/online-rate")
    @Operation(summary = "获取设备在线率", description = "获取各站点设备在线率统计")
    public Result<OnlineRateVO> getOnlineRate() {
        log.info("获取设备在线率");
        // TODO: 调用dashboardService.getOnlineRate()
        return Result.success();
    }

    /**
     * 获取最近报警列表
     */
    @GetMapping("/recent-alarms")
    @Operation(summary = "获取最近报警", description = "获取最近发生的报警(最多10条)")
    @Parameter(name = "limit", description = "返回条数(默认10，最大20)")
    public Result<List<AlarmVO>> getRecentAlarms(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取最近报警列表, limit: {}", limit);
        // TODO: 调用dashboardService.getRecentAlarms(limit)
        return Result.success();
    }
}
