package com.pdi.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 编号生成工具
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public class CodeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成预警编号 ALM + yyyyMMdd + 序号
     */
    public static String generateAlarmNo(Long sequence) {
        return "ALM" + DateUtil.format(LocalDateTime.now(), "yyyyMMdd") + StrUtil.fillBefore(String.valueOf(sequence), '0', 4);
    }

    /**
     * 生成PDI作业编号 PDI + yyyyMMdd + 序号
     */
    public static String generateTaskNo(Long sequence) {
        return "PDI" + DateUtil.format(LocalDateTime.now(), "yyyyMMdd") + StrUtil.fillBefore(String.valueOf(sequence), '0', 4);
    }

    /**
     * 生成盒子编号 BOX + 序号
     */
    public static String generateBoxCode(Long sequence) {
        return "BOX" + StrUtil.fillBefore(String.valueOf(sequence), '0', 3);
    }

    /**
     * 生成通道编号 CH + 序号
     */
    public static String generateChannelCode(Long sequence) {
        return "CH" + StrUtil.fillBefore(String.valueOf(sequence), '0', 3);
    }

}
