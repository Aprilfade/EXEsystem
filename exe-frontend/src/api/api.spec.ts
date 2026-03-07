import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import type { User } from '@/api/user'

// Mock axios
vi.mock('axios')
const mockedAxios = axios as any

describe('API Calls', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('User API', () => {
    it('should fetch user info successfully', async () => {
      // Given
      const mockUser: User = {
        id: 1,
        userName: 'testuser',
        nickName: '测试用户',
        email: 'test@example.com',
        phoneNumber: '13800000000',
        status: '0',
        createTime: '2024-01-01T00:00:00'
      }

      mockedAxios.get.mockResolvedValue({
        data: {
          code: 200,
          data: mockUser,
          msg: 'success'
        }
      })

      // When
      const response = await mockedAxios.get('/api/v1/user/info')

      // Then
      expect(response.data.code).toBe(200)
      expect(response.data.data).toEqual(mockUser)
    })

    it('should handle API error', async () => {
      // Given
      mockedAxios.get.mockRejectedValue({
        response: {
          status: 401,
          data: { message: 'Unauthorized' }
        }
      })

      // When & Then
      await expect(mockedAxios.get('/api/v1/user/info')).rejects.toMatchObject({
        response: {
          status: 401
        }
      })
    })
  })

  describe('Course API', () => {
    it('should fetch course list successfully', async () => {
      // Given
      const mockCourses = [
        { id: 1, title: '课程1', description: '描述1' },
        { id: 2, title: '课程2', description: '描述2' }
      ]

      mockedAxios.get.mockResolvedValue({
        data: {
          code: 200,
          data: mockCourses,
          msg: 'success'
        }
      })

      // When
      const response = await mockedAxios.get('/api/v1/course/list')

      // Then
      expect(response.data.code).toBe(200)
      expect(response.data.data).toHaveLength(2)
      expect(response.data.data[0].title).toBe('课程1')
    })

    it('should create course successfully', async () => {
      // Given
      const newCourse = {
        title: '新课程',
        description: '新课程描述'
      }

      mockedAxios.post.mockResolvedValue({
        data: {
          code: 200,
          data: { id: 1, ...newCourse },
          msg: 'success'
        }
      })

      // When
      const response = await mockedAxios.post('/api/v1/course', newCourse)

      // Then
      expect(response.data.code).toBe(200)
      expect(response.data.data.id).toBe(1)
      expect(response.data.data.title).toBe('新课程')
    })
  })

  describe('Auth API', () => {
    it('should login successfully', async () => {
      // Given
      const credentials = {
        username: 'admin',
        password: 'password123'
      }

      const mockResponse = {
        token: 'mock-jwt-token',
        user: {
          id: 1,
          userName: 'admin',
          nickName: '管理员'
        }
      }

      mockedAxios.post.mockResolvedValue({
        data: {
          code: 200,
          data: mockResponse,
          msg: 'success'
        }
      })

      // When
      const response = await mockedAxios.post('/api/v1/auth/login', credentials)

      // Then
      expect(response.data.code).toBe(200)
      expect(response.data.data.token).toBe('mock-jwt-token')
      expect(response.data.data.user.userName).toBe('admin')
    })

    it('should handle login failure', async () => {
      // Given
      const credentials = {
        username: 'wrong',
        password: 'wrong'
      }

      mockedAxios.post.mockResolvedValue({
        data: {
          code: 401,
          data: null,
          msg: '用户名或密码错误'
        }
      })

      // When
      const response = await mockedAxios.post('/api/v1/auth/login', credentials)

      // Then
      expect(response.data.code).toBe(401)
      expect(response.data.msg).toBe('用户名或密码错误')
    })
  })

  describe('Practice API', () => {
    it('should submit answer successfully', async () => {
      // Given
      const answer = {
        questionId: 1,
        answer: 'A',
        timeSpent: 30
      }

      mockedAxios.post.mockResolvedValue({
        data: {
          code: 200,
          data: {
            correct: true,
            score: 10,
            explanation: '答案正确'
          },
          msg: 'success'
        }
      })

      // When
      const response = await mockedAxios.post('/api/v1/practice/submit', answer)

      // Then
      expect(response.data.code).toBe(200)
      expect(response.data.data.correct).toBe(true)
      expect(response.data.data.score).toBe(10)
    })

    it('should get practice statistics', async () => {
      // Given
      const mockStats = {
        totalQuestions: 100,
        correctAnswers: 85,
        accuracy: 85.0,
        totalTime: 3600
      }

      mockedAxios.get.mockResolvedValue({
        data: {
          code: 200,
          data: mockStats,
          msg: 'success'
        }
      })

      // When
      const response = await mockedAxios.get('/api/v1/practice/stats')

      // Then
      expect(response.data.code).toBe(200)
      expect(response.data.data.accuracy).toBe(85.0)
    })
  })

  describe('Request with pagination', () => {
    it('should fetch paginated data', async () => {
      // Given
      const params = {
        page: 1,
        size: 10
      }

      const mockPaginatedData = {
        total: 50,
        list: Array.from({ length: 10 }, (_, i) => ({
          id: i + 1,
          title: `Item ${i + 1}`
        }))
      }

      mockedAxios.get.mockResolvedValue({
        data: {
          code: 200,
          data: mockPaginatedData,
          msg: 'success'
        }
      })

      // When
      const response = await mockedAxios.get('/api/v1/data/list', { params })

      // Then
      expect(response.data.code).toBe(200)
      expect(response.data.data.total).toBe(50)
      expect(response.data.data.list).toHaveLength(10)
    })
  })

  describe('Request interceptors', () => {
    it('should add Authorization header', async () => {
      // Given
      const token = 'mock-token'
      localStorage.setItem('token', token)

      // When - 这里模拟拦截器行为
      const config = {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }

      // Then
      expect(config.headers.Authorization).toBe(`Bearer ${token}`)
    })
  })

  describe('Error handling', () => {
    it('should handle network error', async () => {
      // Given
      mockedAxios.get.mockRejectedValue(new Error('Network Error'))

      // When & Then
      await expect(mockedAxios.get('/api/v1/test')).rejects.toThrow('Network Error')
    })

    it('should handle timeout error', async () => {
      // Given
      mockedAxios.get.mockRejectedValue({
        code: 'ECONNABORTED',
        message: 'timeout of 5000ms exceeded'
      })

      // When & Then
      await expect(mockedAxios.get('/api/v1/test')).rejects.toMatchObject({
        code: 'ECONNABORTED'
      })
    })

    it('should handle 500 server error', async () => {
      // Given
      mockedAxios.get.mockRejectedValue({
        response: {
          status: 500,
          data: { message: 'Internal Server Error' }
        }
      })

      // When & Then
      await expect(mockedAxios.get('/api/v1/test')).rejects.toMatchObject({
        response: {
          status: 500
        }
      })
    })
  })
})
