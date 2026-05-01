package com.vdc.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vdc.platform.common.JsonbTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "rule_config", autoResultMap = true)
public class RuleConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleName;

    private String channelType;

    private Boolean requireVehicle;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<Object> enterPattern;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<Object> exitPattern;

    private Integer standardDuration;

    private BigDecimal criticalThresholdPct;

    private Integer personAbsentTimeout;

    private Boolean isEnabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
