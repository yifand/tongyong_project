package com.vdc.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.platform.dto.EdgeBoxRequest;
import com.vdc.platform.entity.EdgeBox;
import com.vdc.platform.entity.SysRole;
import com.vdc.platform.entity.SysUser;
import com.vdc.platform.security.jwt.JwtUtil;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.service.IEdgeBoxService;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.vdc.platform.VdcPlatformApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class DeviceControllerTest {

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
    private IEdgeBoxService edgeBoxService;

    private String token;

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
    }

    @Test
    void listBoxes_shouldReturn200_withPageData() throws Exception {
        mockMvc.perform(get("/api/v1/devices/boxes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void createBox_shouldCreateNewBox() throws Exception {
        EdgeBoxRequest request = new EdgeBoxRequest();
        request.setBoxId("BOX-TEST-001");
        request.setBoxName("Test Box");
        request.setSiteId(1L);
        request.setIpAddress("192.168.1.100");
        request.setVersion("v1.0");

        mockMvc.perform(post("/api/v1/devices/boxes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteBox_shouldDeleteBox() throws Exception {
        EdgeBox box = new EdgeBox();
        box.setBoxId("BOX-DELETE-001");
        box.setBoxName("Delete Me");
        box.setSiteId(1L);
        box.setStatus(0);
        edgeBoxService.save(box);

        mockMvc.perform(delete("/api/v1/devices/boxes/{id}", box.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listBoxes_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/devices/boxes"))
                .andExpect(status().isUnauthorized());
    }
}
