<template>
  <div class="graph-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="28"><Share /></el-icon>
        </div>
        <div>
          <h2>知识图谱编辑器</h2>
          <p>可视化知识点关联关系，构建完整知识体系</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="Refresh" @click="handleRefresh" :loading="loading">刷新</el-button>
        <el-button icon="Download" type="primary" plain @click="handleExport">导出图谱</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-select v-model="subjectId" placeholder="选择科目" @change="loadGraph" style="width: 200px;" size="large">
        <template #prefix><el-icon><Collection /></el-icon></template>
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <div class="tips">
        <el-tag type="info" effect="dark" round>
          <el-icon><InfoFilled /></el-icon> 节点大小代表关联强度，拖拽可调整布局
        </el-tag>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-nodes">
          <div class="stat-icon">
            <el-icon :size="32"><Grid /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">知识点数量</div>
            <div class="stat-value">{{ nodes.length }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-links">
          <div class="stat-icon">
            <el-icon :size="32"><Connection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">关联数量</div>
            <div class="stat-value">{{ links.length }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-core">
          <div class="stat-icon">
            <el-icon :size="32"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">核心节点</div>
            <div class="stat-value">{{ coreNodeCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-subject">
          <div class="stat-icon">
            <el-icon :size="32"><Collection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">当前科目</div>
            <div class="stat-value">{{ currentSubjectName }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="main-area">
      <div ref="chartRef" class="chart-box"></div>

      <div class="edit-panel">
        <h3>🔗 关联编辑</h3>
        <el-form :inline="false" label-position="top" size="default">
          <el-form-item label="前置知识点 (父)">
            <el-select v-model="form.parentId" filterable placeholder="请选择或点击图谱节点" style="width: 100%">
              <el-option v-for="n in nodes" :key="n.id" :label="n.realName || n.name" :value="Number(n.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="后置知识点 (子)">
            <el-select v-model="form.childId" filterable placeholder="请选择或点击图谱节点" style="width: 100%">
              <el-option v-for="n in nodes" :key="n.id" :label="n.realName || n.name" :value="Number(n.id)" />
            </el-select>
          </el-form-item>
          <div class="btn-group">
            <el-button type="primary" @click="submitRelation" icon="Link" style="flex: 1;">建立关联</el-button>
            <el-button type="danger" plain @click="handleRemoveRelation" icon="Link" style="flex: 1;">断开</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick, computed } from 'vue';
import * as echarts from 'echarts';
import { fetchAllSubjects } from '@/api/subject';
import { fetchKnowledgeGraph, addKpRelation, removeKpRelation } from '@/api/knowledgePoint';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Collection, InfoFilled, Link, Share, Grid, Connection, Star, Refresh, Download } from '@element-plus/icons-vue';

const loading = ref(false);
const chartRef = ref<HTMLElement>();
let myChart: echarts.ECharts | null = null;

const subjects = ref<any[]>([]);
const subjectId = ref<number>();
const nodes = ref<any[]>([]);
const links = ref<any[]>([]);

const form = reactive({ parentId: undefined as number | undefined, childId: undefined as number | undefined });

// 计算属性
const coreNodeCount = computed(() => {
  return nodes.value.filter(n => n.degree > 4).length;
});

const currentSubjectName = computed(() => {
  const subject = subjects.value.find(s => s.id === subjectId.value);
  return subject ? subject.name : '未选择';
});

// 炫酷的配色方案
const colors = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'
];

const loadGraph = async () => {
  if (!subjectId.value) return;
  loading.value = true;
  try {
    const res = await fetchKnowledgeGraph(subjectId.value);
    if (res.code === 200) {
      // 对数据进行预处理，计算权重和类别
      const { processedNodes, processedLinks, categories } = processData(res.data.nodes, res.data.links);
      nodes.value = processedNodes;
      links.value = processedLinks;
      renderChart(categories);
    }
  } catch (error) {
    ElMessage.error('加载知识图谱失败');
  } finally {
    loading.value = false;
  }
};

// 数据预处理：计算节点度数（连接数），以此决定大小
const processData = (rawNodes: any[], rawLinks: any[]) => {
  const nodeMap = new Map();

  // 初始化节点数据
  rawNodes.forEach(n => {
    nodeMap.set(String(n.id), { ...n, degree: 0 });
  });

  // 计算度数
  rawLinks.forEach(l => {
    if (nodeMap.has(String(l.source))) nodeMap.get(String(l.source)).degree++;
    if (nodeMap.has(String(l.target))) nodeMap.get(String(l.target)).degree++;
  });

  // 生成类别（简单的逻辑：按度数分层，或者直接统一为一个类别）
  const categories = [{ name: '核心考点' }, { name: '基础知识' }, { name: '扩展点' }];

  const processedNodes = Array.from(nodeMap.values()).map((n, index) => {
    // 根据度数计算大小：最小 20，每多一个连接 +5，最大 60
    const size = Math.min(60, 20 + n.degree * 5);

    // 简单的分类逻辑
    let categoryIndex = 2;
    if (n.degree > 4) categoryIndex = 0;
    else if (n.degree > 2) categoryIndex = 1;

    return {
      ...n,
      symbolSize: size,
      category: categoryIndex,
      // 节点样式：添加发光效果
      itemStyle: {
        shadowBlur: 10,
        shadowColor: colors[index % colors.length] // 自身颜色的光晕
      },
      label: {
        show: n.degree > 1, // 连接少的节点平时不显示文字，减少杂乱
      }
    };
  });

  return { processedNodes, processedLinks: rawLinks, categories };
};

const renderChart = (categories: any[]) => {
  if (!chartRef.value) return;
  if (myChart) myChart.dispose();

  myChart = echarts.init(chartRef.value);

  const option = {
    // 背景色，为了突出光晕，可以使用稍暗的背景，或者利用CSS设置
    backgroundColor: 'rgba(255,255,255,0.5)',
    title: {
      text: '知识图谱可视化',
      subtext: 'Knowledge Graph',
      top: 20,
      left: 20,
      textStyle: { color: '#333', fontSize: 18, fontWeight: 'bold' }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          const n = params.data;
          return `<b>${n.realName}</b><br/>编码: ${n.value}<br/>关联数: ${n.degree}`;
        }
        return `${params.name}`; // 连线提示
      },
      backgroundColor: 'rgba(50,50,50,0.7)',
      textStyle: { color: '#fff' },
      borderColor: '#777'
    },
    legend: {
      data: categories.map(a => a.name),
      bottom: 20,
      right: 20,
      textStyle: { color: '#666' }
    },
    animationDuration: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: nodes.value,
        links: links.value,
        categories: categories,
        roam: true, // 允许缩放和平移
        draggable: true, // 允许拖拽节点

        // 核心特效配置
        label: {
          position: 'right',
          formatter: '{b}', // 显示 realName 已经在 processData 中 map 到了 name 属性(如果后端没改)，或者需要在 formatter 里处理
          fontSize: 12,
          color: '#333',
          fontWeight: 'bold'
        },
        edgeSymbol: ['none', 'arrow'], // 箭头
        edgeSymbolSize: [4, 8],
        edgeLabel: {
          fontSize: 10
        },

        // 力引导布局配置
        force: {
          repulsion: 400, // 斥力，越大节点分得越开
          gravity: 0.05,  // 引力，越小越不容易聚成一团
          edgeLength: 120, // 边的长度
          layoutAnimation: true
        },

        // 连线样式
        lineStyle: {
          color: 'source', // 连线颜色跟随源节点
          curveness: 0.3,  // 曲线程度，0为直线
          width: 2,
          opacity: 0.7
        },

        // 高亮样式 (悬停时)
        emphasis: {
          focus: 'adjacency', // 聚焦关系：只显示与当前节点相连的点和线
          lineStyle: {
            width: 4,
            opacity: 1
          },
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(0,0,0,0.8)'
          }
        }
      }
    ]
  };

  // 修正 Label formatter，因为 ECharts data.name 必须是 ID 才能匹配 link，
  // 所以我们要在 label.formatter 里显示真正的中文名
  (option.series[0].label as any).formatter = (params: any) => {
    return params.data.realName;
  };

  myChart.setOption(option);

  // 点击事件
  myChart.on('click', (params: any) => {
    if (params.dataType === 'edge') {
      form.parentId = Number(params.data.source);
      form.childId = Number(params.data.target);
      ElMessage.info('已选中关联，点击“断开”即可删除');
    } else if (params.dataType === 'node') {
      // 智能填入：如果空，填入父节点；如果父节点有值，填入子节点
      if (!form.parentId) {
        form.parentId = Number(params.data.id);
      } else if (form.parentId !== Number(params.data.id)) {
        form.childId = Number(params.data.id);
      }
    }
  });

  window.addEventListener('resize', () => myChart?.resize());
};

