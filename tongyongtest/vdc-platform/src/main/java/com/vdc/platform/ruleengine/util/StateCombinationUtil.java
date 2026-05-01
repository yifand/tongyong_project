package com.vdc.platform.ruleengine.util;

import com.vdc.platform.entity.RuleConfig;

import java.util.Set;

public class StateCombinationUtil {

    private static final Set<Integer> INVALID_COMBINATIONS = Set.of(2, 4, 6, 8, 10, 12, 14);

    public static int compute(boolean vehicle, boolean door, boolean person, boolean entering) {
        int combo = 1;
        if (vehicle) combo += 8;
        if (door) combo += 4;
        if (person) combo += 2;
        if (entering) combo += 1;
        return combo;
    }

    public static boolean isInvalid(int combo) {
        return INVALID_COMBINATIONS.contains(combo);
    }

    public static boolean shouldSkip(boolean vehiclePresent, RuleConfig config) {
        return Boolean.TRUE.equals(config.getRequireVehicle()) && !vehiclePresent;
    }
}
