<template>
  <div class="video-player-wrapper">
    <!-- 播放器容器 -->
    <div ref="playerRef" class="video-player-container"></div>

    <!-- 控制面板 -->
    <div v-if="showControls" class="player-controls">
      <!-- 倍速控制 -->
      <div class="control-group">
        <span class="control-label">播放速度:</span>
        <el-select v-model="playbackRate" @change="handlePlaybackRateChange" size="small" style="width: 100px">
          <el-option label="0.5x" :value="0.5"></el-option>
          <el-option label="0.75x" :value="0.75"></el-option>
          <el-option label="1.0x" :value="1.0"></el-option>
          <el-option label="1.25x" :value="1.25"></el-option>
          <el-option label="1.5x" :value="1.5"></el-option>
          <el-option label="2.0x" :value="2.0"></el-option>
        </el-select>
      </div>

      <!-- 进度信息 -->
      <div class="control-group progress-info">
        <span class="control-label">学习进度:</span>
        <el-progress
          :percentage="progressPercent"
          :color="progressPercent >= 95 ? '#67C23A' : '#409EFF'"
          style="width: 200px"
        />
        <span class="progress-text">{{ progressPercent }}%</span>
      </div>

      <!-- 学习时长 -->
      <div class="control-group">
        <span class="control-label">学习时长:</span>
        <span class="time-text">{{ formatTime(studyDuration) }}</span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 错误提示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      style="margin-top: 10px"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { useCourseProgress } from '@/hooks/useCourseProgress';

/**
 * 组件属性
 */
interface Props {
  videoUrl: string;              // 视频URL
  resourceId: number;            // 资源ID
  lastPosition?: number;         // 最后播放位置（秒）
  showControls?: boolean;        // 是否显示控制面板
  autoplay?: boolean;            // 是否自动播放
  theme?: string;                // 主题颜色
}

const props = withDefaults(defineProps<Props>(), {
  lastPosition: 0,
  showControls: true,
  autoplay: false,
  theme: '#409EFF'
});

/**
 * 组件事件
 */
const emit = defineEmits<{
  (e: 'play'): void;
  (e: 'pause'): void;
  (e: 'ended'): void;
  (e: 'progress', percent: number): void;
}>();

// Hooks
const { handleVideoProgress, getResourcePercent, formatStudyTime } = useCourseProgress();

// 播放器实例
let player: any = null;
const playerRef = ref<HTMLElement | null>(null);

// 状态
const loading = ref(true);
const error = ref<string | null>(null);
const playbackRate = ref(1.0);
const progressPercent = computed(() => getResourcePercent.value(props.resourceId));
const studyDuration = ref(0);

// 播放状态
const currentTime = ref(0);
const duration = ref(0);
let lastSaveTime = 0;
let progressCheckInterval: ReturnType<typeof setInterval> | null = null;

/**
 * 初始化DPlayer
 */
const initPlayer = async () => {
  if (!playerRef.value) {
    error.value = '播放器容器未找到';
    loading.value = false;
    return;
  }

  try {
    // 动态导入DPlayer（如果未安装依赖，这里会失败）
    const DPlayer = (await import('dplayer')).default;

    // 创建播放器实例
    player = new DPlayer({
      container: playerRef.value,
      video: {
        url: props.videoUrl,
        type: 'auto'
      },
      autoplay: props.autoplay,
      theme: props.theme,
      loop: false,
      lang: 'zh-cn',
      screenshot: false,
      hotkey: true,
      preload: 'auto',
      volume: 0.7
    });

    // 监听播放器就绪事件
    player.on('loadedmetadata', () => {
      duration.value = player.video.duration;
      loading.value = false;

      // 从上次位置继续播放
      if (props.lastPosition && props.lastPosition > 0) {
        player.seek(props.lastPosition);
        ElMessage.success(`从 ${formatTime(props.lastPosition)} 处继续播放`);
      }
    });

    // 监听播放事件
    player.on('play', () => {
      emit('play');
      startProgressTracking();
    });

    // 监听暂停事件
    player.on('pause', () => {
      emit('pause');
      saveCurrentProgress();
    });

    // 监听时间更新
    player.on('timeupdate', () => {
      currentTime.value = player.video.currentTime;
      duration.value = player.video.duration;

      // 每5秒保存一次进度
      if (currentTime.value - lastSaveTime >= 5) {
        handleVideoProgress(props.resourceId, currentTime.value, duration.value);
        lastSaveTime = currentTime.value;
      }
    });

    // 监听播放结束
    player.on('ended', () => {
      emit('ended');
      saveCurrentProgress(true); // 强制保存为100%
      ElMessage.success('视频播放完成！');
    });

    // 监听错误
    player.on('error', () => {
      error.value = '视频加载失败，请检查视频地址或网络连接';
      loading.value = false;
    });

  } catch (err: any) {
    console.error('初始化播放器失败:', err);
    error.value = err.message || '播放器初始化失败，请确保已安装DPlayer依赖';
    loading.value = false;
  }
};

