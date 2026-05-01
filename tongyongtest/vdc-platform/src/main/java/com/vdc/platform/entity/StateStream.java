package com.vdc.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("state_stream")
public class StateStream implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String boxId;

    private String channelId;

    private LocalDateTime ts;

    private Boolean doorOpen;

    private Boolean personPresent;

    private Boolean personEnteringExiting;

    private Boolean vehiclePresent;

    private Integer stateCombination;

    private String snapshotTarget;

    private String snapshotScene;
}
