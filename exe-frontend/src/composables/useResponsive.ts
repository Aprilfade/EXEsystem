/**
 * useResponsive - 响应式断点检测 Composable
 * 用于检测当前设备类型：移动端、平板、桌面端
 */

import { ref, onMounted, onUnmounted } from 'vue'

export function useResponsive() {
  // 响应式状态
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isDesktop = ref(true)
  const screenWidth = ref(window.innerWidth)

  // 媒体查询对象
  const mobileQuery = window.matchMedia('(max-width: 768px)')
  const tabletQuery = window.matchMedia('(min-width: 769px) and (max-width: 1199px)')
  const desktopQuery = window.matchMedia('(min-width: 1200px)')

  // 更新响应式状态
  const updateResponsive = () => {
    screenWidth.value = window.innerWidth
    isMobile.value = mobileQuery.matches
    isTablet.value = tabletQuery.matches
    isDesktop.value = desktopQuery.matches
  }

  // 媒体查询变化处理器
  const handleChange = () => {
    updateResponsive()
  }

  // 窗口大小变化处理器
  const handleResize = () => {
    updateResponsive()
  }

  // 生命周期钩子
  onMounted(() => {
    updateResponsive()

    // 添加媒体查询监听器
    mobileQuery.addEventListener('change', handleChange)
    tabletQuery.addEventListener('change', handleChange)
    desktopQuery.addEventListener('change', handleChange)

    // 添加窗口大小变化监听器
    window.addEventListener('resize', handleResize)
  })

  onUnmounted(() => {
    // 移除媒体查询监听器
    mobileQuery.removeEventListener('change', handleChange)
    tabletQuery.removeEventListener('change', handleChange)
    desktopQuery.removeEventListener('change', handleChange)

    // 移除窗口大小变化监听器
    window.removeEventListener('resize', handleResize)
  })

  return {
    isMobile,      // 是否为移动端（<= 768px）
    isTablet,      // 是否为平板端（769px - 1199px）
    isDesktop,     // 是否为桌面端（>= 1200px）
    screenWidth    // 当前屏幕宽度
  }
}
