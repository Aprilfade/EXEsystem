package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizFavorite;
import com.ice.exebackend.entity.BizQuestion;

public interface BizFavoriteService extends IService<BizFavorite> {
    // 收藏/取消收藏 (切换状态)
    boolean toggleFavorite(Long studentId, Long questionId);

    // 检查是否已收藏
    boolean isFavorited(Long studentId, Long questionId);

    // 分页获取我的收藏（返回题目详情）
    Page<BizQuestion> getMyFavorites(Page<BizQuestion> page, Long studentId);
}