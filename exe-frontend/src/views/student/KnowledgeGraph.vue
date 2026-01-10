<template>
  <div class="knowledge-graph-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon :size="32" color="#667eea"><Histogram /></el-icon>
        <div class="title-text">
          <h1>知识图谱</h1>
          <p>可视化知识关联，系统化掌握学科知识</p>
        </div>
      </div>
      <div class="header-right">
        <!-- 全局搜索框 -->
        <el-input
          v-model="globalSearchKeyword"
          placeholder="搜索知识点（支持拼音）"
          :prefix-icon="Search"
          style="width: 250px"
          clearable
          @input="handleGlobalSearch"
          @keyup.enter="handleSearchEnter"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearchEnter" />
          </template>
        </el-input>

        <el-select v-model="selectedSubject" @change="loadKnowledgeGraph" style="width: 150px">
          <el-option
            v-for="subject in subjects"
            :key="subject.value"
            :label="subject.label"
            :value="subject.value"
          />
        </el-select>

        <!-- 操作按钮 -->
        <el-button-group>
          <el-button :icon="Refresh" @click="loadKnowledgeGraph" :loading="loading">
            刷新
          </el-button>
          <el-button :icon="Download" @click="showExportDialog = true">
            导出
          </el-button>
          <el-button :icon="Setting" @click="showSettingsDialog = true">
            设置
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 搜索结果下拉 -->
    <div v-if="searchResults.length > 0" class="search-dropdown">
      <div class="search-header">
        <span>找到 {{ searchResults.length }} 个结果</span>
        <el-button text @click="clearSearch">清除</el-button>
      </div>
      <div class="search-results">
        <div
          v-for="result in searchResults"
          :key="result.id"
          class="search-result-item"
          @click="locateNode(result)"
        >
          <span v-html="highlightText(result.name, globalSearchKeyword)"></span>
          <el-tag size="small">{{ result.category }}</el-tag>
        </div>
      </div>
    </div>

    <!-- 搜索历史 -->
    <div v-if="showSearchHistory && searchHistory.length > 0" class="search-history">
      <div class="history-header">
        <span>搜索历史</span>
        <el-button text size="small" @click="clearSearchHistory">清空</el-button>
      </div>
      <div class="history-items">
        <el-tag
          v-for="(history, index) in searchHistory"
          :key="index"
          closable
          @close="removeSearchHistory(index)"
          @click="globalSearchKeyword = history; handleGlobalSearch()"
        >
          {{ history }}
        </el-tag>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="page-content">
      <el-row :gutter="20">
        <!-- 左侧：知识点列表 -->
        <el-col :xs="24" :lg="6">
          <el-card class="knowledge-list-card" shadow="hover">
            <template #header>
              <div class="list-header">
                <span>知识点列表</span>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索知识点"
                  :prefix-icon="Search"
                  size="small"
                  clearable
                  @input="filterKnowledgePoints"
                />
              </div>
            </template>

            <!-- 虚拟滚动树形列表 -->
            <div class="knowledge-tree" ref="treeContainer">
              <el-tree
                ref="treeRef"
                :data="filteredKnowledgeTree"
                :props="treeProps"
                node-key="id"
                :expand-on-click-node="false"
                :highlight-current="true"
                :default-expanded-keys="expandedKeys"
                @node-click="handleNodeClick"
                virtual
                :height="400"
              >
                <template #default="{ node, data }">
                  <div class="tree-node">
                    <span class="node-label">{{ node.label }}</span>
                    <div class="node-status">
                      <el-progress
                        v-if="data.masteryRate !== undefined"
                        :percentage="data.masteryRate"
                        :stroke-width="4"
                        :color="getMasteryColor(data.masteryRate)"
                        :show-text="false"
                        style="width: 50px"
                      />
                      <el-icon
                        v-if="data.masteryRate === 100"
                        color="#67C23A"
                        :size="16"
                      >
                        <CircleCheck />
                      </el-icon>
                    </div>
                  </div>
                </template>
              </el-tree>
            </div>
          </el-card>

          <!-- 统计信息 -->
          <el-card class="stats-card" shadow="hover">
            <template #header>
              <span>学习统计</span>
            </template>
            <div class="stat-item">
              <span>总知识点</span>
              <span class="stat-value">{{ totalKnowledgePoints }}</span>
            </div>
            <div class="stat-item">
              <span>已掌握</span>
              <span class="stat-value success">{{ masteredPoints }}</span>
            </div>
            <div class="stat-item">
              <span>学习中</span>
              <span class="stat-value warning">{{ learningPoints }}</span>
            </div>
            <div class="stat-item">
              <span>未学习</span>
              <span class="stat-value danger">{{ unlearnedPoints }}</span>
            </div>
            <el-divider />
            <div class="overall-progress">
              <p>总体掌握度</p>
              <el-progress
                :percentage="overallMasteryRate"
                :stroke-width="12"
                :color="progressColors"
              >
                <template #default="{ percentage }">
                  <span style="font-weight: 600">{{ percentage }}%</span>
                </template>
              </el-progress>
            </div>
          </el-card>
        </el-col>

        <!-- 中间：知识图谱可视化 -->
        <el-col :xs="24" :lg="12">
          <el-card class="graph-card" shadow="hover" v-loading="loading">
            <template #header>
              <div class="graph-header">
                <span>知识关系图谱</span>
                <div class="graph-controls">
                  <!-- 布局切换 -->
                  <el-select
                    v-model="graphSettings.layout"
                    @change="updateGraphLayout"
                    size="small"
                    style="width: 120px; margin-right: 8px"
                  >
                    <el-option label="力导向" value="force" />
                    <el-option label="层次布局" value="circular" />
                    <el-option label="辐射布局" value="radial" />
                    <el-option label="环形布局" value="ring" />
                  </el-select>

                  <el-button-group size="small">
                    <el-button :icon="ZoomIn" @click="zoomIn">放大</el-button>
                    <el-button :icon="ZoomOut" @click="zoomOut">缩小</el-button>
                    <el-button :icon="Refresh" @click="resetGraph">重置</el-button>
                  </el-button-group>
                </div>
              </div>
            </template>

            <!-- ECharts 图表容器 -->
            <div ref="graphRef" class="graph-container" @contextmenu.prevent="handleGraphContextMenu"></div>

            <!-- 缩略图导航器 -->
            <div class="graph-minimap" v-if="graphSettings.showMinimap">
              <div ref="minimapRef" class="minimap-container"></div>
            </div>

            <!-- 图例 -->
            <div class="graph-legend">
              <div class="legend-item">
                <span class="legend-dot" style="background: #67C23A"></span>
                <span>已掌握</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: #E6A23C"></span>
                <span>学习中</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background: #909399"></span>
                <span>未学习</span>
              </div>
            </div>
          </el-card>

          <!-- 学习路径推荐 -->
          <el-card class="path-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>AI推荐学习路径</span>
                <el-space>
                  <el-button text :icon="Refresh" @click="generateLearningPath" :loading="generatingPath">
                    重新生成
                  </el-button>
                  <el-button text :icon="Share" @click="shareLearningPath" v-if="learningPath.length > 0">
                    分享
                  </el-button>
                </el-space>
              </div>
            </template>

            <!-- 多路径对比 -->
            <div v-if="learningPaths.length > 1" class="path-tabs">
              <el-radio-group v-model="selectedPathIndex" @change="switchLearningPath">
                <el-radio-button
                  v-for="(path, index) in learningPaths"
                  :key="index"
                  :label="index"
                >
                  路径{{ index + 1 }}
                  <el-tag size="small" type="info">{{ path.difficulty }}</el-tag>
                </el-radio-button>
              </el-radio-group>
            </div>

            <div v-if="learningPath.length > 0" class="learning-path">
              <!-- 路径信息 -->
              <div class="path-info">
                <el-descriptions :column="3" size="small" border>
                  <el-descriptions-item label="难度">
                    <el-tag :type="getDifficultyType(currentPathInfo.difficulty)">
                      {{ currentPathInfo.difficulty }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="预计时长">
                    {{ currentPathInfo.estimatedTime }}
                  </el-descriptions-item>
                  <el-descriptions-item label="进度">
                    {{ currentPathInfo.progress }}%
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <!-- 学习步骤 -->
              <el-steps :active="learningPathActive" align-center direction="vertical" class="path-steps">
                <el-step
                  v-for="(step, index) in learningPath"
                  :key="index"
                  :title="step.name"
                  :description="step.description"
                  :status="getStepStatus(step)"
                >
                  <template #icon>
                    <el-icon v-if="step.completed" color="#67C23A"><CircleCheck /></el-icon>
                    <el-icon v-else-if="step.current" color="#409EFF"><Clock /></el-icon>
                    <span v-else>{{ index + 1 }}</span>
                  </template>
                </el-step>
              </el-steps>

              <div class="path-actions">
                <el-button type="primary" @click="startLearningPath">
                  开始学习
                </el-button>
                <el-button @click="exportLearningPathPDF">
                  导出为PDF
                </el-button>
              </div>
            </div>
            <el-empty v-else description="点击重新生成获取AI智能学习路径推荐" />
          </el-card>
        </el-col>

        <!-- 右侧：知识点详情 -->
        <el-col :xs="24" :lg="6">
          <el-card class="detail-card" shadow="hover">
            <template #header>
              <span>知识点详情</span>
            </template>

            <div v-if="selectedNode" class="node-detail">
              <!-- 基本信息 -->
              <div class="detail-section">
                <h3>{{ selectedNode.name }}</h3>
                <p class="node-description">{{ selectedNode.description }}</p>
                <div class="node-tags">
                  <el-tag v-if="selectedNode.difficulty" type="info">
                    {{ selectedNode.difficulty }}
                  </el-tag>
                  <el-tag v-if="selectedNode.category">
                    {{ selectedNode.category }}
                  </el-tag>
                  <el-tag v-if="selectedNode.marked" type="success">
                    已标记
                  </el-tag>
                </div>
              </div>

              <!-- 掌握度 -->
              <div class="detail-section">
                <h4>掌握情况</h4>
                <el-progress
                  :percentage="selectedNode.masteryRate || 0"
                  :stroke-width="12"
                  :color="getMasteryColor(selectedNode.masteryRate || 0)"
                />
                <div class="mastery-stats">
                  <div class="mastery-item">
                    <span>练习次数</span>
                    <span class="value">{{ selectedNode.practiceCount || 0 }}</span>
                  </div>
                  <div class="mastery-item">
                    <span>正确率</span>
                    <span class="value">{{ selectedNode.accuracy || 0 }}%</span>
                  </div>
                </div>
              </div>

              <!-- 前置知识点 -->
              <div class="detail-section" v-if="selectedNode.prerequisites && selectedNode.prerequisites.length > 0">
                <h4>前置知识点</h4>
                <div class="related-points">
                  <el-tag
                    v-for="pre in selectedNode.prerequisites"
                    :key="pre.id"
                    class="related-tag"
                    @click="jumpToNode(pre)"
                  >
                    {{ pre.name }}
                  </el-tag>
                </div>
              </div>

              <!-- 后续知识点 -->
              <div class="detail-section" v-if="selectedNode.successors && selectedNode.successors.length > 0">
                <h4>后续知识点</h4>
                <div class="related-points">
                  <el-tag
                    v-for="suc in selectedNode.successors"
                    :key="suc.id"
                    type="success"
                    class="related-tag"
                    @click="jumpToNode(suc)"
                  >
                    {{ suc.name }}
                  </el-tag>
                </div>
              </div>

              <!-- 相关资源 -->
              <div class="detail-section">
                <h4>学习资源</h4>
                <div class="resource-list">
                  <div class="resource-item">
                    <el-icon><Reading /></el-icon>
                    <span>视频教程</span>
                    <el-button text size="small">观看</el-button>
                  </div>
                  <div class="resource-item">
                    <el-icon><Edit /></el-icon>
                    <span>练习题</span>
                    <el-button text size="small">开始</el-button>
                  </div>
                  <div class="resource-item">
                    <el-icon><Document /></el-icon>
                    <span>知识卡片</span>
                    <el-button text size="small">查看</el-button>
                  </div>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="detail-actions">
                <el-button type="primary" :icon="Reading" @click="startLearning">
                  开始学习
                </el-button>
                <el-button :icon="Edit" @click="startPractice">
                  开始练习
                </el-button>
                <el-button
                  :type="selectedNode.marked ? 'success' : 'default'"
                  @click="toggleNodeMark"
                >
                  {{ selectedNode.marked ? '已标记' : '标记为已掌握' }}
                </el-button>
              </div>
            </div>

            <el-empty v-else description="请选择一个知识点查看详情" :image-size="80" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 右键菜单 -->
    <el-dropdown
      ref="contextMenuRef"
      trigger="contextmenu"
      :teleported="false"
      :style="{
        position: 'fixed',
        left: contextMenu.x + 'px',
        top: contextMenu.y + 'px',
        visibility: contextMenu.visible ? 'visible' : 'hidden'
      }"
    >
      <span></span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="markNodeAsMastered">标记为已掌握</el-dropdown-item>
          <el-dropdown-item @click="addToLearningPlan">添加到学习计划</el-dropdown-item>
          <el-dropdown-item @click="viewNodeDetail">查看详情</el-dropdown-item>
          <el-dropdown-item divided @click="expandNode">展开子节点</el-dropdown-item>
          <el-dropdown-item @click="collapseNode">收起子节点</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 设置对话框 -->
    <el-dialog v-model="showSettingsDialog" title="图谱设置" width="600px">
      <el-form label-width="140px">
        <el-divider content-position="left">显示设置</el-divider>
        <el-form-item label="显示节点标签">
          <el-switch v-model="graphSettings.showLabels" @change="updateGraph" />
        </el-form-item>
        <el-form-item label="显示关系线">
          <el-switch v-model="graphSettings.showLinks" @change="updateGraph" />
        </el-form-item>
        <el-form-item label="显示缩略图">
          <el-switch v-model="graphSettings.showMinimap" @change="updateGraph" />
        </el-form-item>
        <el-form-item label="启用节点动画">
          <el-switch v-model="graphSettings.enableAnimation" @change="updateGraph" />
        </el-form-item>

        <el-divider content-position="left">节点设置</el-divider>
        <el-form-item label="节点大小">
          <el-slider v-model="graphSettings.nodeSize" :min="20" :max="80" @change="updateGraph" />
        </el-form-item>
        <el-form-item label="节点颜色方案">
          <el-radio-group v-model="graphSettings.colorScheme" @change="updateGraph">
            <el-radio label="mastery">按掌握度</el-radio>
            <el-radio label="importance">按重要性</el-radio>
            <el-radio label="subject">按科目</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">连线设置</el-divider>
        <el-form-item label="连线样式">
          <el-radio-group v-model="graphSettings.lineStyle" @change="updateGraph">
            <el-radio label="solid">实线</el-radio>
            <el-radio label="dashed">虚线</el-radio>
            <el-radio label="dotted">点线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示箭头">
          <el-switch v-model="graphSettings.showArrow" @change="updateGraph" />
        </el-form-item>
        <el-form-item label="连线粗细按强度">
          <el-switch v-model="graphSettings.lineWidthByStrength" @change="updateGraph" />
        </el-form-item>

        <el-divider content-position="left">性能设置</el-divider>
        <el-form-item label="启用增量渲染">
          <el-switch v-model="graphSettings.progressiveRender" @change="updateGraph" />
        </el-form-item>
        <el-form-item label="节点数据缓存">
          <el-switch v-model="graphSettings.enableCache" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettingsDialog = false">取消</el-button>
        <el-button @click="resetSettings">恢复默认</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导出对话框 -->
    <el-dialog v-model="showExportDialog" title="导出知识图谱" width="500px">
      <el-form label-width="120px">
        <el-form-item label="导出格式">
          <el-radio-group v-model="exportFormat">
            <el-radio label="png">PNG 图片</el-radio>
            <el-radio label="svg">SVG 矢量图</el-radio>
            <el-radio label="json">JSON 数据</el-radio>
            <el-radio label="xmind">思维导图</el-radio>
            <el-radio label="pdf">PDF 报告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图片质量" v-if="exportFormat === 'png'">
          <el-slider v-model="exportQuality" :min="1" :max="5" :marks="{ 1: '低', 3: '中', 5: '高' }" />
        </el-form-item>
        <el-form-item label="包含内容" v-if="exportFormat === 'json' || exportFormat === 'pdf'">
          <el-checkbox-group v-model="exportOptions">
            <el-checkbox label="nodes">节点数据</el-checkbox>
            <el-checkbox label="links">关系数据</el-checkbox>
            <el-checkbox label="progress">学习进度</el-checkbox>
            <el-checkbox label="stats">统计信息</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleExport" :loading="exporting">
          导出
        </el-button>
      </template>
    </el-dialog>

    <!-- 分享对话框 -->
    <el-dialog v-model="showShareDialog" title="分享学习路径" width="500px">
      <div class="share-content">
        <div class="share-qrcode" ref="qrcodeRef">
          <!-- 二维码将在这里生成 -->
        </div>
        <el-input
          v-model="shareLink"
          readonly
          placeholder="分享链接"
        >
          <template #append>
            <el-button @click="copyShareLink">复制</el-button>
          </template>
        </el-input>
        <div class="share-tips">
          <el-alert
            title="分享链接有效期为7天"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed, watch, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import {
  Histogram, Refresh, Setting, Search, CircleCheck, ZoomIn, ZoomOut,
  Clock, Reading, Edit, Document, Download, Share
} from '@element-plus/icons-vue';
import { fetchKnowledgeGraph } from '@/api/knowledgePoint';
import { analyzeQuestionStream } from '@/api/ai';

