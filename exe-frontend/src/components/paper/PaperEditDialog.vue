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

      <!-- ✅ 新增：实时显示试卷统计信息 -->
      <el-row :gutter="20" v-if="form.paperType === 1" style="margin-bottom: 20px;">
        <el-col :span="12">
          <el-form-item label="试卷总分">
            <el-tag type="success" size="large" effect="dark" style="font-size: 16px; padding: 10px 20px;">
              <el-icon><Trophy /></el-icon>
              {{ totalScore }} 分
            </el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="题目总数">
            <el-tag type="info" size="large" style="font-size: 16px; padding: 10px 20px;">
              <el-icon><Document /></el-icon>
              {{ totalQuestions }} 道
            </el-tag>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="试卷描述" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入试卷描述" />
      </el-form-item>
    </el-form>

    <el-divider />

    <!-- 【知识点功能增强】当编辑已有试卷时，显示Tab切换 -->
    <el-tabs v-if="props.paperId" v-model="activeTab" type="border-card">
      <el-tab-pane label="试卷内容" name="content">
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
      </el-tab-pane>

      <!-- 【知识点功能增强】知识点分布Tab -->
      <el-tab-pane label="知识点分布" name="knowledge">
        <paper-knowledge-point-chart :paper-id="props.paperId" />
      </el-tab-pane>
    </el-tabs>

    <!-- 新建试卷时不显示Tabs，直接显示内容 -->
    <div v-else>
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
    </div>

    <el-dialog v-model="showSmartDialog" title="智能组卷配置" width="480px" append-to-body>
      <el-form :model="smartForm" label-width="100px">
        <el-alert title="系统将根据当前选择的科目和年级，结合遗传算法生成最优试卷" type="info" :closable="false" style="margin-bottom: 20px;" />

        <el-form-item label="期望难度">
          <div style="width: 90%; padding-left: 6px;"> <el-slider
              v-model="smartForm.targetDifficulty"
              :min="0.1"
              :max="1.0"
              :step="0.1"
              show-stops
              :marks="difficultyMarks"
          />
          </div>
        </el-form-item>
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
        <el-button type="primary" @click="handleSmartGenerate" :loading="generating">
          开始进化生成
        </el-button>
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

      <draggable
          v-model="form.paperImages"
          item-key="imageUrl"
          class="image-list-container"
          animation="300"
          ghost-class="ghost"
          @end="handleImageSort"
      >
        <template #item="{ element: image, index }">
          <div class="image-item">
            <el-image
                :src="image.imageUrl"
                class="preview-image"
                fit="cover"
                :preview-src-list="form.paperImages?.map(i => i.imageUrl)"
                :initial-index="index"
                preview-teleported
            />
            <div class="image-actions">
              <el-button type="danger" :icon="Delete" circle @click="removeImage(index)"></el-button>
            </div>
            <div class="image-index">第 {{ index + 1 }} 页</div>
          </div>
        </template>
      </draggable>

      <div v-if="!form.paperImages || form.paperImages.length === 0" class="empty-placeholder">
        暂无图片，请从上方上传
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
import draggable from 'vuedraggable';

import type { Subject } from '@/api/subject';
import PaperQuestionManager from './PaperQuestionManager.vue';
import PaperKnowledgePointChart from './PaperKnowledgePointChart.vue'; // 【知识点功能增强】
import { useAuthStore } from '@/stores/auth';
import { UploadFilled, Delete, Trophy, Document } from '@element-plus/icons-vue';  // ✅ 新增Trophy和Document图标



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
const activeTab = ref('content'); // 【知识点功能增强】当前激活的Tab

const dialogTitle = computed(() => (props.paperId ? '编辑试卷' : '新增试卷'));

// ✅ 实时计算试卷总分
const totalScore = computed(() => {
  if (form.value.paperType === 1 && form.value.groups) {
    return form.value.groups.reduce((total, group) => {
      return total + (group.questions?.reduce((sum, q) => sum + (q.score || 0), 0) || 0);
    }, 0);
  } else if (form.value.paperType === 2) {
    return 100; // 图片试卷默认100分
  }
  return 0;
});

// ✅ 实时计算题目总数
const totalQuestions = computed(() => {
  if (form.value.paperType === 1 && form.value.groups) {
    return form.value.groups.reduce((total, group) => {
      return total + (group.questions?.length || 0);
    }, 0);
  }
  return 0;
});

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
// 【新增】定义滑块的刻度标记
const difficultyMarks = {
  0.1: '极易',
  0.3: '简单',
  0.5: '中等',
  0.7: '较难',
  0.9: '极难'
};
// 3. 新增排序处理函数 (其实 draggable 会自动更新数组，这里主要是为了确保 sortOrder 字段正确)
const handleImageSort = () => {
  if (form.value.paperImages) {
    form.value.paperImages.forEach((img, idx) => {
      img.sortOrder = idx;
    });
  }
};

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
  subjectiveCount: 1,
  targetDifficulty: 0.5 // 【新增】默认难度设为 0.5 (中等)
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
/* 优化图片列表样式 */
.image-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 10px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  min-height: 150px;
  background-color: #fcfcfc;
}

.image-item {
  position: relative;
  width: 160px;
  height: 220px; /* 模拟A4纸比例 */
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s;
  cursor: move;
}

.image-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.preview-image {
  width: 100%;
  height: 100%;
  display: block;
}

/* 拖拽时的占位样式 */
.ghost {
  opacity: 0.5;
  background: #ecf5ff;
  border: 2px dashed #409eff;
}

.image-index {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 12px;
  text-align: center;
  padding: 4px 0;
}

/* 修改操作按钮位置，避免遮挡 */
.image-actions {
  position: absolute;
  top: 5px;
  right: 5px;
  opacity: 0;
  transition: opacity 0.2s;
}
.image-item:hover .image-actions {
  opacity: 1;
}
</style>