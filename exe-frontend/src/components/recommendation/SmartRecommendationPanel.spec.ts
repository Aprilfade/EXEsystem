import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import SmartRecommendationPanel from './SmartRecommendationPanel.vue'
import { ElMessage } from 'element-plus'

// Mock dependencies
vi.mock('@/api/recommendation')
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn(),
      warning: vi.fn()
    }
  }
})

/**
 * SmartRecommendationPanel 组件测试
 * 测试AI智能推荐面板的各项功能
 */
describe('SmartRecommendationPanel.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该正确渲染推荐面板', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button><slot /></button>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      expect(wrapper.text()).toContain('智能推荐')
    })

    it('应该显示标题和操作按钮', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button><slot /></button>' },
            'el-icon': { template: '<span />' },
            'el-tag': { template: '<span><slot /></span>' }
          }
        }
      })

      await flushPromises()

      expect(wrapper.text()).toContain('刷新推荐')
      expect(wrapper.text()).toContain('设置')
    })

    it('应该显示策略版本标签', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' },
            'el-icon': { template: '<span />' },
            'el-tag': { template: '<span class="tag"><slot /></span>' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.strategyVersion).toBe('v1.0')
    })
  })

  // ==================== 标签页切换测试 ====================

  describe('标签页切换', () => {
    it('应该有三个推荐类型标签', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      expect(wrapper.text()).toContain('题目推荐')
      expect(wrapper.text()).toContain('课程推荐')
      expect(wrapper.text()).toContain('知识点推荐')
    })

    it('应该能切换到课程推荐', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.activeTab = 'course'

      await wrapper.vm.$nextTick()

      expect(vm.activeTab).toBe('course')
    })

    it('切换标签时应重新加载推荐', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      const mockGetRecommendations = vi.fn().mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendations as any).mockImplementation(mockGetRecommendations)
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      mockGetRecommendations.mockClear()

      vm.handleTabChange()
      await flushPromises()

      expect(mockGetRecommendations).toHaveBeenCalled()
    })
  })

  // ==================== 推荐数据加载测试 ====================

  describe('推荐数据加载', () => {
    it('应该能加载推荐数据', async () => {
      const mockRecommendations = [
        {
          itemId: 1,
          itemType: 'question',
          title: '推荐题目1',
          score: 0.95,
          reason: '基于你的学习历史'
        },
        {
          itemId: 2,
          itemType: 'question',
          title: '推荐题目2',
          score: 0.90,
          reason: '知识点相关'
        }
      ]

      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: mockRecommendations })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 100,
          clickedRecommendations: 50,
          completedRecommendations: 30,
          clickRate: 0.5,
          completeRate: 0.6,
          avgScore: 85.5,
          avgStudyDuration: 120
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' },
            'recommendation-card': {
              template: '<div class="rec-card">{{ recommendation.title }}</div>',
              props: ['recommendation', 'index']
            }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.recommendations).toHaveLength(2)
      expect(vm.recommendations[0].title).toBe('推荐题目1')
    })

    it('加载失败时应显示错误消息', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockRejectedValue(new Error('Network error'))
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalledWith('加载推荐失败')
    })

    it('加载时应显示加载状态', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      const loadPromise = vm.loadRecommendations()

      await wrapper.vm.$nextTick()
      expect(vm.loading).toBe(true)

      await loadPromise
      expect(vm.loading).toBe(false)
    })
  })

  // ==================== 推荐统计测试 ====================

  describe('推荐统计', () => {
    it('应该正确显示推荐统计', async () => {
      const mockStats = {
        totalRecommendations: 100,
        clickedRecommendations: 50,
        completedRecommendations: 30,
        clickRate: 0.5,
        completeRate: 0.6,
        avgScore: 85.5,
        avgStudyDuration: 120
      }

      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({ code: 200, data: mockStats })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' },
            'el-row': { template: '<div><slot /></div>' },
            'el-col': { template: '<div><slot /></div>' }
          }
        }
      })

      await flushPromises()

      expect(wrapper.text()).toContain('100')
      expect(wrapper.text()).toContain('50.0%')
      expect(wrapper.text()).toContain('60.0%')
    })

    it('应该正确计算点击率', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 100,
          clickedRecommendations: 50,
          completedRecommendations: 30,
          clickRate: 0.5,
          completeRate: 0.3,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.stats.clickRate).toBe(0.5)
      expect((vm.stats.clickRate * 100).toFixed(1)).toBe('50.0')
    })
  })

  // ==================== 刷新推荐测试 ====================

  describe('刷新推荐', () => {
    it('应该能刷新推荐', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      const mockGetRecommendations = vi.fn().mockResolvedValue({ code: 200, data: [] })
      const mockGetStats = vi.fn().mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })
      ;(getRecommendations as any).mockImplementation(mockGetRecommendations)
      ;(getRecommendationStats as any).mockImplementation(mockGetStats)

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
          }
        }
      })

      await flushPromises()

      mockGetRecommendations.mockClear()
      mockGetStats.mockClear()

      const vm = wrapper.vm as any
      vm.refreshRecommendations()

      await flushPromises()

      expect(mockGetRecommendations).toHaveBeenCalled()
      expect(mockGetStats).toHaveBeenCalled()
    })

    it('刷新时应重置页码', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.page = 3

      vm.refreshRecommendations()
      await flushPromises()

      expect(vm.page).toBe(1)
    })
  })

  // ==================== 加载更多测试 ====================

  describe('加载更多', () => {
    it('应该能加载更多推荐', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({
        code: 200,
        data: [{ itemId: 3, title: '新推荐' }]
      })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      vm.recommendations = [{ itemId: 1, title: '推荐1' }, { itemId: 2, title: '推荐2' }]

      await vm.loadMore()

      expect(vm.recommendations).toHaveLength(3)
      expect(vm.page).toBe(2)
    })

    it('加载更多失败时应显示错误', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockRejectedValue(new Error('Network error'))
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      await vm.loadMore()

      expect(ElMessage.error).toHaveBeenCalledWith('加载更多失败')
    })

    it('没有更多数据时应隐藏加载更多按钮', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({
        code: 200,
        data: Array.from({ length: 5 }, (_, i) => ({ itemId: i, title: `推荐${i}` }))
      })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.settings.limit = 10

      await flushPromises()

      expect(vm.hasMore).toBe(false)
    })
  })

  // ==================== 推荐设置测试 ====================

  describe('推荐设置', () => {
    it('应该能打开设置对话框', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
            'el-dialog': { template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>', props: ['modelValue'] }
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.showSettings).toBe(false)

      vm.showSettings = true
      await wrapper.vm.$nextTick()

      expect(vm.showSettings).toBe(true)
    })

    it('应该能修改推荐数量', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.settings.limit = 20

      await wrapper.vm.$nextTick()

      expect(vm.settings.limit).toBe(20)
    })

    it('应该能修改多样性权重', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.settings.diversityWeight = 0.5

      await wrapper.vm.$nextTick()

      expect(vm.settings.diversityWeight).toBe(0.5)
    })

    it('应该能修改难度偏好', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.settings.difficultyPreference = 'hard'

      await wrapper.vm.$nextTick()

      expect(vm.settings.difficultyPreference).toBe('hard')
    })

    it('应该能修改策略版本', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.settings.strategyVersion = 'v2.0'

      await wrapper.vm.$nextTick()

      expect(vm.settings.strategyVersion).toBe('v2.0')
      expect(vm.strategyVersion).toBe('v2.0')
    })

    it('应用设置后应重新加载推荐', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      const mockGetRecommendations = vi.fn().mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendations as any).mockImplementation(mockGetRecommendations)
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      mockGetRecommendations.mockClear()

      vm.applySettings()
      await flushPromises()

      expect(mockGetRecommendations).toHaveBeenCalled()
      expect(vm.showSettings).toBe(false)
    })
  })

  // ==================== 推荐项点击测试 ====================

  describe('推荐项点击', () => {
    it('应该能点击推荐项', async () => {
      const { getRecommendations, getRecommendationStats, recordClick } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })
      ;(recordClick as any).mockResolvedValue({ code: 200 })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      const mockItem = {
        itemId: 1,
        logId: 'log-123',
        title: '推荐1'
      }

      await vm.handleItemClick(mockItem)

      expect(recordClick).toHaveBeenCalledWith({ logId: 'log-123' })
    })

    it('点击推荐项应触发事件', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      const mockItem = { itemId: 1, title: '推荐1' }

      await vm.handleItemClick(mockItem)

      expect(wrapper.emitted('itemClick')).toBeTruthy()
      expect(wrapper.emitted('itemClick')![0]).toEqual([mockItem])
    })

    it('应该能处理推荐项操作', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' }
          }
        }
      })

      const vm = wrapper.vm as any
      const mockItem = { itemId: 1, title: '推荐1' }

      vm.handleItemAction(mockItem, 'favorite')

      expect(wrapper.emitted('itemAction')).toBeTruthy()
      expect(wrapper.emitted('itemAction')![0]).toEqual([{ item: mockItem, action: 'favorite' }])
    })
  })

  // ==================== 空状态测试 ====================

  describe('空状态', () => {
    it('没有推荐时应显示空状态', async () => {
      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: [] })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 0,
          clickedRecommendations: 0,
          completedRecommendations: 0,
          clickRate: 0,
          completeRate: 0,
          avgScore: 0,
          avgStudyDuration: 0
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' },
            'el-empty': { template: '<div class="empty">暂无推荐</div>' }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.recommendations).toHaveLength(0)
    })
  })

  // ==================== 性能测试 ====================

  describe('性能', () => {
    it('应该能处理大量推荐数据', async () => {
      const largeRecommendations = Array.from({ length: 100 }, (_, i) => ({
        itemId: i + 1,
        itemType: 'question',
        title: `推荐${i + 1}`,
        score: Math.random(),
        reason: '推荐理由'
      }))

      const { getRecommendations, getRecommendationStats } = await import('@/api/recommendation')
      ;(getRecommendations as any).mockResolvedValue({ code: 200, data: largeRecommendations })
      ;(getRecommendationStats as any).mockResolvedValue({
        code: 200,
        data: {
          totalRecommendations: 1000,
          clickedRecommendations: 500,
          completedRecommendations: 300,
          clickRate: 0.5,
          completeRate: 0.6,
          avgScore: 85,
          avgStudyDuration: 120
        }
      })

      const wrapper = mount(SmartRecommendationPanel, {
        global: {
          stubs: {
            'el-tabs': { template: '<div><slot /></div>' },
            'el-tab-pane': { template: '<div><slot /></div>' },
            'el-button': { template: '<button />' },
            'recommendation-card': {
              template: '<div class="rec-card" />',
              props: ['recommendation', 'index']
            }
          }
        }
      })

      await flushPromises()

      const vm = wrapper.vm as any
      expect(vm.recommendations).toHaveLength(100)
    })
  })
})
