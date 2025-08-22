<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入学生姓名" />
      </el-form-item>
      <el-form-item label="学号" prop="studentNo">
        <el-input v-model="form.studentNo" placeholder="请输入学号" />
      </el-form-item>
      <el-form-item label="所属科目" prop="subjectId">
        <el-select v-model="form.subjectId" placeholder="请选择所属科目" style="width: 100%;">
          <el-option v-for="sub in allSubjects" :key="sub.id" :label="`${sub.name} (${sub.grade})`" :value="sub.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="年级" prop="grade">
        <el-input v-model="form.grade" placeholder="例如: 2009级" />
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
import { ref, reactive, computed, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createStudent, updateStudent } from '@/api/student';
import type { Student } from '@/api/student';
import type { Subject } from '@/api/subject';

const props = defineProps<{
  visible: boolean;
  studentData?: Student;
  subjects: Subject[];
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
const form = ref<Partial<Student> | null>(null);
const allSubjects = ref<Subject[]>([]);

const dialogTitle = computed(() => (props.studentData?.id ? '编辑学生' : '新增学生'));

watch(() => props.visible, (isVisible) => {
  allSubjects.value = props.subjects;
  if (isVisible) {
    if (props.studentData) {
      form.value = { ...props.studentData };
    } else {
      form.value = {
        name: '',
        studentNo: '',
        subjectId: undefined,
        grade: '',
        contact: ''
      };
    }
  } else {
    form.value = null;
    formRef.value?.resetFields();
  }
});

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
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
</script>