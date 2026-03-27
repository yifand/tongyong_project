package com.pdi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 算法配置DTO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门ROI区域 [x, y, width, height]
     */
    private List<Integer> doorRoi;

    /**
     * 人员ROI区域 [x, y, width, height]
     */
    private List<Integer> personRoi;

    /**
     * 置信度阈值
     */
    private Double confidenceThreshold;

    /**
     * 最小人员尺寸 [width, height]
     */
    private List<Integer> minPersonSize;

    /**
     * 门开启阈值
     */
    private Double doorOpenThreshold;
}
