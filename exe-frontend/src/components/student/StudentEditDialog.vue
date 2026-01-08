<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      :width="props.studentData?.id ? '900px' : '600px'"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <!-- 【知识点功能增强】当编辑已有学生时，显示Tab切换 -->
    <el-tabs v-if="props.studentData?.id" v-model="activeTab" type="border-card">
      <el-tab-pane label="基本信息" name="basic">
        <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入学生姓名" />
          </el-form-item>
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%;">
              <el-option v-for="g in GRADE_OPTIONS" :key="g" :label="g" :value="g" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级" prop="className">
            <el-input v-model="form.className" placeholder="例如: 1班" />
          </el-form-item>
          <el-form-item label="联系方式" prop="contact">
            <el-input v-model="form.contact" placeholder="请输入联系方式" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 【知识点功能增强】知识点掌握度Tab -->
      <el-tab-pane label="知识点掌握度" name="mastery">
        <knowledge-mastery-radar :student-id="props.studentData.id" :subjects="availableSubjects" />
      </el-tab-pane>
    </el-tabs>

    <!-- 新建学生时不显示Tabs，直接显示表单 -->
    <el-form v-else-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入学生姓名" />
      </el-form-item>
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="form.studentNo" placeholder="请输入学号" />
      </el-form-item>
      <el-form-item label="年级" prop="grade">
        <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%;">
          <el-option v-for="g in GRADE_OPTIONS" :key="g" :label="g" :value="g" />
        </el-select>
      </el-form-item>
      <el-form-item label="班级" prop="className">
        <el-input v-model="form.className" placeholder="例如: 1班" />
      </el-form-item>
      <el-form-item label="联系方式" prop="contact">
        <el-input v-model="form.contact" placeholder="请输入联系方式" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createStudent, updateStudent } from '@/api/student';
import type { Student } from '@/api/student';
import type { Subject } from '@/api/subject';
import { getSubjectList } from '@/api/subject';
import { GRADE_OPTIONS } from '@/utils/constants'; // 引入年级常量
import KnowledgeMasteryRadar from './KnowledgeMasteryRadar.vue';

const props = defineProps<{
  visible: boolean;
  studentData?: Student;
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
const form = ref<Partial<Student> | null>(null);
const activeTab = ref('basic');
const availableSubjects = ref<Array<{ id: number; name: string }>>([]);

const dialogTitle = computed(() => (props.studentData?.id ? '编辑学生' : '新增学生'));

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    if (props.studentData) {
      form.value = { ...props.studentData };
    } else {
      form.value = {
        name: '',
        studentNo: '',
        grade: '',
        className: '',
        contact: ''
      };
    }
  } else {
    form.value = null;
    formRef.value?.resetFields();
  }
});

const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在 2-20 个字符之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5a-zA-Z\s]+$/, message: '姓名只能包含中文、英文和空格', trigger: 'blur' }
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 4, max: 20, message: '学号长度应在 4-20 个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '学号只能包含字母和数字', trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ],
  className: [
    { required: true, message: '请输入班级', trigger: 'blur' },
    { min: 1, max: 20, message: '班级名称长度应在 1-20 个字符之间', trigger: 'blur' }
  ],
  contact: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码（11位数字，以1开头）',
      trigger: 'blur'
    }
  ]
});

const handleClose = () => {
  emit('update:visible', false);
};

const submitForm = async () => {
  if (!formRef.value || !form.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value?.id) {
          await updateStudent(form.value.id, form.value);
          ElMessage.success('更新成功');
        } else {
          await createStudent(form.value!);
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

// 加载科目列表
const loadSubjects = async () => {
  try {
    const res = await getSubjectList();
    if (res.code === 200 && res.data) {
      availableSubjects.value = res.data.map((subject: Subject) => ({
        id: subject.id,
        name: subject.name
      }));
    }
  } catch (error) {
    console.error('获取科目列表失败:', error);
  }
};

// 组件挂载时加载科目列表
onMounted(() => {
  loadSubjects();
});
</script>