// ==================== 类型定义 ====================
interface KnowledgeNode {
  id: string;
  label: string;
  name: string;
  description?: string;
  masteryRate?: number;
  category?: string;
  difficulty?: string;
  practiceCount?: number;
  accuracy?: number;
  prerequisites?: KnowledgeNode[];
  successors?: KnowledgeNode[];
  children?: KnowledgeNode[];
  marked?: boolean;
  importance?: number;
}

interface GraphSettings {
  showLabels: boolean;
  showLinks: boolean;
  showMinimap: boolean;
  enableAnimation: boolean;
  nodeSize: number;
  layout: string;
  colorScheme: 'mastery' | 'importance' | 'subject';
  lineStyle: 'solid' | 'dashed' | 'dotted';
  showArrow: boolean;
  lineWidthByStrength: boolean;
  progressiveRender: boolean;
  enableCache: boolean;
}

interface LearningPathInfo {
  difficulty: string;
  estimatedTime: string;
  progress: number;
}

// ==================== 响应式数据 ====================
const subjects = ref([
  { value: 'math', label: '数学' },
  { value: 'physics', label: '物理' },
  { value: 'chemistry', label: '化学' },
  { value: 'english', label: '英语' }
]);

const selectedSubject = ref('math');
const loading = ref(false);
const searchKeyword = ref('');
const globalSearchKeyword = ref('');
const searchResults = ref<KnowledgeNode[]>([]);
const showSearchHistory = ref(false);
const searchHistory = ref<string[]>([]);

