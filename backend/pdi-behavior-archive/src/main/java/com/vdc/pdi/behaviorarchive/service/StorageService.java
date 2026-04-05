package com.vdc.pdi.behaviorarchive.service;

import java.io.InputStream;

/**
 * 存储服务接口
 * 用于获取图片文件
 */
public interface StorageService {

    /**
     * 获取对象输入流
     *
     * @param objectUrl 对象URL
     * @return 输入流
     */
    InputStream getObject(String objectUrl);

    /**
     * 检查对象是否存在
     *
     * @param objectUrl 对象URL
     * @return true-存在，false-不存在
     */
    boolean exists(String objectUrl);
}
