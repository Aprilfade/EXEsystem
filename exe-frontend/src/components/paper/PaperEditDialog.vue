<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="80%"
      @close="handleClose"
      :close-on-click-modal="false"
      custom-class="resizable-paper-dialog"
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
              <el-option v-for="sub in subjects" :key="sub.id" :label="`${sub.name} (${sub.grade})`" :value="sub.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="所属年级" prop="grade">
            <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%;">
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
        <el-col :span="8">
          <el-form-item label="发布状态" prop="status">
            <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="立即发布"
                inactive-text="存为草稿"
                inline-prompt
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="试卷描述" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入试卷描述" />
      </el-form-item>
    </el-form>

    <el-divider />

    <div v-if="form.paperType === 1">
      <div class="smart-entry-bar" style="margin-bottom: 15px; display: flex; align-items: center; justify-content: space-between; background: #f0f9eb; padding: 10px; border-radius: 4px; border: 1px solid #e1f3d8;">
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon color="#67C23A"><MagicStick /></el-icon>
          <span style="font-size: 14px; color: #606266;">想快速创建试卷？试试智能生成功能。</span>
        </div>
        <el-button type="success" size="small" @click="openSmartDialog">
          <el-icon style="margin-right: 4px"><MagicStick /></el-icon> 一键智能组卷
        </el-button>
      </div>
      <paper-question-manager
          v-if="form.paperType === 1 && form.subjectId"
          :paper-groups="form.groups"
          :subject-id="form.subjectId"
          @update:groups="updateGroups"
      />
      <el-alert v-else title="请先选择一个科目来管理试题" type="info" show-icon :closable="false" />
    </div>
    <el-dialog v-model="showSmartDialog" title="智能组卷配置" width="480px" append-to-body>
      <el-form :model="smartForm" label-width="100px">
        <el-alert title="系统将根据当前选择的科目和年级随机抽取题目" type="info" :closable="false" style="margin-bottom: 20px;" />

        <el-form-item label="单选题数量">
          <el-input-number v-model="smartForm.singleCount" :min="0" :max="50" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="多选题数量">
          <el-input-number v-model="smartForm.multiCount" :min="0" :max="20" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="判断题数量">
          <el-input-number v-model="smartForm.judgeCount" :min="0" :max="20" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="填空题数量">
          <el-input-number v-model="smartForm.fillCount" :min="0" :max="20" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="主观题数量">
          <el-input-number v-model="smartForm.subjectiveCount" :min="0" :max="10" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSmartDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSmartGenerate" :loading="generating">开始生成</el-button>
      </template>
    </el-dialog>

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
import { MagicStick } from '@element-plus/icons-vue'; // 引入图标
import { generateSmartPaper, type SmartPaperReq } from '@/api/paper'; // 引入API
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
const form = ref<Partial<Paper>>({ name: '', subjectId: undefined, description: '', paperType: 1, groups: [], paperImages: [],status: 0 });
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
      // === 【新增】第4点：提交前的数据校验 ===
      if (form.value.paperType === 1) {
        // 1. 检查是否至少添加了一个题目
        // 逻辑：遍历所有分组，只要有一个分组里的 questions 数组长度大于 0，就视为有效
        const hasQuestions = form.value.groups?.some(g => g.questions && g.questions.length > 0);

        if (!hasQuestions) {
          ElMessage.warning('试卷不能为空，请至少添加一道试题！');
          return; // 阻止提交
        }

        // 2. 【修复总分计算Bug】：必须遍历 groups 来累加分数
        form.value.totalScore = form.value.groups?.reduce((total, group) => {
          const groupScore = group.questions?.reduce((sum, q) => sum + (q.score || 0), 0) || 0;
          return total + groupScore;
        }, 0) || 0;

      } else {
        // 图片试卷的处理逻辑保持不变
        form.value.totalScore = 100;
        form.value.paperImages?.forEach((image, index) => {
          image.sortOrder = index;
        });
      }
      // === 校验结束 ===

      try {
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
// --- 智能组卷相关变量 ---
const showSmartDialog = ref(false);
const generating = ref(false);
const smartForm = reactive<SmartPaperReq>({
  subjectId: 0, // 这里的初始值无所谓，打开弹窗时会覆盖
  singleCount: 5,
  multiCount: 2,
  judgeCount: 2,
  fillCount: 2,
  subjectiveCount: 1
});

// 打开智能组卷弹窗
const openSmartDialog = () => {
  if (!form.value.subjectId) {
    ElMessage.warning('请先在上方选择所属科目！');
    return;
  }
  // 同步当前的科目和年级
  smartForm.subjectId = form.value.subjectId;
  smartForm.grade = form.value.grade;
  showSmartDialog.value = true;
};

// 执行智能生成
const handleSmartGenerate = async () => {
  generating.value = true;
  try {
    const res = await generateSmartPaper(smartForm);
    if (res.code === 200) {
      // 核心：直接替换当前的 groups
      form.value.groups = res.data;

      // 自动计算一下总分 (利用我们之前修复的逻辑)
      const newTotalScore = res.data.reduce((total, group) => {
        return total + (group.questions?.reduce((sum, q) => sum + (q.score || 0), 0) || 0);
      }, 0);
      form.value.totalScore = newTotalScore;

      ElMessage.success(`智能组卷成功！共生成 ${res.data.length} 个分组。`);
      showSmartDialog.value = false;
    }
  } catch (error) {
    console.error(error);
  } finally {
    generating.value = false;
  }
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
        paperImages: [],
        status: 0
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