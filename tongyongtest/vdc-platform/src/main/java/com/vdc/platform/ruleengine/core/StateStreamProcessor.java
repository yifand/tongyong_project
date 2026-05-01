package com.vdc.platform.ruleengine.core;

import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.entity.StateStream;
import com.vdc.platform.ruleengine.config.RuleConfigCache;
import com.vdc.platform.ruleengine.model.ChannelStateContext;
import com.vdc.platform.ruleengine.model.RuleEvent;
import com.vdc.platform.service.IChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class StateStreamProcessor {

    private final ConcurrentHashMap<String, ChannelStateContext> contextCache = new ConcurrentHashMap<>();

    private final PatternMatcher patternMatcher;
    private final EventGenerator eventGenerator;
    private final RuleActionExecutor ruleActionExecutor;
    private final RuleConfigCache ruleConfigCache;
    private final IChannelService channelService;

    public void process(StateStream stateStream) {
        if (stateStream == null || stateStream.getChannelId() == null) {
            return;
        }

        String channelId = stateStream.getChannelId();
        ChannelStateContext ctx = contextCache.computeIfAbsent(channelId, id -> {
            ChannelStateContext newCtx = new ChannelStateContext();
            newCtx.setChannelId(id);
            return newCtx;
        });

        RuleConfig config = loadRuleConfig(channelId);
        if (config == null) {
            log.warn("No rule config found for channel: {}", channelId);
            return;
        }

        RuleEvent matchedEvent = patternMatcher.match(ctx, stateStream, config);
        if (matchedEvent != null) {
            RuleEvent event = eventGenerator.fromMatch(matchedEvent);
            if (event != null) {
                dispatchEvent(event, config, ctx);
            }
        }
    }

    public void clearContext(String channelId) {
        if (channelId != null) {
            contextCache.remove(channelId);
        }
    }

    private RuleConfig loadRuleConfig(String channelId) {
        Channel channel = channelService.lambdaQuery()
                .eq(Channel::getChannelId, channelId)
                .last("LIMIT 1")
                .one();
        if (channel == null || channel.getAlgorithmType() == null) {
            return ruleConfigCache.getOrDefault("DEFAULT");
        }
        RuleConfig config = ruleConfigCache.get(channel.getAlgorithmType());
        if (config == null) {
            config = ruleConfigCache.getOrDefault(channel.getAlgorithmType());
        }
        return config;
    }

    private void dispatchEvent(RuleEvent event, RuleConfig config, ChannelStateContext ctx) {
        switch (event.getEventType()) {
            case ENTER -> {
                Long sessionId = ruleActionExecutor.onEnter(event, config);
                ctx.setCurrentWorkSessionId(sessionId);
            }
            case EXIT -> ruleActionExecutor.onExit(event, config);
            case SMOKE -> ruleActionExecutor.onSmoke(event);
            case VIOLATION -> ruleActionExecutor.onViolation(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
