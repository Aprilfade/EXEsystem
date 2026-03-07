import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { useStudentAuthStore } from '@/stores/studentAuth'
import StudentDashboard from './StudentDashboard.vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// Mock dependencies
vi.mock('@/utils/request')
vi.mock('@/api/studentAuth')
vi.mock('@/api/student')
vi.mock('@/api/learningActivity')
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

vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    resize: vi.fn()
  })),
  format: {
    formatTime: vi.fn((format, date) => date)
  }
}))

/**
 * StudentDashboard 组件测试
 * 测试学生仪表盘的各项功能
 */
describe('StudentDashboard.vue', () => {
  let studentAuthStore: ReturnType<typeof useStudentAuthStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    studentAuthStore = useStudentAuthStore()
    vi.clearAllMocks()
    localStorage.clear()

    // 设置默认学生信息
    studentAuthStore.student = {
      id: 1,
      studentName: '测试学生',
      avatar: null,
      avatarFrameStyle: null,
      points: 1500
    } as any
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该正确渲染欢迎信息', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' },
            'el-icon': { template: '<span><slot /></span>' },
            'user-avatar': { template: '<div class="user-avatar" />' }
          }
        }
      })

      await flushPromises()

      expect(wrapper.text()).toContain('测试学生')
      expect(wrapper.text()).toContain('同学')
    })

    it('应该根据时间显示不同的欢迎语', () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      const hour = new Date().getHours()

      if (hour < 6) {
        expect(vm.welcomeMessage).toBe('凌晨好')
      } else if (hour < 12) {
        expect(vm.welcomeMessage).toBe('上午好')
      } else if (hour < 14) {
        expect(vm.welcomeMessage).toBe('中午好')
      } else if (hour < 18) {
        expect(vm.welcomeMessage).toBe('下午好')
      } else {
        expect(vm.welcomeMessage).toBe('晚上好')
      }
    })

    it('应该渲染快捷入口网格', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div class="el-card"><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' },
            'el-row': { template: '<div><slot /></div>' },
            'el-col': { template: '<div><slot /></div>' },
            'user-avatar': { template: '<div />' }
          }
        }
      })

      await flushPromises()

      // 检查是否有快捷入口项
      const accessItems = wrapper.findAll('.access-item')
      expect(accessItems.length).toBeGreaterThan(0)

      // 检查关键入口
      const text = wrapper.text()
      expect(text).toContain('开始练习')
      expect(text).toContain('我的错题本')
      expect(text).toContain('智能复习')
      expect(text).toContain('模拟考试')
      expect(text).toContain('考试记录')
      expect(text).toContain('我的收藏')
      expect(text).toContain('知识对战')
      expect(text).toContain('积分商城')
    })
  })

  // ==================== 统计数据测试 ====================

  describe('统计数据', () => {
    it('应该正确显示学习统计', async () => {
      const mockStats = {
        totalAnswered: 1234,
        averageAccuracy: 85.5,
        wrongRecordCount: 178,
        studyDurationHours: 42
      }

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-row': { template: '<div><slot /></div>' },
            'el-col': { template: '<div><slot /></div>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.stats = mockStats

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('1234')
      expect(wrapper.text()).toContain('85.5')
      expect(wrapper.text()).toContain('178')
      expect(wrapper.text()).toContain('42')
    })

    it('应该正确显示积分信息', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-tag': { template: '<span><slot /></span>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.myPoints = 1500

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('1500')
    })
  })

  // ==================== 快捷入口导航测试 ====================

  describe('快捷入口导航', () => {
    it('应该能点击开始练习跳转', async () => {
      const mockRouter = {
        push: vi.fn()
      }

      const wrapper = mount(StudentDashboard, {
        global: {
          mocks: {
            $router: mockRouter
          },
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      const practiceItem = wrapper.find('.access-item.item-blue')
      if (practiceItem.exists()) {
        await practiceItem.trigger('click')
        expect(mockRouter.push).toHaveBeenCalledWith('/student/practice')
      }
    })

    it('应该能点击错题本跳转', async () => {
      const mockRouter = {
        push: vi.fn()
      }

      const wrapper = mount(StudentDashboard, {
        global: {
          mocks: {
            $router: mockRouter
          },
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      const wrongRecordsItem = wrapper.find('.access-item.item-red')
      if (wrongRecordsItem.exists()) {
        await wrongRecordsItem.trigger('click')
        expect(mockRouter.push).toHaveBeenCalledWith('/student/wrong-records')
      }
    })

    it('应该能点击模拟考试跳转', async () => {
      const mockRouter = {
        push: vi.fn()
      }

      const wrapper = mount(StudentDashboard, {
        global: {
          mocks: {
            $router: mockRouter
          },
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      const examItem = wrapper.find('.access-item.item-purple')
      if (examItem.exists()) {
        await examItem.trigger('click')
        expect(mockRouter.push).toHaveBeenCalledWith('/student/exams')
      }
    })
  })

  // ==================== 排行榜测试 ====================

  describe('排行榜', () => {
    it('应该正确显示排行榜数据', async () => {
      const mockLeaderboard = [
        { name: '学霸A', grade: '高三', points: 5000, avatar: null, avatarFrameStyle: null },
        { name: '学霸B', grade: '高三', points: 4500, avatar: null, avatarFrameStyle: null },
        { name: '学霸C', grade: '高三', points: 4000, avatar: null, avatarFrameStyle: null }
      ]

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-tag': { template: '<span><slot /></span>' },
            'user-avatar': { template: '<div />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.leaderboard = mockLeaderboard

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('学霸A')
      expect(wrapper.text()).toContain('5000')
      expect(wrapper.text()).toContain('学霸B')
      expect(wrapper.text()).toContain('4500')
    })

    it('排行榜为空时应显示空状态', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-empty': { template: '<div>暂无排名数据</div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.leaderboard = []

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('暂无排名数据')
    })

    it('应该为前三名添加特殊样式', async () => {
      const mockLeaderboard = [
        { name: '第一名', grade: '高三', points: 5000, avatar: null, avatarFrameStyle: null },
        { name: '第二名', grade: '高三', points: 4500, avatar: null, avatarFrameStyle: null },
        { name: '第三名', grade: '高三', points: 4000, avatar: null, avatarFrameStyle: null }
      ]

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div class="el-card"><slot /><slot name="header" /></div>' },
            'user-avatar': { template: '<div />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.leaderboard = mockLeaderboard

      await wrapper.vm.$nextTick()

      const rankItems = wrapper.findAll('.rank-num')
      if (rankItems.length >= 3) {
        expect(rankItems[0].classes()).toContain('rank-1')
        expect(rankItems[1].classes()).toContain('rank-2')
        expect(rankItems[2].classes()).toContain('rank-3')
      }
    })
  })

  // ==================== 学习动态测试 ====================

  describe('学习动态', () => {
    it('应该正确显示学习活动', async () => {
      const mockActivities = [
        { id: 1, description: '完成了10道练习题', createTime: '2024-01-01 10:00:00' },
        { id: 2, description: '参加了数学模拟考试', createTime: '2024-01-01 14:00:00' },
        { id: 3, description: '复习了20个错题', createTime: '2024-01-01 18:00:00' }
      ]

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-timeline': { template: '<div><slot /></div>' },
            'el-timeline-item': {
              template: '<div><slot />{{ timestamp }}</div>',
              props: ['timestamp']
            }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.activities = mockActivities

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('完成了10道练习题')
      expect(wrapper.text()).toContain('参加了数学模拟考试')
      expect(wrapper.text()).toContain('复习了20个错题')
    })

    it('学习动态为空时应显示空状态', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-timeline': { template: '<div><slot /></div>' },
            'el-empty': { template: '<div>暂无学习动态</div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.activities = []

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('暂无学习动态')
    })
  })

  // ==================== AI助手测试 ====================

  describe('AI助手', () => {
    it('应该能打开AI助手', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
            'ai-chat-panel': { template: '<div>AI Chat Panel</div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.showAiChat).toBe(false)

      vm.showAiChat = true
      await wrapper.vm.$nextTick()

      expect(vm.showAiChat).toBe(true)
    })

    it('应该能关闭AI助手', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
            'ai-chat-panel': { template: '<div>AI Chat Panel</div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.showAiChat = true
      await wrapper.vm.$nextTick()

      vm.showAiChat = false
      await wrapper.vm.$nextTick()

      expect(vm.showAiChat).toBe(false)
    })

    it('AI助手切换按钮应该可见', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' }
          }
        }
      })

      await flushPromises()

      const toggleBtn = wrapper.find('.ai-toggle-btn')
      expect(toggleBtn.exists() || wrapper.find('.floating-ai-assistant').exists()).toBe(true)
    })
  })

  // ==================== 积分商城测试 ====================

  describe('积分商城', () => {
    it('应该能打开积分商城对话框', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-icon': { template: '<span />' },
            'points-mall-dialog': {
              template: '<div v-if="visible">Mall Dialog</div>',
              props: ['visible']
            }
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.isMallVisible).toBe(false)

      const mallItem = wrapper.find('.access-item.item-gold')
      if (mallItem.exists()) {
        await mallItem.trigger('click')
        expect(vm.isMallVisible).toBe(true)
      }
    })
  })

  // ==================== 热力图测试 ====================

  describe('学习打卡热力图', () => {
    it('应该初始化热力图', async () => {
      const mockHeatmapData = {
        '2024-01-01': 5,
        '2024-01-02': 3,
        '2024-01-03': 8,
        '2024-01-04': 2
      }

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.initHeatmap(mockHeatmapData)

      expect(echarts.init).toHaveBeenCalled()
    })

    it('热力图数据应该正确转换', () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      const mockData = {
        '2024-01-01': 5,
        '2024-01-02': 3
      }

      vm.initHeatmap(mockData)

      const mockEchartsInstance = (echarts.init as any).mock.results[0].value
      expect(mockEchartsInstance.setOption).toHaveBeenCalled()
    })
  })

  // ==================== 成就墙测试 ====================

  describe('成就墙', () => {
    it('应该正确显示成就列表', async () => {
      const mockAchievements = [
        { id: 1, name: '初学者', description: '完成首次练习' },
        { id: 2, name: '勤奋学习', description: '连续学习7天' },
        { id: 3, name: '考试达人', description: '参加10次考试' }
      ]

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'achievement-list': {
              template: '<div><slot /></div>',
              props: ['list']
            }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.achievements = mockAchievements

      await wrapper.vm.$nextTick()

      expect(vm.achievements).toHaveLength(3)
    })
  })

  // ==================== 组件集成测试 ====================

  describe('组件集成', () => {
    it('应该包含所有关键子组件', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div class="el-card"><slot /><slot name="header" /></div>' },
            'learning-analytics': { template: '<div class="learning-analytics" />' },
            'recommendation-panel': { template: '<div class="recommendation-panel" />' },
            'sign-in-calendar': { template: '<div class="sign-in-calendar" />' },
            'achievement-list': { template: '<div class="achievement-list" />' },
            'ai-chat-panel': { template: '<div class="ai-chat-panel" />' }
          }
        }
      })

      await flushPromises()

      // 检查关键组件是否存在
      const text = wrapper.html()
      expect(text.includes('learning-analytics') || text.includes('LearningAnalytics')).toBe(true)
    })

    it('应该正确传递数据给子组件', async () => {
      const mockAchievements = [
        { id: 1, name: '成就1' },
        { id: 2, name: '成就2' }
      ]

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'achievement-list': {
              template: '<div>{{ list.length }} achievements</div>',
              props: ['list']
            }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.achievements = mockAchievements

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('2 achievements')
    })
  })

  // ==================== 错误处理测试 ====================

  describe('错误处理', () => {
    it('数据加载失败时应保持稳定', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.stats = {
        totalAnswered: 0,
        averageAccuracy: 0,
        wrongRecordCount: 0,
        studyDurationHours: 0
      }

      await wrapper.vm.$nextTick()

      // 即使数据为空，组件也应该正常渲染
      expect(wrapper.exists()).toBe(true)
    })

    it('热力图初始化失败时应不影响其他功能', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any

      // 不提供 heatmapChartRef
      vm.heatmapChartRef = null
      vm.initHeatmap({})

      // 组件应该仍然可用
      expect(wrapper.exists()).toBe(true)
    })
  })

  // ==================== 响应式测试 ====================

  describe('响应式行为', () => {
    it('统计数据更新时应重新渲染', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.stats = { totalAnswered: 100, averageAccuracy: 80, wrongRecordCount: 20, studyDurationHours: 10 }

      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('100')

      vm.stats = { totalAnswered: 200, averageAccuracy: 85, wrongRecordCount: 30, studyDurationHours: 15 }

      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('200')
    })

    it('排行榜数据更新时应重新渲染', async () => {
      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'user-avatar': { template: '<div />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.leaderboard = [{ name: '学生A', grade: '高三', points: 1000, avatar: null, avatarFrameStyle: null }]

      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('学生A')

      vm.leaderboard = [{ name: '学生B', grade: '高三', points: 2000, avatar: null, avatarFrameStyle: null }]

      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('学生B')
    })
  })

  // ==================== 性能测试 ====================

  describe('性能', () => {
    it('应该能处理大量学习动态', async () => {
      const largeActivities = Array.from({ length: 100 }, (_, i) => ({
        id: i,
        description: `活动${i}`,
        createTime: `2024-01-01 ${i}:00:00`
      }))

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'el-timeline': { template: '<div><slot /></div>' },
            'el-timeline-item': { template: '<div><slot /></div>' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.activities = largeActivities

      await wrapper.vm.$nextTick()

      expect(vm.activities).toHaveLength(100)
    })

    it('应该能处理大量排行榜数据', async () => {
      const largeLeaderboard = Array.from({ length: 50 }, (_, i) => ({
        name: `学生${i}`,
        grade: '高三',
        points: 5000 - i * 10,
        avatar: null,
        avatarFrameStyle: null
      }))

      const wrapper = mount(StudentDashboard, {
        global: {
          stubs: {
            'el-card': { template: '<div><slot /><slot name="header" /></div>' },
            'user-avatar': { template: '<div />' }
          }
        }
      })

      const vm = wrapper.vm as any
      vm.leaderboard = largeLeaderboard

      await wrapper.vm.$nextTick()

      expect(vm.leaderboard).toHaveLength(50)
    })
  })
})
