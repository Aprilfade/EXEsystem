import { vi } from 'vitest'
import type { Router } from 'vue-router'

/**
 * 创建 Mock Router
 */
export function createMockRouter(): Router {
  return {
    push: vi.fn(),
    replace: vi.fn(),
    go: vi.fn(),
    back: vi.fn(),
    forward: vi.fn(),
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
    currentRoute: {
      value: {
        path: '/',
        name: 'home',
        params: {},
        query: {},
        hash: '',
        fullPath: '/',
        matched: [],
        meta: {},
        redirectedFrom: undefined
      }
    }
  } as unknown as Router
}

/**
 * 创建 Mock Store
 */
export function createMockStore(initialState = {}) {
  return {
    state: initialState,
    commit: vi.fn(),
    dispatch: vi.fn(),
    getters: {}
  }
}

/**
 * 延迟执行
 */
export function delay(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Mock LocalStorage
 */
export function mockLocalStorage() {
  const storage: Record<string, string> = {}

  return {
    getItem: vi.fn((key: string) => storage[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      storage[key] = value
    }),
    removeItem: vi.fn((key: string) => {
      delete storage[key]
    }),
    clear: vi.fn(() => {
      Object.keys(storage).forEach(key => delete storage[key])
    })
  }
}

/**
 * Mock API Response
 */
export function mockApiResponse<T>(data: T, delay = 0): Promise<T> {
  return new Promise(resolve => {
    setTimeout(() => resolve(data), delay)
  })
}

/**
 * Mock API Error
 */
export function mockApiError(message: string, code = 500, delay = 0): Promise<never> {
  return new Promise((_, reject) => {
    setTimeout(() => reject({ message, code }), delay)
  })
}