// 知识树
const knowledgeTree = ref<KnowledgeNode[]>([]);
const filteredKnowledgeTree = computed(() => {
  if (!searchKeyword.value) return knowledgeTree.value;
  return filterTree(knowledgeTree.value, searchKeyword.value);
});

const treeProps = {
  children: 'children',
  label: 'label'
};

const treeRef = ref();
const treeContainer = ref<HTMLElement>();
const expandedKeys = ref<string[]>([]);

// 统计信息
const totalKnowledgePoints = computed(() => {
  let count = 0;
  const countNodes = (nodes: KnowledgeNode[]) => {
    nodes.forEach(node => {
      count++;
      if (node.children) countNodes(node.children);
    });
  };
  countNodes(knowledgeTree.value);
  return count;
});

const masteredPoints = ref(0);
const learningPoints = ref(0);
const unlearnedPoints = ref(0);
const overallMasteryRate = ref(0);

const progressColors = [
  { color: '#F56C6C', percentage: 40 },
  { color: '#E6A23C', percentage: 70 },
  { color: '#67C23A', percentage: 100 }
];

// 图谱相关
const graphRef = ref<HTMLElement>();
const minimapRef = ref<HTMLElement>();
let graphChart: echarts.ECharts | null = null;
let minimapChart: echarts.ECharts | null = null;
let resizeObserver: ResizeObserver | null = null;
let resizeTimer: number | null = null;

