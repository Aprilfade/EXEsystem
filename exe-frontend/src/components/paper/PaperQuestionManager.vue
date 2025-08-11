<template>
  <div>
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="isQuestionSelectVisible = true">从题库选题</el-button>
      <el-button :icon="FolderAdd" @click="addGroup">新建分组</el-button>
    </div>

    <div class="paper-groups-container">
      <draggable
          v-model="localGroups"
          item-key="id"
          class="group-draggable-area"
          handle=".group-drag-handle"
          @end="emitUpdate"
      >
        <template #item="{ element: group, index: groupIndex }">
          <div class="paper-group">
            <div class="group-header">
              <el-icon class="group-drag-handle"><Rank /></el-icon>
              <el-input v-model="group.name" placeholder="请输入分组标题" class="group-title-input" @change="emitUpdate" />
              <el-button type="danger" :icon="Delete" circle plain @click="removeGroup(groupIndex)"></el-button>
            </div>

            <draggable
                v-model="group.questions"
                item-key="questionId"
                class="question-list"
                group="questions"
                @end="emitUpdate"
            >
              <template #item="{ element: pq, index: qIndex }">
                <div class="question-item">
                  <div class="question-item-left">
                    <el-icon class="question-drag-handle"><Rank /></el-icon>
                    <span class="question-index">{{ qIndex + 1 }}.</span>
                    <p class="question-content" :title="pq.questionDetail?.content">
                      {{ pq.questionDetail?.content || '题目加载中...' }}
                    </p>
                  </div>
                  <div class="question-actions">
                    <el-input-number v-model="pq.score" :min="0" size="small" style="width: 100px;" @change="emitUpdate" />
                    <el-button type="danger" link :icon="Delete" @click="removeQuestion(groupIndex, qIndex)">移除</el-button>
                  </div>
                </div>
              </template>
            </draggable>

            <div v-if="!group.questions || group.questions.length === 0" class="empty-group-placeholder">
              可从题库选题后拖拽题目至此区域
            </div>
          </div>
        </template>
      </draggable>
      <div v-if="!localGroups || localGroups.length === 0" class="empty-paper-placeholder">
        暂无分组，请点击 "新建分组" 开始组卷
      </div>
    </div>

    <el-dialog v-model="isQuestionSelectVisible" title="从题库选择试题" width="70%">
      <question-selector :subject-id="subjectId" @add="addQuestionsToDefaultGroup" @close="isQuestionSelectVisible = false" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { Plus, Delete, FolderAdd, Rank } from '@element-plus/icons-vue';
import draggable from 'vuedraggable'; // 导入 draggable 组件
import type { PaperGroup, PaperQuestion } from '@/api/paper';
import type { Question } from '@/api/question';
import { fetchQuestionList } from '@/api/question';
import QuestionSelector from './QuestionSelector.vue';
import { ElMessageBox } from 'element-plus';

const props = defineProps<{
  paperGroups: PaperGroup[];
  subjectId: number;
}>();

const emit = defineEmits(['update:groups']);

const localGroups = ref<PaperGroup[]>([]);
const isQuestionSelectVisible = ref(false);


// 异步获取所有题目的详细信息
const enrichAllQuestionDetails = async () => {
  const allQuestionIds = localGroups.value.flatMap(g => g.questions.map(q => q.questionId));
  if (allQuestionIds.length === 0) return;

  // 实际项目最好有一个 `findByIds` 的接口，这里用分页接口模拟
  const res = await fetchQuestionList({ current: 1, size: 9999, subjectId: props.subjectId });
  const questionsMap = new Map(res.data.map((q: Question) => [q.id, q]));

  localGroups.value.forEach(group => {
    group.questions.forEach(pq => {
      if (!pq.questionDetail) {
        pq.questionDetail = questionsMap.get(pq.questionId);
      }
    });
  });
};

// 通知父组件数据已更新
const emitUpdate = () => {
  // 更新所有排序字段
  localGroups.value.forEach((group, groupIndex) => {
    group.sortOrder = groupIndex;
    group.questions.forEach((q, qIndex) => {
      q.sortOrder = qIndex;
    });
  });
  emit('update:groups', localGroups.value);
};

// 新增分组
const addGroup = () => {
  localGroups.value.push({
    name: `新分组 ${localGroups.value.length + 1}`,
    sortOrder: localGroups.value.length,
    questions: []
  });
  emitUpdate();
};

// 移除分组
const removeGroup = (index: number) => {
  ElMessageBox.confirm(
      '确定要删除这个分组及其包含的所有题目吗?',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    localGroups.value.splice(index, 1);
    emitUpdate();
  })
};

// 移除题目
const removeQuestion = (groupIndex: number, questionIndex: number) => {
  localGroups.value[groupIndex].questions.splice(questionIndex, 1);
  emitUpdate();
};

// 将从题库选择的题目添加到第一个分组
const addQuestionsToDefaultGroup = (selectedQuestions: Question[]) => {
  if (localGroups.value.length === 0) {
    addGroup();
  }
  const defaultGroup = localGroups.value[0];

  selectedQuestions.forEach(q => {
    // 检查整个试卷是否已存在该题目
    const exists = localGroups.value.some(g => g.questions.some(pq => pq.questionId === q.id));
    if (!exists) {
      defaultGroup.questions.push({
        paperId: 0,
        questionId: q.id,
        score: 5, // 默认分值
        sortOrder: defaultGroup.questions.length,
        questionDetail: q
      });
    }
  });
  isQuestionSelectVisible.value = false;
  emitUpdate();
};
// 深度拷贝 props 数据到本地 state，避免直接修改 props
watch(() => props.paperGroups, (newVal) => {
  localGroups.value = JSON.parse(JSON.stringify(newVal || []));
  enrichAllQuestionDetails();
}, { immediate: true, deep: true });


</script>

<style scoped>
.action-bar {
  margin-bottom: 20px;
}

.paper-groups-container {
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 10px;
  background-color: #f9f9f9;
}

.paper-group {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 15px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.group-header {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.group-drag-handle {
  cursor: move;
  margin-right: 10px;
  color: #909399;
}

.group-title-input {
  flex-grow: 1;
  margin-right: 10px;
}
.group-title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  background-color: transparent;
}


.question-list {
  padding: 10px;
  min-height: 80px;
}

.question-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fff;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: move;
}
.question-item:hover {
  border-color: var(--brand-color);
}
.question-item-left {
  display: flex;
  align-items: center;
  flex-grow: 1;
  overflow: hidden;
}

.question-drag-handle {
  margin-right: 8px;
  color: #c0c4cc;
}
.question-index {
  margin-right: 8px;
  font-weight: bold;
}
.question-content {
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  padding-right: 10px;
}

.question-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.empty-paper-placeholder,
.empty-group-placeholder {
  text-align: center;
  color: #a8abb2;
  padding: 20px;
  font-size: 14px;
}
</style>