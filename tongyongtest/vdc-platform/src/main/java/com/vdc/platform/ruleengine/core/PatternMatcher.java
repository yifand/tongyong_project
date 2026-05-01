package com.vdc.platform.ruleengine.core;

import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.entity.StateStream;
import com.vdc.platform.ruleengine.enums.RuleEventType;
import com.vdc.platform.ruleengine.model.ChannelStateContext;
import com.vdc.platform.ruleengine.model.RuleEvent;
import com.vdc.platform.ruleengine.util.StateCombinationUtil;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PatternMatcher {

    public RuleEvent match(ChannelStateContext ctx, StateStream newState, RuleConfig config) {
        ctx.addState(newState);

        if (StateCombinationUtil.shouldSkip(Boolean.TRUE.equals(newState.getVehiclePresent()), config)) {
            ctx.clearWindow();
            if (ctx.getState() != ChannelStateContext.State.IDLE) {
                ctx.setState(ChannelStateContext.State.IDLE);
            }
            return null;
        }

        updatePersonTracking(ctx, newState);

        RuleEvent violation = checkViolations(ctx, newState, config);
        if (violation != null) {
            return violation;
        }

        return switch (ctx.getState()) {
            case IDLE, ENTERING -> matchEnter(ctx, newState, config);
            case WORKING, EXITING -> matchExit(ctx, newState, config);
        };
    }

    private void updatePersonTracking(ChannelStateContext ctx, StateStream newState) {
        if (Boolean.TRUE.equals(newState.getPersonPresent())) {
            ctx.updatePersonPresentTimestamp(newState.getTs());
            if (ctx.getState() == ChannelStateContext.State.WORKING && ctx.isPersonAbsentViolationTriggered()) {
                ctx.resetViolationFlag();
            }
        }
    }

    private RuleEvent checkViolations(ChannelStateContext ctx, StateStream newState, RuleConfig config) {
        if (ctx.getState() != ChannelStateContext.State.WORKING) {
            return null;
        }

        RuleEvent personAbsentViolation = checkPersonAbsentViolation(ctx, newState, config);
        if (personAbsentViolation != null) {
            return personAbsentViolation;
        }

        return checkDurationViolation(ctx, newState, config);
    }

    private RuleEvent checkPersonAbsentViolation(ChannelStateContext ctx, StateStream newState, RuleConfig config) {
        if (config.getPersonAbsentTimeout() == null || config.getPersonAbsentTimeout() <= 0) {
            return null;
        }
        if (Boolean.TRUE.equals(newState.getPersonPresent())) {
            return null;
        }
        if (ctx.isPersonAbsentViolationTriggered()) {
            return null;
        }
        if (ctx.getLastPersonPresentTimestamp() == null) {
            return null;
        }

        long absentSeconds = Duration.between(ctx.getLastPersonPresentTimestamp(), newState.getTs()).getSeconds();
        if (absentSeconds >= config.getPersonAbsentTimeout()) {
            ctx.setPersonAbsentViolationTriggered(true);
            RuleEvent event = new RuleEvent();
            event.setEventType(RuleEventType.VIOLATION);
            event.setChannelId(ctx.getChannelId());
            event.setTimestamp(newState.getTs());
            event.setWorkSessionId(ctx.getCurrentWorkSessionId());
            event.setDescription("Person absent timeout violation: absent for " + absentSeconds + "s, threshold=" + config.getPersonAbsentTimeout() + "s");
            return event;
        }
        return null;
    }

    private RuleEvent checkDurationViolation(ChannelStateContext ctx, StateStream newState, RuleConfig config) {
        if (config.getStandardDuration() == null || config.getStandardDuration() <= 0
                || config.getCriticalThresholdPct() == null) {
            return null;
        }
        if (ctx.getEnterTimestamp() == null) {
            return null;
        }

        long elapsedSeconds = Duration.between(ctx.getEnterTimestamp(), newState.getTs()).getSeconds();
        long thresholdSeconds = Math.round(config.getStandardDuration() * (1 + config.getCriticalThresholdPct().doubleValue() / 100.0));

        if (elapsedSeconds >= thresholdSeconds) {
            RuleEvent event = new RuleEvent();
            event.setEventType(RuleEventType.VIOLATION);
            event.setChannelId(ctx.getChannelId());
            event.setTimestamp(newState.getTs());
            event.setWorkSessionId(ctx.getCurrentWorkSessionId());
            event.setDescription("Work duration violation: elapsed=" + elapsedSeconds + "s, threshold=" + thresholdSeconds + "s, standard=" + config.getStandardDuration() + "s");
            return event;
        }
        return null;
    }

    private RuleEvent matchEnter(ChannelStateContext ctx, StateStream newState, RuleConfig config) {
        List<Integer> enterPattern = extractIntegerPattern(config.getEnterPattern());
        if (enterPattern == null || enterPattern.isEmpty()) {
            return null;
        }

        Integer firstPatternState = enterPattern.get(0);
        if (ctx.getState() == ChannelStateContext.State.IDLE) {
            List<Integer> windowValues = getValidWindowValues(ctx);
            if (windowValues.contains(firstPatternState)) {
                ctx.setState(ChannelStateContext.State.ENTERING);
            }
        }

        if (containsSubsequence(ctx, enterPattern)) {
            RuleEvent event = new RuleEvent();
            event.setEventType(RuleEventType.ENTER);
            event.setChannelId(ctx.getChannelId());
            event.setTimestamp(newState.getTs());
            event.setDescription("Enter pattern matched: " + enterPattern);

            ctx.clearWindow();
            ctx.setState(ChannelStateContext.State.WORKING);
            ctx.setEnterTimestamp(newState.getTs());
            ctx.resetViolationFlag();
            return event;
        }
        return null;
    }

    private RuleEvent matchExit(ChannelStateContext ctx, StateStream newState, RuleConfig config) {
        List<List<Integer>> exitPatterns = extractExitPatterns(config.getExitPattern());
        if (exitPatterns == null || exitPatterns.isEmpty()) {
            return null;
        }

        List<Integer> firstExitPattern = exitPatterns.get(0);
        if (!firstExitPattern.isEmpty()
                && ctx.getState() == ChannelStateContext.State.WORKING) {
            Integer firstPatternState = firstExitPattern.get(0);
            List<Integer> windowValues = getValidWindowValues(ctx);
            if (windowValues.contains(firstPatternState)) {
                ctx.setState(ChannelStateContext.State.EXITING);
            }
        }

        for (List<Integer> pattern : exitPatterns) {
            if (containsSubsequence(ctx, pattern)) {
                RuleEvent event = new RuleEvent();
                event.setEventType(RuleEventType.EXIT);
                event.setChannelId(ctx.getChannelId());
                event.setTimestamp(newState.getTs());
                event.setWorkSessionId(ctx.getCurrentWorkSessionId());
                event.setDescription("Exit pattern matched: " + pattern);

                ctx.clearWindow();
                ctx.setState(ChannelStateContext.State.IDLE);
                ctx.setEnterTimestamp(null);
                ctx.setLastPersonPresentTimestamp(null);
                return event;
            }
        }
        return null;
    }

    private List<Integer> getValidWindowValues(ChannelStateContext ctx) {
        List<Integer> windowValues = new ArrayList<>();
        for (StateStream s : ctx.getWindow()) {
            Integer combo = s.getStateCombination();
            if (!StateCombinationUtil.isInvalid(combo)) {
                windowValues.add(combo);
            }
        }
        return windowValues;
    }

    private boolean containsSubsequence(ChannelStateContext ctx, List<Integer> pattern) {
        List<Integer> windowValues = getValidWindowValues(ctx);

        if (pattern.size() > windowValues.size()) {
            return false;
        }

        for (int i = 0; i <= windowValues.size() - pattern.size(); i++) {
            boolean match = true;
            for (int j = 0; j < pattern.size(); j++) {
                if (!pattern.get(j).equals(windowValues.get(i + j))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> extractIntegerPattern(List<Object> rawPattern) {
        if (rawPattern == null) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (Object obj : rawPattern) {
            if (obj instanceof Integer) {
                result.add((Integer) obj);
            } else if (obj instanceof Number) {
                result.add(((Number) obj).intValue());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<List<Integer>> extractExitPatterns(List<Object> rawPattern) {
        if (rawPattern == null) {
            return null;
        }

        List<List<Integer>> patterns = new ArrayList<>();
        List<Integer> base = new ArrayList<>();
        List<List<Integer>> alternatives = new ArrayList<>();
        alternatives.add(new ArrayList<>());

        for (Object obj : rawPattern) {
            if (obj instanceof Integer) {
                base.add((Integer) obj);
                for (List<Integer> alt : alternatives) {
                    alt.add((Integer) obj);
                }
            } else if (obj instanceof Number) {
                base.add(((Number) obj).intValue());
                for (List<Integer> alt : alternatives) {
                    alt.add(((Number) obj).intValue());
                }
            } else if (obj instanceof List) {
                List<Object> nested = (List<Object>) obj;
                List<Integer> nestedValues = new ArrayList<>();
                for (Object nestedObj : nested) {
                    if (nestedObj instanceof Integer) {
                        nestedValues.add((Integer) nestedObj);
                    } else if (nestedObj instanceof Number) {
                        nestedValues.add(((Number) nestedObj).intValue());
                    }
                }
                if (!nestedValues.isEmpty()) {
                    List<List<Integer>> newAlternatives = new ArrayList<>();
                    for (List<Integer> alt : alternatives) {
                        for (Integer val : nestedValues) {
                            List<Integer> copy = new ArrayList<>(alt);
                            copy.add(val);
                            newAlternatives.add(copy);
                        }
                    }
                    alternatives = newAlternatives;
                }
            }
        }

        patterns.addAll(alternatives);
        return patterns;
    }
}