/**
 * 开始进度追踪
 */
const startProgressTracking = () => {
  if (progressCheckInterval) return;

  progressCheckInterval = setInterval(() => {
    if (player && !player.video.paused) {
      studyDuration.value += 1;
    }
  }, 1000);
};

/**
 * 保存当前进度
 */
const saveCurrentProgress = (forceComplete = false) => {
  if (!player) return;

  const current = player.video.currentTime;
  const total = player.video.duration;

  if (total > 0) {
    handleVideoProgress(props.resourceId, current, total);

    // 如果强制完成，保存100%进度
    if (forceComplete) {
      handleVideoProgress(props.resourceId, total, total);
    }
  }
};

/**
 * 处理倍速变化
 */
const handlePlaybackRateChange = () => {
  if (player && player.video) {
    player.speed(playbackRate.value);
    ElMessage.success(`播放速度已设置为 ${playbackRate.value}x`);
  }
};

/**
 * 格式化时间
 */
const formatTime = (seconds: number): string => {
  if (!seconds || seconds < 0) return '00:00';

  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);

  if (h > 0) {
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  } else {
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }
};

/**
 * 监听视频URL变化
 */
watch(() => props.videoUrl, (newUrl) => {
  if (newUrl && player) {
    player.switchVideo({
      url: newUrl,
      type: 'auto'
    });
  }
});

/**
 * 组件挂载
 */
onMounted(() => {
  initPlayer();
});

/**
 * 组件卸载
 */
onUnmounted(() => {
  // 保存进度
  saveCurrentProgress();

  // 清除定时器
  if (progressCheckInterval) {
    clearInterval(progressCheckInterval);
    progressCheckInterval = null;
  }

  // 销毁播放器
  if (player) {
    player.destroy();
    player = null;
  }
});

/**
 * 暴露给父组件的方法
 */
defineExpose({
  play: () => player?.play(),
  pause: () => player?.pause(),
  seek: (time: number) => player?.seek(time),
  getCurrentTime: () => player?.video.currentTime || 0,
  getDuration: () => player?.video.duration || 0
});
</script>

<style scoped lang="scss">
.video-player-wrapper {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-player-container {
  width: 100%;
  min-height: 400px;
  background: #000;

  :deep(.dplayer) {
    border-radius: 8px 8px 0 0;
  }
}

.player-controls {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px 20px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;

  .control-group {
    display: flex;
    align-items: center;
    gap: 10px;

    .control-label {
      font-size: 14px;
      color: #606266;
      white-space: nowrap;
    }

    .progress-text {
      font-size: 14px;
      font-weight: 600;
      color: #409EFF;
      margin-left: 10px;
    }

    .time-text {
      font-size: 14px;
      font-weight: 500;
      color: #409EFF;
    }
  }

  .progress-info {
    flex: 1;
  }
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  z-index: 10;

  p {
    margin-top: 10px;
    font-size: 16px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .player-controls {
    flex-wrap: wrap;
    gap: 10px;

    .control-group {
      flex: 1 1 100%;
    }

    .progress-info {
      order: -1;
    }
  }
}
</style>
