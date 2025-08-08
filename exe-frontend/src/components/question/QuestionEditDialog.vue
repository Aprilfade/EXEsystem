<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="800px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属科目" prop="subjectId">
            <el-select v-model="form.subjectId" placeholder="请选择科目" style="width: 100%;" @change="handleSubjectChange">
              <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="题型" prop="questionType">
            <el-select v-model="form.questionType" placeholder="请选择题型" style="width: 100%;">
              <el-option label="单选题" :value="1" />
              <el-option label="多选题" :value="2" />
              <el-option label="填空题" :value="3" />
              <el-option label="判断题" :value="4" />
              <el-option label="主观题" :value="5" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="关联知识点" prop="knowledgePointIds">
        <el-select v-model="form.knowledgePointIds" multiple filterable placeholder="请选择知识点" style="width: 100%;">
          <el-option v-for="kp in availableKnowledgePoints" :key="kp.id" :label="kp.name" :value="kp.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="题干内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入题干内容" />
      </el-form-item>

      <el-form-item label="题目图片" prop="imageUrl">
        <el-upload
            class="question-uploader"
            action="/api/v1/files/upload"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            :headers="{ 'Authorization': 'Bearer ' + authStore.token }"
        >
          <img v-if="form.imageUrl" :src="form.imageUrl" class="question-image" />
          <el-icon v-else class="uploader-icon"><Plus /></el-icon>
        </el-upload>
        <el-button v-if="form.imageUrl" type="danger" link @click="removeImage" style="margin-left: 10px;">删除图片</el-button>
      </el-form-item>



      <template v-if="form.questionType === 1 || form.questionType === 2">
        <el-form-item label="选项设置" prop="options">
          <div v-for="(option, index) in localOptions" :key="index" style="display: flex; align-items: center; margin-bottom: 8px;">
            <el-input v-model="option.value" placeholder="请输入选项内容">
              <template #prepend>{{ option.key }}</template>
            </el-input>
            <el-button type="danger" :icon="Delete" circle plain @click="removeOption(index)" style="margin-left: 10px;"></el-button>
          </div>
          <el-button type="primary" link @click="addOption">添加选项</el-button>
        </el-form-item>
        <el-form-item label="正确答案" prop="answer">
          <el-radio-group v-if="form.questionType === 1" v-model="form.answer">
            <el-radio v-for="option in localOptions" :key="option.key" :label="option.key">{{ option.key }}</el-radio>
          </el-radio-group>
          <el-checkbox-group v-if="form.questionType === 2" v-model="localAnswerArray">
            <el-checkbox v-for="option in localOptions" :key="option.key" :label="option.key">{{ option.key }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </template>

      <template v-if="form.questionType === 4">
        <el-form-item label="正确答案" prop="answer">
          <el-radio-group v-model="form.answer">
            <el-radio label="T">正确</el-radio>
            <el-radio label="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>

      <template v-if="form.questionType === 3 || form.questionType === 5">
        <el-form-item label="参考答案" prop="answer">
          <el-input v-model="form.answer" type="textarea" :rows="3" placeholder="请输入参考答案，填空题多个答案请用 ### 分隔" />
        </el-form-item>
      </template>
      <el-form-item label="题目解析" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入题目解析" />
      </el-form-item>

      <el-form-item label="解析图片" prop="answerImageUrl">
        <el-upload
            class="question-uploader"
            action="/api/v1/files/upload"
            :show-file-list="false"
            :on-success="handleAnswerImageSuccess"
            :before-upload="beforeImageUpload"
            :headers="{ 'Authorization': 'Bearer ' + authStore.token }"
        >
          <img v-if="form.answerImageUrl" :src="form.answerImageUrl" class="question-image" />
          <el-icon v-else class="uploader-icon"><Plus /></el-icon>
        </el-upload>
        <el-button v-if="form.answerImageUrl" type="danger" link @click="removeAnswerImage" style="margin-left: 10px;">删除图片</el-button>
      </el-form-item>



    </el-form>
    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue';
import type { FormInstance, FormRules, UploadProps } from 'element-plus'; // 引入 UploadProps
import { ElMessage } from 'element-plus';
import { Delete, Plus } from '@element-plus/icons-vue'; // 【修复】同时导入 Delete 和 Plus 图标
import { createQuestion, updateQuestion, fetchQuestionById } from '@/api/question';
import type { Question, QuestionOption } from '@/api/question';
import { fetchAllSubjects, type Subject } from '@/api/subject';
import { fetchKnowledgePointList, type KnowledgePoint } from '@/api/knowledgePoint';
import { useAuthStore } from '@/stores/auth'; // 【修复】导入 authStore

const authStore = useAuthStore(); // 【修复】获取 authStore 实例，使其在模板中可用

const props = defineProps<{
  visible: boolean;
  questionId?: number;
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
const form = ref<Partial<Question> | null>(null);
const allSubjects = ref<Subject[]>([]);
const availableKnowledgePoints = ref<KnowledgePoint[]>([]);

const localOptions = ref<QuestionOption[]>([]);
const localAnswerArray = ref<string[]>([]);

const dialogTitle = computed(() => (props.questionId ? '编辑试题' : '新增试题'));


// 【新增】处理答案图片上传成功
const handleAnswerImageSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    if (form.value) {
      form.value.answerImageUrl = response.data;
    }
  } else {
    ElMessage.error(response.msg || '图片上传失败');
  }
}

