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
import type {Subject} from "@/api/subject.ts";

// 【核心修改】这里的 props 定义需要保持如下所示
const props = defineProps<{
  visible: boolean;
  subjectData?: Subject; // 属性名应为 subjectData，且是可选的
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
  // 【调试日志 1】: 确认函数被调用
  console.log(`[调试] fetchDetails 开始执行, subjectId: ${sId}`);
  if (!sId) {
    console.log('[调试] subjectId 无效，函数提前返回');
    return;
  }

  // 重置状态
  knowledgePoints.value = [];
  questions.value = [];
  loadingKp.value = true;
  loadingQ.value = true;
  activeTab.value = 'knowledgePoints';

  try {
    // 【调试日志 2】: 确认准备发起API请求
    console.log('[调试] 准备并行获取知识点和试题...');

    const [kpRes, qRes] = await Promise.all([
      fetchKnowledgePointList({ current: 1, size: 9999, subjectId: sId }),
      fetchQuestionList({ current: 1, size: 9999, subjectId: sId })
    ]);

    // 【调试日志 3】: 打印API返回的原始结果
    console.log('[调试] 知识点API原始返回:', kpRes);
    console.log('[调试] 试题API原始返回:', qRes);

    if (kpRes.code === 200) {
      knowledgePoints.value = kpRes.data;
      console.log(`[调试] 已加载 ${kpRes.data.length} 个知识点`);
    }
    if (qRes.code === 200) {
      questions.value = qRes.data;
      console.log(`[调试] 已加载 ${qRes.data.length} 道试题`);
    }

    if(knowledgePoints.value.length === 0 && questions.value.length > 0){
      activeTab.value = 'questions';
    }

  } catch (error) {
    // 【调试日志 4】: 捕获并打印任何在请求过程中发生的错误
    console.error('[调试] fetchDetails 执行出错:', error);
    ElMessage.error('加载科目详情失败，请查看浏览器控制台获取详细错误信息。');
  } finally {
    loadingKp.value = false;
    loadingQ.value = false;
    console.log('[调试] fetchDetails 执行完毕');
  }
};

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    if (props.subjectData) {
      // 编辑模式
      form.value = { ...props.subjectData };
    } else {
      // 新增模式
      form.value = { name: '', description: '', grade: '' };
    }
  } else {
    form.value = null;
    formRef.value?.resetFields();
  }
});

const handleClose = () => {
  emit('update:visible', false);
};
</script>

<style scoped>
/* 样式保持不变 */
</style>