package com.ice.exebackend.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.ice.exebackend.entity.SysUser;

/**
 * MockDataGenerator 测试类
 */
class MockDataGeneratorTest {

    @Test
    void testMockUser() {
        // 生成模拟用户
        SysUser user = MockDataGenerator.mockUser(1L, "testuser");

        // 验证用户信息
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUserName());
        assertEquals("测试用户1", user.getNickName());
        assertEquals("testuser@test.com", user.getEmail());
        assertEquals("0", user.getStatus());
        assertEquals("0", user.getDelFlag());
    }

    @Test
    void testMockUsers() {
        // 生成批量用户
        var users = MockDataGenerator.mockUsers(5);

        // 验证用户列表
        assertNotNull(users);
        assertEquals(5, users.size());

        // 验证第一个用户
        SysUser firstUser = users.get(0);
        assertEquals(1L, firstUser.getId());
        assertEquals("user1", firstUser.getUserName());
    }

    @Test
    void testRandomInt() {
        // 测试随机整数生成
        int random = MockDataGenerator.randomInt(1, 100);

        assertTrue(random >= 1);
        assertTrue(random <= 100);
    }

    @Test
    void testRandomLong() {
        // 测试随机长整数生成
        Long random = MockDataGenerator.randomLong(1L, 1000L);

        assertTrue(random >= 1L);
        assertTrue(random <= 1000L);
    }
}
