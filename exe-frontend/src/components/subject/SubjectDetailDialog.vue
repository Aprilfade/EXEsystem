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

// 【重要修改】拆分API调用并增加详细日志
const fetchDetails = async (sId: number) => {
  console.log(`[调试] 1. fetchDetails 开始执行, subjectId: ${sId}`);
  if (!sId) {
    console.error('[调试] 错误：subjectId 无效，函数提前返回');
    return;
  }

  // 重置状态
  knowledgePoints.value = [];
  questions.value = [];
  loadingKp.value = true;
  loadingQ.value = true;
  activeTab.value = 'knowledgePoints';

  // --- 知识点获取 ---
  try {
    console.log(`[调试] 2. 准备获取知识点, subjectId: ${sId}`);
    const kpRes = await fetchKnowledgePointList({ current: 1, size: 9999, subjectId: sId });
    console.log('[调试] 3. 知识点API原始返回:', kpRes);
    if (kpRes.code === 200) {
      knowledgePoints.value = kpRes.data;
      console.log(`[调试] 4. 成功加载 ${kpRes.data.length} 个知识点`);
    } else {
      ElMessage.error('加载知识点失败: ' + kpRes.msg);
    }
  } catch (error) {
    console.error('[调试] 错误：获取知识点时发生异常:', error);
    ElMessage.error('加载知识点时发生网络或脚本错误');
  } finally {
    loadingKp.value = false;
  }

  // --- 试题获取 ---
  try {
    console.log(`[调试] 5. 准备获取试题, subjectId: ${sId}`);
    const qRes = await fetchQuestionList({ current: 1, size: 9999, subjectId: sId });
    console.log('[调试] 6. 试题API原始返回:', qRes);
    if (qRes.code === 200) {
      questions.value = qRes.data;
      console.log(`[调试] 7. 成功加载 ${qRes.data.length} 道试题`);
    } else {
      ElMessage.error('加载关联试题失败: ' + qRes.msg);
    }
  } catch (error) {
    console.error('[调试] 错误：获取试题时发生异常:', error);
    ElMessage.error('加载关联试题时发生网络或脚本错误');
  } finally {
    loadingQ.value = false;
  }

  // 切换Tab逻辑
  if(knowledgePoints.value.length === 0 && questions.value.length > 0){
    activeTab.value = 'questions';
  }
  console.log('[调试] 8. fetchDetails 执行完毕');
};

watch(() => props.visible, (isVisible) => {
  console.log(`[调试] 0. 'visible' 状态改变为: ${isVisible}, subjectId 为: ${props.subjectId}`);
  if (isVisible && props.subjectId) {
    fetchDetails(props.subjectId);
  }
});

const handleClose = () => {
  emit('update:visible', false);
};
</script>

<style scoped>
/* 样式保持不变 */
</style>