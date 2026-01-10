<template>
  <el-card class="ai-chat-panel" :body-style="{ padding: '0', height: '100%' }">
    <!-- 顶部标题栏 -->
    <template #header>
      <div class="chat-header">
        <div class="header-left">
          <el-icon :size="24" color="#409EFF"><ChatDotRound /></el-icon>
          <span class="header-title">🤖 小艾学习助手</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <el-button circle :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="new-chat">
                  <el-icon><Plus /></el-icon>
                  新建对话
                </el-dropdown-item>
                <el-dropdown-item command="history">
                  <el-icon><Clock /></el-icon>
                  历史会话
                </el-dropdown-item>
                <el-dropdown-item command="clear" divided>
                  <el-icon><Delete /></el-icon>
                  清空当前对话
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </template>

    <!-- 对话内容区域 -->
    <div class="chat-content" ref="chatContentRef">
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="welcome-message">
        <div class="welcome-icon">👋</div>
        <h3>你好！我是小艾</h3>
        <p>你的专属AI学习助手，随时为你解答学习问题</p>

        <!-- 快捷问题 -->
        <div class="quick-questions">
          <h4>试试问我：</h4>
          <el-button
            v-for="(q, index) in quickQuestions"
            :key="index"
            type="primary"
            plain
            size="small"
            @click="sendQuickQuestion(q.question, q.type)"
          >
            {{ q.icon }} {{ q.label }}
          </el-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-item"
        :class="{ 'message-user': msg.role === 'user', 'message-ai': msg.role === 'assistant' }"
      >
        <!-- AI消息 -->
        <div v-if="msg.role === 'assistant'" class="message-ai-wrapper">
          <div class="avatar avatar-ai">
            <el-icon :size="20"><Avatar /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-name">小艾</div>
            <div class="message-text markdown-body" v-html="renderMarkdown(msg.content)"></div>
            <div class="message-actions">
              <el-button text size="small" @click="copyMessage(msg.content)">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </div>
          </div>
        </div>

        <!-- 用户消息 -->
        <div v-else class="message-user-wrapper">
          <div class="message-content">
            <div class="message-name">我</div>
            <div class="message-text">{{ msg.content }}</div>
          </div>
          <div class="avatar avatar-user">
            <el-icon :size="20"><User /></el-icon>
          </div>
        </div>
      </div>

      <!-- AI正在输入... -->
      <div v-if="isTyping" class="message-item message-ai">
        <div class="message-ai-wrapper">
          <div class="avatar avatar-ai">
            <el-icon :size="20"><Avatar /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-name">小艾</div>
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 建议问题（在最近一次AI回复后显示） -->
    <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-bar">
      <span class="suggestions-label">你可能还想问：</span>
      <el-button
        v-for="(suggestion, index) in suggestions"
        :key="index"
        type="info"
        text
        size="small"
        @click="sendMessage(suggestion)"
      >
        {{ suggestion }}
      </el-button>
    </div>

    <!-- 底部输入框 -->
    <div class="chat-input-wrapper">
      <!-- 对话类型选择 -->
      <div class="chat-type-selector">
        <el-segmented v-model="chatType" :options="chatTypeOptions" size="small" />
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          :maxlength="1000"
          show-word-limit
          placeholder="输入你的问题...（Shift+Enter换行，Enter发送）"
          @keydown.enter="handleEnter"
        />
        <div class="input-actions">
          <el-checkbox v-model="useContext" size="small">
            记忆上下文
          </el-checkbox>
          <el-button
            type="primary"
            :icon="Promotion"
            :loading="isSending"
            :disabled="!inputMessage.trim()"
            @click="sendMessage()"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>

    <!-- 历史会话抽屉 -->
    <el-drawer
      v-model="historyDrawerVisible"
      title="历史会话"
      direction="rtl"
      size="400px"
    >
      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.sessionId"
          class="session-item"
          :class="{ active: session.sessionId === currentSessionId }"
          @click="loadSession(session.sessionId)"
        >
          <div class="session-info">
            <div class="session-title">{{ session.title }}</div>
            <div class="session-meta">
              {{ session.messageCount }} 条消息 · {{ formatTime(session.lastMessageTime) }}
            </div>
          </div>
          <el-button
            circle
            size="small"
            :icon="Delete"
            @click.stop="deleteSession(session.sessionId)"
          />
        </div>

        <el-empty v-if="sessions.length === 0" description="暂无历史会话" />
      </div>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ChatDotRound, MoreFilled, Plus, Clock, Delete, Avatar, User,
  DocumentCopy, Promotion
} from '@element-plus/icons-vue';
import { useStudentAuthStore } from '@/stores/studentAuth';
import request from '@/utils/request';
import { marked } from 'marked';

