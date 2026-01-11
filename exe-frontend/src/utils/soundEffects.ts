/**
 * 音效系统
 * 为练习系统提供音效反馈
 */

// 音效配置
const SOUND_CONFIG = {
  enabled: true, // 是否启用音效
  volume: 0.5 // 音量 0-1
}

// 音效文件路径
const SOUND_PATHS: Record<string, string> = {
  correct: '/sounds/correct.mp3',
  wrong: '/sounds/wrong.mp3',
  achievement_unlock: '/sounds/achievement.mp3',
  level_up: '/sounds/level_up.mp3',
  streak: '/sounds/streak.mp3',
  complete: '/sounds/complete.mp3',
  click: '/sounds/click.mp3'
}

// 音效实例缓存
const soundCache: Map<string, HTMLAudioElement> = new Map()

/**
 * 预加载音效
 */
export function preloadSounds() {
  Object.entries(SOUND_PATHS).forEach(([name, path]) => {
    const audio = new Audio(path)
    audio.volume = SOUND_CONFIG.volume
    audio.preload = 'auto'
    soundCache.set(name, audio)
  })
}

/**
 * 播放音效
 * @param soundName 音效名称
 * @param volume 可选的音量覆盖
 */
export function playSound(soundName: keyof typeof SOUND_PATHS, volume?: number) {
  if (!SOUND_CONFIG.enabled) return

  try {
    let audio = soundCache.get(soundName)

    if (!audio) {
      // 如果缓存中没有，创建新的音效实例
      const path = SOUND_PATHS[soundName]
      if (!path) {
        console.warn(`Sound not found: ${soundName}`)
        return
      }

      audio = new Audio(path)
      audio.volume = volume !== undefined ? volume : SOUND_CONFIG.volume
      soundCache.set(soundName, audio)
    } else {
      // 重置播放位置
      audio.currentTime = 0
      if (volume !== undefined) {
        audio.volume = volume
      }
    }

    // 播放音效
    audio.play().catch(error => {
      // 浏览器可能会阻止自动播放
      console.warn('Sound play failed:', soundName, error)
    })
  } catch (error) {
    console.error('Error playing sound:', soundName, error)
  }
}

/**
 * 停止音效
 */
export function stopSound(soundName: string) {
  const audio = soundCache.get(soundName)
  if (audio) {
    audio.pause()
    audio.currentTime = 0
  }
}

/**
 * 停止所有音效
 */
export function stopAllSounds() {
  soundCache.forEach(audio => {
    audio.pause()
    audio.currentTime = 0
  })
}

/**
 * 设置音效启用状态
 */
export function setSoundEnabled(enabled: boolean) {
  SOUND_CONFIG.enabled = enabled
  localStorage.setItem('sound_enabled', enabled ? '1' : '0')
}

/**
 * 设置音效音量
 */
export function setSoundVolume(volume: number) {
  SOUND_CONFIG.volume = Math.max(0, Math.min(1, volume))
  localStorage.setItem('sound_volume', SOUND_CONFIG.volume.toString())

  // 更新所有缓存音效的音量
  soundCache.forEach(audio => {
    audio.volume = SOUND_CONFIG.volume
  })
}

/**
 * 获取音效启用状态
 */
export function isSoundEnabled(): boolean {
  return SOUND_CONFIG.enabled
}

/**
 * 获取音效音量
 */
export function getSoundVolume(): number {
  return SOUND_CONFIG.volume
}

/**
 * 从LocalStorage加载音效配置
 */
export function loadSoundConfig() {
  const enabled = localStorage.getItem('sound_enabled')
  if (enabled !== null) {
    SOUND_CONFIG.enabled = enabled === '1'
  }

  const volume = localStorage.getItem('sound_volume')
  if (volume !== null) {
    SOUND_CONFIG.volume = parseFloat(volume)
  }
}

/**
 * 答对音效
 */
export function playCorrectSound() {
  playSound('correct', 0.6)
}

/**
 * 答错音效
 */
export function playWrongSound() {
  playSound('wrong', 0.5)
}

/**
 * 成就解锁音效
 */
export function playAchievementSound() {
  playSound('achievement_unlock', 0.7)
}

/**
 * 升级音效
 */
export function playLevelUpSound() {
  playSound('level_up', 0.8)
}

/**
 * 连击音效
 */
export function playStreakSound() {
  playSound('streak', 0.6)
}

/**
 * 完成音效
 */
export function playCompleteSound() {
  playSound('complete', 0.7)
}

/**
 * 点击音效
 */
export function playClickSound() {
  playSound('click', 0.3)
}

// 初始化时加载配置
loadSoundConfig()

// 导出默认配置
export default {
  preloadSounds,
  playSound,
  stopSound,
  stopAllSounds,
  setSoundEnabled,
  setSoundVolume,
  isSoundEnabled,
  getSoundVolume,
  loadSoundConfig,
  playCorrectSound,
  playWrongSound,
  playAchievementSound,
  playLevelUpSound,
  playStreakSound,
  playCompleteSound,
  playClickSound
}
