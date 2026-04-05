package com.vdc.pdi.behaviorarchive.service.impl;

import com.vdc.pdi.behaviorarchive.service.StorageService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 测试用存储服务实现
 * 用于测试环境，模拟存储服务
 */
@Service
@Primary
@Profile("test")
public class TestStorageServiceImpl implements StorageService {

    @Override
    public InputStream getObject(String objectUrl) {
        // 测试环境返回模拟数据
        return new ByteArrayInputStream("test image data".getBytes());
    }

    @Override
    public boolean exists(String objectUrl) {
        // 测试环境假设所有对象都存在
        return objectUrl != null && !objectUrl.isEmpty();
    }
}
