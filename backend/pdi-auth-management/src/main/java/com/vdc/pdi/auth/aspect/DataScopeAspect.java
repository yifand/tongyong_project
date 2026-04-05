package com.vdc.pdi.auth.aspect;

import com.vdc.pdi.auth.domain.entity.Role;
import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.repository.UserRepository;
import com.vdc.pdi.auth.domain.repository.UserRoleRepository;
import com.vdc.pdi.auth.security.UserDetailsServiceImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限AOP切面
 * 用于自动注入数据权限过滤条件
 */
@Aspect
@Component
public class DataScopeAspect {

    private static final Logger logger = LoggerFactory.getLogger(DataScopeAspect.class);

    /**
     * 数据权限范围定义
     */
    public static final int DATA_SCOPE_ALL = 1;           // 全部数据
    public static final int DATA_SCOPE_DEPT = 2;          // 本部门数据
    public static final int DATA_SCOPE_DEPT_AND_CHILD = 3; // 本部门及子部门数据
    public static final int DATA_SCOPE_SELF = 4;          // 仅本人数据
    public static final int DATA_SCOPE_CUSTOM = 5;        // 自定义数据

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * 在Service层方法执行前注入数据权限
     * 拦截带有@DataScope注解的方法
     */
    @Before("@annotation(com.vdc.pdi.auth.annotation.DataScope)")
    public void before(JoinPoint point) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        String username = authentication.getName();
        
        try {
            // 获取用户实体
            User user = userDetailsService.loadUserEntityByUsername(username);
            
            // 获取用户的数据权限范围
            Integer dataScope = getDataScope(user);
            
            // 将数据权限信息存入ThreadLocal，供后续查询使用
            DataScopeContext.setDataScope(dataScope);
            DataScopeContext.setUserId(user.getId());
            DataScopeContext.setDeptId(user.getDeptId());
            
            logger.debug("Data scope set for user {}: scope={}, deptId={}", 
                    username, dataScope, user.getDeptId());
            
        } catch (Exception e) {
            logger.error("Failed to set data scope for user: {}", username, e);
        }
    }

    /**
     * 获取用户的数据权限范围
     * 取用户角色中最大的数据权限范围
     */
    private Integer getDataScope(User user) {
        // 优先使用用户自身的数据权限设置
        if (user.getDataScope() != null) {
            return user.getDataScope();
        }

        // 获取用户的角色列表
        List<Role> roles = userRoleRepository.findRolesByUserId(user.getId());
        
        // 取角色中最小的数据权限范围（数字越小权限越大）
        Integer minScope = DATA_SCOPE_SELF; // 默认仅本人
        for (Role role : roles) {
            if (role.getDataScope() != null && role.getDataScope() < minScope) {
                minScope = role.getDataScope();
            }
        }
        
        return minScope;
    }

    /**
     * 数据权限上下文
     * 使用ThreadLocal存储当前线程的数据权限信息
     */
    public static class DataScopeContext {
        private static final ThreadLocal<Integer> DATA_SCOPE = new ThreadLocal<>();
        private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
        private static final ThreadLocal<Long> DEPT_ID = new ThreadLocal<>();

        public static void setDataScope(Integer scope) {
            DATA_SCOPE.set(scope);
        }

        public static Integer getDataScope() {
            return DATA_SCOPE.get();
        }

        public static void setUserId(Long userId) {
            USER_ID.set(userId);
        }

        public static Long getUserId() {
            return USER_ID.get();
        }

        public static void setDeptId(Long deptId) {
            DEPT_ID.set(deptId);
        }

        public static Long getDeptId() {
            return DEPT_ID.get();
        }

        public static void clear() {
            DATA_SCOPE.remove();
            USER_ID.remove();
            DEPT_ID.remove();
        }
    }
}