const submitRelation = async () => {
  if (!form.parentId || !form.childId) {
    ElMessage.warning('请完整选择前置点和后置点');
    return;
  }
  try {
    await addKpRelation({ parentId: form.parentId, childId: form.childId });
    ElMessage.success('关联建立成功');
    loadGraph();
    // 不清空表单，方便连续操作，或者根据需求清空
  } catch(e) { }
};

const handleRemoveRelation = async () => {
  if (!form.parentId || !form.childId) {
    ElMessage.warning('请先选择要解除关联的两个节点');
    return;
  }
  try {
    await ElMessageBox.confirm('确定要切断这两个知识点之间的联系吗？', '提示', {
      confirmButtonText: '断开',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await removeKpRelation({ parentId: form.parentId, childId: form.childId });
    ElMessage.success('关联已断开');
    loadGraph();
  } catch (e) { }
};

const handleRefresh = () => {
  if (subjectId.value) {
    loadGraph();
    ElMessage.success('刷新成功');
  } else {
    ElMessage.warning('请先选择科目');
  }
};

const handleExport = () => {
  ElMessage.info('导出功能开发中...');
};

onMounted(async () => {
  const res = await fetchAllSubjects();
  subjects.value = res.data;
});
</script>

<style scoped>
/* 页面容器 */
.graph-container {
  padding: 24px;
  height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #303133;
}

.page-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  background: rgba(255, 255, 255, 0.8);
  padding: 10px 20px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.tips {
  margin-left: 20px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

/* 统计卡片颜色 */
.stat-nodes::before { background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%); }
.stat-nodes .stat-icon {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(102, 177, 255, 0.1) 100%);
  color: #409eff;
}
.stat-nodes .stat-value { color: #409eff; }

.stat-links::before { background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%); }
.stat-links .stat-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(133, 206, 97, 0.1) 100%);
  color: #67c23a;
}
.stat-links .stat-value { color: #67c23a; }

.stat-core::before { background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%); }
.stat-core .stat-icon {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(235, 181, 99, 0.1) 100%);
  color: #e6a23c;
}
.stat-core .stat-value { color: #e6a23c; }

.stat-subject::before { background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); }
.stat-subject .stat-icon {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}
.stat-subject .stat-value {
  color: #667eea;
  font-size: 18px;
}

.main-area {
  flex: 1;
  display: flex;
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  background: #ffffff;
}

.chart-box {
  width: 100%;
  height: 100%;
  /* 可以添加一个网格背景增加科技感 */
  background-image:
      linear-gradient(rgba(200, 200, 200, 0.1) 1px, transparent 1px),
      linear-gradient(90deg, rgba(200, 200, 200, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
}

.edit-panel {
  position: absolute;
  right: 20px;
  top: 20px;
  width: 280px;
  background: rgba(255, 255, 255, 0.9);
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: transform 0.3s;
}
.edit-panel:hover {
  transform: translateY(-2px);
}

.edit-panel h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 16px;
  color: #303133;
  border-left: 4px solid #409eff;
  padding-left: 10px;
  font-weight: 700;
}

.btn-group {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .graph-container {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 24px;
  }

  .edit-panel {
    position: static;
    width: 100%;
    margin-top: 20px;
  }
}
</style>