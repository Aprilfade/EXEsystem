<template>
  <el-dialog
      :model-value="visible"
      title="配置 AI 智能助教"
      width="520px"
      @close="handleClose"
      append-to-body
  >
    <div class="key-setting-content">
      <el-alert
          title="请配置您的 AI 服务"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
      >
        <p>支持 <b>DeepSeek</b> 和 <b>通义千问</b>。Key 仅保存在本地浏览器。</p>
      </el-alert>

      <el-form label-position="top">
        <el-form-item label="选择模型提供商">
          <el-radio-group v-model="form.provider">
            <el-radio-button label="DEEPSEEK">DeepSeek</el-radio-button>
            <el-radio-button label="QWEN">通义千问</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="API Key">
          <el-input
              v-model="form.key"
              type="password"
              show-password
              :placeholder="placeholderText"
          />
          <div class="form-tip">
            <el-link type="primary" :href="helpLink" target="_blank" style="font-size: 12px;">
              👉 点击获取 {{ providerName }} API Key
            </el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="saveConfig">保存并继续</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { ElMessage } from 'element-plus';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits(['update:visible', 'saved']);

const store = useStudentAuthStore();
const form = reactive({
  key: '',
  provider: 'DEEPSEEK'
});

// 根据选择显示不同的提示
const providerName = computed(() => form.provider === 'DEEPSEEK' ? 'DeepSeek' : '阿里云百炼');
const placeholderText = computed(() => form.provider === 'DEEPSEEK' ? 'sk-...' : 'sk-...');
const helpLink = computed(() =>
    form.provider === 'DEEPSEEK'
        ? 'https://platform.deepseek.com/api_keys'
        : 'https://bailian.console.aliyun.com/?apiKey=1'
);

watch(() => props.visible, (val) => {
  if (val) {
    form.key = store.aiKey;
    form.provider = store.aiProvider || 'DEEPSEEK';
  }
});

const handleClose = () => {
  emit('update:visible', false);
};

const saveConfig = () => {
  if (!form.key.trim()) {
    ElMessage.warning('请输入有效的 API Key');
    return;
  }
  // 调用 Store 新增的方法
  store.setAiConfig(form.key.trim(), form.provider);
  ElMessage.success('设置成功');
  emit('saved');
  handleClose();
};
</script>

<style scoped>
.form-tip {
  margin-top: 5px;
  text-align: right;
}
</style>