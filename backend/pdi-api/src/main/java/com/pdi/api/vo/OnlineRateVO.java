package com.pdi.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 设备在线率VO
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineRateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总计数据
     */
    private OnlineRateItemVO total;

    /**
     * 各站点数据
     */
    private List<OnlineRateItemVO> sites;

    /**
     * 在线率统计项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnlineRateItemVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 站点ID
         */
        private Long siteId;

        /**
         * 站点名称
         */
        private String siteName;

        /**
         * 在线数
         */
        private Integer online;

        /**
         * 离线数
         */
        private Integer offline;

        /**
         * 总数
         */
        private Integer total;

        /**
         * 在线率（百分比）
         */
        private Integer rate;
    }
}
