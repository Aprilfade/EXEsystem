import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createMockRouter } from '../tests/utils/testHelpers'

// 创建简单的测试组件
const SimpleButton = {
  template: '<button @click="handleClick">{{ text }}</button>',
  props: {
    text: {
      type: String,
      default: 'Click me'
    }
  },
  emits: ['click'],
  setup(props: any, { emit }: any) {
    const handleClick = () => {
      emit('click')
    }
    return { handleClick }
  }
}

const CounterComponent = {
  template: `
    <div>
      <span class="count">{{ count }}</span>
      <button class="increment" @click="increment">+</button>
      <button class="decrement" @click="decrement">-</button>
    </div>
  `,
  setup() {
    const count = ref(0)
    const increment = () => count.value++
    const decrement = () => count.value--
    return { count, increment, decrement }
  }
}

const FormComponent = {
  template: `
    <form @submit.prevent="handleSubmit">
      <input v-model="username" placeholder="Username" />
      <input v-model="password" type="password" placeholder="Password" />
      <button type="submit">Submit</button>
    </form>
  `,
  emits: ['submit'],
  setup(_: any, { emit }: any) {
    const username = ref('')
    const password = ref('')

    const handleSubmit = () => {
      emit('submit', { username: username.value, password: password.value })
    }

    return { username, password, handleSubmit }
  }
}

// 需要导入 ref
import { ref } from 'vue'

describe('Vue Component Tests', () => {
  describe('SimpleButton Component', () => {
    it('should render button with default text', () => {
      const wrapper = mount(SimpleButton)
      expect(wrapper.text()).toBe('Click me')
    })

    it('should render button with custom text', () => {
      const wrapper = mount(SimpleButton, {
        props: {
          text: 'Custom Text'
        }
      })
      expect(wrapper.text()).toBe('Custom Text')
    })

    it('should emit click event when button is clicked', async () => {
      const wrapper = mount(SimpleButton)

      await wrapper.find('button').trigger('click')

      expect(wrapper.emitted()).toHaveProperty('click')
      expect(wrapper.emitted('click')).toHaveLength(1)
    })

    it('should emit multiple click events', async () => {
      const wrapper = mount(SimpleButton)
      const button = wrapper.find('button')

      await button.trigger('click')
      await button.trigger('click')
      await button.trigger('click')

      expect(wrapper.emitted('click')).toHaveLength(3)
    })
  })

  describe('CounterComponent', () => {
    it('should render initial count as 0', () => {
      const wrapper = mount(CounterComponent)
      expect(wrapper.find('.count').text()).toBe('0')
    })

    it('should increment count when + button is clicked', async () => {
      const wrapper = mount(CounterComponent)

      await wrapper.find('.increment').trigger('click')

      expect(wrapper.find('.count').text()).toBe('1')
    })

    it('should decrement count when - button is clicked', async () => {
      const wrapper = mount(CounterComponent)

      await wrapper.find('.decrement').trigger('click')

      expect(wrapper.find('.count').text()).toBe('-1')
    })

    it('should handle multiple increments and decrements', async () => {
      const wrapper = mount(CounterComponent)

      await wrapper.find('.increment').trigger('click')
      await wrapper.find('.increment').trigger('click')
      await wrapper.find('.increment').trigger('click')
      await wrapper.find('.decrement').trigger('click')

      expect(wrapper.find('.count').text()).toBe('2')
    })
  })

  describe('FormComponent', () => {
    it('should render form with inputs', () => {
      const wrapper = mount(FormComponent)

      const inputs = wrapper.findAll('input')
      expect(inputs).toHaveLength(2)
      expect(inputs[0].attributes('placeholder')).toBe('Username')
      expect(inputs[1].attributes('placeholder')).toBe('Password')
    })

    it('should update input values with v-model', async () => {
      const wrapper = mount(FormComponent)

      const usernameInput = wrapper.findAll('input')[0]
      const passwordInput = wrapper.findAll('input')[1]

      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')

      expect((wrapper.vm as any).username).toBe('testuser')
      expect((wrapper.vm as any).password).toBe('password123')
    })

    it('should emit submit event with form data', async () => {
      const wrapper = mount(FormComponent)

      const usernameInput = wrapper.findAll('input')[0]
      const passwordInput = wrapper.findAll('input')[1]

      await usernameInput.setValue('admin')
      await passwordInput.setValue('admin123')

      await wrapper.find('form').trigger('submit')

      expect(wrapper.emitted('submit')).toBeTruthy()
      expect(wrapper.emitted('submit')?.[0]).toEqual([
        { username: 'admin', password: 'admin123' }
      ])
    })

    it('should handle empty form submission', async () => {
      const wrapper = mount(FormComponent)

      await wrapper.find('form').trigger('submit')

      expect(wrapper.emitted('submit')?.[0]).toEqual([
        { username: '', password: '' }
      ])
    })
  })

  describe('Component with Router', () => {
    it('should work with mocked router', () => {
      const TestComponent = {
        template: '<div>Test Component</div>'
      }

      const mockRouter = createMockRouter()

      const wrapper = mount(TestComponent, {
        global: {
          mocks: {
            $router: mockRouter
          }
        }
      })

      expect(wrapper.text()).toBe('Test Component')
    })
  })

  describe('Component Lifecycle', () => {
    it('should call setup function', () => {
      const setupSpy = vi.fn()

      const TestComponent = {
        template: '<div>Test</div>',
        setup() {
          setupSpy()
          return {}
        }
      }

      mount(TestComponent)

      expect(setupSpy).toHaveBeenCalledTimes(1)
    })
  })

  describe('Async Component', () => {
    it('should handle async operations', async () => {
      const AsyncComponent = {
        template: `
          <div>
            <span class="status">{{ status }}</span>
            <button @click="loadData">Load</button>
          </div>
        `,
        setup() {
          const status = ref('idle')

          const loadData = async () => {
            status.value = 'loading'
            await new Promise(resolve => setTimeout(resolve, 100))
            status.value = 'loaded'
          }

          return { status, loadData }
        }
      }

      const wrapper = mount(AsyncComponent)

      expect(wrapper.find('.status').text()).toBe('idle')

      await wrapper.find('button').trigger('click')
      expect(wrapper.find('.status').text()).toBe('loading')

      await new Promise(resolve => setTimeout(resolve, 100))
      expect(wrapper.find('.status').text()).toBe('loaded')
    })
  })

  describe('Computed Properties', () => {
    it('should update when dependencies change', async () => {
      const ComputedComponent = {
        template: `
          <div>
            <input v-model="firstName" />
            <input v-model="lastName" />
            <span class="full-name">{{ fullName }}</span>
          </div>
        `,
        setup() {
          const firstName = ref('John')
          const lastName = ref('Doe')

          const fullName = computed(() => `${firstName.value} ${lastName.value}`)

          return { firstName, lastName, fullName }
        }
      }

      // 需要导入 computed
      const { computed } = await import('vue')

      const wrapper = mount({
        template: `
          <div>
            <input v-model="firstName" />
            <input v-model="lastName" />
            <span class="full-name">{{ fullName }}</span>
          </div>
        `,
        setup() {
          const firstName = ref('John')
          const lastName = ref('Doe')

          const fullName = computed(() => `${firstName.value} ${lastName.value}`)

          return { firstName, lastName, fullName }
        }
      })

      expect(wrapper.find('.full-name').text()).toBe('John Doe')

      await wrapper.findAll('input')[0].setValue('Jane')
      expect(wrapper.find('.full-name').text()).toBe('Jane Doe')
    })
  })
})