// 节点数据缓存
const nodeCache = new Map<string, KnowledgeNode>();
const graphDataCache = ref<any>(null);

// 选中的节点
const selectedNode = ref<KnowledgeNode | null>(null);
const contextMenuNode = ref<KnowledgeNode | null>(null);

// 右键菜单
const contextMenuRef = ref();
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0
});

// 学习路径
const learningPath = ref<any[]>([]);
const learningPaths = ref<any[]>([]);
const selectedPathIndex = ref(0);
const currentPathInfo = ref<LearningPathInfo>({
  difficulty: '中等',
  estimatedTime: '2小时',
  progress: 0
});
const learningPathActive = ref(1);
const generatingPath = ref(false);

// 图谱设置
const showSettingsDialog = ref(false);
const defaultSettings: GraphSettings = {
  showLabels: true,
  showLinks: true,
  showMinimap: false,
  enableAnimation: true,
  nodeSize: 50,
  layout: 'force',
  colorScheme: 'mastery',
  lineStyle: 'solid',
  showArrow: true,
  lineWidthByStrength: true,
  progressiveRender: true,
  enableCache: true
};
const graphSettings = ref<GraphSettings>({ ...defaultSettings });

// 导出相关
const showExportDialog = ref(false);
const exportFormat = ref('png');
const exportQuality = ref(3);
const exportOptions = ref(['nodes', 'links', 'progress', 'stats']);
const exporting = ref(false);

// 分享相关
const showShareDialog = ref(false);
const shareLink = ref('');
const qrcodeRef = ref<HTMLElement>();

// ==================== 拼音搜索工具 ====================
const pinyinMap: { [key: string]: string } = {
  '函数': 'hanshu',
  '几何': 'jihe',
  '三角': 'sanjiao',
  '代数': 'daishu',
  '微积分': 'weijifen',
  // 可以扩展更多...
};

const getPinyin = (text: string): string => {
  return pinyinMap[text] || text;
};

const matchPinyin = (text: string, keyword: string): boolean => {
  const pinyin = getPinyin(text).toLowerCase();
  const lowerKeyword = keyword.toLowerCase();
  return text.includes(keyword) || pinyin.includes(lowerKeyword);
};

// ==================== 数据持久化 ====================
const STORAGE_KEYS = {
  GRAPH_SETTINGS: 'knowledge_graph_settings',
  NODE_MARKS: 'knowledge_graph_node_marks',
  LEARNING_PROGRESS: 'knowledge_graph_learning_progress',
  SEARCH_HISTORY: 'knowledge_graph_search_history',
  CUSTOM_LAYOUT: 'knowledge_graph_custom_layout'
};

const loadFromStorage = () => {
  try {
    const savedSettings = localStorage.getItem(STORAGE_KEYS.GRAPH_SETTINGS);
    if (savedSettings) {
      graphSettings.value = { ...defaultSettings, ...JSON.parse(savedSettings) };
    }

    const savedHistory = localStorage.getItem(STORAGE_KEYS.SEARCH_HISTORY);
    if (savedHistory) {
      searchHistory.value = JSON.parse(savedHistory);
    }

    const savedMarks = localStorage.getItem(STORAGE_KEYS.NODE_MARKS);
    if (savedMarks) {
      const marks = JSON.parse(savedMarks);
      // 应用标记到节点
      applyMarksToNodes(marks);
    }
  } catch (error) {
    console.error('加载本地数据失败:', error);
  }
};

const saveToStorage = () => {
  try {
    localStorage.setItem(STORAGE_KEYS.GRAPH_SETTINGS, JSON.stringify(graphSettings.value));
    localStorage.setItem(STORAGE_KEYS.SEARCH_HISTORY, JSON.stringify(searchHistory.value));
  } catch (error) {
    console.error('保存数据失败:', error);
  }
};

const applyMarksToNodes = (marks: { [key: string]: boolean }) => {
  const applyMark = (nodes: KnowledgeNode[]) => {
    nodes.forEach(node => {
      if (marks[node.id]) {
        node.marked = true;
      }
      if (node.children) {
        applyMark(node.children);
      }
    });
  };
  applyMark(knowledgeTree.value);
};

// ==================== 搜索功能 ====================
const handleGlobalSearch = () => {
  if (!globalSearchKeyword.value.trim()) {
    searchResults.value = [];
    return;
  }

  // 搜索所有节点
  const results: KnowledgeNode[] = [];
  const searchInTree = (nodes: KnowledgeNode[]) => {
    nodes.forEach(node => {
      if (matchPinyin(node.name, globalSearchKeyword.value)) {
        results.push(node);
      }
      if (node.children) {
        searchInTree(node.children);
      }
    });
  };
  searchInTree(knowledgeTree.value);
  searchResults.value = results;
};

