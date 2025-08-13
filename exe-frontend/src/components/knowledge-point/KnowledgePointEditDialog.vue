<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="所属科目" prop="subjectId">
        <el-select v-model="form.subjectId" placeholder="请选择所属科目" style="width: 100%;">
          <el-option v-for="sub in allSubjects" :key="sub.id" :label="sub.name" :value="sub.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属年级" prop="grade">
        <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%;">
          <el-option label="一年级" value="一年级" />
          <el-option label="二年级" value="二年级" />
          <el-option label="三年级" value="三年级" />
          <el-option label="四年级" value="四年级" />
          <el-option label="五年级" value="五年级" />
          <el-option label="六年级" value="六年级" />
          <el-option label="七年级" value="七年级" />
          <el-option label="八年级" value="八年级" />
          <el-option label="九年级" value="九年级" />
          <el-option label="高一" value="高一" />
          <el-option label="高二" value="高二" />
          <el-option label="高三" value="高三" />
        </el-select>
      </el-form-item>
      <el-form-item label="知识点名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入知识点名称" />
      </el-form-item>
      <el-form-item label="知识点编码" prop="code">
        <el-input v-model="form.code" placeholder="请输入知识点编码" />
      </el-form-item>
      <el-form-item label="标签" prop="tags">
        <el-input v-model="form.tags" placeholder="多个标签请用英文逗号 , 分隔" />
      </el-form-item>
      <el-form-item label="详细描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入详细描述" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { createKnowledgePoint, updateKnowledgePoint, fetchKnowledgePointById } from '@/api/knowledgePoint';
import type { KnowledgePoint } from '@/api/knowledgePoint';
import { fetchAllSubjects } from '@/api/subject'; // 我们需要一个获取所有科目的接口
import type { Subject } from '@/api/subject';

const props = defineProps<{
  visible: boolean;
  knowledgePointId?: number;
}>();

const emit = defineEmits(['update:visible', 'success']);

const formRef = ref<FormInstance>();
const form = ref<Partial<KnowledgePoint> | null>(null);
const allSubjects = ref<Subject[]>([]);

const dialogTitle = computed(() => (props.knowledgePointId ? '编辑知识点' : '新增知识点'));

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    // 获取所有科目用于下拉选择
    if (allSubjects.value.length === 0) {
      fetchAllSubjects().then(res => {
        if (res.code === 200) allSubjects.value = res.data;
      });
    }

    if (props.knowledgePointId) {
      // 编辑：加载现有数据
      fetchKnowledgePointById(props.knowledgePointId).then(res => {
        form.value = res.data;
      });
    } else {
      // 新增：初始化空表单
      form.value = {
        name: '',
        subjectId: undefined,
        code: '',
        description: '',
        grade: '', // <-- 新增
        tags: '',
        remark: ''
      };
    }
  } else {
    form.value = null;
    formRef.value?.resetFields();
  }
});

const rules = reactive<FormRules>({
  subjectId: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
  name: [{ required: true, message: '请输入知识点名称', trigger: 'blur' }],
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
          // 更新
          await updateKnowledgePoint(form.value.id, form.value);
          ElMessage.success('更新成功');
        } else {
          // 创建
          await createKnowledgePoint(form.value!);
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