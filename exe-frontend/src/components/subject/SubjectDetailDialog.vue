<template>
  <el-dialog
      :model-value="visible"
      :title="subjectName + ' - 详情'"
      width="60%"
      @close="handleClose"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="关联知识点" name="knowledgePoints">
        <el-table :data="knowledgePoints" v-loading="loadingKp" height="400px">
          <el-table-column property="name" label="知识点名称" />
          <el-table-column property="code" label="编码" width="150" />
          <el-table-column property="description" label="描述" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="关联试题" name="questions">
        <el-table :data="questions" v-loading="loadingQ" height="400px">
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column property="content" label="题干" show-overflow-tooltip />
          <el-table-column property="questionType" label="题型" width="120">
            <template #default="scope">
              <el-tag>{{ questionTypeMap[scope.row.questionType] }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { fetchKnowledgePointList, type KnowledgePoint } from '@/api/knowledgePoint';
import { fetchQuestionList, type Question } from '@/api/question';
import { ElMessage } from 'element-plus';

const props = defineProps<{
  visible: boolean;
  subjectId: number | null;
  subjectName: string;
}>();

const emit = defineEmits(['update:visible']);

const activeTab = ref('knowledgePoints');
const knowledgePoints = ref<KnowledgePoint[]>([]);
const questions = ref<Question[]>([]);
const loadingKp = ref(false);
const loadingQ = ref(false);

const questionTypeMap: { [key: number]: string } = {
  1: '单选题', 2: '多选题', 3: '填空题', 4: '判断题', 5: '主观题',
};

const fetchDetails = async (sId: number) => {
  if (!sId) return;

  // 重置状态
  knowledgePoints.value = [];
  questions.value = [];
  loadingKp.value = true;
  loadingQ.value = true;
  activeTab.value = 'knowledgePoints'; // 默认显示第一个Tab

  try {
    const [kpRes, qRes] = await Promise.all([
      fetchKnowledgePointList({ current: 1, size: 9999, subjectId: sId }),
      fetchQuestionList({ current: 1, size: 9999, subjectId: sId })
    ]);

    if (kpRes.code === 200) {
      knowledgePoints.value = kpRes.data;
    }
    if (qRes.code === 200) {
      questions.value = qRes.data;
    }
    // 如果知识点为空，但试题不为空，则自动切换到试题Tab
    if(knowledgePoints.value.length === 0 && questions.value.length > 0){
      activeTab.value = 'questions';
    }

  } catch (error) {
    ElMessage.error('加载科目详情失败');
  } finally {
    loadingKp.value = false;
    loadingQ.value = false;
  }
};

// 【核心修复】: 将监听对象从 subjectId 改为 visible
// 这样可以确保每次弹窗打开时都触发数据加载
watch(() => props.visible, (isVisible) => {
  if (isVisible && props.subjectId) {
    fetchDetails(props.subjectId);
  }
});

const handleClose = () => {
  emit('update:visible', false);
};
</script>

<style scoped>
/* 可以根据需要添加样式 */
</style>