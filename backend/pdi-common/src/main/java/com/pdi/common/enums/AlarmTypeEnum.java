package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报警类型枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum AlarmTypeEnum {

    PDI_TIMEOUT(1, "PDI超时", "PDI作业时长不足"),
    SMOKING(2, "违规吸烟", "检测到违规吸烟行为"),
    DOOR_ABNORMAL(3, "门异常开启", "检测到门异常开启"),
    PERSON_ABNORMAL(4, "人员异常", "检测到人员异常行为");

    private final Integer code;
    private final String name;
    private final String defaultTitle;

    public static AlarmTypeEnum getByCode(Integer code) {
        for (AlarmTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
