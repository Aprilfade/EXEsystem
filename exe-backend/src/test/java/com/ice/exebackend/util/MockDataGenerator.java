package com.ice.exebackend.util;

import com.ice.exebackend.entity.SysUser;
import com.ice.exebackend.entity.SysRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock 数据生成器
 * 用于测试环境生成模拟数据
 */
public class MockDataGenerator {

    private static final Random RANDOM = new Random();

    /**
     * 生成模拟用户
     */
    public static SysUser mockUser(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUserName(username);
        user.setNickName("测试用户" + id);
        user.setEmail(username + "@test.com");
        user.setPhoneNumber("138000000" + String.format("%02d", id));
        user.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // password: "123456"
        user.setStatus("0");
        user.setDelFlag("0");
        user.setCreateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 生成模拟角色
     */
    public static SysRole mockRole(Long id, String roleName, String roleKey) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setRoleSort(id.intValue());
        role.setStatus("0");
        role.setDelFlag("0");
        role.setCreateTime(LocalDateTime.now());
        return role;
    }

    /**
     * 生成批量模拟用户
     */
    public static List<SysUser> mockUsers(int count) {
        List<SysUser> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(mockUser((long) i, "user" + i));
        }
        return users;
    }

    /**
     * 生成随机整数
     */
    public static int randomInt(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * 生成随机长整数
     */
    public static Long randomLong(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }
}