interface Message {
  role: 'user' | 'assistant' | 'system';
  content: string;
  createTime?: string;
}

interface Session {
  sessionId: string;
  title: string;
  messageCount: number;
  lastMessageTime: string;
}

const studentAuth = useStudentAuthStore();

const messages = ref<Message[]>([]);
const inputMessage = ref('');
const isSending = ref(false);
const isTyping = ref(false);
const currentSessionId = ref('');
const chatType = ref('general');
const useContext = ref(true);
const suggestions = ref<string[]>([]);
const showSuggestions = ref(false);
const chatContentRef = ref<HTMLElement>();

// 历史会话
const historyDrawerVisible = ref(false);
const sessions = ref<Session[]>([]);

// 对话类型选项
const chatTypeOptions = [
  { label: '💬 闲聊', value: 'general' },
  { label: '📚 学习', value: 'learning' },
  { label: '❓ 答疑', value: 'question' },
  { label: '💪 激励', value: 'motivation' },
  { label: '📅 规划', value: 'planning' }
];

// 快捷问题
const quickQuestions = [
  { icon: '📊', label: '我的学习情况', question: '帮我分析一下我的学习情况', type: 'general' },
  { icon: '📝', label: '制定学习计划', question: '帮我制定一个学习计划', type: 'planning' },
  { icon: '💡', label: '学习方法', question: '有什么好的学习方法推荐吗', type: 'learning' },
  { icon: '🎯', label: '薄弱知识点', question: '我的薄弱知识点有哪些', type: 'general' }
];

/**
 * 发送消息
 */
const sendMessage = async (customMessage?: string) => {
  const message = customMessage || inputMessage.value.trim();

  if (!message) return;

  // 检查AI Key
  const apiKey = localStorage.getItem('student_ai_key');
  const provider = localStorage.getItem('student_ai_provider') || 'deepseek';

  if (!apiKey) {
    ElMessage.warning('请先在个人设置中配置AI Key');
    return;
  }

  // 添加用户消息到列表
  messages.value.push({
    role: 'user',
    content: message
  });

  // 清空输入框
  if (!customMessage) {
    inputMessage.value = '';
  }

  // 滚动到底部
  scrollToBottom();

  // 显示AI正在输入
  isSending.value = true;
  isTyping.value = true;
  showSuggestions.value = false;

  try {
    const res = await request({
      url: '/api/v1/student/ai-chat/send',
      method: 'post',
      data: {
        message: message,
        sessionId: currentSessionId.value || undefined,
        chatType: chatType.value,
        useContext: useContext.value,
        contextSize: 5
      },
      headers: {
        'X-Ai-Api-Key': apiKey,
        'X-Ai-Provider': provider
      }
    });

    if (res.code === 200) {
      const response = res.data;

      // 更新会话ID
      if (response.sessionId) {
        currentSessionId.value = response.sessionId;
      }

      // 添加AI回复
      messages.value.push({
        role: 'assistant',
        content: response.message
      });

      // 显示建议问题
      if (response.suggestions && response.suggestions.length > 0) {
        suggestions.value = response.suggestions;
        showSuggestions.value = true;
      }

      scrollToBottom();
    } else {
      ElMessage.error(res.message || '发送失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '网络请求失败');
  } finally {
    isSending.value = false;
    isTyping.value = false;
  }
};

/**
 * 快捷提问
 */
const sendQuickQuestion = (question: string, type: string) => {
  chatType.value = type;
  sendMessage(question);
};

/**
 * 处理Enter按键
 */
const handleEnter = (e: KeyboardEvent) => {
  if (e.shiftKey) {
    // Shift+Enter 换行，不做处理
    return;
  }

  // Enter 发送
  e.preventDefault();
  sendMessage();
};

/**
 * 渲染Markdown
 */
const renderMarkdown = (content: string) => {
  return marked(content);
};

/**
 * 复制消息
 */
const copyMessage = (content: string) => {
  navigator.clipboard.writeText(content)
    .then(() => {
      ElMessage.success('已复制到剪贴板');
    })
    .catch(() => {
      ElMessage.error('复制失败');
    });
};

/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContentRef.value) {
      chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight;
    }
  });
};

