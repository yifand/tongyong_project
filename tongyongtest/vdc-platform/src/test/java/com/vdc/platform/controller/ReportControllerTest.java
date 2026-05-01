package com.vdc.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.platform.entity.SysRole;
import com.vdc.platform.entity.SysUser;
import com.vdc.platform.entity.WorkSession;
import com.vdc.platform.security.jwt.JwtUtil;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.ISysRoleService;
import com.vdc.platform.service.ISysUserService;
import com.vdc.platform.service.IWorkSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.vdc.platform.VdcPlatformApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ReportControllerTest {

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
    private IWorkSessionService workSessionService;

    private String token;
    private WorkSession testSession;

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

        testSession = new WorkSession();
        testSession.setSiteId(1L);
        testSession.setChannelId(1L);
        testSession.setVehicleInfo("TEST-VEHICLE-001");
        testSession.setStartTime(LocalDateTime.now());
        testSession.setStandardDuration(720);
        testSession.setResult("PASS");
        testSession.setStatus(0);
        workSessionService.save(testSession);
    }

    @Test
    void listPdiReports_shouldReturn200_withPageData() throws Exception {
        mockMvc.perform(get("/api/v1/reports/pdi")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getPdiReportById_shouldReturn200_withReportDetail() throws Exception {
        mockMvc.perform(get("/api/v1/reports/pdi/{id}", testSession.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.session.id").value(testSession.getId()));
    }

    @Test
    void listPdiReports_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/reports/pdi"))
                .andExpect(status().isUnauthorized());
    }
}