const handleSearchEnter = () => {
  if (globalSearchKeyword.value.trim() && !searchHistory.value.includes(globalSearchKeyword.value)) {
    searchHistory.value.unshift(globalSearchKeyword.value);
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10);
    }
    saveToStorage();
  }
  handleGlobalSearch();
};

const clearSearch = () => {
  globalSearchKeyword.value = '';
  searchResults.value = [];
};

const clearSearchHistory = () => {
  searchHistory.value = [];
  saveToStorage();
};

const removeSearchHistory = (index: number) => {
  searchHistory.value.splice(index, 1);
  saveToStorage();
};

const highlightText = (text: string, keyword: string) => {
  if (!keyword) return text;
  const regex = new RegExp(`(${keyword})`, 'gi');
  return text.replace(regex, '<mark>$1</mark>');
};

const locateNode = (node: KnowledgeNode) => {
  // 在树中高亮节点
  if (treeRef.value) {
    treeRef.value.setCurrentKey(node.id);
  }

  // 在图谱中定位节点
  if (graphChart) {
    graphChart.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      name: node.name
    });

    // 使用 requestAnimationFrame 优化动画
    requestAnimationFrame(() => {
      graphChart?.dispatchAction({
        type: 'showTip',
        seriesIndex: 0,
        name: node.name
      });
    });
  }

  // 更新选中节点
  selectedNode.value = node;
  searchResults.value = [];
  globalSearchKeyword.value = '';
};

// ==================== 树形列表操作 ====================
const filterTree = (nodes: KnowledgeNode[], keyword: string): KnowledgeNode[] => {
  const result: KnowledgeNode[] = [];
  nodes.forEach(node => {
    if (matchPinyin(node.name, keyword)) {
      result.push(node);
    } else if (node.children) {
      const filteredChildren = filterTree(node.children, keyword);
      if (filteredChildren.length > 0) {
        result.push({
          ...node,
          children: filteredChildren
        });
      }
    }
  });
  return result;
};

const filterKnowledgePoints = () => {
  // 过滤逻辑已通过 computed 实现
};

const handleNodeClick = (data: KnowledgeNode) => {
  // 从缓存中获取或构建节点详情
  if (graphSettings.value.enableCache && nodeCache.has(data.id)) {
    selectedNode.value = nodeCache.get(data.id)!;
  } else {
    selectedNode.value = {
      ...data,
      description: data.description || '这是一个重要的知识点，需要掌握其基本概念和应用方法',
      difficulty: data.difficulty || '中等',
      category: data.category || selectedSubject.value,
      practiceCount: data.practiceCount || Math.floor(Math.random() * 50),
      accuracy: data.accuracy || Math.floor(Math.random() * 100)
    };

    if (graphSettings.value.enableCache) {
      nodeCache.set(data.id, selectedNode.value);
    }
  }

  // 在图谱中高亮
  if (graphChart) {
    graphChart.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      name: data.name
    });
  }
};

// ==================== 图谱初始化与更新 ====================
const loadKnowledgeGraph = async () => {
  loading.value = true;
  try {
    // 这里应该调用真实的 API
    // const result = await fetchKnowledgeGraph(selectedSubject.value);

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 1000));

    knowledgeTree.value = generateMockData();
    calculateStats();

    await nextTick();
    initGraph();

    ElMessage.success('知识图谱加载成功');
  } catch (error) {
    ElMessage.error('加载失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const generateMockData = (): KnowledgeNode[] => {
  // 生成模拟数据（支持大量节点测试）
  return [
    {
      id: '1',
      label: '函数',
      name: '函数',
      masteryRate: 75,
      category: 'math',
      children: [
        { id: '1-1', label: '一次函数', name: '一次函数', masteryRate: 100, category: 'math' },
        { id: '1-2', label: '二次函数', name: '二次函数', masteryRate: 85, category: 'math' },
        { id: '1-3', label: '反比例函数', name: '反比例函数', masteryRate: 60, category: 'math' },
        {
          id: '1-4',
          label: '函数性质',
          name: '函数性质',
          masteryRate: 50,
          category: 'math',
          children: [
            { id: '1-4-1', label: '单调性', name: '单调性', masteryRate: 65, category: 'math' },
            { id: '1-4-2', label: '奇偶性', name: '奇偶性', masteryRate: 45, category: 'math' }
          ]
        }
      ]
    },
    {
      id: '2',
      label: '几何',
      name: '几何',
      masteryRate: 60,
      category: 'math',
      children: [
        { id: '2-1', label: '三角形', name: '三角形', masteryRate: 80, category: 'math' },
        { id: '2-2', label: '圆', name: '圆', masteryRate: 55, category: 'math' },
        { id: '2-3', label: '立体几何', name: '立体几何', masteryRate: 40, category: 'math' }
      ]
    }
  ];
};

const calculateStats = () => {
  let total = 0;
  let mastered = 0;
  let learning = 0;
  let unlearned = 0;
  let totalRate = 0;

  const traverse = (nodes: KnowledgeNode[]) => {
    nodes.forEach(node => {
      total++;
      const rate = node.masteryRate || 0;
      totalRate += rate;

      if (rate >= 80) mastered++;
      else if (rate >= 40) learning++;
      else unlearned++;

      if (node.children) traverse(node.children);
    });
  };

  traverse(knowledgeTree.value);

  masteredPoints.value = mastered;
  learningPoints.value = learning;
  unlearnedPoints.value = unlearned;
  overallMasteryRate.value = total > 0 ? Math.round(totalRate / total) : 0;
};

const initGraph = () => {
  if (!graphRef.value) return;

  if (graphChart) {
    graphChart.dispose();
  }

  graphChart = echarts.init(graphRef.value);

  // 构建图数据
  const { nodes, links, categories } = buildGraphData();

  // 设置图表选项
  const option = {
    animation: graphSettings.value.enableAnimation,
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    tooltip: {
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          return `${params.name}<br/>掌握度: ${params.data.value}%`;
        }
        return '';
      }
    },
    legend: graphSettings.value.showLabels ? [{
      data: categories.map(c => c.name),
      orient: 'horizontal',
      bottom: 10
    }] : undefined,
    series: [{
      type: 'graph',
      layout: graphSettings.value.layout,
      data: nodes,
      links: links,
      categories: categories,
      roam: true,
      draggable: true,
      label: {
        show: graphSettings.value.showLabels,
        position: 'right',
        formatter: '{b}'
      },
      labelLayout: {
        hideOverlap: true
      },
      lineStyle: {
        color: 'source',
        curveness: 0.3,
        width: graphSettings.value.lineWidthByStrength ? (params: any) => {
          return params.data.strength || 2;
        } : 2,
        type: graphSettings.value.lineStyle
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 5
        },
        label: {
          show: true
        }
      },
      force: {
        repulsion: 1000,
        edgeLength: 150,
        layoutAnimation: graphSettings.value.enableAnimation
      },
      itemStyle: {
        color: (params: any) => getNodeColor(params.data)
      },
      // 增量渲染配置
      progressive: graphSettings.value.progressiveRender ? 400 : undefined,
      progressiveThreshold: graphSettings.value.progressiveRender ? 3000 : undefined
    }]
  };

  graphChart.setOption(option);

  // 绑定事件
  bindGraphEvents();

  // 初始化缩略图
  if (graphSettings.value.showMinimap) {
    initMinimap();
  }

  // 监听窗口大小变化（使用防抖）
  setupResizeObserver();
};

