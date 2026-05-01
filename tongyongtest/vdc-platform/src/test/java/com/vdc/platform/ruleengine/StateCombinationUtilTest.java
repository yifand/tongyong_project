package com.vdc.platform.ruleengine;

import com.vdc.platform.ruleengine.util.StateCombinationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateCombinationUtilTest {

    @Test
    void testComputeAllFalse() {
        assertEquals(1, StateCombinationUtil.compute(false, false, false, false));
    }

    @Test
    void testComputeAllTrue() {
        assertEquals(16, StateCombinationUtil.compute(true, true, true, true));
    }

    @Test
    void testComputeMixed() {
        assertEquals(11, StateCombinationUtil.compute(true, false, true, false));
    }

    @Test
    void testIsInvalidTrue() {
        assertTrue(StateCombinationUtil.isInvalid(2));
        assertTrue(StateCombinationUtil.isInvalid(4));
        assertTrue(StateCombinationUtil.isInvalid(6));
        assertTrue(StateCombinationUtil.isInvalid(14));
    }

    @Test
    void testIsInvalidFalse() {
        assertFalse(StateCombinationUtil.isInvalid(1));
        assertFalse(StateCombinationUtil.isInvalid(11));
        assertFalse(StateCombinationUtil.isInvalid(15));
        assertFalse(StateCombinationUtil.isInvalid(16));
    }
}
