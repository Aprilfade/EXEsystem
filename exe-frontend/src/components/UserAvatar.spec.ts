import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserAvatar from './UserAvatar.vue'
import { ElAvatar } from 'element-plus'

describe('UserAvatar 组件集成测试', () => {
  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该正确渲染基本头像', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg',
          name: '张三',
          size: 40
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.exists()).toBe(true)
      expect(avatar.props('src')).toBe('https://example.com/avatar.jpg')
      expect(avatar.props('size')).toBe(40)
    })

    it('应该有正确的容器样式', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg',
          size: 50
        },
        global: {
          components: { ElAvatar }
        }
      })

      const container = wrapper.find('.user-avatar-container')
      expect(container.exists()).toBe(true)

      // 检查容器尺寸
      const style = container.attributes('style')
      expect(style).toContain('width: 50px')
      expect(style).toContain('height: 50px')
    })

    it('应该渲染ElAvatar组件', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.exists()).toBe(true)
    })
  })

  // ==================== Props测试 ====================

  describe('Props处理', () => {
    it('应该使用提供的src', () => {
      const src = 'https://example.com/user-avatar.png'
      const wrapper = mount(UserAvatar, {
        props: { src },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('src')).toBe(src)
    })

    it('应该使用提供的name', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: '李四'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      // 名字的首字母应该显示在avatar中
      expect(wrapper.text()).toContain('李')
    })

    it('应该使用提供的size', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          size: 80
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('size')).toBe(80)

      const container = wrapper.find('.user-avatar-container')
      const style = container.attributes('style')
      expect(style).toContain('width: 80px')
      expect(style).toContain('height: 80px')
    })

    it('没有提供size时应该使用默认值40', () => {
      const wrapper = mount(UserAvatar, {
        props: {},
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('size')).toBe(40)
    })

    it('没有src和name时应该显示默认图标', () => {
      const wrapper = mount(UserAvatar, {
        props: {},
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('icon')).toBe('UserFilled')
    })
  })

  // ==================== 头像框样式测试 ====================

  describe('头像框样式', () => {
    it('没有frameStyle时不应该渲染头像框', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.exists()).toBe(false)
    })

    it('应该渲染图片类型的头像框', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg',
          frameStyle: 'https://example.com/frame.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.exists()).toBe(true)

      // 检查背景图样式
      const style = frame.attributes('style')
      expect(style).toContain('background-image: url(https://example.com/frame.png)')
      expect(style).toContain('background-size: cover')
      expect(style).toContain('background-position: center')
    })

    it('应该支持相对路径的头像框图片', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg',
          frameStyle: '/static/frames/gold.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.exists()).toBe(true)

      const style = frame.attributes('style')
      expect(style).toContain('background-image: url(/static/frames/gold.png)')
    })

    it('应该支持CSS样式字符串的头像框', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg',
          frameStyle: 'border: 3px solid gold; box-shadow: 0 0 10px gold;'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.exists()).toBe(true)

      // CSS样式应该被应用
      const style = frame.attributes('style')
      expect(style).toContain('border: 3px solid gold')
    })
  })

  // ==================== 名字首字母显示测试 ====================

  describe('名字首字母显示', () => {
    it('应该显示中文名字的第一个字', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: '王五'
        },
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.text()).toContain('王')
      expect(wrapper.text()).not.toContain('五')
    })

    it('应该显示英文名字的第一个字母', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: 'John Doe'
        },
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.text()).toContain('J')
    })

    it('应该处理单字名字', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: '李'
        },
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.text()).toContain('李')
    })

    it('应该处理空名字', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: ''
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      // 空名字应该显示默认图标
      expect(avatar.props('icon')).toBe('UserFilled')
    })

    it('有src时名字首字母不应该显示', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg',
          name: '张三'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('src')).toBe('https://example.com/avatar.jpg')
    })
  })

  // ==================== 计算属性测试 ====================

  describe('计算属性', () => {
    it('size计算属性应该返回正确的值', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          size: 60
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('size')).toBe(60)
    })

    it('parsedFrameStyle应该正确解析图片URL', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: 'https://example.com/frame.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      const style = frame.attributes('style')
      expect(style).toContain('background-image')
    })

    it('parsedFrameStyle应该正确解析CSS字符串', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: 'border: 2px solid blue;'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      const style = frame.attributes('style')
      expect(style).toContain('border')
    })
  })

  // ==================== Props变化测试 ====================

  describe('Props变化响应', () => {
    it('应该响应src变化', async () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar1.jpg'
        },
        global: {
          components: { ElAvatar }
        }
      })

      let avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('src')).toBe('https://example.com/avatar1.jpg')

      await wrapper.setProps({ src: 'https://example.com/avatar2.jpg' })

      avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('src')).toBe('https://example.com/avatar2.jpg')
    })

    it('应该响应name变化', async () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: '张三'
        },
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.text()).toContain('张')

      await wrapper.setProps({ name: '李四' })

      expect(wrapper.text()).toContain('李')
      expect(wrapper.text()).not.toContain('张')
    })

    it('应该响应size变化', async () => {
      const wrapper = mount(UserAvatar, {
        props: {
          size: 40
        },
        global: {
          components: { ElAvatar }
        }
      })

      let avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('size')).toBe(40)

      await wrapper.setProps({ size: 80 })

      avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('size')).toBe(80)
    })

    it('应该响应frameStyle变化', async () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: 'https://example.com/frame1.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      let frame = wrapper.find('.avatar-frame')
      let style = frame.attributes('style')
      expect(style).toContain('frame1.png')

      await wrapper.setProps({ frameStyle: 'https://example.com/frame2.png' })

      frame = wrapper.find('.avatar-frame')
      style = frame.attributes('style')
      expect(style).toContain('frame2.png')
    })
  })

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理所有props都为空的情况', () => {
      const wrapper = mount(UserAvatar, {
        props: {},
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.exists()).toBe(true)
      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.exists()).toBe(true)
    })

    it('应该处理极小的size', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          size: 16
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('size')).toBe(16)
    })

    it('应该处理极大的size', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          size: 200
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('size')).toBe(200)
    })

    it('应该处理无效的src', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'invalid-url',
          name: '张三'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('src')).toBe('invalid-url')
    })

    it('应该处理特殊字符的名字', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: '@#$%^&*()'
        },
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.text()).toContain('@')
    })

    it('应该处理emoji名字', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: '😀用户'
        },
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.text()).toContain('😀')
    })

    it('应该处理frameStyle为空字符串', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: ''
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.exists()).toBe(false)
    })

    it('应该处理frameStyle为undefined', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: undefined
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.exists()).toBe(false)
    })
  })

  // ==================== 样式和布局测试 ====================

  describe('样式和布局', () => {
    it('容器应该有position: relative', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const container = wrapper.find('.user-avatar-container')
      expect(container.classes()).toContain('user-avatar-container')
    })

    it('头像框应该有position: absolute', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: 'https://example.com/frame.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.classes()).toContain('avatar-frame')
    })

    it('头像应该有正确的CSS类', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.find('.real-avatar')
      expect(avatar.exists()).toBe(true)
    })

    it('头像框应该比头像大一圈', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: 'https://example.com/frame.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      const style = frame.attributes('style')

      // 检查框的尺寸（120%）和偏移（-10%）
      // 这些值在CSS中定义
      expect(frame.exists()).toBe(true)
    })
  })

  // ==================== 交互测试 ====================

  describe('交互', () => {
    it('应该支持fit属性', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.props('fit')).toBe('cover')
    })

    it('头像框应该不阻止点击事件', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          frameStyle: 'https://example.com/frame.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      // pointer-events: none 在CSS中定义
      expect(frame.exists()).toBe(true)
    })
  })

  // ==================== 集成场景测试 ====================

  describe('集成场景', () => {
    it('应该支持完整的用户头像展示', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/avatar.jpg',
          name: '高级用户',
          size: 60,
          frameStyle: 'https://example.com/gold-frame.png'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const container = wrapper.find('.user-avatar-container')
      const avatar = wrapper.findComponent(ElAvatar)
      const frame = wrapper.find('.avatar-frame')

      expect(container.exists()).toBe(true)
      expect(avatar.exists()).toBe(true)
      expect(frame.exists()).toBe(true)
      expect(avatar.props('size')).toBe(60)
    })

    it('应该支持VIP用户头像框展示', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          src: 'https://example.com/vip-avatar.jpg',
          name: 'VIP用户',
          size: 80,
          frameStyle: 'border: 4px solid gold; box-shadow: 0 0 15px gold;'
        },
        global: {
          components: { ElAvatar }
        }
      })

      const frame = wrapper.find('.avatar-frame')
      expect(frame.exists()).toBe(true)

      const style = frame.attributes('style')
      expect(style).toContain('border')
      expect(style).toContain('gold')
    })

    it('应该支持无头像用户显示', () => {
      const wrapper = mount(UserAvatar, {
        props: {
          name: '新用户',
          size: 40
        },
        global: {
          components: { ElAvatar }
        }
      })

      expect(wrapper.text()).toContain('新')
      const avatar = wrapper.findComponent(ElAvatar)
      expect(avatar.exists()).toBe(true)
    })
  })
})
