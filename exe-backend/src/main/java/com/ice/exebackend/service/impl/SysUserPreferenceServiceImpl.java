package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ice.exebackend.entity.SysUserPreference;
import com.ice.exebackend.mapper.SysUserPreferenceMapper;
import com.ice.exebackend.service.SysUserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户个性化配置服务实现
 *
 * @author ice
 */
@Service
public class SysUserPreferenceServiceImpl implements SysUserPreferenceService {

    @Autowired
    private SysUserPreferenceMapper preferenceMapper;

    @Override
    public String getPreference(Long userId, String key) {
        LambdaQueryWrapper<SysUserPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPreference::getUserId, userId)
               .eq(SysUserPreference::getPreferenceKey, key);

        SysUserPreference preference = preferenceMapper.selectOne(wrapper);
        return preference != null ? preference.getPreferenceValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePreference(Long userId, String key, String value) {
        LambdaQueryWrapper<SysUserPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPreference::getUserId, userId)
               .eq(SysUserPreference::getPreferenceKey, key);

        SysUserPreference existing = preferenceMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新现有配置
            existing.setPreferenceValue(value);
            preferenceMapper.updateById(existing);
        } else {
            // 创建新配置
            SysUserPreference newPreference = new SysUserPreference();
            newPreference.setUserId(userId);
            newPreference.setPreferenceKey(key);
            newPreference.setPreferenceValue(value);
            preferenceMapper.insert(newPreference);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePreference(Long userId, String key) {
        LambdaQueryWrapper<SysUserPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPreference::getUserId, userId)
               .eq(SysUserPreference::getPreferenceKey, key);

        preferenceMapper.delete(wrapper);
    }
}
