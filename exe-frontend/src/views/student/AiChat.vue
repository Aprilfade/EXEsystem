<template>
  <div class="ai-chat-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="title-section">
          <el-icon :size="32" color="#667eea"><ChatDotRound /></el-icon>
          <div class="title-text">
            <h1>🤖 小艾学习助手</h1>
            <p>智能AI助手，随时为你解答学习问题</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button :icon="ChatDotRound" @click="createNewSession" type="primary">新建会话</el-button>
        <el-button :icon="Setting" @click="showSettingsDialog = true">设置</el-button>
        <el-dropdown @command="handleQuickAction">
          <el-button :icon="MoreFilled">更多</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="export">
                <el-icon><Download /></el-icon>
                导出对话
              </el-dropdown-item>
              <el-dropdown-item command="clear" divided>
                <el-icon><Delete /></el-icon>
                清空历史
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="page-content">
      <!-- 左侧：快捷功能区 -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <h3 v-if="!sidebarCollapsed">快捷功能</h3>
          <el-button
            circle
            :icon="sidebarCollapsed ? Expand : Fold"
            @click="toggleSidebar"
            size="small"
          />
        </div>

        <div class="quick-actions" v-if="!sidebarCollapsed">
          <!-- 学习场景 -->
          <div class="action-group">
            <h4>📚 学习场景</h4>
            <el-button
              v-for="scene in learningScenes"
              :key="scene.value"
              class="scene-btn"
              @click="selectScene(scene)"
            >
              <el-icon><component :is="scene.icon" /></el-icon>
              {{ scene.label }}
            </el-button>
          </div>

          <!-- 常用问题 -->
          <div class="action-group">
            <h4>💡 常用问题</h4>
            <el-button
              v-for="(question, index) in commonQuestions"
              :key="index"
              class="question-btn"
              type="info"
              text
              @click="askQuestion(question)"
            >
              {{ question }}
            </el-button>
          </div>

          <!-- 统计信息 -->
          <div class="action-group stats">
            <h4>📊 使用统计</h4>
            <div class="stat-item">
              <span class="stat-label">今日对话</span>
              <span class="stat-value">{{ todayChats }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">总对话数</span>
              <span class="stat-value">{{ totalChats }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">解决问题</span>
              <span class="stat-value">{{ solvedQuestions }}</span>
            </div>
          </div>

          <!-- 快捷键提示 -->
          <div class="action-group shortcuts">
            <h4>⌨️ 快捷键</h4>
            <div class="shortcut-item">
              <kbd>Ctrl</kbd> + <kbd>N</kbd>
              <span>新建会话</span>
            </div>
            <div class="shortcut-item">
              <kbd>Ctrl</kbd> + <kbd>K</kbd>
              <span>快速提问</span>
            </div>
            <div class="shortcut-item">
              <kbd>Ctrl</kbd> + <kbd>E</kbd>
              <span>导出对话</span>
            </div>
            <div class="shortcut-item">
              <kbd>ESC</kbd>
              <span>关闭弹窗</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：对话区域 -->
      <div class="main-chat-area">
        <AiChatPanel ref="chatPanelRef" />
      </div>

      <!-- 右侧：辅助信息区 -->
      <div class="info-panel" v-if="showInfoPanel">
        <el-tabs v-model="activeInfoTab">
          <!-- 使用技巧 -->
          <el-tab-pane label="使用技巧" name="tips">
            <div class="tips-content">
              <div class="tip-card" v-for="(tip, index) in aiTips" :key="index">
                <el-icon :color="tip.color"><InfoFilled /></el-icon>
                <div class="tip-text">
                  <h4>{{ tip.title }}</h4>
                  <p>{{ tip.content }}</p>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 学习建议 -->
          <el-tab-pane label="学习建议" name="suggestions">
            <div class="suggestions-content">
              <el-empty v-if="aiSuggestions.length === 0" description="暂无学习建议" />
              <div v-else class="suggestion-list">
                <div
                  v-for="(suggestion, index) in aiSuggestions"
                  :key="index"
                  class="suggestion-item"
                >
                  <el-tag :type="suggestion.type" size="small">{{ suggestion.tag }}</el-tag>
                  <p>{{ suggestion.content }}</p>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 最近会话 -->
          <el-tab-pane label="最近会话" name="history">
            <div class="history-content">
              <el-timeline>
                <el-timeline-item
                  v-for="(session, index) in recentSessions"
                  :key="index"
                  :timestamp="session.time"
                >
                  <div class="session-item" @click="loadSession(session.id)">
                    <h4>{{ session.title }}</h4>
                    <p>{{ session.preview }}</p>
                  </div>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-if="recentSessions.length === 0" description="暂无历史会话" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 设置对话框 -->
    <el-dialog
      v-model="showSettingsDialog"
      title="AI助手设置"
      width="600px"
    >
      <el-form label-width="120px">
        <el-form-item label="显示信息面板">
          <el-switch v-model="showInfoPanel" />
        </el-form-item>
        <el-form-item label="自动保存对话">
          <el-switch v-model="autoSave" />
        </el-form-item>
        <el-form-item label="语音输入">
          <el-switch v-model="voiceInput" disabled />
          <span class="form-tip">（即将推出）</span>
        </el-form-item>
        <el-form-item label="默认对话类型">
          <el-select v-model="defaultChatType">
            <el-option label="闲聊" value="general" />
            <el-option label="学习" value="learning" />
            <el-option label="答疑" value="question" />
            <el-option label="激励" value="motivation" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettingsDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ChatDotRound, Setting, MoreFilled, Download, Delete,
  Expand, Fold, InfoFilled, Reading, QuestionFilled,
  TrendCharts, Calendar, Notebook
} from '@element-plus/icons-vue';
import AiChatPanel from '@/components/ai/AiChatPanel.vue';

// 会话数据接口
interface ChatSession {
  id: string;
  title: string;
  preview: string;
  time: string;
  messages: any[];
  createdAt: number;
}

// 侧边栏状态
const sidebarCollapsed = ref(false);
const showInfoPanel = ref(true);
const activeInfoTab = ref('tips');

// 设置对话框
const showSettingsDialog = ref(false);
const autoSave = ref(true);
const voiceInput = ref(false);
const defaultChatType = ref('learning');

// 统计数据
const todayChats = ref(0);
const totalChats = ref(0);
const solvedQuestions = ref(0);

// 当前会话ID
const currentSessionId = ref('');

// 学习场景
const learningScenes = ref([
  { label: '知识问答', value: 'qa', icon: QuestionFilled },
  { label: '题目讲解', value: 'explain', icon: Reading },
  { label: '学习规划', value: 'plan', icon: Calendar },
  { label: '错题分析', value: 'analysis', icon: TrendCharts },
  { label: '学习笔记', value: 'notes', icon: Notebook }
]);

// 常用问题
const commonQuestions = ref([
  '如何提高数学成绩？',
  '英语语法怎么学习？',
  '物理公式记不住怎么办？',
  '如何制定学习计划？',
  '考试焦虑怎么缓解？'
]);

// AI使用技巧
const aiTips = ref([
  {
    title: '清晰表达',
    content: '尽量清晰地描述你的问题，提供必要的背景信息',
    color: '#409EFF'
  },
  {
    title: '分步提问',
    content: '对于复杂问题，可以分成几个小问题逐步询问',
    color: '#67C23A'
  },
  {
    title: '提供上下文',
    content: '如果是继续之前的话题，可以简单回顾一下',
    color: '#E6A23C'
  },
  {
    title: '举例说明',
    content: '通过具体例子能帮助AI更好地理解你的需求',
    color: '#F56C6C'
  },
  {
    title: '快捷键提示',
    content: '使用 Ctrl+K 打开快速提问，ESC 关闭对话框',
    color: '#909399'
  }
]);

// 学习建议
const aiSuggestions = ref([
  { tag: '数学', type: 'warning', content: '建议加强函数与导数部分的练习' },
  { tag: '英语', type: 'success', content: '词汇量有明显提升，继续保持' },
  { tag: '物理', type: 'danger', content: '力学部分需要重点复习' }
]);

// 最近会话
const recentSessions = ref<ChatSession[]>([]);

// 聊天面板引用
const chatPanelRef = ref();

// ============= 会话管理 =============

// 生成唯一ID
const generateId = () => {
  return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

// 格式化时间
const formatTime = (timestamp: number) => {
  const now = Date.now();
  const diff = now - timestamp;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days === 1) return '昨天';
  if (days === 2) return '前天';
  if (days < 7) return `${days}天前`;

  const date = new Date(timestamp);
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

// 保存当前会话
const saveCurrentSession = () => {
  if (!currentSessionId.value || !chatPanelRef.value) return;

  try {
    const sessions = JSON.parse(localStorage.getItem('ai_chat_sessions') || '[]');
    const existingIndex = sessions.findIndex((s: ChatSession) => s.id === currentSessionId.value);

    // 这里假设 chatPanelRef.value 有 messages 属性
    const messages = chatPanelRef.value.messages || [];
    if (messages.length === 0) return;

    const sessionData: ChatSession = {
      id: currentSessionId.value,
      title: messages[0]?.content?.substring(0, 20) || '新会话',
      preview: messages[messages.length - 1]?.content?.substring(0, 50) || '',
      time: formatTime(Date.now()),
      messages: messages,
      createdAt: Date.now()
    };

    if (existingIndex >= 0) {
      sessions[existingIndex] = sessionData;
    } else {
      sessions.unshift(sessionData);
    }

    // 只保留最近20个会话
    localStorage.setItem('ai_chat_sessions', JSON.stringify(sessions.slice(0, 20)));
    loadRecentSessions();
  } catch (error) {
    console.error('保存会话失败:', error);
  }
};

// 加载最近会话
const loadRecentSessions = () => {
  try {
    const sessions = JSON.parse(localStorage.getItem('ai_chat_sessions') || '[]');
    recentSessions.value = sessions.slice(0, 10).map((s: ChatSession) => ({
      ...s,
      time: formatTime(s.createdAt)
    }));
  } catch (error) {
    console.error('加载会话失败:', error);
    recentSessions.value = [];
  }
};

// 创建新会话
const createNewSession = () => {
  currentSessionId.value = generateId();
  ElMessage.success('已创建新会话');
};

// 加载会话
const loadSession = (sessionId: string) => {
  try {
    const sessions = JSON.parse(localStorage.getItem('ai_chat_sessions') || '[]');
    const session = sessions.find((s: ChatSession) => s.id === sessionId);

    if (session && chatPanelRef.value && chatPanelRef.value.loadMessages) {
      chatPanelRef.value.loadMessages(session.messages);
      currentSessionId.value = sessionId;
      ElMessage.success('会话已加载');
    } else {
      ElMessage.warning('会话加载失败');
    }
  } catch (error) {
    console.error('加载会话失败:', error);
    ElMessage.error('加载会话时发生错误');
  }
};

// ============= 统计数据管理 =============

// 更新统计数据
const updateStats = () => {
  todayChats.value++;
  totalChats.value++;

  const stats = {
    todayChats: todayChats.value,
    totalChats: totalChats.value,
    solvedQuestions: solvedQuestions.value,
    lastUpdate: Date.now(),
    lastUpdateDate: new Date().toDateString()
  };

  localStorage.setItem('ai_chat_stats', JSON.stringify(stats));
};

// 加载统计数据
const loadStats = () => {
  try {
    const stats = JSON.parse(localStorage.getItem('ai_chat_stats') || '{}');
    const today = new Date().toDateString();

    // 如果是新的一天，重置今日统计
    if (stats.lastUpdateDate !== today) {
      todayChats.value = 0;
      totalChats.value = stats.totalChats || 0;
      solvedQuestions.value = stats.solvedQuestions || 0;
    } else {
      todayChats.value = stats.todayChats || 0;
      totalChats.value = stats.totalChats || 0;
      solvedQuestions.value = stats.solvedQuestions || 0;
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// ============= 导出功能 =============

// 导出对话为Markdown
const exportAsMarkdown = () => {
  if (!chatPanelRef.value || !chatPanelRef.value.messages) {
    ElMessage.warning('暂无对话内容可导出');
    return;
  }

  const messages = chatPanelRef.value.messages;
  let markdown = `# AI对话记录\n\n`;
  markdown += `导出时间: ${new Date().toLocaleString()}\n\n`;
  markdown += `---\n\n`;

  messages.forEach((msg: any, index: number) => {
    const role = msg.role === 'user' ? '👤 用户' : '🤖 AI助手';
    markdown += `## ${role}\n\n${msg.content}\n\n`;
  });

  // 创建下载
  const blob = new Blob([markdown], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `AI对话记录_${new Date().getTime()}.md`;
  a.click();
  URL.revokeObjectURL(url);

  ElMessage.success('对话已导出为Markdown文件');
};

// 导出对话为JSON
const exportAsJSON = () => {
  if (!chatPanelRef.value || !chatPanelRef.value.messages) {
    ElMessage.warning('暂无对话内容可导出');
    return;
  }

  const exportData = {
    exportTime: new Date().toISOString(),
    sessionId: currentSessionId.value,
    messages: chatPanelRef.value.messages,
    stats: {
      todayChats: todayChats.value,
      totalChats: totalChats.value,
      solvedQuestions: solvedQuestions.value
    }
  };

  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `AI对话数据_${new Date().getTime()}.json`;
  a.click();
  URL.revokeObjectURL(url);

  ElMessage.success('对话已导出为JSON文件');
};

// ============= 快捷键支持 =============

// 快捷键处理
const handleKeyDown = (event: KeyboardEvent) => {
  // Ctrl/Cmd + K: 快速提问
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    event.preventDefault();
    if (commonQuestions.value.length > 0) {
      askQuestion(commonQuestions.value[0]);
    }
  }

  // Ctrl/Cmd + N: 新建会话
  if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
    event.preventDefault();
    createNewSession();
  }

  // Ctrl/Cmd + E: 导出对话
  if ((event.ctrlKey || event.metaKey) && event.key === 'e') {
    event.preventDefault();
    exportAsMarkdown();
  }

  // ESC: 关闭设置对话框
  if (event.key === 'Escape' && showSettingsDialog.value) {
    showSettingsDialog.value = false;
  }
};

// ============= 原有功能 =============

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};

// 选择学习场景
const selectScene = (scene: any) => {
  ElMessage.info(`已切换到${scene.label}模式`);
  currentSessionId.value = generateId();
  updateStats();
};

// 快速提问
const askQuestion = (question: string) => {
  if (chatPanelRef.value && chatPanelRef.value.sendMessage) {
    chatPanelRef.value.sendMessage(question);
    updateStats();
  } else {
    ElMessage.warning('聊天面板未就绪');
  }
};

// 处理快捷操作
const handleQuickAction = (command: string) => {
  switch (command) {
    case 'export':
      ElMessageBox.confirm('选择导出格式', '导出对话', {
        distinguishCancelAndClose: true,
        confirmButtonText: 'Markdown',
        cancelButtonText: 'JSON',
        type: 'info'
      }).then(() => {
        exportAsMarkdown();
      }).catch((action) => {
        if (action === 'cancel') {
          exportAsJSON();
        }
      });
      break;
    case 'clear':
      ElMessageBox.confirm('确定要清空所有历史对话吗？此操作不可恢复！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('ai_chat_sessions');
        recentSessions.value = [];
        ElMessage.success('历史对话已清空');
      }).catch(() => {});
      break;
  }
};

// 保存设置
const saveSettings = () => {
  localStorage.setItem('ai_chat_settings', JSON.stringify({
    showInfoPanel: showInfoPanel.value,
    autoSave: autoSave.value,
    defaultChatType: defaultChatType.value
  }));
  ElMessage.success('设置已保存');
  showSettingsDialog.value = false;
};

// 加载设置
const loadSettings = () => {
  const settings = localStorage.getItem('ai_chat_settings');
  if (settings) {
    const parsed = JSON.parse(settings);
    showInfoPanel.value = parsed.showInfoPanel ?? true;
    autoSave.value = parsed.autoSave ?? true;
    defaultChatType.value = parsed.defaultChatType ?? 'learning';
  }
};

// 自动保存定时器
let autoSaveTimer: number | null = null;

// 启动自动保存
const startAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
  }

  if (autoSave.value) {
    // 每30秒自动保存一次
    autoSaveTimer = window.setInterval(() => {
      saveCurrentSession();
    }, 30000);
  }
};

