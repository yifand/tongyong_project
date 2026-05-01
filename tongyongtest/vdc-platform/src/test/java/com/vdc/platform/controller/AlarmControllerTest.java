package com.vdc.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.platform.dto.AlarmProcessRequest;
import com.vdc.platform.entity.Alarm;
import com.vdc.platform.entity.SysRole;
import com.vdc.platform.entity.SysUser;
import com.vdc.platform.security.jwt.JwtUtil;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IAlarmService;
import com.vdc.platform.service.ISysRoleService;
import com.vdc.platform.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.vdc.platform.VdcPlatformApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private IAlarmService alarmService;

    private String token;
    private Alarm testAlarm;

    @BeforeEach
    void setUp() {
        SysRole role = new SysRole();
        role.setRoleCode("TEST_ADMIN");
        role.setRoleName("Test Admin");
        role.setPermissions(List.of("*"));
        role.setDataScope("ALL");
        sysRoleService.save(role);

        SysUser user = new SysUser();
        user.setUsername("testadmin");
        user.setPasswordHash(passwordEncoder.encode("testpass"));
        user.setRealName("Test Admin");
        user.setRoleId(role.getId());
        user.setStatus(1);
        sysUserService.save(user);

        SecurityUser securityUser = new SecurityUser();
        securityUser.setUserId(user.getId());
        securityUser.setUsername(user.getUsername());
        securityUser.setPassword(user.getPasswordHash());
        securityUser.setSiteId(user.getSiteId());
        securityUser.setRoleCode(role.getRoleCode());
        securityUser.setDataScope(role.getDataScope());
        securityUser.setPermissions(role.getPermissions());
        securityUser.setEnabled(true);
        securityUser.setAccountNonExpired(true);
        securityUser.setAccountNonLocked(true);
        securityUser.setCredentialsNonExpired(true);

        token = jwtUtil.generateAccessToken(securityUser);

        testAlarm = new Alarm();
        testAlarm.setAlarmType("TEST_ALARM");
        testAlarm.setSiteId(1L);
        testAlarm.setChannelId(1L);
        testAlarm.setAlarmTime(LocalDateTime.now());
        testAlarm.setProcessStatus("UNPROCESSED");
        alarmService.save(testAlarm);
    }

    @Test
    void listAlarms_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/alarms")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getAlarmById_shouldReturn200_forExistingAlarm() throws Exception {
        mockMvc.perform(get("/api/v1/alarms/{id}", testAlarm.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.alarm.id").value(testAlarm.getId()));
    }

    @Test
    void processAlarm_shouldUpdateProcessStatus() throws Exception {
        AlarmProcessRequest request = new AlarmProcessRequest();
        request.setProcessStatus("PROCESSED");
        request.setDescription("Handled");

        mockMvc.perform(put("/api/v1/alarms/{id}/process", testAlarm.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listAlarms_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/alarms"))
                .andExpect(status().isUnauthorized());
    }
}
