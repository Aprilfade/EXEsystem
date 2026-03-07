import { describe, it, expect } from 'vitest'
import { mockUser, mockTodoItem } from '../mocks/mockData'

describe('Mock Data Generator', () => {
  it('should generate mock user', () => {
    const user = mockUser(1)
    expect(user).toBeDefined()
    expect(user.id).toBe(1)
    expect(user.userName).toBe('user1')
    expect(user.email).toBe('user1@test.com')
  })

  it('should generate mock todo item', () => {
    const todo = mockTodoItem('grading')
    expect(todo).toBeDefined()
    expect(todo.type).toBe('grading')
    expect(todo.title).toBe('待批阅试卷')
  })
})
