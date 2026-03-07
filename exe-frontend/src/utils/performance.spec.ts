import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { debounce, throttle } from '@/utils/performance'

describe('Performance Utils', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('debounce', () => {
    it('should delay function execution', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 300)

      debouncedFn()
      expect(fn).not.toHaveBeenCalled()

      vi.advanceTimersByTime(300)
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should only execute the last call', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 300)

      debouncedFn('call1')
      debouncedFn('call2')
      debouncedFn('call3')

      vi.advanceTimersByTime(300)

      expect(fn).toHaveBeenCalledTimes(1)
      expect(fn).toHaveBeenCalledWith('call3')
    })

    it('should reset timer on each call', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 300)

      debouncedFn()
      vi.advanceTimersByTime(200)

      debouncedFn()
      vi.advanceTimersByTime(200)

      expect(fn).not.toHaveBeenCalled()

      vi.advanceTimersByTime(100)
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should work with default delay', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn) // 默认300ms

      debouncedFn()
      vi.advanceTimersByTime(300)

      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should pass arguments correctly', () => {
      const fn = vi.fn()
      const debouncedFn = debounce(fn, 300)

      debouncedFn('arg1', 'arg2', 123)
      vi.advanceTimersByTime(300)

      expect(fn).toHaveBeenCalledWith('arg1', 'arg2', 123)
    })

    it('should handle multiple debounced functions independently', () => {
      const fn1 = vi.fn()
      const fn2 = vi.fn()
      const debouncedFn1 = debounce(fn1, 300)
      const debouncedFn2 = debounce(fn2, 500)

      debouncedFn1()
      debouncedFn2()

      vi.advanceTimersByTime(300)
      expect(fn1).toHaveBeenCalledTimes(1)
      expect(fn2).not.toHaveBeenCalled()

      vi.advanceTimersByTime(200)
      expect(fn2).toHaveBeenCalledTimes(1)
    })
  })

  describe('throttle', () => {
    it('should execute function immediately on first call', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 300)

      throttledFn()
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should limit function execution rate', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 300)

      throttledFn() // 立即执行
      throttledFn() // 被节流
      throttledFn() // 被节流

      expect(fn).toHaveBeenCalledTimes(1)

      vi.advanceTimersByTime(300)
      expect(fn).toHaveBeenCalledTimes(2) // 延迟执行最后一次
    })

    it('should allow execution after delay', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 300)

      throttledFn()
      expect(fn).toHaveBeenCalledTimes(1)

      vi.advanceTimersByTime(300)

      throttledFn()
      expect(fn).toHaveBeenCalledTimes(2)
    })

    it('should work with default delay', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn) // 默认300ms

      throttledFn()
      expect(fn).toHaveBeenCalledTimes(1)
    })

    it('should pass arguments correctly', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 300)

      throttledFn('arg1', 123)
      expect(fn).toHaveBeenCalledWith('arg1', 123)
    })

    it('should execute the last call after throttle period', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 300)

      throttledFn('call1') // 立即执行
      throttledFn('call2') // 被节流，但会延迟执行
      throttledFn('call3') // 被节流，覆盖call2

      expect(fn).toHaveBeenCalledTimes(1)
      expect(fn).toHaveBeenCalledWith('call1')

      vi.advanceTimersByTime(300)

      expect(fn).toHaveBeenCalledTimes(2)
      expect(fn).toHaveBeenNthCalledWith(2, 'call3')
    })

    it('should handle rapid successive calls', () => {
      const fn = vi.fn()
      const throttledFn = throttle(fn, 1000)

      throttledFn() // t=0, 执行
      vi.advanceTimersByTime(500)
      throttledFn() // t=500, 被节流

      expect(fn).toHaveBeenCalledTimes(1)

      vi.advanceTimersByTime(500) // t=1000
      expect(fn).toHaveBeenCalledTimes(2) // 延迟执行第二次
    })
  })

  describe('debounce and throttle integration', () => {
    it('should work correctly when used together', () => {
      const fn1 = vi.fn()
      const fn2 = vi.fn()
      const debouncedFn = debounce(fn1, 300)
      const throttledFn = throttle(fn2, 300)

      debouncedFn()
      throttledFn()

      expect(fn1).not.toHaveBeenCalled()
      expect(fn2).toHaveBeenCalledTimes(1)

      vi.advanceTimersByTime(300)

      expect(fn1).toHaveBeenCalledTimes(1)
      expect(fn2).toHaveBeenCalledTimes(1)
    })
  })
})
