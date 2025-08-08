<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="80%"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="试卷名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入试卷名称" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="所属科目" prop="subjectId">
            <el-select v-model="form.subjectId" placeholder="请选择科目" style="width: 100%;">
              <el-option v-for="sub in props.subjects" :key="sub.id" :label="sub.name" :value="sub.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="组卷方式" prop="paperType">
            <el-radio-group v-model="form.paperType" :disabled="!!props.paperId">
              <el-radio :label="1">手动选题</el-radio>
              <el-radio :label="2">图片拼接</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="试卷描述" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入试卷描述" />
      </el-form-item>
    </el-form>

    <el-divider />

    <div v-if="form.paperType === 1">
      <paper-question-manager
          v-if="form.subjectId"
          :paper-questions="form.questions"
          :subject-id="form.subjectId"
          @update:questions="updateQuestions"
      />
      <el-alert v-else title="请先选择一个科目来管理试题" type="info" show-icon :closable="false" />
    </div>

    <div v-if="form.paperType === 2">
      <el-upload
          drag
          multiple
          action="/api/v1/files/upload"
          :headers="{ 'Authorization': 'Bearer ' + authStore.token }"
          :on-success="handleImageSuccess"
          :file-list="fileList"
          :on-remove="handleImageRemove"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
      </el-upload>
    </div>


    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watchEffect, watch } from 'vue';
import type { FormInstance, FormRules, UploadUserFile, UploadProps } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createPaper, updatePaper, fetchPaperById } from '@/api/paper';
import type { Paper, PaperQuestion, PaperImage } from '@/api/paper';
import type { Subject } from '@/api/subject';
import PaperQuestionManager from './PaperQuestionManager.vue';
import { useAuthStore } from '@/stores/auth';
import { UploadFilled } from '@element-plus/icons-vue';

const props = defineProps<{
  visible: boolean;
  paperId?: number;
  subjects: Subject[];
}>();

const emit = defineEmits(['update:visible', 'success']);
const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const form = ref<Partial<Paper>>({ name: '', subjectId: undefined, description: '', paperType: 1, questions: [], paperImages: [] });
const fileList = ref<UploadUserFile[]>([]);

const dialogTitle = computed(() => (props.paperId ? '编辑试卷' : '新增试卷'));

watchEffect(async () => {
  if (props.visible) {
    if (props.paperId) {
      try {
        const res = await fetchPaperById(props.paperId);
        form.value = res.data;
        // 如果是图片试卷，回显图片列表
        if (res.data.paperType === 2 && res.data.paperImages) {
          fileList.value = res.data.paperImages.map(img => ({
            name: img.imageUrl.substring(img.imageUrl.lastIndexOf('/') + 1),
            url: img.imageUrl,
            uid: img.id || 0 // 确保有uid
          }));
        }
      } catch (e) {
        // ...
      }
    } else {
      // 新增模式
      form.value = { name: '', subjectId: undefined, description: '', paperType: 1, questions: [], paperImages: [] };
      fileList.value = [];
    }
  }
});

// 监听组卷方式变化，清空另一种方式的数据
watch(() => form.value.paperType, (newType) => {
  if (newType === 1) {
    form.value.paperImages = [];
    fileList.value = [];
  } else {
    form.value.questions = [];
  }
});


const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
  paperType: [{ required: true, message: '请选择组卷方式', trigger: 'change' }],
});

const updateQuestions = (newQuestions: PaperQuestion[]) => {
  form.value.questions = newQuestions;
}

const handleImageSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  if(response.code === 200) {
    if (!form.value.paperImages) {
      form.value.paperImages = [];
    }
    form.value.paperImages.push({
      paperId: form.value.id || 0,
      imageUrl: response.data,
      sortOrder: form.value.paperImages.length
    });
    // 更新fileList以正确显示
    const file = fileList.value.find(f => f.uid === uploadFile.uid);
    if (file) {
      file.url = response.data;
    }
  }
}

const handleImageRemove: UploadProps['onRemove'] = (uploadFile) => {
  if (form.value.paperImages) {
    form.value.paperImages = form.value.paperImages.filter(img => img.imageUrl !== uploadFile.url);
  }
}


const handleClose = () => emit('update:visible', false);

const submitForm = async () => {
  if (!formRef.value || !form.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if(form.value.paperType === 1) {
          form.value.totalScore = form.value.questions?.reduce((sum, q) => sum + (q.score || 0), 0);
        } else {
          form.value.totalScore = 100; // 图片试卷可以给一个默认分
        }

        if (form.value.id) {
          await updatePaper(form.value.id, form.value);
          ElMessage.success('更新成功');
        } else {
          await createPaper(form.value);
          ElMessage.success('新增成功');
        }
        emit('success');
        handleClose();
      } catch (error) {
        console.error('操作失败:', error);
        ElMessage.error("操作失败，请重试");
      }
    }
  });
};
</script>