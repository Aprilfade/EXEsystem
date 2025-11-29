package com.ice.exebackend.controller;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.entity.BizGoods;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.mapper.BizUserGoodsMapper;
import com.ice.exebackend.service.BizGoodsService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/mall")
@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentGoodsController {

    @Autowired
    private BizGoodsService goodsService;
    @Autowired
    private BizStudentService studentService;

    @Autowired
    private BizUserGoodsMapper userGoodsMapper; // 2. 使用正确的类

    // 获取当前学生ID的辅助方法
    private Long getCurrentStudentId(Authentication auth) {
        String studentNo = auth.getName();
        BizStudent s = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();
        return s != null ? s.getId() : null;
    }

    /**
     * 获取商品列表（含拥有状态）
     */
    @GetMapping("/list")
    public Result getGoodsList(Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        List<BizGoods> list = goodsService.getGoodsListForStudent(studentId);
        return Result.suc(list);
    }

    /**
     * 兑换商品 - 优化后
     * 移除了 try-catch，Service 层抛出的 "积分不足" 或 "商品不存在" 异常会自动被处理
     */
    @PostMapping("/exchange/{goodsId}")
    public Result exchange(@PathVariable Long goodsId, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);

        // 直接调用，不再需要 try-catch 包裹
        goodsService.exchangeGoods(studentId, goodsId);

        return Result.suc("兑换成功！");
    }
    /**
     * 【新增】装配/使用商品
     */
    @PostMapping("/equip/{goodsId}")
    public Result equipGoods(@PathVariable Long goodsId, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);

        // 1. 检查是否拥有该商品
        Long count = userGoodsMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ice.exebackend.entity.BizUserGoods>()
                .eq("student_id", studentId)
                .eq("goods_id", goodsId));

        if (count == 0) {
            return Result.fail("你还未拥有该商品，请先兑换");
        }

        // 2. 获取商品详情
        BizGoods goods = goodsService.getById(goodsId);
        if (goods == null) return Result.fail("商品不存在");

        // 3. 更新学生表配置
        BizStudent student = studentService.getById(studentId);

        if ("AVATAR_FRAME".equals(goods.getType())) {
            student.setAvatarFrameStyle(goods.getResourceValue());
        } else if ("BACKGROUND".equals(goods.getType())) {
            student.setBackgroundUrl(goods.getResourceValue());
        }

        studentService.updateById(student);
        return Result.suc("装配成功");
    }

    // 【新增】卸载装扮 (恢复默认)
    @PostMapping("/unequip")
    public Result unequipGoods(@RequestParam String type, Authentication auth) {
        Long studentId = getCurrentStudentId(auth);
        BizStudent student = studentService.getById(studentId);

        if ("AVATAR_FRAME".equals(type)) {
            student.setAvatarFrameStyle(null); // 清空
        } else if ("BACKGROUND".equals(type)) {
            student.setBackgroundUrl(null); // 清空
        }

        studentService.updateById(student);
        return Result.suc("已恢复默认");
    }
}