const buildGraphData = () => {
  const nodes: any[] = [];
  const links: any[] = [];
  const categories = [
    { name: '已掌握' },
    { name: '学习中' },
    { name: '未学习' }
  ];

  const nodeMap = new Map<string, any>();

  // 递归构建节点
  const buildNodes = (treeNodes: KnowledgeNode[], parentId?: string) => {
    treeNodes.forEach(node => {
      const masteryRate = node.masteryRate || 0;
      let category = 2; // 未学习
      if (masteryRate >= 80) category = 0; // 已掌握
      else if (masteryRate >= 40) category = 1; // 学习中

      const graphNode = {
        id: node.id,
        name: node.name,
        category: category,
        value: masteryRate,
        symbolSize: graphSettings.value.nodeSize * (0.5 + masteryRate / 200),
        importance: node.importance || Math.random()
      };

      nodes.push(graphNode);
      nodeMap.set(node.id, graphNode);

      // 添加连接
      if (parentId) {
        links.push({
          source: parentId,
          target: node.id,
          strength: Math.random() * 3 + 1
        });
      }

      // 递归处理子节点
      if (node.children) {
        buildNodes(node.children, node.id);
      }
    });
  };

  buildNodes(knowledgeTree.value);

  return { nodes, links, categories };
};

const getNodeColor = (nodeData: any) => {
  if (graphSettings.value.colorScheme === 'mastery') {
    const colors = ['#67C23A', '#E6A23C', '#909399'];
    return colors[nodeData.category];
  } else if (graphSettings.value.colorScheme === 'importance') {
    const hue = nodeData.importance * 120; // 0-120 从红到绿
    return `hsl(${hue}, 70%, 60%)`;
  } else {
    // 按科目
    const subjectColors: { [key: string]: string } = {
      math: '#409EFF',
      physics: '#67C23A',
      chemistry: '#E6A23C',
      english: '#F56C6C'
    };
    return subjectColors[selectedSubject.value] || '#909399';
  }
};

const bindGraphEvents = () => {
  if (!graphChart) return;

  // 双击节点展开/收起
  graphChart.on('dblclick', (params: any) => {
    if (params.dataType === 'node') {
      toggleNodeExpand(params.data);
    }
  });

  // 右键菜单
  graphChart.getZr().on('contextmenu', (e: any) => {
    e.event.preventDefault();
    handleGraphContextMenu(e.event);
  });

  // 节点点击
  graphChart.on('click', (params: any) => {
    if (params.dataType === 'node') {
      const node = findNodeById(params.data.id);
      if (node) {
        handleNodeClick(node);
      }
    }
  });
};

const toggleNodeExpand = (nodeData: any) => {
  // 实现节点展开/收起逻辑
  ElMessage.info(`切换节点: ${nodeData.name}`);
  // TODO: 实现具体逻辑
};

const findNodeById = (id: string): KnowledgeNode | null => {
  const search = (nodes: KnowledgeNode[]): KnowledgeNode | null => {
    for (const node of nodes) {
      if (node.id === id) return node;
      if (node.children) {
        const found = search(node.children);
        if (found) return found;
      }
    }
    return null;
  };
  return search(knowledgeTree.value);
};

// ==================== 图谱操作 ====================
const zoomIn = () => {
  if (graphChart) {
    const option = graphChart.getOption();
    const series: any = option.series?.[0];
    if (series?.zoom) {
      graphChart.setOption({
        series: [{
          zoom: series.zoom * 1.2
        }]
      });
    }
  }
};

const zoomOut = () => {
  if (graphChart) {
    const option = graphChart.getOption();
    const series: any = option.series?.[0];
    if (series?.zoom) {
      graphChart.setOption({
        series: [{
          zoom: series.zoom * 0.8
        }]
      });
    }
  }
};

const resetGraph = () => {
  if (graphChart) {
    graphChart.clear();
    initGraph();
  }
};

const updateGraph = () => {
  initGraph();
  saveToStorage();
};

const updateGraphLayout = () => {
  if (graphChart) {
    graphChart.setOption({
      series: [{
        layout: graphSettings.value.layout
      }]
    });
  }
};

// ==================== 缩略图 ====================
const initMinimap = () => {
  if (!minimapRef.value) return;

  if (minimapChart) {
    minimapChart.dispose();
  }

  minimapChart = echarts.init(minimapRef.value);

  // 复制主图表的简化版本
  const mainOption: any = graphChart?.getOption();
  if (mainOption) {
    minimapChart.setOption({
      ...mainOption,
      legend: undefined,
      tooltip: undefined,
      series: [{
        ...mainOption.series[0],
        label: { show: false }
      }]
    });
  }
};

// ==================== Resize 处理（防抖） ====================
const setupResizeObserver = () => {
  if (!graphRef.value) return;

  resizeObserver = new ResizeObserver(() => {
    if (resizeTimer) {
      clearTimeout(resizeTimer);
    }
    resizeTimer = window.setTimeout(() => {
      requestAnimationFrame(() => {
        graphChart?.resize();
        minimapChart?.resize();
      });
    }, 300);
  });

  resizeObserver.observe(graphRef.value);
};

