package com.vdc.pdi.common.enums;

/**
 * PDI状态码枚举
 * 基于状态三元组 (door_open, person_present, entering_exiting)
 */
public enum StateCodeEnum implements EnumCode<Integer> {

    /**
     * S1: 门关 + 无人 + 未进出 (空闲状态)
     */
    S1(1, "空闲", 0, 0, 0),

    /**
     * S3: 门关 + 有人 + 未进出 (人员在车内)
     */
    S3(3, "门关有人", 0, 1, 0),

    /**
     * S5: 门开 + 无人 + 未进出 (人员已离开)
     */
    S5(5, "门开无人", 1, 0, 0),

    /**
     * S7: 门开 + 有人 + 未进出 (准备进出)
     */
    S7(7, "门开有人", 1, 1, 0),

    /**
     * S8: 门开 + 有人 + 进出中 (正在进出)
     */
    S8(8, "进出中", 1, 1, 1);

    private final Integer code;
    private final String description;
    private final int doorOpen;
    private final int personPresent;
    private final int enteringExiting;

    StateCodeEnum(Integer code, String description, int doorOpen, int personPresent, int enteringExiting) {
        this.code = code;
        this.description = description;
        this.doorOpen = doorOpen;
        this.personPresent = personPresent;
        this.enteringExiting = enteringExiting;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return description;
    }

    public String getDescription() {
        return description;
    }

    public int getDoorOpen() {
        return doorOpen;
    }

    public int getPersonPresent() {
        return personPresent;
    }

    public int getEnteringExiting() {
        return enteringExiting;
    }

    /**
     * 根据编码获取枚举
     */
    public static StateCodeEnum fromCode(Integer code) {
        for (StateCodeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid state code: " + code);
    }

    /**
     * 根据状态三元组获取枚举
     */
    public static StateCodeEnum fromState(int doorOpen, int personPresent, int enteringExiting) {
        for (StateCodeEnum value : values()) {
            if (value.doorOpen == doorOpen
                    && value.personPresent == personPresent
                    && value.enteringExiting == enteringExiting) {
                return value;
            }
        }
        throw new IllegalArgumentException(
                String.format("Invalid state: door=%d, person=%d, entering=%d",
                        doorOpen, personPresent, enteringExiting));
    }
}