/**
 * 命令处理
 */
const handleCommand = (command: string) => {
  switch (command) {
    case 'new-chat':
      createNewChat();
      break;
    case 'history':
      loadSessions();
      historyDrawerVisible.value = true;
      break;
    case 'clear':
      clearChat();
      break;
  }
};

/**
 * 创建新对话
 */
const createNewChat = () => {
  ElMessageBox.confirm(
    '确定要开始新的对话吗？当前对话将被保存。',
    '新建对话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    messages.value = [];
    currentSessionId.value = '';
    suggestions.value = [];
    showSuggestions.value = false;
    ElMessage.success('已开始新对话');
  }).catch(() => {});
};

/**
 * 清空对话
 */
const clearChat = () => {
  ElMessageBox.confirm(
    '确定要清空当前对话吗？此操作不可恢复。',
    '清空对话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    messages.value = [];
    ElMessage.success('对话已清空');
  }).catch(() => {});
};

/**
 * 加载会话列表
 */
const loadSessions = async () => {
  try {
    const res = await request({
      url: '/api/v1/student/ai-chat/sessions',
      method: 'get',
      params: { limit: 20 }
    });

    if (res.code === 200) {
      sessions.value = res.data;
    }
  } catch (error) {
    console.error('加载会话列表失败:', error);
  }
};

/**
 * 加载历史会话
 */
const loadSession = async (sessionId: string) => {
  try {
    const res = await request({
      url: `/api/v1/student/ai-chat/sessions/${sessionId}/messages`,
      method: 'get'
    });

    if (res.code === 200) {
      messages.value = res.data;
      currentSessionId.value = sessionId;
      historyDrawerVisible.value = false;
      scrollToBottom();
      ElMessage.success('会话已加载');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载会话失败');
  }
};

/**
 * 删除会话
 */
const deleteSession = async (sessionId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '删除会话', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const res = await request({
      url: `/api/v1/student/ai-chat/sessions/${sessionId}`,
      method: 'delete'
    });

    if (res.code === 200) {
      // 重新加载会话列表
      await loadSessions();

      // 如果删除的是当前会话，清空消息
      if (sessionId === currentSessionId.value) {
        messages.value = [];
        currentSessionId.value = '';
      }

      ElMessage.success('会话已删除');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;

  return date.toLocaleDateString();
};

onMounted(() => {
  // 初始化时自动滚动到底部
  scrollToBottom();
});
</script>

<style scoped>
.ai-chat-panel {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 60px 20px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-message h3 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.welcome-message p {
  color: #909399;
  margin-bottom: 32px;
}

.quick-questions {
  margin-top: 32px;
}

.quick-questions h4 {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
}

.quick-questions .el-button {
  margin: 8px 4px;
}

/* 消息项 */
.message-item {
  margin-bottom: 24px;
}

.message-ai-wrapper,
.message-user-wrapper {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-user-wrapper {
  margin-left: auto;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-ai {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.avatar-user {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.message-content {
  flex: 1;
}

.message-name {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-ai .message-text {
  background-color: white;
  border: 1px solid #e4e7ed;
}

.message-user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: right;
}

.message-actions {
  margin-top: 8px;
}

/* Markdown样式 */
.markdown-body :deep(p) {
  margin: 8px 0;
}

.markdown-body :deep(code) {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.markdown-body :deep(pre) {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 24px;
}

/* 输入中动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background-color: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.3;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* 建议栏 */
.suggestions-bar {
  padding: 12px 20px;
  background-color: white;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.suggestions-label {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

/* 输入框 */
.chat-input-wrapper {
  background-color: white;
  border-top: 1px solid #e4e7ed;
}

.chat-type-selector {
  padding: 12px 20px 0;
  display: flex;
  justify-content: center;
}

.chat-input {
  padding: 12px 20px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

/* 历史会话 */
.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #f5f7fa;
}

.session-item:hover {
  background-color: #e6f0ff;
}

.session-item.active {
  background-color: #409eff;
  color: white;
}

.session-info {
  flex: 1;
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.session-meta {
  font-size: 12px;
  opacity: 0.7;
}

/* 滚动条样式 */
.chat-content::-webkit-scrollbar {
  width: 6px;
}

.chat-content::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.chat-content::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}
</style>
