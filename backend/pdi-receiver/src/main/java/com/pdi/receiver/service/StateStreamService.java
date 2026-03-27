package com.pdi.receiver.service;

import com.pdi.receiver.dto.StateStreamDTO;

/**
 * 状态流处理服务
 *
 * @author PDI Team
 * @version 1.0.0
 * @since 2026-03-27
 */
public interface StateStreamService {

    /**
     * 处理状态流数据
     *
     * @param dto 状态流DTO
     */
    void processStateStream(StateStreamDTO dto);

    /**
     * 根据原始状态计算状态码
     *
     * @param doorOpen            门状态
     * @param personPresent       人员状态
     * @param personEnteringExiting 人员进出状态
     * @return 状态码
     */
    String calculateStateCode(Integer doorOpen, Integer personPresent, Integer personEnteringExiting);

}