// 监听自动保存设置变化
watch(autoSave, (newValue) => {
  if (newValue) {
    startAutoSave();
  } else if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
    autoSaveTimer = null;
  }
});

// 组件挂载时
onMounted(() => {
  loadSettings();
  loadStats();
  loadRecentSessions();
  createNewSession();
  startAutoSave();

  // 添加快捷键监听
  window.addEventListener('keydown', handleKeyDown);

  // 页面卸载前保存当前会话
  window.addEventListener('beforeunload', saveCurrentSession);
});

// 组件卸载时
onUnmounted(() => {
  // 保存当前会话
  saveCurrentSession();

  // 清理定时器
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
  }

  // 移除事件监听
  window.removeEventListener('keydown', handleKeyDown);
  window.removeEventListener('beforeunload', saveCurrentSession);
});

</script>

<style scoped>
.ai-chat-page {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-text p {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主体内容 */
.page-content {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

/* 左侧边栏 */
.sidebar {
  width: 280px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  overflow-y: auto;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.sidebar.collapsed {
  width: 60px;
  padding: 16px 8px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--text-primary);
}

.action-group {
  margin-bottom: 24px;
}

.action-group h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.scene-btn,
.question-btn {
  width: 100%;
  margin-bottom: 8px;
  justify-content: flex-start;
  text-align: left;
}

.scene-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-btn {
  white-space: normal;
  height: auto;
  padding: 8px 12px;
  line-height: 1.5;
}

/* 统计信息 */
.stats {
  background: linear-gradient(135deg, #667eea22 0%, #764ba222 100%);
  padding: 12px;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed var(--border-color);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
}

/* 快捷键提示 */
.shortcuts {
  background: var(--bg-secondary);
  padding: 12px;
  border-radius: 8px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.shortcut-item kbd {
  display: inline-block;
  padding: 2px 6px;
  font-size: 11px;
  font-family: 'Courier New', monospace;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  min-width: 24px;
  text-align: center;
}

.shortcut-item span {
  flex: 1;
}

/* 主聊天区域 */
.main-chat-area {
  flex: 1;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.main-chat-area :deep(.ai-chat-panel) {
  height: 100%;
}

/* 右侧信息面板 */
.info-panel {
  width: 320px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  overflow-y: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 使用技巧 */
.tips-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border-left: 3px solid #409EFF;
}

.tip-text h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: var(--text-primary);
}

.tip-text p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 学习建议 */
.suggestions-content {
  padding: 8px 0;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.suggestion-item .el-tag {
  margin-bottom: 8px;
}

.suggestion-item p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 历史会话 */
.history-content {
  padding: 8px 0;
}

.session-item {
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.3s;
}

.session-item:hover {
  background: var(--bg-secondary);
}

.session-item h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: var(--text-primary);
}

.session-item p {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 表单提示 */
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar,
.info-panel::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-thumb,
.info-panel::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

/* 响应式 */
@media (max-width: 1400px) {
  .info-panel {
    display: none;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .header-left,
  .header-right {
    width: 100%;
  }

  .header-right {
    justify-content: flex-end;
  }

  .page-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    max-height: 200px;
  }

  .sidebar.collapsed {
    width: 100%;
    max-height: 60px;
  }

  .main-chat-area {
    height: 500px;
  }
}
</style>