// 【新增】移除答案图片
const removeAnswerImage = () => {
  if (form.value) {
    form.value.answerImageUrl = '';
  }
}





// --- 【新增】处理图片上传成功 ---
const handleImageSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    if (form.value) {
      form.value.imageUrl = response.data;
    }
  } else {
    ElMessage.error(response.msg || '图片上传失败');
  }
}

// --- 【新增】图片上传前的校验 ---
const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('图片只能是 JPG 或 PNG 格式!');
    return false;
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
}

// --- 【新增】移除图片 ---
const removeImage = () => {
  if (form.value) {
    form.value.imageUrl = '';
  }
}


// --- 下面的代码保持不变 ---
const loadInitialData = async () => {
  if (allSubjects.value.length === 0) {
    const res = await fetchAllSubjects();
    if (res.code === 200) allSubjects.value = res.data;
  }
};

const handleSubjectChange = async (subjectId: number | undefined) => {
  if(!subjectId) {
    availableKnowledgePoints.value = [];
    if(form.value) form.value.knowledgePointIds = [];
    return;
  }
  const res = await fetchKnowledgePointList({ current: 1, size: 999, subjectId: subjectId });
  if(res.code === 200) {
    availableKnowledgePoints.value = res.data;
  }
  if(form.value && form.value.knowledgePointIds) form.value.knowledgePointIds = [];
}

const parseOptionsAndAnswer = (data: Question) => {
  if(data.questionType === 1 || data.questionType === 2) {
    localOptions.value = typeof data.options === 'string' ? JSON.parse(data.options) : (data.options || []);
  }
  if(data.questionType === 2) {
    localAnswerArray.value = data.answer ? data.answer.split(',') : [];
  }
}

const resetForm = () => {
  nextTick(() => {
    form.value = {
      subjectId: undefined,
      questionType: 1,
      knowledgePointIds: [],
      content: '',
      imageUrl: '', // 确保 imageUrl 被初始化
      answerImageUrl: '', // 【新增】
      options: [],
      answer: '',
      description: ''
    };
    localOptions.value = [
      { key: 'A', value: '' }, { key: 'B', value: '' },
      { key: 'C', value: '' }, { key: 'D', value: '' },
    ];
    localAnswerArray.value = [];
    availableKnowledgePoints.value = [];
  });
};

const loadQuestionData = async (id: number) => {
  const res = await fetchQuestionById(id);
  if (res.code === 200) {
    form.value = res.data;
    if(form.value.subjectId) {
      await handleSubjectChange(form.value.subjectId);
    }
    parseOptionsAndAnswer(res.data);
  }
};

watch(() => props.visible, async (isVisible) => {
  if (isVisible) {
    await loadInitialData();
    if (props.questionId) {
      await loadQuestionData(props.questionId);
    } else {
      resetForm();
    }
  } else {
    form.value = null;
    formRef.value?.resetFields();
  }
}, { immediate: true });

watch(() => form.value?.questionType, (newType, oldType) => {
  if (form.value && newType !== oldType) {
    form.value.answer = '';
    localAnswerArray.value = [];
    if (form.value.questionType !== 1 && form.value.questionType !== 2){
      localOptions.value = [];
    }
    if ((newType === 1 || newType === 2) && localOptions.value.length === 0) {
      localOptions.value = [
        { key: 'A', value: '' }, { key: 'B', value: '' },
        { key: 'C', value: '' }, { key: 'D', value: '' },
      ];
    }
  }
});

watch(localAnswerArray, (newVal) => {
  if (form.value && form.value.questionType === 2) {
    form.value.answer = newVal.sort().join(',');
  }
});

const rules = reactive<FormRules>({
  subjectId: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请提供答案', trigger: 'blur' }],
});

const handleClose = () => emit('update:visible', false);

const addOption = () => {
  if (localOptions.value.length >= 26) {
    ElMessage.warning('最多只能添加26个选项');
    return;
  }
  const nextChar = String.fromCharCode(65 + localOptions.value.length);
  localOptions.value.push({ key: nextChar, value: '' });
}

const removeOption = (index: number) => {
  localOptions.value.splice(index, 1);
  localOptions.value.forEach((option, i) => {
    option.key = String.fromCharCode(65 + i);
  });
}

const submitForm = () => {
  if (!formRef.value || !form.value) return;
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = { ...form.value };
        if(formData.questionType === 1 || formData.questionType === 2) {
          formData.options = JSON.stringify(localOptions.value);
        } else {
          formData.options = null;
        }

        if (formData.id) {
          await updateQuestion(formData.id, formData);
          ElMessage.success('更新成功');
        } else {
          await createQuestion(formData);
          ElMessage.success('新增成功');
        }
        emit('success');
        handleClose();
      } catch (error) {
        console.error('操作失败:', error);
      }
    }
  });
};
</script>


<style>
.question-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: .2s;
}

.question-uploader .el-upload:hover {
  border-color: #409EFF;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.question-image {
  width: 178px;
  height: 178px;
  display: block;
}
</style>