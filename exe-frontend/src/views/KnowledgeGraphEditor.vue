<template>
  <div class="graph-container">
    <div class="toolbar">
      <el-select v-model="subjectId" placeholder="选择科目" @change="loadGraph" style="width: 200px;">
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-alert type="info" show-icon :closable="false" style="margin-left: 20px; width: auto;">
        提示：这是一个可视化图谱。节点代表知识点，箭头方向表示：前置 -> 后置。可拖拽节点。
      </el-alert>
    </div>

    <div class="main-area">
      <div ref="chartRef" class="chart-box"></div>

      <div class="edit-panel">
        <h3>关联编辑</h3>
        <el-form :inline="false" label-position="top">
          <el-form-item label="前置点(父)">
            <el-select v-model="form.parentId" filterable placeholder="选择前置点" style="width: 100%">
              <el-option v-for="n in nodes" :key="n.id" :label="n.realName || n.name" :value="Number(n.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="后置点(子)">
            <el-select v-model="form.childId" filterable placeholder="选择后置点" style="width: 100%">
              <el-option v-for="n in nodes" :key="n.id" :label="n.realName || n.name" :value="Number(n.id)" />
            </el-select>
          </el-form-item>
          <el-button type="primary" @click="submitRelation" style="width: 100%">添加关联</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import * as echarts from 'echarts';
import { fetchAllSubjects } from '@/api/subject';
import { fetchKnowledgeGraph, addKpRelation } from '@/api/knowledgePoint';
import { ElMessage } from 'element-plus';

const chartRef = ref<HTMLElement>();
let myChart: echarts.ECharts | null = null;

const subjects = ref<any[]>([]);
const subjectId = ref<number>();
const nodes = ref<any[]>([]);
const links = ref<any[]>([]);

const form = reactive({ parentId: undefined, childId: undefined });

const loadGraph = async () => {
  if (!subjectId.value) return;
  const res = await fetchKnowledgeGraph(subjectId.value);
  if (res.code === 200) {
    nodes.value = res.data.nodes;
    links.value = res.data.links;
    renderChart();
  }
};

const renderChart = () => {
  if (!chartRef.value) return;
  // 如果已经存在实例，先销毁，防止缓存问题
  if (myChart) myChart.dispose();

  myChart = echarts.init(chartRef.value);

  const option = {
    title: { text: '知识图谱可视化', top: 'bottom', left: 'right' },
    tooltip: {},
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        symbolSize: 50,
        roam: true,
        label: {
          show: true,
          // 【核心修复】使用 formatter 显示真实的中文名称 (realName)
          formatter: (params: any) => {
            return params.data.realName || params.name;
          }
        },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 10],
        data: nodes.value,
        links: links.value,
        force: {
          repulsion: 300,
          edgeLength: 150,
          gravity: 0.1
        },
        // 【核心修复】修复颜色配置错误，'source' 表示跟随源节点颜色
        lineStyle: {
          color: 'source',
          curveness: 0.3,
          width: 2
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: { width: 5 }
        }
      }
    ]
  };
  myChart.setOption(option);

  // 窗口大小改变时自适应
  window.addEventListener('resize', () => myChart?.resize());
};

const submitRelation = async () => {
  if (!form.parentId || !form.childId) {
    ElMessage.warning('请完整选择前置点和后置点');
    return;
  }
  try {
    await addKpRelation({ parentId: form.parentId, childId: form.childId });
    ElMessage.success('关联成功');
    loadGraph(); // 刷新图谱
  } catch(e) {
    // 错误已由 request 拦截器处理
  }
};

onMounted(async () => {
  const res = await fetchAllSubjects();
  subjects.value = res.data;
});
</script>

<style scoped>
.graph-container { padding: 20px; height: calc(100vh - 80px); display: flex; flex-direction: column; }
.toolbar { display: flex; align-items: center; margin-bottom: 10px; }
.main-area { flex: 1; display: flex; position: relative; border: 1px solid #dcdfe6; border-radius: 8px; background: #f9f9f9; }
.chart-box { width: 100%; height: 100%; }
.edit-panel {
  position: absolute;
  right: 20px;
  top: 20px;
  width: 240px;
  background: rgba(255,255,255,0.95);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  backdrop-filter: blur(5px);
}
.edit-panel h3 { margin-top: 0; margin-bottom: 15px; font-size: 16px; color: #303133; border-left: 4px solid #409eff; padding-left: 10px; }
</style>