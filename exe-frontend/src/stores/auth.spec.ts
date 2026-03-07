import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import * as authApi from '@/api/auth'
import type { UserInfo } from '@/api/user'

// Mock router
const mockPush = vi.fn()
vi.mock('@/router', () => ({
  default: {
    push: mockPush
  }
}))

// Mock API
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  getUserInfo: vi.fn(),
  logoutApi: vi.fn()
}))

describe('Auth Store测试', () => {
  beforeEach(() => {
    // 创建新的pinia实例
    setActivePinia(createPinia())
    // 清除localStorage
    localStorage.clear()
    // 清除mock
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 初始状态测试 ====================

  describe('初始状态', () => {
    it('应该正确初始化状态', () => {
      const store = useAuthStore()

      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(store.permissions).toEqual([])
    })

    it('应该从localStorage读取token', () => {
      localStorage.setItem('token', 'test-token')

      const store = useAuthStore()

      expect(store.token).toBe('test-token')
    })

    it('没有token时isAuthenticated应为false', () => {
      const store = useAuthStore()

      expect(store.isAuthenticated).toBe(false)
    })

    it('有token时isAuthenticated应为true', () => {
      const store = useAuthStore()
      store.token = 'test-token'

      expect(store.isAuthenticated).toBe(true)
    })
  })

  // ==================== Getters测试 ====================

  describe('Getters', () => {
    it('userNickname应该返回用户昵称', () => {
      const store = useAuthStore()
      store.user = {
        id: 1,
        userName: 'testuser',
        nickName: '测试用户',
        email: 'test@example.com'
      } as UserInfo

      expect(store.userNickname).toBe('测试用户')
    })

    it('没有用户时userNickname应该返回默认值', () => {
      const store = useAuthStore()

      expect(store.userNickname).toBe('用户')
    })

    it('isSuperAdmin应该正确判断超级管理员', () => {
      const store = useAuthStore()
      store.user = {
        id: 1,
        userName: 'admin',
        nickName: '超级管理员',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      expect(store.isSuperAdmin).toBe(true)
    })

    it('非超级管理员时isSuperAdmin应为false', () => {
      const store = useAuthStore()
      store.user = {
        id: 2,
        userName: 'teacher',
        nickName: '教师',
        roles: [{ id: 2, code: 'TEACHER', name: '教师' }]
      } as UserInfo

      expect(store.isSuperAdmin).toBe(false)
    })

    it('isAdmin应该正确判断管理员', () => {
      const store = useAuthStore()
      store.user = {
        id: 1,
        userName: 'admin',
        nickName: '管理员',
        roles: [{ id: 1, code: 'ADMIN', name: '管理员' }]
      } as UserInfo

      expect(store.isAdmin).toBe(true)
    })
  })

  // ==================== 登录测试 ====================

  describe('登录', () => {
    it('应该成功登录并保存token', async () => {
      const store = useAuthStore()
      const mockResponse = {
        code: 200,
        data: {
          token: 'test-token-123',
          user: {
            id: 1,
            userName: 'testuser',
            nickName: '测试用户'
          } as UserInfo,
          permissions: ['sys:user:list', 'sys:role:list']
        }
      }

      vi.mocked(authApi.login).mockResolvedValue(mockResponse)
      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        code: 200,
        data: {
          user: mockResponse.data.user,
          permissions: mockResponse.data.permissions
        }
      })

      await store.login({ username: 'testuser', password: 'password123' })

      expect(store.token).toBe('test-token-123')
      expect(localStorage.getItem('token')).toBe('test-token-123')
      expect(mockPush).toHaveBeenCalledWith('/home')
    })

    it('应该支持自定义重定向路径', async () => {
      const store = useAuthStore()
      const mockResponse = {
        code: 200,
        data: {
          token: 'test-token',
          user: { id: 1, userName: 'test' } as UserInfo,
          permissions: []
        }
      }

      vi.mocked(authApi.login).mockResolvedValue(mockResponse)
      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        code: 200,
        data: { user: mockResponse.data.user, permissions: [] }
      })

      await store.login({ username: 'test', password: 'pass' }, '/dashboard')

      expect(mockPush).toHaveBeenCalledWith('/dashboard')
    })

    it('登录失败时应该处理错误', async () => {
      const store = useAuthStore()
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      vi.mocked(authApi.login).mockRejectedValue(new Error('登录失败'))

      await store.login({ username: 'wrong', password: 'wrong' })

      expect(store.token).toBeNull()
      expect(consoleErrorSpy).toHaveBeenCalled()

      consoleErrorSpy.mockRestore()
    })
  })

  // ==================== 获取用户信息测试 ====================

  describe('获取用户信息', () => {
    it('应该成功获取用户信息', async () => {
      const store = useAuthStore()
      store.token = 'test-token'

      const mockUserInfo = {
        user: {
          id: 1,
          userName: 'testuser',
          nickName: '测试用户',
          email: 'test@example.com'
        } as UserInfo,
        permissions: ['sys:user:list', 'sys:role:list', 'sys:question:add']
      }

      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        code: 200,
        data: mockUserInfo
      })

      const result = await store.fetchUserInfo()

      expect(store.user).toEqual(mockUserInfo.user)
      expect(store.permissions).toEqual(mockUserInfo.permissions)
      expect(result).toEqual(mockUserInfo.permissions)
    })

    it('没有token时应该拒绝请求', async () => {
      const store = useAuthStore()

      await expect(store.fetchUserInfo()).rejects.toEqual('No token')
    })

    it('获取用户信息失败时应该登出', async () => {
      const store = useAuthStore()
      store.token = 'test-token'

      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        code: 401,
        message: '未授权'
      })

      // Mock window.location.href
      delete (window as any).location
      window.location = { href: '' } as any

      await expect(store.fetchUserInfo()).rejects.toBeDefined()

      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(store.permissions).toEqual([])
    })

    it('API异常时应该登出', async () => {
      const store = useAuthStore()
      store.token = 'test-token'
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      vi.mocked(authApi.getUserInfo).mockRejectedValue(new Error('网络错误'))

      // Mock window.location.href
      delete (window as any).location
      window.location = { href: '' } as any

      await expect(store.fetchUserInfo()).rejects.toThrow()

      expect(store.token).toBeNull()
      expect(consoleErrorSpy).toHaveBeenCalled()

      consoleErrorSpy.mockRestore()
    })
  })

  // ==================== 登出测试 ====================

  describe('登出', () => {
    it('应该成功登出并清除状态', async () => {
      const store = useAuthStore()
      store.token = 'test-token'
      store.user = { id: 1, userName: 'test' } as UserInfo
      store.permissions = ['sys:user:list']
      localStorage.setItem('token', 'test-token')

      vi.mocked(authApi.logoutApi).mockResolvedValue({ code: 200 })

      // Mock window.location.href
      delete (window as any).location
      window.location = { href: '' } as any

      await store.logout()

      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(store.permissions).toEqual([])
      expect(localStorage.getItem('token')).toBeNull()
      expect(window.location.href).toBe('/portal')
    })

    it('即使登出API失败也应该清除本地状态', async () => {
      const store = useAuthStore()
      store.token = 'test-token'
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      vi.mocked(authApi.logoutApi).mockRejectedValue(new Error('网络错误'))

      // Mock window.location.href
      delete (window as any).location
      window.location = { href: '' } as any

      await store.logout()

      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(window.location.href).toBe('/portal')

      consoleErrorSpy.mockRestore()
    })
  })

  // ==================== 权限检查测试 ====================

  describe('权限检查', () => {
    it('超级管理员应该拥有所有权限', () => {
      const store = useAuthStore()
      store.user = {
        id: 1,
        userName: 'superadmin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      expect(store.hasPermission('sys:user:add')).toBe(true)
      expect(store.hasPermission('sys:role:delete')).toBe(true)
      expect(store.hasPermission('any:permission:code')).toBe(true)
    })

    it('应该正确检查普通用户权限', () => {
      const store = useAuthStore()
      store.user = {
        id: 2,
        userName: 'teacher',
        roles: [{ id: 2, code: 'TEACHER', name: '教师' }]
      } as UserInfo
      store.permissions = ['sys:user:list', 'sys:question:add', 'sys:class:list']

      expect(store.hasPermission('sys:user:list')).toBe(true)
      expect(store.hasPermission('sys:question:add')).toBe(true)
      expect(store.hasPermission('sys:user:delete')).toBe(false)
    })

    it('没有权限码时应该放行', () => {
      const store = useAuthStore()

      expect(store.hasPermission('')).toBe(true)
      expect(store.hasPermission(null as any)).toBe(true)
      expect(store.hasPermission(undefined as any)).toBe(true)
    })

    it('没有用户时应该正确处理权限检查', () => {
      const store = useAuthStore()

      expect(store.hasPermission('sys:user:list')).toBe(false)
    })
  })

  // ==================== LocalStorage集成测试 ====================

  describe('LocalStorage集成', () => {
    it('登录后token应该保存到localStorage', async () => {
      const store = useAuthStore()

      vi.mocked(authApi.login).mockResolvedValue({
        code: 200,
        data: {
          token: 'new-token-123',
          user: { id: 1, userName: 'test' } as UserInfo,
          permissions: []
        }
      })
      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        code: 200,
        data: { user: { id: 1, userName: 'test' } as UserInfo, permissions: [] }
      })

      await store.login({ username: 'test', password: 'pass' })

      expect(localStorage.getItem('token')).toBe('new-token-123')
    })

    it('登出后应该从localStorage移除token', async () => {
      const store = useAuthStore()
      localStorage.setItem('token', 'test-token')

      vi.mocked(authApi.logoutApi).mockResolvedValue({ code: 200 })

      // Mock window.location.href
      delete (window as any).location
      window.location = { href: '' } as any

      await store.logout()

      expect(localStorage.getItem('token')).toBeNull()
    })

    it('store初始化时应该从localStorage恢复token', () => {
      localStorage.setItem('token', 'restored-token')

      const store = useAuthStore()

      expect(store.token).toBe('restored-token')
      expect(store.isAuthenticated).toBe(true)
    })
  })

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理空的用户信息响应', async () => {
      const store = useAuthStore()
      store.token = 'test-token'

      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        code: 200,
        data: {
          user: null as any,
          permissions: []
        }
      })

      await store.fetchUserInfo()

      expect(store.user).toBeNull()
      expect(store.permissions).toEqual([])
    })

    it('应该处理空的权限数组', async () => {
      const store = useAuthStore()
      store.token = 'test-token'

      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        code: 200,
        data: {
          user: { id: 1, userName: 'test' } as UserInfo,
          permissions: null as any
        }
      })

      await store.fetchUserInfo()

      expect(store.permissions).toBeNull()
    })

    it('应该处理用户没有roles的情况', () => {
      const store = useAuthStore()
      store.user = {
        id: 1,
        userName: 'test',
        roles: undefined
      } as any

      expect(store.isSuperAdmin).toBe(false)
      expect(store.isAdmin).toBe(false)
    })

    it('应该处理用户roles为空数组的情况', () => {
      const store = useAuthStore()
      store.user = {
        id: 1,
        userName: 'test',
        roles: []
      } as UserInfo

      expect(store.isSuperAdmin).toBe(false)
      expect(store.isAdmin).toBe(false)
    })
  })
})
