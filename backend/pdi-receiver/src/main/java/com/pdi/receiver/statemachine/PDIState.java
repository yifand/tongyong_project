package com.pdi.receiver.statemachine;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PDI状态机状态枚举
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum PDIState {

    /**
     * 初始状态（门关闭，无人）
     */
    S1("S1", "初始状态", 0, 0, 0),

    /**
     * 门开，无人
     */
    S3("S3", "门开无人", 1, 0, 0),

    /**
     * 门关闭，有人
     */
    S5("S5", "门关有人", 0, 1, 0),

    /**
     * 门开，有人，进入/离开中
     */
    S7("S7", "门开有人-未进出", 1, 1, 0),

    /**
     * 门开，有人，进出中
     */
    S8("S8", "门开有人-进出中", 1, 1, 1);

    private final String code;
    private final String description;
    private final Integer doorOpen;
    private final Integer personPresent;
    private final Integer personEnteringExiting;

    public static PDIState getByCode(String code) {
        for (PDIState state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }

    public static PDIState getByStates(Integer doorOpen, Integer personPresent, Integer personEnteringExiting) {
        for (PDIState state : values()) {
            if (state.getDoorOpen().equals(doorOpen)
                    && state.getPersonPresent().equals(personPresent)
                    && state.getPersonEnteringExiting().equals(personEnteringExiting)) {
                return state;
            }
        }
        return null;
    }

}
