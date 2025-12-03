<template>
  <div class="graph-container">
    <div class="toolbar">
      <el-select v-model="subjectId" placeholder="选择科目" @change="loadGraph">
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-alert type="info" show-icon :closable="false" style="margin-left: 20px; width: auto;">
        提示：这是一个可视化图谱。节点代表知识点，箭头方向表示：前置 -> 后置。
      </el-alert>
    </div>

    <div class="main-area">
      <div ref="chartRef" class="chart-box"></div>

      <div class="edit-panel">
        <h3>关联编辑</h3>
        <el-form inline>
          <el-form-item label="前置点(父)">
            <el-select v-model="form.parentId" filterable placeholder="选择前置点">
              <el-option v-for="n in nodes" :key="n.id" :label="n.name" :value="Number(n.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="后置点(子)">
            <el-select v-model="form.childId" filterable placeholder="选择后置点">
              <el-option v-for="n in nodes" :key="n.id" :label="n.name" :value="Number(n.id)" />
            </el-select>
          </el-form-item>
          <el-button type="primary" @click="submitRelation">添加关联</el-button>
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
  if (!myChart) myChart = echarts.init(chartRef.value);

  const option = {
    tooltip: {},
    series: [
      {
        type: 'graph',
        layout: 'force',
        symbolSize: 40,
        roam: true,
        label: { show: true },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 10],
        data: nodes.value,
        links: links.value,
        force: { repulsion: 200, edgeLength: 120 },
        lineStyle: { color: '#source', curveness: 0.3 }
      }
    ]
  };
  myChart.setOption(option);
};

const submitRelation = async () => {
  if (!form.parentId || !form.childId) return;
  await addKpRelation({ parentId: form.parentId, childId: form.childId });
  ElMessage.success('关联成功');
  loadGraph(); // 刷新
};

onMounted(async () => {
  const res = await fetchAllSubjects();
  subjects.value = res.data;
});
</script>

<style scoped>
.graph-container { padding: 20px; height: calc(100vh - 100px); display: flex; flex-direction: column; }
.toolbar { display: flex; align-items: center; margin-bottom: 10px; }
.main-area { flex: 1; display: flex; position: relative; border: 1px solid #eee; }
.chart-box { width: 100%; height: 100%; }
.edit-panel { position: absolute; right: 10px; top: 10px; background: rgba(255,255,255,0.9); padding: 15px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
</style>