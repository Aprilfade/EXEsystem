/**
 * useResponsiveDialog - 响应式对话框/抽屉 Composable
 * 自动根据设备类型调整对话框和抽屉的尺寸和行为
 */

import { computed } from 'vue'
import { useResponsive } from './useResponsive'

export function useResponsiveDialog() {
  const { isMobile, isTablet } = useResponsive()

  // 对话框宽度 - 移动端全屏，平板80%，桌面50%
  const dialogWidth = computed(() => {
    if (isMobile.value) return '95%'
    if (isTablet.value) return '80%'
    return '50%'
  })

  // 对话框是否全屏 - 仅移动端全屏
  const dialogFullscreen = computed(() => {
    return isMobile.value
  })

  // 对话框顶部间距
  const dialogTop = computed(() => {
    return isMobile.value ? '5vh' : '15vh'
  })

  // 抽屉方向 - 移动端从底部，桌面从右侧
  const drawerDirection = computed<'left' | 'right' | 'top' | 'bottom'>(() => {
    return isMobile.value ? 'bottom' : 'right'
  })

  // 抽屉尺寸
  const drawerSize = computed(() => {
    if (isMobile.value) return '80%'
    if (isTablet.value) return '60%'
    return '40%'
  })

  // 消息框宽度
  const messageBoxWidth = computed(() => {
    return isMobile.value ? '90%' : '420px'
  })

  // 表单标签位置 - 移动端顶部，桌面右侧
  const formLabelPosition = computed<'left' | 'right' | 'top'>(() => {
    return isMobile.value ? 'top' : 'right'
  })

  // 表单标签宽度
  const formLabelWidth = computed(() => {
    return isMobile.value ? 'auto' : '100px'
  })

  // 表格尺寸 - 移动端小号
  const tableSize = computed<'large' | 'default' | 'small'>(() => {
    return isMobile.value ? 'small' : 'default'
  })

  // 按钮尺寸
  const buttonSize = computed<'large' | 'default' | 'small'>(() => {
    if (isMobile.value) return 'default'
    return 'default'
  })

  // 输入框尺寸
  const inputSize = computed<'large' | 'default' | 'small'>(() => {
    if (isMobile.value) return 'default'
    return 'default'
  })

  return {
    isMobile,
    isTablet,

    // 对话框相关
    dialogWidth,
    dialogFullscreen,
    dialogTop,

    // 抽屉相关
    drawerDirection,
    drawerSize,

    // 消息框相关
    messageBoxWidth,

    // 表单相关
    formLabelPosition,
    formLabelWidth,

    // 组件尺寸
    tableSize,
    buttonSize,
    inputSize
  }
}
