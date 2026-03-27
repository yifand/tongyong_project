package com.pdi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态编码枚举 (PDI状态机)
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum StateCodeEnum {

    S1("S1", "空闲", 0, 0, 0),
    S3("S3", "门关+有人", 0, 1, 0),
    S5("S5", "门开+无人", 1, 0, 0),
    S7("S7", "门开+有人+未进出", 1, 1, 0),
    S8("S8", "门开+有人+进出中", 1, 1, 1);

    private final String code;
    private final String description;
    private final Integer doorOpen;
    private final Integer personPresent;
    private final Integer personEnteringExiting;

    public static StateCodeEnum getByCode(String code) {
        for (StateCodeEnum state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }

    public static StateCodeEnum getByStates(Integer doorOpen, Integer personPresent, Integer personEnteringExiting) {
        for (StateCodeEnum state : values()) {
            if (state.getDoorOpen().equals(doorOpen)
                    && state.getPersonPresent().equals(personPresent)
                    && state.getPersonEnteringExiting().equals(personEnteringExiting)) {
                return state;
            }
        }
        return null;
    }

}
