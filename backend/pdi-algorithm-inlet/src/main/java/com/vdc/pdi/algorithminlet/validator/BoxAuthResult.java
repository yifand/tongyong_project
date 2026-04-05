package com.vdc.pdi.algorithminlet.validator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 盒子认证结果
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BoxAuthResult {

    /**
     * 是否认证通过
     */
    private final boolean valid;

    /**
     * 站点ID
     */
    private final Long siteId;

    /**
     * 盒子记录ID
     */
    private final Long boxRecordId;

    /**
     * 错误信息
     */
    private final String errorMessage;

    public static BoxAuthResult valid(Long siteId, Long boxRecordId) {
        return new BoxAuthResult(true, siteId, boxRecordId, null);
    }

    public static BoxAuthResult invalid(String errorMessage) {
        return new BoxAuthResult(false, null, null, errorMessage);
    }
}
