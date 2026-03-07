import type { User } from '@/api/user'
import type { TodoItem } from '@/api/dashboard'

/**
 * Mock 用户数据
 */
export function mockUser(id = 1): User {
  return {
    id,
    userName: `user${id}`,
    nickName: `测试用户${id}`,
    email: `user${id}@test.com`,
    phoneNumber: `138000000${id.toString().padStart(2, '0')}`,
    status: '0',
    createTime: new Date().toISOString()
  }
}

/**
 * Mock 待办事项
 */
export function mockTodoItem(type = 'grading'): TodoItem {
  const configs = {
    grading: {
      type: 'grading',
      title: '待批阅试卷',
      count: 10,
      time: '2小时前',
      color: '#409eff',
      icon: 'Document'
    },
    unread: {
      type: 'unread',
      title: '未读通知',
      count: 5,
      time: '1小时前',
      color: '#67c23a',
      icon: 'Bell'
    }
  }

  return configs[type as keyof typeof configs] || configs.grading
}

/**
 * Mock 用户列表
 */
export function mockUsers(count = 10): User[] {
  return Array.from({ length: count }, (_, i) => mockUser(i + 1))
}
