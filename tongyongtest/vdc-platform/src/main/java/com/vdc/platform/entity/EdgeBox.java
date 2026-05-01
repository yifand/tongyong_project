package com.vdc.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("edge_box")
public class EdgeBox implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String boxId;

    private String boxName;

    private Long siteId;

    private String ipAddress;

    private String secretKey;

    private Integer status;

    private LocalDateTime lastHeartbeat;

    private String version;

    private BigDecimal cpuUsage;

    private BigDecimal memUsage;

    private BigDecimal diskUsage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