// ==================== 右键菜单 ====================
const handleGraphContextMenu = (event: MouseEvent) => {
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY
  };

  // 点击其他地方关闭菜单
  const closeMenu = () => {
    contextMenu.value.visible = false;
    document.removeEventListener('click', closeMenu);
  };
  setTimeout(() => {
    document.addEventListener('click', closeMenu);
  }, 100);
};

const markNodeAsMastered = () => {
  if (selectedNode.value) {
    selectedNode.value.masteryRate = 100;
    selectedNode.value.marked = true;
    updateGraph();
    ElMessage.success('已标记为已掌握');
  }
};

const addToLearningPlan = () => {
  if (selectedNode.value) {
    ElMessage.success(`已添加到学习计划: ${selectedNode.value.name}`);
  }
};

const viewNodeDetail = () => {
  if (selectedNode.value) {
    // 滚动到详情区域
    document.querySelector('.detail-card')?.scrollIntoView({ behavior: 'smooth' });
  }
};

const expandNode = () => {
  ElMessage.info('展开子节点');
  // TODO: 实现逻辑
};

const collapseNode = () => {
  ElMessage.info('收起子节点');
  // TODO: 实现逻辑
};

// ==================== 学习路径 ====================
const generateLearningPath = async () => {
  generatingPath.value = true;
  try {
    // 模拟 AI 生成
    await new Promise(resolve => setTimeout(resolve, 1500));

    // 生成多条路径
    learningPaths.value = [
      {
        difficulty: '简单',
        estimatedTime: '1.5小时',
        progress: 0,
        steps: [
          { name: '函数定义', description: '理解函数基本概念', completed: false, current: true },
          { name: '函数性质', description: '掌握单调性', completed: false, current: false },
          { name: '函数应用', description: '简单应用', completed: false, current: false }
        ]
      },
      {
        difficulty: '中等',
        estimatedTime: '2小时',
        progress: 0,
        steps: [
          { name: '函数定义', description: '深入理解函数概念', completed: false, current: true },
          { name: '函数性质', description: '掌握单调性和奇偶性', completed: false, current: false },
          { name: '函数图像', description: '理解函数图像', completed: false, current: false },
          { name: '函数应用', description: '实际问题应用', completed: false, current: false }
        ]
      },
      {
        difficulty: '困难',
        estimatedTime: '3小时',
        progress: 0,
        steps: [
          { name: '函数定义', description: '完全掌握函数概念', completed: false, current: true },
          { name: '函数性质', description: '掌握所有性质', completed: false, current: false },
          { name: '函数变换', description: '理解函数变换', completed: false, current: false },
          { name: '函数综合', description: '综合应用', completed: false, current: false },
          { name: '高级应用', description: '复杂问题求解', completed: false, current: false }
        ]
      }
    ];

    selectedPathIndex.value = 1; // 默认选中等
    switchLearningPath();

    ElMessage.success('AI学习路径已生成');
  } finally {
    generatingPath.value = false;
  }
};

const switchLearningPath = () => {
  const path = learningPaths.value[selectedPathIndex.value];
  if (path) {
    learningPath.value = path.steps;
    currentPathInfo.value = {
      difficulty: path.difficulty,
      estimatedTime: path.estimatedTime,
      progress: path.progress
    };
  }
};

const getStepStatus = (step: any) => {
  if (step.completed) return 'success';
  if (step.current) return 'process';
  return 'wait';
};

const getDifficultyType = (difficulty: string) => {
  const types: { [key: string]: any } = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  };
  return types[difficulty] || 'info';
};

const startLearningPath = () => {
  ElMessage.success('开始按照推荐路径学习');
  // TODO: 实现学习路径跟踪
};

const exportLearningPathPDF = async () => {
  ElMessage.info('正在生成PDF报告...');
  // TODO: 实现PDF导出
  await new Promise(resolve => setTimeout(resolve, 1000));
  ElMessage.success('PDF报告已生成');
};

const shareLearningPath = () => {
  // 生成分享链接
  shareLink.value = `https://example.com/share/${Date.now()}`;
  showShareDialog.value = true;

  // TODO: 生成二维码
  nextTick(() => {
    if (qrcodeRef.value) {
      // 使用二维码库生成
      qrcodeRef.value.innerHTML = '<p>二维码占位</p>';
    }
  });
};

const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value);
  ElMessage.success('分享链接已复制');
};

// ==================== 导出功能 ====================
const handleExport = async () => {
  exporting.value = true;
  try {
    switch (exportFormat.value) {
      case 'png':
        await exportAsPNG();
        break;
      case 'svg':
        await exportAsSVG();
        break;
      case 'json':
        await exportAsJSON();
        break;
      case 'xmind':
        await exportAsXMind();
        break;
      case 'pdf':
        await exportAsPDF();
        break;
    }
    ElMessage.success('导出成功');
    showExportDialog.value = false;
  } catch (error) {
    ElMessage.error('导出失败');
    console.error(error);
  } finally {
    exporting.value = false;
  }
};

const exportAsPNG = async () => {
  if (!graphChart) return;

  const url = graphChart.getDataURL({
    type: 'png',
    pixelRatio: exportQuality.value,
    backgroundColor: '#fff'
  });

  downloadFile(url, `knowledge-graph-${Date.now()}.png`);
};

const exportAsSVG = async () => {
  if (!graphChart) return;

  const url = graphChart.getDataURL({
    type: 'svg',
    backgroundColor: '#fff'
  });

  downloadFile(url, `knowledge-graph-${Date.now()}.svg`);
};

const exportAsJSON = async () => {
  const data: any = {
    subject: selectedSubject.value,
    exportTime: new Date().toISOString()
  };

  if (exportOptions.value.includes('nodes')) {
    data.nodes = knowledgeTree.value;
  }
  if (exportOptions.value.includes('links')) {
    data.links = buildGraphData().links;
  }
  if (exportOptions.value.includes('progress')) {
    data.progress = {
      total: totalKnowledgePoints.value,
      mastered: masteredPoints.value,
      learning: learningPoints.value,
      unlearned: unlearnedPoints.value
    };
  }
  if (exportOptions.value.includes('stats')) {
    data.stats = {
      overallMasteryRate: overallMasteryRate.value
    };
  }

  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  downloadFile(url, `knowledge-graph-${Date.now()}.json`);
  URL.revokeObjectURL(url);
};

