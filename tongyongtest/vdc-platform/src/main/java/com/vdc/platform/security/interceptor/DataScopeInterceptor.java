package com.vdc.platform.security.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.vdc.platform.security.model.SecurityUser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class DataScopeInterceptor implements InnerInterceptor {

    private static final List<String> SITE_ISOLATION_TABLES = List.of("edge_box", "channel", "work_session", "alarm", "state_stream");

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser user)) {
            return;
        }

        if ("SUPER_ADMIN".equals(user.getRoleCode()) || "ALL".equals(user.getDataScope())) {
            return;
        }

        if (user.getSiteId() == null) {
            return;
        }

        String originalSql = boundSql.getSql();
        try {
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);
            if (select.getSelectBody() instanceof PlainSelect plainSelect) {
                if (isSiteIsolationTable(plainSelect)) {
                    Expression siteCondition = buildSiteCondition(user.getSiteId(), plainSelect);
                    Expression where = plainSelect.getWhere();
                    if (where == null) {
                        plainSelect.setWhere(siteCondition);
                    } else {
                        plainSelect.setWhere(new AndExpression(where, siteCondition));
                    }

                    String newSql = select.toString();
                    ReflectUtil.setFieldValue(boundSql, "sql", newSql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply data scope interceptor", e);
        }
    }

    private boolean isSiteIsolationTable(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() instanceof net.sf.jsqlparser.schema.Table table) {
            String tableName = table.getName().toLowerCase(Locale.ROOT);
            return SITE_ISOLATION_TABLES.contains(tableName);
        }
        return false;
    }

    private String getSiteIsolationColumn(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() instanceof net.sf.jsqlparser.schema.Table table) {
            String tableName = table.getName().toLowerCase(Locale.ROOT);
            if ("state_stream".equals(tableName)) {
                return "channel_id";
            }
        }
        return "site_id";
    }

    private Expression buildSiteCondition(Long siteId, PlainSelect plainSelect) {
        if (plainSelect.getFromItem() instanceof net.sf.jsqlparser.schema.Table table) {
            String tableName = table.getName().toLowerCase(Locale.ROOT);
            if ("state_stream".equals(tableName)) {
                return buildStateStreamSiteCondition(siteId);
            }
        }
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column("site_id"));
        equalsTo.setRightExpression(new LongValue(siteId));
        return equalsTo;
    }

    private Expression buildStateStreamSiteCondition(Long siteId) {
        net.sf.jsqlparser.expression.operators.relational.InExpression inExpr = new net.sf.jsqlparser.expression.operators.relational.InExpression();
        inExpr.setLeftExpression(new Column("channel_id"));
        net.sf.jsqlparser.expression.operators.relational.ExpressionList exprList = new net.sf.jsqlparser.expression.operators.relational.ExpressionList();
        exprList.setExpressions(List.of(new net.sf.jsqlparser.expression.StringValue("subquery")));
        inExpr.setRightExpression(exprList);
        String subquerySql = "SELECT id FROM channel WHERE site_id = " + siteId;
        try {
            Select subSelect = (Select) CCJSqlParserUtil.parse(subquerySql);
            inExpr.setRightExpression(subSelect.getSelectBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to build state_stream site condition", e);
        }
        return inExpr;
    }
}
