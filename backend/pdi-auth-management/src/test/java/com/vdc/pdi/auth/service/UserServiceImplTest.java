package com.vdc.pdi.auth.service;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.entity.UserRole;
import com.vdc.pdi.auth.domain.repository.RoleRepository;
import com.vdc.pdi.auth.domain.repository.UserRepository;
import com.vdc.pdi.auth.domain.repository.UserRoleRepository;
import com.vdc.pdi.auth.dto.request.CreateUserRequest;
import com.vdc.pdi.auth.dto.request.UpdateUserRequest;
import com.vdc.pdi.auth.dto.response.UserResponse;
import com.vdc.pdi.auth.service.impl.UserServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setEmail("test@example.com");
        mockUser.setStatus(1);
        mockUser.setDeleted(false);

        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setRoleCode("ADMIN");
        mockRole.setRoleName("管理员");
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userService.getUserById(999L)
        );
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsernameAndNotDeleted("testuser")).thenReturn(Optional.of(mockUser));

        // When
        UserResponse response = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getUsers_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(mockUser));
        when(userRepository.findByDeletedFalse(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> result = userService.getUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    void createUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("Password123");
        request.setEmail("new@example.com");
        request.setRealName("新用户");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        verify(passwordValidator).validate("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existinguser");
        request.setPassword("Password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userService.createUser(request)
        );
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void updateUser_Success() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRealName("Updated Name");
        request.setPhone("13800138000");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        UserResponse response = userService.updateUser(1L, request);

        // Then
        assertNotNull(response);
        verify(userRepository).save(mockUser);
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        userService.deleteUser(1L);

        // Then
        assertTrue(mockUser.getDeleted());
        verify(userRepository).save(mockUser);
    }

    @Test
    void assignRoles_Success() {
        // Given
        List<Long> roleIds = Collections.singletonList(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.assignRoles(1L, roleIds);

        // Then
        verify(userRoleRepository).deleteByUserId(1L);
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void resetPassword_Success() {
        // Given
        String newPassword = "NewPassword123";
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        userService.resetPassword(1L, newPassword);

        // Then
        verify(passwordValidator).validate(newPassword);
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUserStatus_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        userService.updateUserStatus(1L, 0);

        // Then
        assertEquals(0, mockUser.getStatus());
        verify(userRepository).save(mockUser);
    }
}
