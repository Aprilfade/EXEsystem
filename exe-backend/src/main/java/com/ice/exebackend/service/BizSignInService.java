package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizSignIn;
import java.util.Map;

public interface BizSignInService extends IService<BizSignIn> {
    // 执行签到
    Map<String, Object> doSignIn(Long studentId);
    // 获取当月签到状态
    Map<String, Object> getSignInStatus(Long studentId, String yearMonth);
}