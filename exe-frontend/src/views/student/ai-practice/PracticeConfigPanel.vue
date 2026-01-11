<template>
  <el-card class="config-card" shadow="hover">
    <template #header>
      <span>🎯 练习配置</span>
    </template>

    <el-form :model="config" label-width="100px">
      <!-- 科目选择 -->
      <el-form-item label="科目">
        <el-select v-model="config.subject" placeholder="请选择科目" style="width: 100%">
          <el-option label="数学" value="math" />
          <el-option label="英语" value="english" />
          <el-option label="物理" value="physics" />
          <el-option label="化学" value="chemistry" />
        </el-select>
      </el-form-item>

      <!-- 题目数量 -->
      <el-form-item label="题目数量">
        <el-input-number
          v-model="config.questionCount"
          :min="5"
          :max="50"
          :step="5"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 难度选择 -->
      <el-form-item label="难度">
        <el-radio-group v-model="config.difficulty">
          <el-radio value="easy">简单</el-radio>
          <el-radio value="medium">中等</el-radio>
          <el-radio value="hard">困难</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 题型选择 -->
      <el-form-item label="题型">
        <el-checkbox-group v-model="config.questionTypes">
          <el-checkbox value="1" label="单选题" />
          <el-checkbox value="2" label="多选题" />
          <el-checkbox value="3" label="填空题" />
          <el-checkbox value="5" label="主观题" />
        </el-checkbox-group>
      </el-form-item>

      <!-- 开始按钮 -->
      <el-form-item>
        <el-button
          type="primary"
          :icon="MagicStick"
          @click="handleStart"
          style="width: 100%"
          size="large"
        >
          开始练习
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import type { PracticeConfig } from '@/types/practice'

const emit = defineEmits<{
  start: [config: PracticeConfig]
}>()

const config = reactive<PracticeConfig>({
  subject: 'math',
  questionCount: 10,
  difficulty: 'medium',
  questionTypes: ['1', '2', '3'],
  knowledgePoints: [],
  timeLimit: 0
})

const handleStart = () => {
  emit('start', { ...config })
}
</script>

<style scoped lang="scss">
.config-card {
  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}
</style>
