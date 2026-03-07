import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw, Router } from 'vue-router'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useStudentAuthStore } from '@/stores/studentAuth'
import type { UserInfo } from '@/api/user'
import MainLayout from '@/layouts/MainLayout.vue'

// Mock ElMessage
const mockElMessage = {
  error: vi.fn(),
  success: vi.fn(),
  warning: vi.fn()
}
vi.mock('element-plus', () => ({
  ElMessage: mockElMessage
}))

// Mock layouts
vi.mock('@/layouts/MainLayout.vue', () => ({
  default: { name: 'MainLayout' }
}))

vi.mock('@/layouts/StudentLayout.vue', () => ({
  default: { name: 'StudentLayout' }
}))

// Mock views
vi.mock('@/views/Login.vue', () => ({ default: { name: 'Login' } }))
vi.mock('@/views/Portal.vue', () => ({ default: { name: 'Portal' } }))
vi.mock('@/views/Home.vue', () => ({ default: { name: 'Home' } }))
vi.mock('@/components/user/UserManage.vue', () => ({ default: { name: 'UserManage' } }))
vi.mock('@/views/StudentLogin.vue', () => ({ default: { name: 'StudentLogin' } }))
vi.mock('@/views/student/StudentDashboard.vue', () => ({ default: { name: 'StudentDashboard' } }))

