package com.ice.exebackend.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.entity.BizGoods;
import java.util.List;

public interface BizGoodsService extends IService<BizGoods> {
    // 获取商品列表（并标记学生是否已拥有）
    List<BizGoods> getGoodsListForStudent(Long studentId);
    // 兑换商品
    void exchangeGoods(Long studentId, Long goodsId);
}