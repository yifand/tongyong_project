package com.vdc.platform.ruleengine;

import com.vdc.platform.entity.Alarm;
import com.vdc.platform.entity.Channel;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.entity.RuleConfig;
import com.vdc.platform.entity.Site;
import com.vdc.platform.entity.StateStream;
import com.vdc.platform.entity.WorkSession;
import com.vdc.platform.ruleengine.config.RuleConfigCache;
import com.vdc.platform.ruleengine.core.RuleEngine;
import com.vdc.platform.ruleengine.core.StateStreamProcessor;
import com.vdc.platform.service.IAlarmService;
import com.vdc.platform.service.IChannelService;
import com.vdc.platform.service.IEdgeBoxService;
import com.vdc.platform.service.IRuleConfigService;
import com.vdc.platform.service.ISiteService;
import com.vdc.platform.service.IWorkSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vdc.platform.AbstractIntegrationTest;

@Transactional
public class RuleEngineIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private IWorkSessionService workSessionService;

    @Autowired
    private IAlarmService alarmService;

    @Autowired
    private IChannelService channelService;

    @Autowired
    private IRuleConfigService ruleConfigService;

    @Autowired
    private IEdgeBoxService edgeBoxService;

    @Autowired
    private ISiteService siteService;

    @Autowired
    private StateStreamProcessor stateStreamProcessor;

    @Autowired
    private RuleConfigCache ruleConfigCache;

    private static final String TEST_CHANNEL_ID = "test-ch-pdi-001";
    private static final String TEST_BOX_ID = "test-box-001";
    private Long siteId;
    private Long boxId;
    private Long channelDbId;

    @BeforeEach
    void setUp() {
        // Clear processor context for the test channel
        stateStreamProcessor.clearContext(TEST_CHANNEL_ID);

        // Create site
        Site site = new Site();
        site.setSiteCode("TEST-SITE-001");
        site.setSiteName("Test Site");
        siteService.save(site);
        siteId = site.getId();

        // Create edge box
        EdgeBox box = new EdgeBox();
        box.setBoxId(TEST_BOX_ID);
        box.setBoxName("Test Box");
        box.setSiteId(siteId);
        box.setStatus(0);
        edgeBoxService.save(box);
        boxId = box.getId();

        // Create channel with PDI_FRONT algorithm
        Channel channel = new Channel();
        channel.setChannelId(TEST_CHANNEL_ID);
        channel.setChannelName("Test PDI Front");
        channel.setBoxId(boxId);
        channel.setChannelType("PDI");
        channel.setAlgorithmType("PDI_FRONT");
        channel.setStatus(0);
        channelService.save(channel);
        channelDbId = channel.getId();

        // Ensure rule config exists for PDI_FRONT
        RuleConfig existing = ruleConfigService.lambdaQuery()
                .eq(RuleConfig::getChannelType, "PDI_FRONT")
                .last("LIMIT 1")
                .one();
        if (existing == null) {
            RuleConfig config = new RuleConfig();
            config.setRuleName("左前门PDI规则");
            config.setChannelType("PDI_FRONT");
            config.setRequireVehicle(true);
            config.setEnterPattern(List.of(15, 16, 11));
            config.setExitPattern(List.of(11, 16, 15, List.of(13, 9)));
            config.setStandardDuration(720);
            config.setCriticalThresholdPct(new BigDecimal("90.00"));
            config.setPersonAbsentTimeout(10);
            config.setIsEnabled(true);
            ruleConfigService.save(config);
            existing = config;
        }

        // Populate cache with a fully in-memory config (bypasses JacksonTypeHandler issues with H2)
        RuleConfig cachedConfig = new RuleConfig();
        cachedConfig.setId(existing.getId());
        cachedConfig.setRuleName(existing.getRuleName());
        cachedConfig.setChannelType(existing.getChannelType());
        cachedConfig.setRequireVehicle(existing.getRequireVehicle());
        cachedConfig.setEnterPattern(List.of(15, 16, 11));
        cachedConfig.setExitPattern(List.of(11, 16, 15, List.of(13, 9)));
        cachedConfig.setStandardDuration(existing.getStandardDuration());
        cachedConfig.setCriticalThresholdPct(existing.getCriticalThresholdPct());
        cachedConfig.setPersonAbsentTimeout(existing.getPersonAbsentTimeout());
        cachedConfig.setIsEnabled(existing.getIsEnabled());
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, RuleConfig> cache = (ConcurrentHashMap<String, RuleConfig>) ReflectionTestUtils.getField(ruleConfigCache, "cache");
        if (cache != null) {
            cache.put("PDI_FRONT", cachedConfig);
        }
    }

    @Test
    void testEnterAndExitSequenceCreatesWorkSessionAndAlarm() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

        // Simulate enter sequence: 15, 16, 11
        ruleEngine.process(createStateStream(15, baseTime, true));
        ruleEngine.process(createStateStream(16, baseTime.plusSeconds(1), true));
        ruleEngine.process(createStateStream(11, baseTime.plusSeconds(2), true));

        // Verify WorkSession created with status=0
        WorkSession session = workSessionService.lambdaQuery()
                .eq(WorkSession::getChannelId, channelDbId)
                .orderByDesc(WorkSession::getStartTime)
                .last("LIMIT 1")
                .one();
        assertNotNull(session);
        assertEquals(0, session.getStatus());
        assertEquals(720, session.getStandardDuration());

        // Simulate exit sequence: 11, 16, 15, 13
        // Use a short duration (3 seconds total) so actual_duration < 720 * 0.9 = 648
        ruleEngine.process(createStateStream(11, baseTime.plusSeconds(3), true));
        ruleEngine.process(createStateStream(16, baseTime.plusSeconds(4), true));
        ruleEngine.process(createStateStream(15, baseTime.plusSeconds(5), true));
        ruleEngine.process(createStateStream(13, baseTime.plusSeconds(6), true));

        // Refresh session from DB
        session = workSessionService.getById(session.getId());
        assertNotNull(session);
        assertEquals(1, session.getStatus());
        assertNotNull(session.getActualDuration());
        assertTrue(session.getActualDuration() < 720 * 0.9);
        assertEquals("UNQUALIFIED", session.getResult());

        // Verify Alarm created
        Alarm alarm = alarmService.lambdaQuery()
                .eq(Alarm::getWorkSessionId, session.getId())
                .eq(Alarm::getAlarmType, "PDI_UNQUALIFIED")
                .last("LIMIT 1")
                .one();
        assertNotNull(alarm);
        assertEquals("PDI_UNQUALIFIED", alarm.getAlarmType());
        assertEquals(channelDbId, alarm.getChannelId());
        assertEquals(session.getId(), alarm.getWorkSessionId());
    }

    private StateStream createStateStream(int combo, LocalDateTime ts, boolean vehiclePresent) {
        StateStream s = new StateStream();
        s.setChannelId(TEST_CHANNEL_ID);
        s.setBoxId(TEST_BOX_ID);
        s.setStateCombination(combo);
        s.setVehiclePresent(vehiclePresent);
        s.setDoorOpen((combo & 4) != 0);
        s.setPersonPresent((combo & 2) != 0);
        s.setPersonEnteringExiting((combo & 1) != 0);
        s.setTs(ts);
        return s;
    }
}
