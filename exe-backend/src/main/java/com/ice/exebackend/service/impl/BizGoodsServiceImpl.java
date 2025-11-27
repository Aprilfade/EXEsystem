package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.entity.BizGoods;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.entity.BizUserGoods;
import com.ice.exebackend.mapper.BizGoodsMapper;
import com.ice.exebackend.mapper.BizStudentMapper;
import com.ice.exebackend.mapper.BizUserGoodsMapper;
import com.ice.exebackend.service.BizGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BizGoodsServiceImpl extends ServiceImpl<BizGoodsMapper, BizGoods> implements BizGoodsService {

    @Autowired
    private BizUserGoodsMapper userGoodsMapper;
    @Autowired
    private BizStudentMapper studentMapper;

    @Override
    public List<BizGoods> getGoodsListForStudent(Long studentId) {
        // 1. 查询所有上架商品
        List<BizGoods> allGoods = this.list(new QueryWrapper<BizGoods>().eq("is_enabled", 1));

        // 2. 查询学生已拥有的商品ID
        List<BizUserGoods> userGoods = userGoodsMapper.selectList(new QueryWrapper<BizUserGoods>().eq("student_id", studentId));
        Set<Long> ownedIds = userGoods.stream().map(BizUserGoods::getGoodsId).collect(Collectors.toSet());

        // 3. 标记状态
        for (BizGoods goods : allGoods) {
            goods.setIsOwned(ownedIds.contains(goods.getId()));
        }
        return allGoods;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeGoods(Long studentId, Long goodsId) {
        // 1. 校验商品
        BizGoods goods = this.getById(goodsId);
        if (goods == null || goods.getIsEnabled() == 0) {
            throw new RuntimeException("商品已下架或不存在");
        }

        // 2. 校验是否已拥有 (对于非消耗品)
        Long count = userGoodsMapper.selectCount(new QueryWrapper<BizUserGoods>()
                .eq("student_id", studentId)
                .eq("goods_id", goodsId));
        if (count > 0) {
            throw new RuntimeException("您已经拥有该商品，无需重复兑换");
        }

        // 3. 校验并扣除积分
        BizStudent student = studentMapper.selectById(studentId);
        if (student.getPoints() < goods.getPrice()) {
            throw new RuntimeException("积分不足！");
        }
        student.setPoints(student.getPoints() - goods.getPrice());
        studentMapper.updateById(student);

        // 4. 记录兑换
        BizUserGoods userGoods = new BizUserGoods();
        userGoods.setStudentId(studentId);
        userGoods.setGoodsId(goodsId);
        userGoodsMapper.insert(userGoods);
    }
}