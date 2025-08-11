<template>
  <el-dialog
      :model-value="visible"
      title="题目详情"
      width="700px"
      @close="$emit('update:visible', false)"
  >
    <div v-if="question" class="preview-container">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="题目ID">{{ question.id }}</el-descriptions-item>
        <el-descriptions-item label="所属科目">{{ subjectName }}</el-descriptions-item>
        <el-descriptions-item label="题型">
          <el-tag>{{ questionTypeMap[question.questionType] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联知识点">
          <div v-if="associatedKnowledgePoints.length > 0">
            <el-tag v-for="kp in associatedKnowledgePoints" :key="kp.id" style="margin-right: 5px;">
              {{ kp.name }}
            </el-tag>
          </div>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="题干">{{ question.content }}</el-descriptions-item>
        <el-descriptions-item v-if="question.imageUrl" label="题目图片">
          <el-image :src="question.imageUrl" style="max-width: 200px; max-height: 200px;" fit="contain" :preview-src-list="[question.imageUrl]" />
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="question.questionType === 1 || question.questionType === 2" class="options-section">
        <h4>选项</h4>
        <ul>
          <li v-for="opt in parsedOptions" :key="opt.key">
            <strong>{{ opt.key }}.</strong> {{ opt.value }}
          </li>
        </ul>
      </div>

      <el-descriptions :column="1" border style="margin-top: 20px;">
        <el-descriptions-item label="参考答案">
          <span style="color: #67C23A; font-weight: bold;">{{ question.answer }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="解析">{{ question.description || '暂无解析' }}</el-descriptions-item>
        <el-descriptions-item v-if="question.answerImageUrl" label="答案图片">
          <el-image :src="question.answerImageUrl" style="max-width: 200px; max-height: 200px;" fit="contain" :preview-src-list="[question.answerImageUrl]" />
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <div v-else><p>加载中...</p></div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Question, QuestionOption } from '@/api/question';
import type { Subject } from '@/api/subject';
// 【新增】导入知识点类型
import type { KnowledgePoint } from '@/api/knowledgePoint';



const props = defineProps<{
  visible: boolean;
  question?: Question;
  allSubjects: Subject[];
  // 【新增】接收所有知识点数据
  allKnowledgePoints: KnowledgePoint[];
}>();



defineEmits(['update:visible']);

const questionTypeMap: { [key: number]: string } = {
  1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题',
};


const subjectName = computed(() => {
  if (!props.question) return 'N/A';
  const subject = props.allSubjects.find(s => s.id === props.question.subjectId);
  return subject ? subject.name : '未知';
});

const parsedOptions = computed((): QuestionOption[] => {
  if (props.question && typeof props.question.options === 'string') {
    try {
      return JSON.parse(props.question.options);
    } catch (e) {
      return [];
    }
  }
  return [];
});
// 【新增】计算属性，根据ID查找知识点名称
const associatedKnowledgePoints = computed(() => {
  if (!props.question?.knowledgePointIds || !props.allKnowledgePoints) {
    return [];
  }
  return props.allKnowledgePoints.filter(kp => props.question.knowledgePointIds.includes(kp.id));
});
</script>

<style scoped>
.preview-container { padding: 0 10px; }
.options-section { margin-top: 20px; }
.options-section h4 { margin-bottom: 10px; }
.options-section ul { list-style: none; padding-left: 10px; }
.options-section li { margin-bottom: 8px; }
</style>