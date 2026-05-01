package com.vdc.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("channel")
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String channelId;

    private String channelName;

    private Long boxId;

    private String channelType;

    private Integer status;

    private String algorithmType;

    private String rtspUrl;

    private String username;

    private String password;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
