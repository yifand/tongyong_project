package com.vdc.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vdc.platform.entity.StateStream;

import java.util.List;

public interface IStateStreamService extends IService<StateStream> {

    boolean batchInsert(List<StateStream> list);
}
