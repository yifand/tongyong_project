package com.vdc.pdi.auth.service;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.entity.UserRole;
import com.vdc.pdi.auth.domain.repository.RoleRepository;
import com.vdc.pdi.auth.domain.repository.UserRoleRepository;
import com.vdc.pdi.auth.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 角色服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setCode("ADMIN");
        mockRole.setName("管理员");
        mockRole.setDescription("系统管理员");
        mockRole.setStatus(1);
        mockRole.setDeletedAt(null);  // 未删除
    }

    @Test
    void getRoleById_Success() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));

        // When
        Role result = roleService.getRoleById(1L);

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.getCode());
        assertEquals("管理员", result.getName());
    }

    @Test
    void getRoleById_NotFound() {
        // Given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            roleService.getRoleById(999L)
        );
        assertTrue(exception.getMessage().contains("Role not found"));
    }

    @Test
    void getRoleByCode_Success() {
        // Given
        when(roleRepository.findByCodeAndNotDeleted("ADMIN")).thenReturn(Optional.of(mockRole));

        // When
        Role result = roleService.getRoleByCode("ADMIN");

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.getCode());
    }

    @Test
    void getRoles_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> rolePage = new PageImpl<>(Collections.singletonList(mockRole));
        when(roleRepository.findByDeletedAtIsNull(pageable)).thenReturn(rolePage);

        // When
        Page<Role> result = roleService.getRoles(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ADMIN", result.getContent().get(0).getCode());
    }

    @Test
    void getAllActiveRoles_Success() {
        // Given
        when(roleRepository.findByStatusAndDeletedAtIsNull(1)).thenReturn(Collections.singletonList(mockRole));

        // When
        List<Role> result = roleService.getAllActiveRoles();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getCode());
    }

    @Test
    void createRole_Success() {
        // Given
        Role newRole = new Role();
        newRole.setCode("USER");
        newRole.setName("普通用户");

        when(roleRepository.existsByCode("USER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        Role result = roleService.createRole(newRole);

        // Then
        assertNotNull(result);
        assertEquals("USER", result.getCode());
        assertEquals(1, result.getStatus());
        assertNull(result.getDeletedAt());  // 检查 deletedAt 是否为 null
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_RoleCodeExists() {
        // Given
        Role newRole = new Role();
        newRole.setCode("ADMIN");

        when(roleRepository.existsByCode("ADMIN")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            roleService.createRole(newRole)
        );
        assertEquals("Role code already exists", exception.getMessage());
    }

    @Test
    void updateRole_Success() {
        // Given
        Role updateRequest = new Role();
        updateRequest.setCode("ADMIN");
        updateRequest.setName("超级管理员");
        updateRequest.setDescription("更新后的描述");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        // When
        Role result = roleService.updateRole(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("超级管理员", mockRole.getName());
        assertEquals("更新后的描述", mockRole.getDescription());
        verify(roleRepository).save(mockRole);
    }

    @Test
    void deleteRole_Success() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        // When
        roleService.deleteRole(1L);

        // Then
        assertNotNull(mockRole.getDeletedAt());  // 检查 deletedAt 是否被设置
        verify(userRoleRepository).deleteByRoleId(1L);
        verify(roleRepository).save(mockRole);
    }

    @Test
    void updateRoleStatus_Success() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
        when(roleRepository.save(any(Role.class))).thenReturn(mockRole);

        // When
        roleService.updateRoleStatus(1L, 0);

        // Then
        assertEquals(0, mockRole.getStatus());
        verify(roleRepository).save(mockRole);
    }

    @Test
    void getRolesByUserId_Success() {
        // Given
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        when(userRoleRepository.findRolesByUserId(1L)).thenReturn(Collections.singletonList(mockRole));

        // When
        List<Role> result = roleService.getRolesByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getCode());
    }

    @Test
    void existsByRoleCode_Success() {
        // Given
        when(roleRepository.existsByCode("ADMIN")).thenReturn(true);

        // When
        boolean exists = roleService.existsByRoleCode("ADMIN");

        // Then
        assertTrue(exists);
    }
}
