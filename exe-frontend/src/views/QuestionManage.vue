<template>
  <div class="question-manage-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="科目">
          <el-select v-model="queryParams.subjectId" placeholder="请选择科目" clearable @change="handleQuery">
            <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="queryParams.questionType" placeholder="请选择题型" clearable @change="handleQuery">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="填空题" :value="3" />
            <el-option label="判断题" :value="4" />
            <el-option label="主观题" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增试题</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table v-loading="loading" :data="questionList" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="content" label="题干" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100" align="center">
          <template #default="scope">
            <el-tag>{{ questionTypeMap[scope.row.questionType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属科目" width="150">
          <template #default="scope">
            {{ getSubjectName(scope.row.subjectId) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleUpdate(scope.row.id)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          class="pagination"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          @size-change="getList"
          @current-change="getList"
      />
    </el-card>

    <question-edit-dialog
        v-if="isDialogVisible"
        v-model:visible="isDialogVisible"
        :question-id="editingId"
        @success="getList"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchQuestionList, deleteQuestion } from '@/api/question';
import type { Question, QuestionPageParams } from '@/api/question';
import { fetchAllSubjects } from '@/api/subject';
import type { Subject } from '@/api/subject';
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue';
import QuestionEditDialog from '@/components/question/QuestionEditDialog.vue';

const questionList = ref<Question[]>([]);
const allSubjects = ref<Subject[]>([]);
const total = ref(0);
const loading = ref(true);

const isDialogVisible = ref(false);
const editingId = ref<number | undefined>(undefined);

const queryParams = reactive<QuestionPageParams>({
  current: 1,
  size: 10,
  subjectId: undefined,
  questionType: undefined
});

const questionTypeMap = {
  1: '单选题',
  2: '多选题',
  3: '填空题',
  4: '判断题',
  5: '主观题',
};

const getList = async () => {
  loading.value = true;
  try {
    const response = await fetchQuestionList(queryParams);
    questionList.value = response.data;
    total.value = response.total;
  } finally {
    loading.value = false;
  }
};

const getAllSubjects = async () => {
  const res = await fetchAllSubjects();
  if (res.code === 200) {
    allSubjects.value = res.data;
  }
}

const getSubjectName = (subjectId: number) => {
  const subject = allSubjects.value.find(s => s.id === subjectId);
  return subject ? subject.name : '未知科目';
}

const handleQuery = () => {
  queryParams.current = 1;
  getList();
}

const resetQuery = () => {
  queryParams.current = 1;
  queryParams.subjectId = undefined;
  queryParams.questionType = undefined;
  getList();
}

const handleCreate = () => {
  editingId.value = undefined;
  isDialogVisible.value = true;
};

const handleUpdate = (id: number) => {
  editingId.value = id;
  isDialogVisible.value = true;
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除该试题吗?', '提示', { type: 'warning' })
      .then(async () => {
        await deleteQuestion(id);
        ElMessage.success('删除成功');
        getList();
      })
      .catch(() => {});
};

onMounted(() => {
  getAllSubjects().then(() => {
    getList();
  });
});
</script>

<style scoped>
.question-manage-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.table-container {
  margin-top: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>