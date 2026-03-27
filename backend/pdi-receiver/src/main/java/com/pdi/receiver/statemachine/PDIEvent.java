package com.pdi.receiver.statemachine;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PDI状态机事件枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum PDIEvent {

    /**
     * 门打开
     */
    DOOR_OPEN("DOOR_OPEN", "门打开"),

    /**
     * 门关闭
     */
    DOOR_CLOSE("DOOR_CLOSE", "门关闭"),

    /**
     * 检测到人
     */
    PERSON_DETECTED("PERSON_DETECTED", "检测到人"),

    /**
     * 人离开
     */
    PERSON_LEFT("PERSON_LEFT", "人离开"),

    /**
     * 检测到进入
     */
    ENTERING_DETECTED("ENTERING_DETECTED", "检测到进入"),

    /**
     * 检测到离开
     */
    EXITING_DETECTED("EXITING_DETECTED", "检测到离开"),

    /**
     * 进入完成（门关）
     */
    ENTER_COMPLETE("ENTER_COMPLETE", "进入完成"),

    /**
     * 离开完成（门关）
     */
    EXIT_COMPLETE("EXIT_COMPLETE", "离开完成"),

    /**
     * 超时
     */
    TIMEOUT("TIMEOUT", "超时"),

    /**
     * 异常
     */
    ERROR("ERROR", "异常");

    private final String code;
    private final String description;

    public static PDIEvent getByCode(String code) {
        for (PDIEvent event : values()) {
            if (event.getCode().equals(code)) {
                return event;
            }
        }
        return null;
    }

}
