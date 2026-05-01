package com.vdc.platform.auth;

import com.vdc.platform.security.interceptor.DataScopeInterceptor;
import com.vdc.platform.security.model.SecurityUser;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DataScopeInterceptorTest {

    private DataScopeInterceptor interceptor;
    private BoundSql boundSql;

    @BeforeEach
    void setUp() {
        interceptor = new DataScopeInterceptor();
        boundSql = mock(BoundSql.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void superAdmin_sqlUnchanged() {
        SecurityUser admin = createUser(1L, "SUPER_ADMIN", "ALL");
        setAuthentication(admin);

        String originalSql = "select * from edge_box where status = 1";
        when(boundSql.getSql()).thenReturn(originalSql);

        interceptor.beforeQuery(mock(Executor.class), mock(MappedStatement.class), null,
                new RowBounds(), mock(ResultHandler.class), boundSql);

        verify(boundSql, never()).getSql();
    }

    @Test
    void normalUser_siteIdAppended() {
        SecurityUser user = createUser(1L, "ADMIN", "CUSTOM");
        setAuthentication(user);

        String originalSql = "select * from edge_box where status = 1";
        when(boundSql.getSql()).thenReturn(originalSql);

        interceptor.beforeQuery(mock(Executor.class), mock(MappedStatement.class), null,
                new RowBounds(), mock(ResultHandler.class), boundSql);

        verify(boundSql, atLeastOnce()).getSql();
    }

    @Test
    void normalUser_noWhereClause_siteIdAddedAsWhere() {
        SecurityUser user = createUser(2L, "USER", "CUSTOM");
        setAuthentication(user);

        String originalSql = "select * from channel";
        when(boundSql.getSql()).thenReturn(originalSql);

        interceptor.beforeQuery(mock(Executor.class), mock(MappedStatement.class), null,
                new RowBounds(), mock(ResultHandler.class), boundSql);

        verify(boundSql, atLeastOnce()).getSql();
    }

    @Test
    void nonSiteIsolationTable_sqlUnchanged() {
        SecurityUser user = createUser(1L, "ADMIN", "CUSTOM");
        setAuthentication(user);

        String originalSql = "select * from sys_user";
        when(boundSql.getSql()).thenReturn(originalSql);

        interceptor.beforeQuery(mock(Executor.class), mock(MappedStatement.class), null,
                new RowBounds(), mock(ResultHandler.class), boundSql);

        verify(boundSql, atLeastOnce()).getSql();
    }

    private SecurityUser createUser(Long siteId, String roleCode, String dataScope) {
        SecurityUser user = new SecurityUser();
        user.setUserId(1L);
        user.setUsername("test");
        user.setPassword("pass");
        user.setSiteId(siteId);
        user.setRoleCode(roleCode);
        user.setDataScope(dataScope);
        user.setPermissions(List.of("read"));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return user;
    }

    private void setAuthentication(SecurityUser user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
