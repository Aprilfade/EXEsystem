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
          v-if="form.paperType === 1 && form.subjectId"
          :paper-groups="form.groups"
          :subject-id="form.subjectId"
          @update:groups="updateGroups"
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
          :show-file-list="false"
          style="margin-bottom: 20px;"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
      </el-upload>

      <div
          class="image-list-container"
          @dragover.prevent
          @drop="onDrop"
      >
        <div
            v-for="(image, index) in form.paperImages"
            :key="image.imageUrl"
            class="image-item"
            draggable="true"
            @dragstart="onDragStart(index)"
            @dragend="onDragEnd"
            :class="{ 'dragging': draggingIndex === index }"
        >
          <img :src="image.imageUrl" class="preview-image" />
          <div class="image-actions">
            <el-button type="danger" :icon="Delete" circle @click="removeImage(index)"></el-button>
          </div>
        </div>
        <div v-if="!form.paperImages || form.paperImages.length === 0" class="empty-placeholder">
          暂无图片，请从上方上传
        </div>
      </div>
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
import type { Paper, PaperQuestion, PaperGroup, PaperImage } from '@/api/paper'; // 确保 PaperGroup 已导入
import type { Subject } from '@/api/subject';
import PaperQuestionManager from './PaperQuestionManager.vue';
import { useAuthStore } from '@/stores/auth';
import { UploadFilled, Delete } from '@element-plus/icons-vue';



const props = defineProps<{
  visible: boolean;
  paperId?: number;
  subjects: Subject[];
}>();

const emit = defineEmits(['update:visible', 'success']);
const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const form = ref<Partial<Paper>>({ name: '', subjectId: undefined, description: '', paperType: 1, groups: [], paperImages: [] });
const fileList = ref<UploadUserFile[]>([]);

const dialogTitle = computed(() => (props.paperId ? '编辑试卷' : '新增试卷'));

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
  paperType: [{ required: true, message: '请选择组卷方式', trigger: 'change' }],
});

const updateGroups = (newGroups: PaperGroup[]) => {
  form.value.groups = newGroups;
}

const handleImageSuccess: UploadProps['onSuccess'] = (response, uploadFile, uploadFiles) => {
  if(response.code === 200) {
    if (!form.value.paperImages) {
      form.value.paperImages = [];
    }
    const newImage: PaperImage = {
      paperId: form.value.id || 0,
      imageUrl: response.data,
      sortOrder: form.value.paperImages.length
    };
    form.value.paperImages.push(newImage);

    // 更新 fileList 以匹配 paperImages
    fileList.value = uploadFiles;
  }
}


const handleImageRemove: UploadProps['onRemove'] = (uploadFile, uploadFiles) => {
  if (form.value.paperImages) {
    form.value.paperImages = form.value.paperImages.filter(img => img.imageUrl !== uploadFile.url);
  }
  fileList.value = uploadFiles;
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
          form.value.paperImages?.forEach((image, index) => {
            image.sortOrder = index;
          });
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
// 【新增】用于处理拖拽状态的 ref
const draggingIndex = ref<number | null>(null);

// 【新增】拖拽开始事件
const onDragStart = (index: number) => {
  draggingIndex.value = index;
};

// 【新增】拖拽结束事件，用于清除样式
const onDragEnd = () => {
  draggingIndex.value = null;
};

// 【新增】放置事件，核心排序逻辑
const onDrop = (event: DragEvent) => {
  event.preventDefault();
  if (draggingIndex.value === null) return;

  const targetElement = (event.target as HTMLElement).closest('.image-item');
  if (!targetElement) return;

  const allItems = Array.from(targetElement.parentElement?.children || []);
  const dropIndex = allItems.indexOf(targetElement);

  if (dropIndex === -1) return;

  // 移动数组元素
  const draggedItem = form.value.paperImages!.splice(draggingIndex.value, 1)[0];
  form.value.paperImages!.splice(dropIndex, 0, draggedItem);

  onDragEnd(); // 清理状态
};

// 【新增】一个更健壮的移除图片方法
const removeImage = (index: number) => {
  form.value.paperImages?.splice(index, 1);
  // 同时也要更新 fileList 来确保 el-upload 状态同步（如果需要的话）
  fileList.value.splice(index, 1);
};



watchEffect(async () => {
  if (props.visible) {
    if (props.paperId) {
      // 编辑模式
      try {
        const res = await fetchPaperById(props.paperId);
        form.value = res.data;

        // 【关键修复 A】: 确保 editing 模式下 groups 始终是一个数组，防止API返回null
        if (!form.value.groups) {
          form.value.groups = [];
        }

        // 图片列表回显逻辑 (保持不变)
        if (res.data.paperType === 2 && res.data.paperImages) {
          fileList.value = res.data.paperImages.map(img => ({
            name: img.imageUrl.substring(img.imageUrl.lastIndexOf('/') + 1),
            url: img.imageUrl,
            uid: img.id || 0
          }));
        } else {
          fileList.value = [];
        }

      } catch (e) {
        console.error("加载试卷数据失败:", e);
        // 出错时也应重置表单
        form.value = { name: '', subjectId: undefined, description: '', paperType: 1, groups: [], paperImages: [] };
        fileList.value = [];
      }
    } else {
      // 【关键修复 B】: 新增模式下，正确地初始化带 groups 的表单
      form.value = {
        name: '',
        subjectId: undefined,
        description: '',
        paperType: 1,
        grade: '', // <-- 新增
        groups: [], // <-- 必须是 groups: []
        paperImages: []
      };
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


</script>

<style scoped>
.image-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  padding: 10px;
  min-height: 150px;
}

.image-item {
  position: relative;
  width: 148px;
  height: 148px;
  border: 1px solid #c0ccda;
  border-radius: 6px;
  overflow: hidden;
  cursor: grab;
  transition: transform 0.2s;
}

.image-item.dragging {
  opacity: 0.5;
  transform: scale(0.95);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.empty-placeholder {
  width: 100%;
  text-align: center;
  color: #a8abb2;
  align-self: center;
}
</style>