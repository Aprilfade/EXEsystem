package com.ice.exebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.exebackend.common.Result; // 确保引用的是你的 Result 类
import com.ice.exebackend.entity.BizBattleRecord;
import com.ice.exebackend.entity.BizStudent;
import com.ice.exebackend.service.BizBattleRecordService;
import com.ice.exebackend.service.BizStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle/record")
public class BizBattleRecordController {

    @Autowired
    private BizBattleRecordService battleRecordService;

    @Autowired
    private BizStudentService studentService;

    /**
     * 分页查询当前登录学生的对战记录
     */
    @GetMapping("/my")
    // 修正1：去掉 <?>，因为你的 Result 类没有定义泛型
    public Result getMyRecords(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {

        // 1. 获取当前登录学生
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String studentNo;
        if (principal instanceof String) {
            studentNo = (String) principal;
        } else {
            // 防止类型转换异常，如果未登录或格式不对
            return Result.fail("获取用户信息失败");
        }

        BizStudent student = studentService.lambdaQuery().eq(BizStudent::getStudentNo, studentNo).one();

        if (student == null) {
            // 修正2：使用 fail 代替 error
            return Result.fail("未找到用户信息");
        }

        // 2. 分页查询
        Page<BizBattleRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<BizBattleRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("player_id", student.getId());
        queryWrapper.orderByDesc("create_time"); // 按时间倒序

        Page<BizBattleRecord> resultPage = battleRecordService.page(page, queryWrapper);

        // 修正3：使用 suc 代替 success
        // 注意：这里直接把 page 对象传回去，前端需要从 data.records 取数据，从 data.total 取总数
        // 或者你可以使用 Result.suc(resultPage.getRecords(), resultPage.getTotal());
        return Result.suc(resultPage);
    }
}