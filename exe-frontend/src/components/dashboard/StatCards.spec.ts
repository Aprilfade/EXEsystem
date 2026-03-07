import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import StatCards from './StatCards.vue'
import { ElRow, ElCol, ElCard, ElIcon } from 'element-plus'
import type { DashboardStats } from '@/api/dashboard'

describe('StatCards 组件集成测试', () => {
  const mockStats: DashboardStats = {
    studentCount: 150,
    subjectCount: 8,
    knowledgePointCount: 320,
    questionCount: 1250,
    paperCount: 45,
    wrongRecordCount: 89,
    trends: {
      studentCountTrend: 12,
      subjectCountTrend: 0,
      knowledgePointCountTrend: 15,
      questionCountTrend: -5,
      paperCountTrend: 8,
      onlineCountTrend: 25
    }
  }

  const mockOnlineCount = 42

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该正确渲染所有统计卡片', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      // 应该渲染6个统计卡片
      const cards = wrapper.findAllComponents(ElCard)
      expect(cards).toHaveLength(6)
    })

    it('应该显示正确的统计数据', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const text = wrapper.text()

      // 验证各项统计数据是否显示
      expect(text).toContain('42') // 在线学生数
      expect(text).toContain('150') // 学生总数
      expect(text).toContain('8') // 科目数量
      expect(text).toContain('320') // 知识点
      expect(text).toContain('1250') // 题目数量
      expect(text).toContain('45') // 试卷数量
    })

    it('应该显示正确的标签文本', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const text = wrapper.text()

      expect(text).toContain('在线学生')
      expect(text).toContain('学生总数')
      expect(text).toContain('科目数量')
      expect(text).toContain('知识点')
      expect(text).toContain('题目数量')
      expect(text).toContain('试卷数量')
    })
  })

  // ==================== 趋势显示测试 ====================

  describe('趋势显示', () => {
    it('应该显示正确的上升趋势', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const text = wrapper.text()

      // 学生总数上升12%
      expect(text).toContain('12%')
      // 知识点上升15%
      expect(text).toContain('15%')
      // 在线学生上升25%
      expect(text).toContain('25%')
    })

    it('应该显示正确的下降趋势', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const text = wrapper.text()

      // 题目数量下降5%
      expect(text).toContain('5%')
    })

    it('应该显示持平状态', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const text = wrapper.text()

      // 科目数量持平
      expect(text).toContain('持平')
    })

    it('应该显示"较上周"标签', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const text = wrapper.text()
      expect(text).toContain('较上周')
    })
  })

  // ==================== 响应式布局测试 ====================

  describe('响应式布局', () => {
    it('应该使用响应式列布局', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const cols = wrapper.findAllComponents(ElCol)

      // 每个col应该有响应式属性
      cols.forEach(col => {
        const props = col.props() as any
        expect(props.xs).toBeDefined()
        expect(props.sm).toBeDefined()
        expect(props.md).toBeDefined()
      })
    })

    it('应该设置正确的gutter', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const row = wrapper.findComponent(ElRow)
      expect(row.props('gutter')).toBe(20)
    })
  })

  // ==================== Props变化测试 ====================

  describe('Props变化响应', () => {
    it('应该响应在线人数变化', async () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: 42
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      expect(wrapper.text()).toContain('42')

      // 更新在线人数
      await wrapper.setProps({ onlineCount: 55 })

      expect(wrapper.text()).toContain('55')
    })

    it('应该响应统计数据变化', async () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      expect(wrapper.text()).toContain('150')

      // 更新学生数量
      const newStats = { ...mockStats, studentCount: 200 }
      await wrapper.setProps({ stats: newStats })

      expect(wrapper.text()).toContain('200')
    })

    it('应该响应趋势数据变化', async () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const newStats: DashboardStats = {
        ...mockStats,
        trends: {
          ...mockStats.trends!,
          studentCountTrend: 30
        }
      }

      await wrapper.setProps({ stats: newStats })

      expect(wrapper.text()).toContain('30%')
    })
  })

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理没有趋势数据的情况', () => {
      const statsWithoutTrends: DashboardStats = {
        studentCount: 150,
        subjectCount: 8,
        knowledgePointCount: 320,
        questionCount: 1250,
        paperCount: 45,
        wrongRecordCount: 89
      }

      const wrapper = mount(StatCards, {
        props: {
          stats: statsWithoutTrends,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      // 应该正常渲染，趋势显示为0
      expect(wrapper.text()).toContain('持平')
    })

    it('应该处理零值统计数据', () => {
      const zeroStats: DashboardStats = {
        studentCount: 0,
        subjectCount: 0,
        knowledgePointCount: 0,
        questionCount: 0,
        paperCount: 0,
        wrongRecordCount: 0
      }

      const wrapper = mount(StatCards, {
        props: {
          stats: zeroStats,
          onlineCount: 0
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      // 应该正常渲染
      const cards = wrapper.findAllComponents(ElCard)
      expect(cards).toHaveLength(6)
    })

    it('应该处理极大值统计数据', () => {
      const largeStats: DashboardStats = {
        studentCount: 999999,
        subjectCount: 99999,
        knowledgePointCount: 999999,
        questionCount: 9999999,
        paperCount: 99999,
        wrongRecordCount: 99999
      }

      const wrapper = mount(StatCards, {
        props: {
          stats: largeStats,
          onlineCount: 99999
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      expect(wrapper.text()).toContain('999999')
      expect(wrapper.text()).toContain('9999999')
    })

    it('应该处理负数趋势', () => {
      const negativeStats: DashboardStats = {
        ...mockStats,
        trends: {
          studentCountTrend: -20,
          subjectCountTrend: -10,
          knowledgePointCountTrend: -15,
          questionCountTrend: -25,
          paperCountTrend: -5,
          onlineCountTrend: -30
        }
      }

      const wrapper = mount(StatCards, {
        props: {
          stats: negativeStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const text = wrapper.text()
      expect(text).toContain('20%')
      expect(text).toContain('30%')
    })
  })

  // ==================== 样式和交互测试 ====================

  describe('样式和交互', () => {
    it('所有卡片应该有hover shadow样式', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      const cards = wrapper.findAllComponents(ElCard)
      cards.forEach(card => {
        expect(card.props('shadow')).toBe('hover')
      })
    })

    it('应该渲染图标组件', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          },
          stubs: {
            'el-icon': false
          }
        }
      })

      const icons = wrapper.findAllComponents(ElIcon)
      // 每个卡片有1个主图标 + 可能的趋势图标
      expect(icons.length).toBeGreaterThanOrEqual(6)
    })

    it('应该有正确的CSS类', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      expect(wrapper.find('.stat-row').exists()).toBe(true)
      expect(wrapper.findAll('.stat-card-enhanced').length).toBe(6)
      expect(wrapper.find('.stat-content').exists()).toBe(true)
      expect(wrapper.find('.stat-icon-wrapper').exists()).toBe(true)
      expect(wrapper.find('.stat-info').exists()).toBe(true)
      expect(wrapper.find('.stat-label').exists()).toBe(true)
      expect(wrapper.find('.stat-value').exists()).toBe(true)
    })
  })

  // ==================== 计算属性测试 ====================

  describe('计算属性', () => {
    it('statCards计算属性应该返回正确的卡片数组', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      // 通过检查渲染的卡片数量来验证计算属性
      const cards = wrapper.findAllComponents(ElCard)
      expect(cards.length).toBe(6)
    })

    it('每个卡片应该有所需的所有属性', () => {
      const wrapper = mount(StatCards, {
        props: {
          stats: mockStats,
          onlineCount: mockOnlineCount
        },
        global: {
          components: {
            ElRow,
            ElCol,
            ElCard,
            ElIcon
          }
        }
      })

      // 验证卡片包含label, value, icon等信息
      expect(wrapper.find('.stat-label').exists()).toBe(true)
      expect(wrapper.find('.stat-value').exists()).toBe(true)
      expect(wrapper.find('.stat-icon-wrapper').exists()).toBe(true)
    })
  })
})
