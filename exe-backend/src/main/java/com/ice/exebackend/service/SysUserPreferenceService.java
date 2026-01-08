package com.ice.exebackend.service;

/**
 * 用户个性化配置服务接口
 *
 * @author ice
 */
public interface SysUserPreferenceService {

    /**
     * 获取用户配置
     * @param userId 用户ID
     * @param key 配置键
     * @return 配置值（JSON字符串）
     */
    String getPreference(Long userId, String key);

    /**
     * 保存用户配置
     * @param userId 用户ID
     * @param key 配置键
     * @param value 配置值（JSON字符串）
     */
    void savePreference(Long userId, String key, String value);

    /**
     * 删除用户配置
     * @param userId 用户ID
     * @param key 配置键
     */
    void deletePreference(Long userId, String key);
}