const exportAsXMind = async () => {
  // XMind 格式导出（简化版）
  const xmindData = convertToXMind(knowledgeTree.value);
  const blob = new Blob([JSON.stringify(xmindData, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  downloadFile(url, `knowledge-graph-${Date.now()}.xmind.json`);
  URL.revokeObjectURL(url);
};

const convertToXMind = (nodes: KnowledgeNode[]) => {
  // 简化的 XMind 格式转换
  return {
    root: {
      title: '知识图谱',
      children: nodes.map(node => ({
        title: node.name,
        children: node.children ? convertToXMind(node.children).root.children : []
      }))
    }
  };
};

const exportAsPDF = async () => {
  ElMessage.info('PDF导出功能需要安装额外的库');
  // TODO: 使用 jsPDF 或其他库实现
};

const downloadFile = (url: string, filename: string) => {
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// ==================== 节点操作 ====================
const getMasteryColor = (rate: number) => {
  if (rate >= 80) return '#67C23A';
  if (rate >= 60) return '#E6A23C';
  return '#F56C6C';
};

const jumpToNode = (node: KnowledgeNode) => {
  locateNode(node);
};

const toggleNodeMark = () => {
  if (selectedNode.value) {
    selectedNode.value.marked = !selectedNode.value.marked;

    // 保存标记状态
    const marks: { [key: string]: boolean } = JSON.parse(
      localStorage.getItem(STORAGE_KEYS.NODE_MARKS) || '{}'
    );
    marks[selectedNode.value.id] = selectedNode.value.marked!;
    localStorage.setItem(STORAGE_KEYS.NODE_MARKS, JSON.stringify(marks));

    ElMessage.success(selectedNode.value.marked ? '已标记为已掌握' : '已取消标记');
  }
};

const startLearning = () => {
  if (selectedNode.value) {
    ElMessage.success(`开始学习：${selectedNode.value.name}`);
  }
};

const startPractice = () => {
  if (selectedNode.value) {
    ElMessage.success(`开始练习：${selectedNode.value.name}`);
  }
};

// ==================== 设置 ====================
const saveSettings = () => {
  updateGraph();
  showSettingsDialog.value = false;
  ElMessage.success('设置已保存');
};

const resetSettings = () => {
  ElMessageBox.confirm('确定要恢复默认设置吗？', '提示', {
    type: 'warning'
  }).then(() => {
    graphSettings.value = { ...defaultSettings };
    updateGraph();
    ElMessage.success('已恢复默认设置');
  }).catch(() => {});
};

// ==================== 生命周期 ====================
onMounted(() => {
  loadFromStorage();
  loadKnowledgeGraph();
});

onBeforeUnmount(() => {
  if (graphChart) {
    graphChart.dispose();
  }
  if (minimapChart) {
    minimapChart.dispose();
  }
  if (resizeObserver) {
    resizeObserver.disconnect();
  }
  if (resizeTimer) {
    clearTimeout(resizeTimer);
  }
});

// 监听设置变化
watch(() => graphSettings.value.enableCache, (newVal) => {
  if (!newVal) {
    nodeCache.clear();
  }
});
</script>

<style scoped>
.knowledge-graph-page {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid var(--el-border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-text p {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 搜索结果下拉 */
.search-dropdown {
  position: fixed;
  top: 80px;
  right: 400px;
  width: 300px;
  max-height: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color);
  font-weight: 500;
}

.search-results {
  max-height: 350px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.search-result-item:hover {
  background: var(--el-fill-color-light);
}

.search-result-item mark {
  background: #fff3cd;
  padding: 2px 4px;
  border-radius: 2px;
}

/* 搜索历史 */
.search-history {
  position: fixed;
  top: 140px;
  right: 400px;
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  padding: 12px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 500;
}

.history-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-items .el-tag {
  cursor: pointer;
}

/* 主要内容 */
.page-content {
  padding: 20px;
}

/* 知识点列表卡片 */
.knowledge-list-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.list-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.knowledge-tree {
  max-height: 400px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  padding-right: 8px;
}

.node-label {
  flex: 1;
}

.node-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计卡片 */
.stats-card {
  border-radius: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px dashed var(--el-border-color);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-value {
  font-weight: 600;
  font-size: 18px;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.danger {
  color: #F56C6C;
}

.overall-progress p {
  margin: 12px 0 8px 0;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

/* 图谱卡片 */
.graph-card {
  border-radius: 12px;
  margin-bottom: 16px;
  position: relative;
}

.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.graph-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.graph-container {
  height: 500px;
  width: 100%;
}

.graph-minimap {
  position: absolute;
  bottom: 60px;
  right: 20px;
  width: 150px;
  height: 100px;
  border: 2px solid var(--el-border-color);
  border-radius: 4px;
  background: white;
  overflow: hidden;
}

.minimap-container {
  width: 100%;
  height: 100%;
}

.graph-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

/* 学习路径卡片 */
.path-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.path-tabs {
  margin-bottom: 20px;
}

.path-info {
  margin-bottom: 20px;
}

.learning-path {
  padding: 20px 0;
}

.path-steps {
  margin: 20px 0;
}

.path-actions {
  text-align: center;
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* 详情卡片 */
.detail-card {
  border-radius: 12px;
  position: sticky;
  top: 80px;
}

.node-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section h3 {
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.node-description {
  margin: 0 0 12px 0;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.node-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mastery-stats {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mastery-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.mastery-item .value {
  font-weight: 600;
  color: #667eea;
}

.related-points {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.related-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.related-tag:hover {
  transform: scale(1.05);
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.resource-item span {
  flex: 1;
  font-size: 14px;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-actions .el-button {
  width: 100%;
}

/* 分享对话框 */
.share-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.share-qrcode {
  width: 200px;
  height: 200px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.share-tips {
  width: 100%;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-left,
  .header-right {
    width: 100%;
  }

  .header-right {
    flex-wrap: wrap;
  }

  .graph-container {
    height: 400px;
  }

  .detail-card {
    position: static;
    margin-top: 16px;
  }

  .search-dropdown,
  .search-history {
    right: 20px;
    left: 20px;
    width: auto;
  }
}

/* 滚动条样式 */
.knowledge-tree::-webkit-scrollbar,
.search-results::-webkit-scrollbar {
  width: 6px;
}

.knowledge-tree::-webkit-scrollbar-thumb,
.search-results::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
}

.knowledge-tree::-webkit-scrollbar-thumb:hover,
.search-results::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-darker);
}
</style>
