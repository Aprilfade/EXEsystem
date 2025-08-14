<template>
  <el-dialog
      :model-value="visible"
      :title="knowledgePoint?.name + ' - 详情'"
      width="60%"
      @close="handleClose"
  >
    <div v-if="knowledgePoint" v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="知识点名称">{{ knowledgePoint.name }}</el-descriptions-item>
        <el-descriptions-item label="编码">{{ knowledgePoint.code }}</el-descriptions-item>
        <el-descriptions-item label="所属科目">{{ subjectName }}</el-descriptions-item>
        <el-descriptions-item label="所属年级">{{ knowledgePoint.grade }}</el-descriptions-item>
        <el-descriptions-item label="标签">
          <el-tag v-if="knowledgePoint.tags" v-for="tag in knowledgePoint.tags.split(',')" :key="tag" type="info" size="small">{{ tag }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="简介">{{ knowledgePoint.description }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <h4>关联试题 ({{ questions.length }})</h4>
      <el-table :data="questions" height="300px">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column property="content" label="题干" show-overflow-tooltip />
        <el-table-column property="questionType" label="题型" width="120">
          <template #default="scope">
            <el-tag>{{ questionTypeMap[scope.row.questionType] }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { fetchKnowledgePointById, fetchQuestionsForKnowledgePoint, type KnowledgePoint } from '@/api/knowledgePoint';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import type { Question } from '@/api/question';
import { ElMessage } from 'element-plus';

const props = defineProps<{
  visible: boolean;
  knowledgePointId: number | null;
}>();

const emit = defineEmits(['update:visible']);

const loading = ref(false);
const knowledgePoint = ref<KnowledgePoint | null>(null);
const questions = ref<Question[]>([]);
const allSubjects = ref<Subject[]>([]);

const questionTypeMap: { [key: number]: string } = {
  1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题',
};

// 获取所有科目数据，用于显示科目名称
const loadAllSubjects = async () => {
  if (allSubjects.value.length === 0) {
    const res = await fetchAllSubjects();
    if (res.code === 200) allSubjects.value = res.data;
  }
};

const subjectName = computed(() => {
  if (!knowledgePoint.value) return 'N/A';
  const subject = allSubjects.value.find(s => s.id === knowledgePoint.value.subjectId);
  return subject ? subject.name : '未知';
});

const fetchDetails = async (kpId: number) => {
  loading.value = true;
  try {
    const [kpRes, qRes] = await Promise.all([
      fetchKnowledgePointById(kpId),
      fetchQuestionsForKnowledgePoint(kpId)
    ]);
    if (kpRes.code === 200) knowledgePoint.value = kpRes.data;
    if (qRes.code === 200) questions.value = qRes.data;
  } catch (error) {
    ElMessage.error('加载详情失败');
  } finally {
    loading.value = false;
  }
};

// 【替换为】下面这个新的 watch 函数
watch(() => props.knowledgePointId, (newId) => {
  // 只有当弹窗可见且传入了有效的ID时，才去加载数据
  if (props.visible && newId) {
    loadAllSubjects().then(() => {
      fetchDetails(newId);
    });
  }
}, { immediate: true }); // immediate: true 确保组件首次加载时也能执行一次
const handleClose = () => {
  emit('update:visible', false);
};
</script>

<style scoped>
h4 {
  margin-top: 20px;
  margin-bottom: 10px;
}
</style>