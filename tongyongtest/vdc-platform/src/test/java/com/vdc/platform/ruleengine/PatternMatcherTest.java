package com.vdc.platform.ruleengine;

import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.entity.StateStream;
import com.vdc.platform.ruleengine.core.PatternMatcher;
import com.vdc.platform.ruleengine.enums.RuleEventType;
import com.vdc.platform.ruleengine.model.ChannelStateContext;
import com.vdc.platform.ruleengine.model.RuleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatternMatcherTest {

    private PatternMatcher patternMatcher;
    private ChannelStateContext ctx;
    private RuleConfig config;

    @BeforeEach
    void setUp() {
        patternMatcher = new PatternMatcher();
        ctx = new ChannelStateContext();
        ctx.setChannelId("ch-001");

        config = new RuleConfig();
        config.setEnterPattern(List.of(15, 16, 11));
        config.setExitPattern(List.of(11, 16, 15, List.of(13, 9)));
        config.setRequireVehicle(false);
        config.setPersonAbsentTimeout(0);
    }

    @Test
    void testEnterSequenceMatches() {
        pushState(15);
        pushState(16);
        RuleEvent event = pushState(11);

        assertNotNull(event);
        assertEquals(RuleEventType.ENTER, event.getEventType());
        assertEquals("ch-001", event.getChannelId());
    }

    @Test
    void testExitSequenceMatches13() {
        // First enter
        pushState(15);
        pushState(16);
        pushState(11);

        // Then exit with 13
        pushState(11);
        pushState(16);
        pushState(15);
        RuleEvent event = pushState(13);

        assertNotNull(event);
        assertEquals(RuleEventType.EXIT, event.getEventType());
        assertEquals("ch-001", event.getChannelId());
    }

    @Test
    void testExitSequenceMatches9() {
        // First enter
        pushState(15);
        pushState(16);
        pushState(11);

        // Then exit with 9
        pushState(11);
        pushState(16);
        pushState(15);
        RuleEvent event = pushState(9);

        assertNotNull(event);
        assertEquals(RuleEventType.EXIT, event.getEventType());
        assertEquals("ch-001", event.getChannelId());
    }

    @Test
    void testInvalidCombosSkipped() {
        // The window contains [15, 2, 16, 4, 11]; subsequence [15, 16, 11] should still match
        pushState(15);
        pushState(2);
        pushState(16);
        pushState(4);
        RuleEvent event = pushState(11);

        assertNotNull(event);
        assertEquals(RuleEventType.ENTER, event.getEventType());
    }

    @Test
    void testRequireVehicleTrueWithoutVehicleCausesSkip() {
        config.setRequireVehicle(true);

        pushState(15);
        pushState(16);
        RuleEvent event = pushStateWithVehicle(11, false);

        assertNull(event);
        assertTrue(ctx.getWindow().isEmpty());
    }

    @Test
    void testRandomSequenceNoMatch() {
        pushState(1);
        pushState(2);
        pushState(4);
        pushState(6);
        pushState(8);

        RuleEvent event = patternMatcher.match(ctx, createState(10), config);
        assertNull(event);
    }

    private RuleEvent pushState(int combo) {
        return patternMatcher.match(ctx, createState(combo), config);
    }

    private RuleEvent pushState(int combo, LocalDateTime ts) {
        return patternMatcher.match(ctx, createState(combo, ts), config);
    }

    private RuleEvent pushStateWithVehicle(int combo, boolean vehiclePresent) {
        return patternMatcher.match(ctx, createState(combo, vehiclePresent), config);
    }

    private StateStream createState(int combo) {
        return createState(combo, LocalDateTime.now());
    }

    private StateStream createState(int combo, LocalDateTime ts) {
        return createState(combo, true, ts);
    }

    private StateStream createState(int combo, boolean vehiclePresent) {
        return createState(combo, vehiclePresent, LocalDateTime.now());
    }

    private StateStream createState(int combo, boolean vehiclePresent, LocalDateTime ts) {
        StateStream s = new StateStream();
        s.setChannelId("ch-001");
        s.setStateCombination(combo);
        s.setVehiclePresent(vehiclePresent);
        s.setTs(ts);
        return s;
    }
}