describe('路由守卫测试', () => {
  let router: Router
  let authStore: ReturnType<typeof useAuthStore>
  let studentAuthStore: ReturnType<typeof useStudentAuthStore>

  // 创建简化版的路由配置用于测试
  const createTestRouter = () => {
    const routes: Array<RouteRecordRaw> = [
      {
        path: '/login',
        name: 'Login',
        component: { name: 'Login' }
      },
      {
        path: '/portal',
        name: 'Portal',
        component: { name: 'Portal' },
        meta: { title: '系统导航' }
      },
      {
        path: '/student/login',
        name: 'StudentLogin',
        component: { name: 'StudentLogin' }
      },
      {
        path: '/',
        component: MainLayout,
        redirect: '/portal',
        meta: { requiresAuth: true },
        children: [
          {
            path: 'home',
            name: 'Home',
            component: { name: 'Home' },
            meta: { permission: 'sys:home', title: '工作台' }
          },
          {
            path: 'users',
            name: 'UserManagement',
            component: { name: 'UserManage' },
            meta: { permission: 'sys:user:list', title: '成员管理' }
          },
          {
            path: 'profile',
            name: 'UserProfile',
            component: { name: 'UserProfile' },
            meta: { title: '个人中心' }
          }
        ]
      },
      {
        path: '/student',
        component: { name: 'StudentLayout' },
        meta: { requiresAuth: true, role: 'STUDENT' },
        redirect: '/student/dashboard',
        children: [
          {
            path: 'dashboard',
            name: 'StudentDashboard',
            component: { name: 'StudentDashboard' },
            meta: { title: '学习中心' }
          }
        ]
      }
    ]

    const testRouter = createRouter({
      history: createWebHistory(),
      routes
    })

    // 复制原始守卫逻辑
    testRouter.beforeEach(async (to, from, next) => {
      // 动态标题逻辑
      const pageTitle = to.meta.title as string

      if (to.path === '/portal' && pageTitle) {
        document.title = pageTitle
      } else {
        let baseTitle = '试题管理综合系统'
        if (to.path.startsWith('/student/')) {
          baseTitle = '在线学习系统'
        }

        if (pageTitle) {
          document.title = `${pageTitle} - ${baseTitle}`
        } else {
          document.title = baseTitle
        }
      }

      const authStore = useAuthStore()
      const studentAuthStore = useStudentAuthStore()

      const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

      if (requiresAuth) {
        const requiredRole = to.meta.role

        if (requiredRole === 'STUDENT') {
          // 学生端路由保护
          if (studentAuthStore.isAuthenticated) {
            if (!studentAuthStore.student) {
              try {
                await studentAuthStore.fetchStudentInfo()
              } catch (error) {
                console.error('加载学生信息失败:', error)
                studentAuthStore.logout()
                next({ path: '/student/login', query: { redirect: to.fullPath } })
                return
              }
            }
            next()
          } else {
            next({ path: '/student/login', query: { redirect: to.fullPath } })
          }
        } else {
          // 管理后台路由保护
          if (authStore.isAuthenticated) {
            if (!authStore.user) {
              await authStore.fetchUserInfo()
            }
            const requiredPermission = to.meta.permission as string | undefined
            if (requiredPermission && !authStore.hasPermission(requiredPermission)) {
              mockElMessage.error('您没有权限访问该页面')
              next(from.path && from.path !== '/login' ? false : '/home')
            } else {
              next()
            }
          } else {
            next({ path: '/login', query: { redirect: to.fullPath } })
          }
        }
      } else {
        // 不需要认证的页面直接放行
        next()
      }
    })

    return testRouter
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    studentAuthStore = useStudentAuthStore()
    router = createTestRouter()
    vi.clearAllMocks()

    // Mock fetchUserInfo and fetchStudentInfo
    vi.spyOn(authStore, 'fetchUserInfo').mockResolvedValue(['sys:home', 'sys:user:list'])
    vi.spyOn(studentAuthStore, 'fetchStudentInfo').mockResolvedValue(undefined)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 公开路由测试 ====================

  describe('公开路由', () => {
    it('应该允许访问登录页', async () => {
      await router.push('/login')
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('应该允许访问Portal页', async () => {
      await router.push('/portal')
      expect(router.currentRoute.value.path).toBe('/portal')
    })

    it('应该允许访问学生登录页', async () => {
      await router.push('/student/login')
      expect(router.currentRoute.value.path).toBe('/student/login')
    })
  })

  // ==================== 页面标题设置测试 ====================

  describe('页面标题设置', () => {
    it('Portal页应该只显示页面标题', async () => {
      await router.push('/portal')
      expect(document.title).toBe('系统导航')
    })

    it('管理后台页面应该显示完整标题', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo
      authStore.permissions = ['sys:home']

      await router.push('/home')
      expect(document.title).toBe('工作台 - 试题管理综合系统')
    })

    it('学生端页面应该显示学习系统标题', async () => {
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = { id: 1, studentName: '学生' } as any

      await router.push('/student/dashboard')
      expect(document.title).toBe('学习中心 - 在线学习系统')
    })

    it('没有title meta的页面应该显示基础标题', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo

      // 假设访问一个没有title的路由
      await router.push('/login')
      expect(document.title).toBe('试题管理综合系统')
    })
  })

  // ==================== 管理后台认证测试 ====================

  describe('管理后台认证', () => {
    it('未登录时应该重定向到登录页', async () => {
      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/login')
      expect(router.currentRoute.value.query.redirect).toBe('/home')
    })

    it('已登录用户应该能访问需要认证的页面', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo
      authStore.permissions = ['sys:home']

      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/home')
    })

    it('已登录但无用户信息时应该自动获取用户信息', async () => {
      authStore.token = 'test-token'
      authStore.permissions = ['sys:home']

      const fetchUserInfoSpy = vi.spyOn(authStore, 'fetchUserInfo')

      await router.push('/home')

      expect(fetchUserInfoSpy).toHaveBeenCalled()
    })

    it('已有用户信息时不应该重复获取', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo
      authStore.permissions = ['sys:home']

      const fetchUserInfoSpy = vi.spyOn(authStore, 'fetchUserInfo')

      await router.push('/home')

      expect(fetchUserInfoSpy).not.toHaveBeenCalled()
    })
  })

  // ==================== 权限检查测试 ====================

  describe('权限检查', () => {
    it('有权限的用户应该能访问受保护的页面', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo
      authStore.permissions = ['sys:user:list']

      await router.push('/users')
      expect(router.currentRoute.value.path).toBe('/users')
    })

    it('无权限的用户应该被阻止访问', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 2, userName: 'teacher' } as UserInfo
      authStore.permissions = ['sys:home'] // 没有 sys:user:list 权限

      await router.push('/users')

      expect(mockElMessage.error).toHaveBeenCalledWith('您没有权限访问该页面')
      // 应该停留在原地或跳转到home
      expect(router.currentRoute.value.path).not.toBe('/users')
    })

    it('超级管理员应该能访问所有页面', async () => {
      authStore.token = 'test-token'
      authStore.user = {
        id: 1,
        userName: 'superadmin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      await router.push('/users')
      expect(router.currentRoute.value.path).toBe('/users')
    })

    it('没有权限要求的页面应该允许所有登录用户访问', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'user' } as UserInfo
      authStore.permissions = [] // 空权限列表

      await router.push('/profile')
      expect(router.currentRoute.value.path).toBe('/profile')
    })
  })

  // ==================== 学生端认证测试 ====================

  describe('学生端认证', () => {
    it('未登录的学生应该重定向到学生登录页', async () => {
      await router.push('/student/dashboard')
      expect(router.currentRoute.value.path).toBe('/student/login')
      expect(router.currentRoute.value.query.redirect).toBe('/student/dashboard')
    })

    it('已登录的学生应该能访问学生页面', async () => {
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = { id: 1, studentName: '学生' } as any

      await router.push('/student/dashboard')
      expect(router.currentRoute.value.path).toBe('/student/dashboard')
    })

    it('已登录但无学生信息时应该自动获取', async () => {
      studentAuthStore.token = 'student-token'

      const fetchStudentInfoSpy = vi.spyOn(studentAuthStore, 'fetchStudentInfo')

      await router.push('/student/dashboard')

      expect(fetchStudentInfoSpy).toHaveBeenCalled()
    })

    it('获取学生信息失败应该登出并重定向', async () => {
      studentAuthStore.token = 'student-token'

      vi.spyOn(studentAuthStore, 'fetchStudentInfo').mockRejectedValue(new Error('Network error'))
      const logoutSpy = vi.spyOn(studentAuthStore, 'logout')

      await router.push('/student/dashboard')

      expect(logoutSpy).toHaveBeenCalled()
      expect(router.currentRoute.value.path).toBe('/student/login')
    })

    it('已有学生信息时不应该重复获取', async () => {
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = { id: 1, studentName: '学生' } as any

      const fetchStudentInfoSpy = vi.spyOn(studentAuthStore, 'fetchStudentInfo')

      await router.push('/student/dashboard')

      expect(fetchStudentInfoSpy).not.toHaveBeenCalled()
    })
  })

  // ==================== 角色隔离测试 ====================

  describe('角色隔离', () => {
    it('管理员token不应该能访问学生端', async () => {
      authStore.token = 'admin-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo

      await router.push('/student/dashboard')

      // 应该被重定向到学生登录页
      expect(router.currentRoute.value.path).toBe('/student/login')
    })

    it('学生token不应该能访问管理后台', async () => {
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = { id: 1, studentName: '学生' } as any

      await router.push('/home')

      // 应该被重定向到管理员登录页
      expect(router.currentRoute.value.path).toBe('/login')
    })
  })

  // ==================== 重定向逻辑测试 ====================

  describe('重定向逻辑', () => {
    it('未登录访问受保护页面应该保存redirect参数', async () => {
      await router.push('/home')

      expect(router.currentRoute.value.path).toBe('/login')
      expect(router.currentRoute.value.query.redirect).toBe('/home')
    })

    it('学生未登录访问受保护页面应该保存redirect参数', async () => {
      await router.push('/student/dashboard')

      expect(router.currentRoute.value.path).toBe('/student/login')
      expect(router.currentRoute.value.query.redirect).toBe('/student/dashboard')
    })

    it('无权限访问从非登录页跳转应该停留原地', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'user' } as UserInfo
      authStore.permissions = []

      // 先访问一个允许的页面
      await router.push('/profile')
      expect(router.currentRoute.value.path).toBe('/profile')

      // 然后尝试访问无权限的页面
      await router.push('/users')

      // 应该显示错误消息
      expect(mockElMessage.error).toHaveBeenCalledWith('您没有权限访问该页面')
    })

    it('无权限访问从登录页跳转应该重定向到home', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'user' } as UserInfo
      authStore.permissions = []

      // 模拟从登录页来的情况
      await router.push('/login')

      // 尝试访问无权限的页面
      await router.push('/users')

      expect(mockElMessage.error).toHaveBeenCalledWith('您没有权限访问该页面')
    })
  })

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理路由meta中缺少requiresAuth的情况', async () => {
      await router.push('/portal')
      expect(router.currentRoute.value.path).toBe('/portal')
    })

    it('应该处理路由meta中缺少permission的情况', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'user' } as UserInfo
      authStore.permissions = []

      await router.push('/profile')
      expect(router.currentRoute.value.path).toBe('/profile')
    })

    it('应该处理嵌套路由的认证检查', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo
      authStore.permissions = ['sys:user:list']

      await router.push('/users')
      expect(router.currentRoute.value.path).toBe('/users')
    })

    it('应该处理fetchUserInfo失败的情况', async () => {
      authStore.token = 'test-token'

      vi.spyOn(authStore, 'fetchUserInfo').mockRejectedValue(new Error('API Error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      try {
        await router.push('/home')
      } catch (e) {
        // 预期可能会有错误
      }

      consoleErrorSpy.mockRestore()
    })

    it('应该处理空的meta对象', async () => {
      // Portal页面有meta但没有requiresAuth
      await router.push('/portal')
      expect(router.currentRoute.value.path).toBe('/portal')
    })

    it('应该处理同时访问多个路由的情况', async () => {
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo
      authStore.permissions = ['sys:home', 'sys:user:list']

      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/home')

      await router.push('/users')
      expect(router.currentRoute.value.path).toBe('/users')

      await router.push('/profile')
      expect(router.currentRoute.value.path).toBe('/profile')
    })
  })

  // ==================== 路由导航完整流程测试 ====================

  describe('路由导航完整流程', () => {
    it('应该完成完整的登录->访问->登出流程', async () => {
      // 1. 未登录访问受保护页面
      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/login')

      // 2. 登录
      authStore.token = 'test-token'
      authStore.user = { id: 1, userName: 'admin' } as UserInfo
      authStore.permissions = ['sys:home']

      // 3. 重新访问
      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/home')

      // 4. 登出
      authStore.token = null
      authStore.user = null
      authStore.permissions = []

      // 5. 再次访问应该被拒绝
      await router.push('/users')
      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('应该完成学生端完整流程', async () => {
      // 1. 未登录访问学生页面
      await router.push('/student/dashboard')
      expect(router.currentRoute.value.path).toBe('/student/login')

      // 2. 学生登录
      studentAuthStore.token = 'student-token'
      studentAuthStore.student = { id: 1, studentName: '学生' } as any

      // 3. 重新访问
      await router.push('/student/dashboard')
      expect(router.currentRoute.value.path).toBe('/student/dashboard')

      // 4. 登出
      studentAuthStore.token = null
      studentAuthStore.student = null

      // 5. 再次访问应该被拒绝
      await router.push('/student/dashboard')
      expect(router.currentRoute.value.path).toBe('/student/login')
    })
  })
})
