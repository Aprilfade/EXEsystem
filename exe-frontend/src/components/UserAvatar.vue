<template>
  <div class="user-avatar-container" :style="{ width: size + 'px', height: size + 'px' }">
    <el-avatar :size="size" :src="src" :icon="!src && !name ? 'UserFilled' : undefined" class="real-avatar">
      {{ name ? name.charAt(0) : '' }}
    </el-avatar>

    <div
        v-if="frameStyle"
        class="avatar-frame"
        :style="parsedFrameStyle"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  src?: string;        // 头像图片地址
  name?: string;       // 用户名 (用于无图片时显示首字母)
  size?: number;       // 尺寸，默认 40
  frameStyle?: string; // 数据库存的 resourceValue
}>();

const size = computed(() => props.size || 40);

// 解析 frameStyle
const parsedFrameStyle = computed(() => {
  if (!props.frameStyle) return {};

  // 如果数据库存的是图片URL (以 http 或 / 开头)，则作为背景图处理
  if (props.frameStyle.startsWith('http') || props.frameStyle.startsWith('/')) {
    return {
      backgroundImage: `url(${props.frameStyle})`,
      backgroundSize: 'cover', // 或者 '100% 100%'
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    };
  }

  // 否则直接作为 CSS 样式字符串 (兼容旧数据的 border 写法)
  return props.frameStyle;
});
</script>

<style scoped>
.user-avatar-container {
  position: relative;
  display: inline-block;
  border-radius: 50%;
  /* 关键：不设置 overflow: hidden，否则大一点的特效框会被切掉 */
}

.real-avatar {
  width: 100% !important;
  height: 100% !important;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-frame {
  position: absolute;
  top: -10%;    /* 向外扩一点，防止遮住头像内容 */
  left: -10%;
  width: 120%;  /* 框比头像大一圈 */
  height: 120%;
  z-index: 10;  /* 浮在头像上面 */
  pointer-events: none; /* 让点击事件穿透到下面的头像/上传按钮 */
  border-radius: 50%;
}
</style>