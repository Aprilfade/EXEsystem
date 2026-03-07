import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { ElDrawer, ElTree, ElButton, ElInput, ElMessage } from 'element-plus'
import PermissionDrawer from './PermissionDrawer.vue'
import { useAuthStore } from '@/stores/auth'
import * as roleApi from '@/api/role'
import type { UserInfo } from '@/api/user'
import type { Role } from '@/api/role'

// Mock API
vi.mock('@/api/role', () => ({
  getRolePermissions: vi.fn(),
  updateRolePermissions: vi.fn()
}))

// Mock ElMessage
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    }
  }
})

describe('PermissionDrawer 组件集成测试', () => {
  let authStore: ReturnType<typeof useAuthStore>

  const mockRole: Role = {
    id: 1,
    name: '教师',
    code: 'TEACHER',
    description: '教师角色',
    status: 1,
    createTime: '2024-01-01',
    updateTime: '2024-01-01'
  }

  const mockPermissions = [
    { id: 1, name: '系统管理', code: 'sys', parentId: 0, type: 1, sort: 1 },
    { id: 2, name: '用户管理', code: 'sys:user', parentId: 1, type: 1, sort: 1 },
    { id: 3, name: '用户列表', code: 'sys:user:list', parentId: 2, type: 2, sort: 1 },
    { id: 4, name: '用户新增', code: 'sys:user:add', parentId: 2, type: 2, sort: 2 },
    { id: 5, name: '角色管理', code: 'sys:role', parentId: 1, type: 1, sort: 2 },
    { id: 6, name: '角色列表', code: 'sys:role:list', parentId: 5, type: 2, sort: 1 }
  ]

  const mockCheckedIds = [3, 6]

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    vi.clearAllMocks()

    // Mock API responses
    vi.mocked(roleApi.getRolePermissions).mockResolvedValue({
      code: 200,
      data: {
        allPermissions: mockPermissions,
        checkedIds: mockCheckedIds
      }
    })

    vi.mocked(roleApi.updateRolePermissions).mockResolvedValue({
      code: 200,
      message: '更新成功'
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('visible为false时不应该显示', () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: false,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      const drawer = wrapper.findComponent(ElDrawer)
      expect(drawer.props('modelValue')).toBe(false)
    })

    it('visible为true时应该显示', () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      const drawer = wrapper.findComponent(ElDrawer)
      expect(drawer.props('modelValue')).toBe(true)
    })

    it('应该显示正确的标题', () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      const drawer = wrapper.findComponent(ElDrawer)
      expect(drawer.props('title')).toBe('为角色 [教师] 分配权限')
    })

    it('role为null时应该处理', () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: null
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      expect(wrapper.exists()).toBe(true)
    })
  })

  // ==================== 权限数据加载测试 ====================

  describe('权限数据加载', () => {
    it('打开时应该加载权限数据', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      expect(roleApi.getRolePermissions).toHaveBeenCalledWith(mockRole.id)
    })

    it('应该正确构建权限树', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      // 权限树应该被渲染
      const tree = wrapper.findComponent(ElTree)
      expect(tree.exists()).toBe(true)
    })

    it('应该设置默认选中的权限', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const tree = wrapper.findComponent(ElTree)
      expect(tree.props('defaultCheckedKeys')).toEqual(mockCheckedIds)
    })

    it('加载失败时应该处理错误', async () => {
      vi.mocked(roleApi.getRolePermissions).mockRejectedValue(new Error('Network error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })
  })

  // ==================== 权限树交互测试 ====================

  describe('权限树交互', () => {
    it('应该支持搜索过滤', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const searchInput = wrapper.findComponent(ElInput)
      await searchInput.setValue('用户')

      // 搜索文本应该被更新
      expect(searchInput.props('modelValue')).toBe('用户')
    })

    it('应该支持全选操作', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const checkAllButton = buttons.find(btn => btn.text() === '全选')

      expect(checkAllButton).toBeDefined()

      // 点击全选按钮
      await checkAllButton!.trigger('click')

      // 验证tree的方法被调用（通过ref）
      // 注意: 实际测试中可能需要mock tree ref
    })

    it('应该支持全不选操作', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const uncheckAllButton = buttons.find(btn => btn.text() === '全不选')

      expect(uncheckAllButton).toBeDefined()
    })

    it('应该显示权限代码', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      // 权限代码应该在tree中显示
      const tree = wrapper.findComponent(ElTree)
      expect(tree.exists()).toBe(true)
    })
  })

  // ==================== 权限更新测试 ====================

  describe('权限更新', () => {
    it('有权限的用户应该能看到确认按钮', async () => {
      authStore.user = {
        id: 1,
        userName: 'admin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const confirmButton = buttons.find(btn => btn.text() === '确认')

      expect(confirmButton).toBeDefined()
    })

    it('无权限的用户不应该看到确认按钮', async () => {
      authStore.user = {
        id: 2,
        userName: 'user',
        roles: [{ id: 2, code: 'USER', name: '普通用户' }]
      } as UserInfo
      authStore.permissions = [] // 没有 sys:role:perm 权限

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const confirmButton = buttons.find(btn => btn.text() === '确认')

      // 确认按钮不应该存在或被隐藏
      expect(confirmButton).toBeUndefined()
    })

    it('应该成功更新权限', async () => {
      authStore.user = {
        id: 1,
        userName: 'admin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const confirmButton = buttons.find(btn => btn.text() === '确认')

      // Mock tree ref methods
      const mockGetCheckedKeys = vi.fn().mockReturnValue([3, 6])
      const mockGetHalfCheckedKeys = vi.fn().mockReturnValue([2])
      ;(wrapper.vm as any).treeRef = {
        getCheckedKeys: mockGetCheckedKeys,
        getHalfCheckedKeys: mockGetHalfCheckedKeys
      }

      await confirmButton!.trigger('click')
      await flushPromises()

      expect(roleApi.updateRolePermissions).toHaveBeenCalledWith(mockRole.id, [3, 6, 2])
      expect(ElMessage.success).toHaveBeenCalledWith('权限更新成功')
    })

    it('更新失败时应该显示错误消息', async () => {
      authStore.user = {
        id: 1,
        userName: 'admin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      vi.mocked(roleApi.updateRolePermissions).mockResolvedValue({
        code: 500,
        message: '更新失败'
      })

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const confirmButton = buttons.find(btn => btn.text() === '确认')

      // Mock tree ref
      ;(wrapper.vm as any).treeRef = {
        getCheckedKeys: vi.fn().mockReturnValue([]),
        getHalfCheckedKeys: vi.fn().mockReturnValue([])
      }

      await confirmButton!.trigger('click')
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalled()
    })

    it('API异常时应该显示错误消息', async () => {
      authStore.user = {
        id: 1,
        userName: 'admin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      vi.mocked(roleApi.updateRolePermissions).mockRejectedValue(new Error('Network error'))

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const confirmButton = buttons.find(btn => btn.text() === '确认')

      // Mock tree ref
      ;(wrapper.vm as any).treeRef = {
        getCheckedKeys: vi.fn().mockReturnValue([]),
        getHalfCheckedKeys: vi.fn().mockReturnValue([])
      }

      await confirmButton!.trigger('click')
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalled()
    })
  })

  // ==================== 事件发射测试 ====================

  describe('事件发射', () => {
    it('关闭时应该发射update:visible事件', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const cancelButton = buttons.find(btn => btn.text() === '取消')

      await cancelButton!.trigger('click')

      expect(wrapper.emitted('update:visible')).toBeTruthy()
      expect(wrapper.emitted('update:visible')![0]).toEqual([false])
    })

    it('更新成功后应该发射success事件', async () => {
      authStore.user = {
        id: 1,
        userName: 'admin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const confirmButton = buttons.find(btn => btn.text() === '确认')

      // Mock tree ref
      ;(wrapper.vm as any).treeRef = {
        getCheckedKeys: vi.fn().mockReturnValue([]),
        getHalfCheckedKeys: vi.fn().mockReturnValue([])
      }

      await confirmButton!.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('success')).toBeTruthy()
      expect(wrapper.emitted('update:visible')).toBeTruthy()
    })
  })

  // ==================== 生命周期测试 ====================

  describe('生命周期', () => {
    it('visible从false变为true时应该加载数据', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: false,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      expect(roleApi.getRolePermissions).not.toHaveBeenCalled()

      await wrapper.setProps({ visible: true })
      await flushPromises()

      expect(roleApi.getRolePermissions).toHaveBeenCalled()
    })

    it('visible从true变为false时应该清理状态', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      await wrapper.setProps({ visible: false })

      // 再次打开时应该重新加载
      await wrapper.setProps({ visible: true })
      await flushPromises()

      // getRolePermissions应该被调用两次（首次打开 + 再次打开）
      expect(roleApi.getRolePermissions).toHaveBeenCalledTimes(2)
    })
  })

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理空权限列表', async () => {
      vi.mocked(roleApi.getRolePermissions).mockResolvedValue({
        code: 200,
        data: {
          allPermissions: [],
          checkedIds: []
        }
      })

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      // 应该显示空状态
      expect(wrapper.text()).toContain('暂无可分配的权限')
    })

    it('应该处理role为null的情况', async () => {
      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: null
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      // 不应该调用API
      expect(roleApi.getRolePermissions).not.toHaveBeenCalled()
    })

    it('应该处理treeRef为null的情况', async () => {
      authStore.user = {
        id: 1,
        userName: 'admin',
        roles: [{ id: 1, code: 'SUPER_ADMIN', name: '超级管理员' }]
      } as UserInfo

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const buttons = wrapper.findAllComponents(ElButton)
      const confirmButton = buttons.find(btn => btn.text() === '确认')

      // 不设置treeRef mock
      ;(wrapper.vm as any).treeRef = null

      await confirmButton!.trigger('click')

      expect(ElMessage.error).toHaveBeenCalledWith('数据异常，请刷新页面重试')
    })

    it('应该处理复杂的权限树层级', async () => {
      const deepPermissions = [
        { id: 1, name: 'Level 1', code: 'l1', parentId: 0, type: 1, sort: 1 },
        { id: 2, name: 'Level 2', code: 'l2', parentId: 1, type: 1, sort: 1 },
        { id: 3, name: 'Level 3', code: 'l3', parentId: 2, type: 1, sort: 1 },
        { id: 4, name: 'Level 4', code: 'l4', parentId: 3, type: 2, sort: 1 }
      ]

      vi.mocked(roleApi.getRolePermissions).mockResolvedValue({
        code: 200,
        data: {
          allPermissions: deepPermissions,
          checkedIds: [4]
        }
      })

      const wrapper = mount(PermissionDrawer, {
        props: {
          visible: true,
          role: mockRole
        },
        global: {
          components: { ElDrawer, ElTree, ElButton, ElInput }
        }
      })

      await flushPromises()

      const tree = wrapper.findComponent(ElTree)
      expect(tree.exists()).toBe(true)
    })
  })
})
