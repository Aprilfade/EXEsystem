<template>
  <el-dialog
    v-model="dialogVisible"
    title="工作台布局设置"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-tabs v-model="activeTab">
      <!-- 模块设置 -->
      <el-tab-pane label="模块管理" name="modules">
        <div class="modules-list">
          <div
            v-for="module in modules"
            :key="module.id"
            class="module-item"
          >
            <div class="module-info">
              <el-switch v-model="module.enabled" />
              <span class="module-name">{{ module.title }}</span>
            </div>
            <div class="module-settings">
              <el-select
                v-model="module.width"
                placeholder="宽度"
                size="small"
                style="width: 120px"
              >
                <el-option label="1/4 宽度" :value="6" />
                <el-option label="1/3 宽度" :value="8" />
                <el-option label="1/2 宽度" :value="12" />
                <el-option label="2/3 宽度" :value="16" />
                <el-option label="全宽" :value="24" />
              </el-select>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 预设模板 -->
      <el-tab-pane label="预设模板" name="presets">
        <div class="presets-grid">
          <div
            v-for="(preset, key) in presets"
            :key="key"
            class="preset-card"
            :class="{ active: selectedPreset === key }"
            @click="handlePresetSelect(key)"
          >
            <div class="preset-header">
              <el-icon v-if="selectedPreset === key" class="check-icon">
                <CircleCheck />
              </el-icon>
              <h4>{{ preset.name }}</h4>
            </div>
            <div class="preset-preview">
              <div
                v-for="(m, idx) in preset.config.modules.filter((x: any) => x.enabled)"
                :key="idx"
                class="preview-block"
                :style="{ width: `${(m.width / 24) * 100}%` }"
              >
                {{ m.title }}
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 高级设置 -->
      <el-tab-pane label="高级设置" name="advanced">
        <el-form label-width="100px">
          <el-form-item label="紧凑模式">
            <el-switch v-model="compactMode" />
            <span class="form-tip">启用后减少卡片间距</span>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleReset" :loading="loading">
          重置为默认
        </el-button>
        <div>
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="loading">
            保存设置
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { CircleCheck } from '@element-plus/icons-vue';
import { layoutPresets } from '@/composables/useLayoutConfig';
import type { DashboardModule } from '@/composables/useLayoutConfig';

interface Props {
  visible: boolean;
  modules: DashboardModule[];
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'save', modules: DashboardModule[], compactMode: boolean): void;
  (e: 'reset'): void;
  (e: 'apply-preset', presetName: string): void;
}>();

const dialogVisible = ref(props.visible);
const activeTab = ref('modules');
const selectedPreset = ref<string>('');
const compactMode = ref(false);
const modules = ref<DashboardModule[]>([...props.modules]);
const presets = layoutPresets;

watch(() => props.visible, (val) => {
  dialogVisible.value = val;
  if (val) {
    // 重置为当前配置
    modules.value = JSON.parse(JSON.stringify(props.modules));
  }
});

watch(dialogVisible, (val) => {
  emit('update:visible', val);
});

const handleClose = () => {
  dialogVisible.value = false;
};

const handleSave = () => {
  emit('save', modules.value, compactMode.value);
};

const handleReset = () => {
  emit('reset');
};

const handlePresetSelect = (presetName: string) => {
  selectedPreset.value = presetName;
  emit('apply-preset', presetName);
  // 更新本地模块配置
  const preset = presets[presetName as keyof typeof presets];
  if (preset) {
    modules.value = JSON.parse(JSON.stringify(preset.config.modules));
    compactMode.value = preset.config.compactMode || false;
  }
};
</script>

<style scoped>
.modules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.module-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.module-item:hover {
  background: #e9ecef;
}

.module-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.presets-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.preset-card {
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preset-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.preset-card.active {
  border-color: #409EFF;
  background: rgba(64, 158, 255, 0.05);
}

.preset-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.check-icon {
  color: #67C23A;
  font-size: 20px;
}

.preset-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.preset-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  min-height: 80px;
}

.preview-block {
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 11px;
  padding: 0 8px;
  box-sizing: border-box;
  opacity: 0.8;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
