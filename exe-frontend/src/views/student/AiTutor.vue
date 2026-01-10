<template>
  <div class="ai-tutor-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon :size="32" color="#667eea"><Reading /></el-icon>
        <div class="title-text">
          <h1>📖 AI智能家教</h1>
          <p>个性化学习辅导，知识点精讲</p>
        </div>
      </div>
      <div class="header-right">
        <el-button :icon="Notebook" @click="showNotesDialog = true">我的笔记</el-button>
        <el-button :icon="Clock" @click="showStudyStats = true">学习统计</el-button>
        <el-dropdown @command="handleCommand">
          <el-button :icon="Setting">设置</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="fontSize">字体大小</el-dropdown-item>
              <el-dropdown-item command="theme">主题切换</el-dropdown-item>
              <el-dropdown-item command="export">导出笔记</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="page-content">
      <el-row :gutter="20">
        <!-- 左侧：课程目录 -->
        <el-col :xs="24" :sm="24" :md="6" :lg="6">
          <el-card class="course-list" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>📚 课程目录</span>
                <el-badge :value="completedCount + '/' + totalSections" class="badge" />
              </div>
            </template>

            <!-- 科目选择 -->
            <el-select
              v-model="currentSubject"
              @change="handleSubjectChange"
              style="width: 100%; margin-bottom: 16px"
              size="large"
            >
              <el-option
                v-for="subject in subjects"
                :key="subject.id"
                :label="subject.name"
                :value="subject.id"
              >
                <span>{{ subject.icon }} {{ subject.name }}</span>
              </el-option>
            </el-select>

            <!-- 章节列表 -->
            <div class="chapter-list">
              <div
                v-for="chapter in currentChapters"
                :key="chapter.id"
                class="chapter-item"
                :class="{ active: currentChapter?.id === chapter.id }"
                @click="selectChapter(chapter)"
              >
                <div class="chapter-header">
                  <el-icon v-if="chapter.completed" color="#67C23A"><CircleCheck /></el-icon>
                  <el-icon v-else color="#909399"><Clock /></el-icon>
                  <span class="chapter-name">{{ chapter.name }}</span>
                </div>
                <div class="chapter-meta">
                  <el-tag size="small" :type="getDifficultyType(chapter.difficulty)">
                    {{ chapter.difficulty }}
                  </el-tag>
                  <span class="time">{{ chapter.estimatedTime }}分钟</span>
                </div>
                <el-progress
                  :percentage="chapter.progress"
                  :stroke-width="4"
                  :show-text="false"
                />
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 中间：学习内容区 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card class="learning-area" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>{{ currentChapter?.name || '请选择章节开始学习' }}</span>
                <div v-if="currentChapter">
                  <el-button
                    :icon="currentChapter.completed ? Refresh : Check"
                    type="primary"
                    size="small"
                    @click="toggleChapterComplete"
                  >
                    {{ currentChapter.completed ? '重新学习' : '标记完成' }}
                  </el-button>
                </div>
              </div>
            </template>

            <!-- 空状态 -->
            <el-empty
              v-if="!currentChapter"
              description="请从左侧选择章节开始学习"
              :image-size="200"
            />

            <!-- 学习内容 -->
            <div v-else class="learning-content" :style="{ fontSize: fontSizeMap[currentFontSize] }">
              <!-- 章节信息 -->
              <div class="chapter-info">
                <el-alert
                  :title="currentChapter.description"
                  type="info"
                  :closable="false"
                  show-icon
                />
              </div>

              <!-- 知识点内容 -->
              <div class="knowledge-sections">
                <div
                  v-for="section in currentChapter.sections"
                  :key="section.id"
                  class="section-block"
                >
                  <div class="section-header" @click="toggleSection(section)">
                    <h3>
                      <el-icon><Document /></el-icon>
                      {{ section.title }}
                    </h3>
                    <el-icon class="toggle-icon" :class="{ rotated: section.expanded }">
                      <ArrowDown />
                    </el-icon>
                  </div>

                  <el-collapse-transition>
                    <div v-show="section.expanded" class="section-content">
                      <!-- 知识点讲解 -->
                      <div class="content-text" v-html="section.content"></div>

                      <!-- 例题 -->
                      <div v-if="section.examples && section.examples.length" class="examples">
                        <h4>📌 例题</h4>
                        <div
                          v-for="(example, idx) in section.examples"
                          :key="idx"
                          class="example-item"
                          v-html="example"
                        ></div>
                      </div>

                      <!-- 练习题 -->
                      <div v-if="section.exercises && section.exercises.length" class="exercises">
                        <h4>✏️ 练习题</h4>
                        <div
                          v-for="(exercise, idx) in section.exercises"
                          :key="idx"
                          class="exercise-item"
                        >
                          <p class="question">{{ idx + 1 }}. {{ exercise.question }}</p>
                          <el-input
                            v-model="exercise.userAnswer"
                            placeholder="请输入答案"
                            class="answer-input"
                          />
                          <div class="exercise-actions">
                            <el-button
                              size="small"
                              type="primary"
                              @click="checkAnswer(section, exercise)"
                              :disabled="!exercise.userAnswer"
                            >
                              检查答案
                            </el-button>
                            <el-button
                              v-if="exercise.checked"
                              size="small"
                              @click="exercise.showExplanation = !exercise.showExplanation"
                            >
                              {{ exercise.showExplanation ? '隐藏' : '查看' }}解析
                            </el-button>
                          </div>

                          <!-- 答题反馈 -->
                          <div v-if="exercise.checked" class="feedback">
                            <el-alert
                              :title="exercise.correct ? '✅ 回答正确！' : '❌ 回答错误'"
                              :type="exercise.correct ? 'success' : 'error'"
                              :closable="false"
                              show-icon
                            >
                              <template v-if="!exercise.correct">
                                正确答案：{{ exercise.answer }}
                              </template>
                            </el-alert>
                          </div>

                          <!-- 答案解析 -->
                          <div v-if="exercise.showExplanation" class="explanation">
                            <el-alert type="info" :closable="false">
                              <template #title>
                                <strong>💡 解析</strong>
                              </template>
                              {{ exercise.explanation }}
                            </el-alert>
                          </div>
                        </div>
                      </div>

                      <!-- 快速笔记 -->
                      <div class="quick-note">
                        <el-button
                          :icon="Edit"
                          size="small"
                          @click="openQuickNote(section)"
                        >
                          记笔记
                        </el-button>
                      </div>
                    </div>
                  </el-collapse-transition>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：AI助手和工具 -->
        <el-col :xs="24" :sm="24" :md="6" :lg="6">
          <!-- AI问答助手 -->
          <el-card class="ai-assistant" shadow="hover">
            <template #header>
              <span>🤖 AI答疑助手</span>
            </template>
            <div class="ai-qa">
              <el-input
                v-model="aiQuestion"
                type="textarea"
                :rows="3"
                placeholder="有什么不懂的问题，问我吧..."
              />
              <el-button
                type="primary"
                style="width: 100%; margin-top: 12px"
                @click="askAI"
                :loading="aiLoading"
              >
                提问
              </el-button>

              <!-- AI回答历史 -->
              <div class="qa-history" v-if="qaHistory.length">
                <el-divider content-position="left">历史问答</el-divider>
                <div
                  v-for="(qa, idx) in qaHistory"
                  :key="idx"
                  class="qa-item"
                >
                  <div class="question-text">
                    <el-icon><QuestionFilled /></el-icon>
                    {{ qa.question }}
                  </div>
                  <div class="answer-text">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ qa.answer }}
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 学习目标 -->
          <el-card class="study-goals" shadow="hover" style="margin-top: 16px">
            <template #header>
              <div class="card-header">
                <span>🎯 今日目标</span>
                <el-button :icon="Plus" size="small" text @click="addGoal">添加</el-button>
              </div>
            </template>
            <div class="goals-list">
              <div
                v-for="goal in dailyGoals"
                :key="goal.id"
                class="goal-item"
              >
                <el-checkbox
                  v-model="goal.completed"
                  @change="saveProgress"
                >
                  {{ goal.title }}
                </el-checkbox>
                <el-button
                  :icon="Delete"
                  size="small"
                  text
                  @click="removeGoal(goal.id)"
                />
              </div>
              <el-empty
                v-if="!dailyGoals.length"
                description="暂无学习目标"
                :image-size="80"
              />
            </div>
          </el-card>

          <!-- 知识卡片 -->
          <el-card class="knowledge-cards" shadow="hover" style="margin-top: 16px">
            <template #header>
              <span>🎴 知识卡片</span>
            </template>
            <div class="cards-list">
              <div
                v-for="card in knowledgeCards"
                :key="card.id"
                class="card-item"
                @click="flipCard(card)"
              >
                <div class="card-front" v-show="!card.flipped">
                  <strong>{{ card.term }}</strong>
                </div>
                <div class="card-back" v-show="card.flipped">
                  <p>{{ card.definition }}</p>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 笔记对话框 -->
    <el-dialog
      v-model="showNotesDialog"
      title="📝 我的笔记"
      width="70%"
      :close-on-click-modal="false"
    >
      <div class="notes-container">
        <!-- 笔记列表 -->
        <div class="notes-list">
          <el-input
            v-model="noteSearch"
            placeholder="搜索笔记..."
            :prefix-icon="Search"
            style="margin-bottom: 16px"
          />
          <div
            v-for="note in filteredNotes"
            :key="note.id"
            class="note-item"
            :class="{ active: currentNote?.id === note.id }"
            @click="selectNote(note)"
          >
            <div class="note-header">
              <strong>{{ note.title }}</strong>
              <el-tag v-if="note.tag" size="small">{{ note.tag }}</el-tag>
            </div>
            <p class="note-preview">{{ note.preview }}</p>
            <span class="note-time">{{ note.time }}</span>
          </div>
          <el-empty v-if="!filteredNotes.length" description="暂无笔记" />
        </div>

        <!-- 笔记编辑 -->
        <div class="note-editor">
          <el-input
            v-model="noteTitle"
            placeholder="笔记标题"
            style="margin-bottom: 12px"
          />
          <el-select
            v-model="noteTag"
            placeholder="选择标签"
            style="width: 100%; margin-bottom: 12px"
          >
            <el-option label="重要" value="重要" />
            <el-option label="易错" value="易错" />
            <el-option label="技巧" value="技巧" />
            <el-option label="总结" value="总结" />
          </el-select>
          <el-input
            v-model="noteContent"
            type="textarea"
            :rows="15"
            placeholder="在这里记录你的学习笔记..."
          />
          <div class="note-actions">
            <el-button @click="clearNote">清空</el-button>
            <el-button type="primary" @click="saveNote">
              {{ currentNote ? '更新笔记' : '保存笔记' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 学习统计对话框 -->
    <el-dialog
      v-model="showStudyStats"
      title="📊 学习统计"
      width="600px"
    >
      <div class="study-stats">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-statistic title="累计学习时长" :value="totalStudyTime" suffix="分钟" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="完成章节" :value="completedCount" :suffix="'/ ' + totalSections" />
          </el-col>
        </el-row>
        <el-divider />
        <el-row :gutter="20">
          <el-col :span="12">
            <el-statistic title="今日学习" :value="todayStudyTime" suffix="分钟" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="笔记数量" :value="notes.length" suffix="条" />
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 添加目标对话框 -->
    <el-dialog
      v-model="showAddGoalDialog"
      title="添加学习目标"
      width="400px"
    >
      <el-input
        v-model="newGoalTitle"
        placeholder="输入学习目标..."
        @keyup.enter="confirmAddGoal"
      />
      <template #footer>
        <el-button @click="showAddGoalDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddGoal">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Reading, Notebook, Clock, Setting, CircleCheck, Check, Refresh,
  Document, ArrowDown, Edit, Delete, Plus, QuestionFilled,
  ChatDotRound, Search
} from '@element-plus/icons-vue';
import { answerQuestion, explainKnowledgePoint, tutorExercise } from '@/api/aiTutor';

// ===== 类型定义 =====
interface Subject {
  id: string;
  name: string;
  icon: string;
}

interface Exercise {
  question: string;
  answer: string;
  explanation: string;
  userAnswer?: string;
  checked?: boolean;
  correct?: boolean;
  showExplanation?: boolean;
}

interface Section {
  id: number;
  title: string;
  content: string;
  examples?: string[];
  exercises?: Exercise[];
  expanded: boolean;
}

interface Chapter {
  id: number;
  name: string;
  description: string;
  difficulty: string;
  estimatedTime: number;
  progress: number;
  completed: boolean;
  sections: Section[];
}

interface Note {
  id: number;
  title: string;
  content: string;
  preview: string;
  tag?: string;
  time: string;
  chapterId?: number;
  timestamp: number;
}

interface Goal {
  id: number;
  title: string;
  completed: boolean;
}

interface KnowledgeCard {
  id: number;
  term: string;
  definition: string;
  flipped?: boolean;
}

interface QA {
  question: string;
  answer: string;
  timestamp: number;
}

// ===== 响应式数据 =====
// 科目和章节
const subjects = ref<Subject[]>([
  { id: 'math', name: '数学', icon: '📐' },
  { id: 'physics', name: '物理', icon: '⚛️' },
  { id: 'chemistry', name: '化学', icon: '🧪' },
  { id: 'english', name: '英语', icon: '📚' },
  { id: 'chinese', name: '语文', icon: '📖' },
  { id: 'biology', name: '生物', icon: '🧬' }
]);

const currentSubject = ref('math');
const currentChapter = ref<Chapter | null>(null);

// 数学课程数据
const mathChapters = ref<Chapter[]>([
  {
    id: 1,
    name: '第1章 函数基础',
    description: '学习函数的定义、表示方法和基本性质',
    difficulty: '基础',
    estimatedTime: 90,
    progress: 0,
    completed: false,
    sections: [
      {
        id: 1,
        title: '1.1 函数的概念',
        expanded: true,
        content: `
          <h4>函数的定义</h4>
          <p>在数学中，函数描述了两个变量之间的对应关系。设A、B是非空数集，如果按照某种确定的对应关系f，使对于集合A中的<strong>任意一个</strong>数x，在集合B中都有<strong>唯一确定</strong>的数f(x)和它对应，那么就称f：A→B为从集合A到集合B的一个函数。</p>
          <p>函数的三要素：</p>
          <ul>
            <li><strong>定义域</strong>：自变量x的取值范围</li>
            <li><strong>值域</strong>：函数值y的取值范围</li>
            <li><strong>对应法则</strong>：x与y之间的对应关系</li>
          </ul>
        `,
        examples: [
          '<p><strong>例1：</strong>判断下列对应是否为函数？</p><p>(1) y = 2x + 1，x ∈ R</p><p>答：是函数，因为对于任意实数x，都有唯一的y值与之对应。</p>',
          '<p><strong>例2：</strong>y² = x，x ≥ 0</p><p>答：不是函数，因为对于同一个x值（如x=4），有两个y值（y=2或y=-2）与之对应。</p>'
        ],
        exercises: [
          {
            question: '判断：y = √x 是否为函数？',
            answer: '是',
            explanation: '对于定义域内（x≥0）的每一个x值，都有唯一的非负实数y与之对应，符合函数定义。'
          },
          {
            question: '函数 f(x) = 1/x 的定义域是什么？',
            answer: 'x≠0或R\\{0}或(-∞,0)∪(0,+∞)',
            explanation: '分母不能为0，所以x不能等于0，定义域为除0外的所有实数。'
          }
        ]
      },
      {
        id: 2,
        title: '1.2 函数的表示方法',
        expanded: false,
        content: `
          <h4>函数的三种表示方法</h4>
          <p><strong>1. 解析式法（公式法）</strong></p>
          <p>用数学表达式表示函数的对应关系，如 f(x) = x² + 2x + 1</p>
          <p><strong>2. 列表法</strong></p>
          <p>用表格的形式列举自变量与函数值的对应关系</p>
          <p><strong>3. 图像法</strong></p>
          <p>用坐标平面上的图形表示函数</p>
          <p>三种方法各有优缺点，在实际应用中可以根据需要选择合适的表示方法。</p>
        `,
        examples: [
          '<p><strong>例：</strong>某商品打8折销售，原价x元，现价y元，用三种方法表示这个函数。</p><p>解析式：y = 0.8x</p><p>图像：一条过原点的直线</p>'
        ],
        exercises: [
          {
            question: '若 f(x) = 2x + 3，求 f(5) 的值',
            answer: '13',
            explanation: '将 x = 5 代入函数式：f(5) = 2×5 + 3 = 10 + 3 = 13'
          }
        ]
      },
      {
        id: 3,
        title: '1.3 函数的性质',
        expanded: false,
        content: `
          <h4>函数的基本性质</h4>
          <p><strong>1. 单调性</strong></p>
          <p>• 单调递增：在某个区间内，x₁ < x₂ 时，f(x₁) < f(x₂)</p>
          <p>• 单调递减：在某个区间内，x₁ < x₂ 时，f(x₁) > f(x₂)</p>
          <p><strong>2. 奇偶性</strong></p>
          <p>• 偶函数：f(-x) = f(x)，图像关于y轴对称</p>
          <p>• 奇函数：f(-x) = -f(x)，图像关于原点对称</p>
          <p><strong>3. 周期性</strong></p>
          <p>存在非零常数T，使得 f(x+T) = f(x) 对定义域内所有x成立</p>
        `,
        exercises: [
          {
            question: '判断函数 f(x) = x² 的奇偶性',
            answer: '偶函数',
            explanation: 'f(-x) = (-x)² = x² = f(x)，满足偶函数定义，且图像关于y轴对称。'
          }
        ]
      }
    ]
  },
  {
    id: 2,
    name: '第2章 一次函数',
    description: '掌握一次函数的性质、图像和应用',
    difficulty: '基础',
    estimatedTime: 80,
    progress: 0,
    completed: false,
    sections: [
      {
        id: 1,
        title: '2.1 一次函数的定义',
        expanded: true,
        content: `
          <h4>一次函数</h4>
          <p>形如 <strong>y = kx + b</strong> (k ≠ 0) 的函数称为一次函数。</p>
          <p>其中：</p>
          <ul>
            <li>k 称为<strong>斜率</strong>，表示函数的变化率</li>
            <li>b 称为<strong>截距</strong>，表示直线与y轴的交点纵坐标</li>
          </ul>
          <p><strong>特殊情况：</strong>当 b = 0 时，y = kx 称为<strong>正比例函数</strong></p>
        `,
        examples: [
          '<p><strong>例：</strong>下列哪些是一次函数？</p><p>(1) y = 3x + 2  ✓</p><p>(2) y = x²  ✗ (二次函数)</p><p>(3) y = 5x  ✓ (正比例函数)</p><p>(4) y = 1/x  ✗ (反比例函数)</p>'
        ],
        exercises: [
          {
            question: '一次函数 y = -2x + 3 中，斜率k和截距b分别是多少？',
            answer: 'k=-2, b=3',
            explanation: '对照一次函数标准形式 y = kx + b，可得斜率 k = -2，截距 b = 3'
          }
        ]
      },
      {
        id: 2,
        title: '2.2 一次函数的图像',
        expanded: false,
        content: `
          <h4>一次函数的图像特征</h4>
          <p>一次函数的图像是一条<strong>直线</strong>，因此也称为线性函数。</p>
          <p><strong>斜率k的作用：</strong></p>
          <ul>
            <li>k > 0：直线从左下到右上，函数单调递增</li>
            <li>k < 0：直线从左上到右下，函数单调递减</li>
            <li>|k| 越大，直线越陡峭</li>
          </ul>
          <p><strong>截距b的作用：</strong></p>
          <ul>
            <li>b > 0：直线与y轴交于正半轴</li>
            <li>b < 0：直线与y轴交于负半轴</li>
            <li>b = 0：直线过原点</li>
          </ul>
        `,
        exercises: [
          {
            question: '一次函数 y = 2x - 1 的图像经过哪个象限？',
            answer: '一、三、四象限',
            explanation: '因为k=2>0，所以直线向右上方倾斜；b=-1<0，所以与y轴交于负半轴，因此经过一、三、四象限。'
          }
        ]
      }
    ]
  },
  {
    id: 3,
    name: '第3章 二次函数',
    description: '学习二次函数的图像、性质和应用',
    difficulty: '中等',
    estimatedTime: 120,
    progress: 0,
    completed: false,
    sections: [
      {
        id: 1,
        title: '3.1 二次函数的定义',
        expanded: true,
        content: `
          <h4>二次函数的标准形式</h4>
          <p>形如 <strong>y = ax² + bx + c</strong> (a ≠ 0) 的函数称为二次函数。</p>
          <p>二次函数的三种常见形式：</p>
          <ul>
            <li><strong>一般式：</strong>y = ax² + bx + c</li>
            <li><strong>顶点式：</strong>y = a(x - h)² + k，顶点坐标为 (h, k)</li>
            <li><strong>交点式：</strong>y = a(x - x₁)(x - x₂)，x₁、x₂为与x轴交点的横坐标</li>
          </ul>
        `,
        examples: [
          '<p><strong>例：</strong>将 y = x² - 4x + 3 化为顶点式</p><p>解：y = (x² - 4x + 4) - 4 + 3 = (x - 2)² - 1</p><p>顶点坐标为 (2, -1)</p>'
        ],
        exercises: [
          {
            question: '二次函数 y = 2(x-1)² + 3 的顶点坐标是？',
            answer: '(1, 3)',
            explanation: '顶点式 y = a(x-h)² + k 中，顶点坐标为 (h, k)，所以顶点为 (1, 3)'
          }
        ]
      }
    ]
  }
]);

// 物理课程数据
const physicsChapters = ref<Chapter[]>([
  {
    id: 1,
    name: '第1章 力学基础',
    description: '学习力的概念、力的三要素和力的图示',
    difficulty: '基础',
    estimatedTime: 90,
    progress: 0,
    completed: false,
    sections: [
      {
        id: 1,
        title: '1.1 力的概念',
        expanded: true,
        content: `
          <h4>什么是力？</h4>
          <p><strong>力</strong>是物体对物体的作用。力不能脱离物体而独立存在。</p>
          <p>力的作用效果：</p>
          <ul>
            <li><strong>改变物体的运动状态</strong>：使静止的物体运动，使运动的物体静止，或改变运动方向和速度</li>
            <li><strong>改变物体的形状</strong>：使物体发生形变（拉伸、压缩、弯曲、扭转等）</li>
          </ul>
          <p>力的三要素：</p>
          <ul>
            <li><strong>大小</strong>：力的强弱程度，单位是牛顿(N)</li>
            <li><strong>方向</strong>：力的作用方向</li>
            <li><strong>作用点</strong>：力作用在物体上的位置</li>
          </ul>
        `,
        examples: [
          '<p><strong>例1：</strong>用手推桌子，桌子运动起来</p><p>分析：手对桌子施加了推力，改变了桌子的运动状态（从静止到运动）</p>',
          '<p><strong>例2：</strong>用力拉弹簧，弹簧变长</p><p>分析：拉力使弹簧发生形变，长度增加</p>'
        ],
        exercises: [
          {
            question: '力的三要素是什么？',
            answer: '大小、方向、作用点',
            explanation: '力的三要素包括：力的大小、力的方向和力的作用点。这三个要素共同决定了力的作用效果。'
          },
          {
            question: '力的单位是什么？',
            answer: '牛顿或N',
            explanation: '力的国际单位是牛顿，简称牛，符号是N。1N大约等于拿起两个鸡蛋所需的力。'
          }
        ]
      },
      {
        id: 2,
        title: '1.2 重力',
        expanded: false,
        content: `
          <h4>重力的概念</h4>
          <p><strong>重力</strong>是由于地球的吸引而使物体受到的力。</p>
          <p>重力的特点：</p>
          <ul>
            <li><strong>方向</strong>：总是竖直向下</li>
            <li><strong>大小</strong>：G = mg（m是质量，g≈10N/kg）</li>
            <li><strong>作用点</strong>：物体的重心</li>
          </ul>
          <p>重心：物体各部分受到的重力的合力的作用点</p>
          <p>质量与重力的关系：</p>
          <ul>
            <li>质量是物体本身的属性，不随位置改变</li>
            <li>重力会随位置改变（g值不同）</li>
            <li>在地球表面附近，g ≈ 10N/kg或9.8N/kg</li>
          </ul>
        `,
        examples: [
          '<p><strong>例：</strong>一个质量为5kg的物体，受到的重力是多少？</p><p>解：G = mg = 5kg × 10N/kg = 50N</p><p>答：物体受到的重力是50N</p>'
        ],
        exercises: [
          {
            question: '质量为2kg的物体，受到的重力是多少？(g=10N/kg)',
            answer: '20N',
            explanation: '根据公式 G = mg，代入数值：G = 2kg × 10N/kg = 20N'
          },
          {
            question: '重力的方向是什么？',
            answer: '竖直向下',
            explanation: '重力的方向总是竖直向下的，这是重力的基本特征。注意是"竖直向下"而不是"垂直向下"。'
          }
        ]
      },
      {
        id: 3,
        title: '1.3 摩擦力',
        expanded: false,
        content: `
          <h4>摩擦力</h4>
          <p><strong>摩擦力</strong>是两个相互接触的物体，当它们相对运动或有相对运动趋势时，在接触面上产生的阻碍相对运动的力。</p>
          <p>摩擦力的分类：</p>
          <ul>
            <li><strong>滑动摩擦力</strong>：物体滑动时产生的摩擦力</li>
            <li><strong>静摩擦力</strong>：物体有相对运动趋势但未滑动时的摩擦力</li>
            <li><strong>滚动摩擦力</strong>：物体滚动时产生的摩擦力（通常较小）</li>
          </ul>
          <p>影响滑动摩擦力大小的因素：</p>
          <ul>
            <li><strong>压力大小</strong>：压力越大，摩擦力越大</li>
            <li><strong>接触面的粗糙程度</strong>：接触面越粗糙，摩擦力越大</li>
          </ul>
          <p>摩擦力的方向：与物体相对运动方向相反</p>
        `,
        examples: [
          '<p><strong>例：</strong>为什么冰面上走路容易滑倒？</p><p>分析：冰面很光滑，接触面粗糙程度小，所以摩擦力小，不足以提供足够的阻力，容易滑倒</p>'
        ],
        exercises: [
          {
            question: '影响滑动摩擦力大小的因素有哪些？',
            answer: '压力大小和接触面的粗糙程度',
            explanation: '滑动摩擦力的大小与两个因素有关：1)压力的大小；2)接触面的粗糙程度。压力越大、接触面越粗糙，摩擦力越大。'
          }
        ]
      }
    ]
  },
  {
    id: 2,
    name: '第2章 运动和力',
    description: '学习牛顿运动定律和力与运动的关系',
    difficulty: '中等',
    estimatedTime: 120,
    progress: 0,
    completed: false,
    sections: [
      {
        id: 1,
        title: '2.1 牛顿第一定律',
        expanded: true,
        content: `
          <h4>牛顿第一定律（惯性定律）</h4>
          <p><strong>内容：</strong>一切物体在没有受到外力作用的时候，总保持静止状态或匀速直线运动状态。</p>
          <p>这个定律说明了两点：</p>
          <ul>
            <li>物体不受力时，原来静止的继续静止</li>
            <li>物体不受力时，原来运动的继续做匀速直线运动</li>
          </ul>
          <p><strong>惯性：</strong>物体保持原来运动状态不变的性质</p>
          <ul>
            <li>一切物体都有惯性</li>
            <li>惯性只与质量有关，质量越大，惯性越大</li>
            <li>惯性不是力，而是物体的一种属性</li>
          </ul>
        `,
        examples: [
          '<p><strong>例：</strong>汽车突然刹车，乘客会向前倾</p><p>分析：汽车刹车时，人的下半身随车减速，但上半身由于惯性，保持原来的运动状态，继续向前运动，所以会向前倾</p>'
        ],
        exercises: [
          {
            question: '牛顿第一定律的内容是什么？',
            answer: '一切物体在没有受到外力作用的时候，总保持静止状态或匀速直线运动状态',
            explanation: '牛顿第一定律又称惯性定律，揭示了力不是维持物体运动的原因，而是改变物体运动状态的原因。'
          },
          {
            question: '惯性与什么因素有关？',
            answer: '质量',
            explanation: '惯性只与物体的质量有关，质量越大，惯性越大。惯性与物体的运动状态、受力情况等都无关。'
          }
        ]
      },
      {
        id: 2,
        title: '2.2 二力平衡',
        expanded: false,
        content: `
          <h4>二力平衡条件</h4>
          <p>当物体在两个力的作用下处于静止或匀速直线运动状态时，这两个力就彼此平衡。</p>
          <p><strong>二力平衡的条件：</strong></p>
          <ul>
            <li><strong>大小相等</strong></li>
            <li><strong>方向相反</strong></li>
            <li><strong>作用在同一直线上</strong></li>
            <li><strong>作用在同一物体上</strong></li>
          </ul>
          <p>简记为：同体、等大、反向、共线</p>
        `,
        examples: [
          '<p><strong>例：</strong>静止在桌面上的书</p><p>分析：书受到重力和桌面的支持力，这两个力大小相等、方向相反、作用在同一直线上、作用在同一物体（书）上，所以书保持静止</p>'
        ],
        exercises: [
          {
            question: '二力平衡的条件是什么？',
            answer: '同体、等大、反向、共线',
            explanation: '二力平衡的四个条件：作用在同一物体上、大小相等、方向相反、作用在同一直线上。'
          }
        ]
      }
    ]
  },
  {
    id: 3,
    name: '第3章 压强和浮力',
    description: '学习压强、液体压强和浮力的概念及应用',
    difficulty: '中等',
    estimatedTime: 100,
    progress: 0,
    completed: false,
    sections: [
      {
        id: 1,
        title: '3.1 压强',
        expanded: true,
        content: `
          <h4>压强的概念</h4>
          <p><strong>压强</strong>是表示压力作用效果的物理量。</p>
          <p>压强的定义：物体单位面积上受到的压力</p>
          <p><strong>公式：p = F/S</strong></p>
          <ul>
            <li>p — 压强，单位：帕斯卡(Pa)</li>
            <li>F — 压力，单位：牛顿(N)</li>
            <li>S — 受力面积，单位：平方米(m²)</li>
          </ul>
          <p>1Pa = 1N/m²，表示1平方米面积上受到1牛顿的压力</p>
          <p>增大压强的方法：增大压力或减小受力面积</p>
          <p>减小压强的方法：减小压力或增大受力面积</p>
        `,
        examples: [
          '<p><strong>例：</strong>为什么钉子要做得很尖？</p><p>分析：钉子尖端面积很小，在压力一定时，根据p=F/S，受力面积越小，压强越大，更容易钉入物体</p>'
        ],
        exercises: [
          {
            question: '压强的计算公式是什么？',
            answer: 'p=F/S',
            explanation: '压强等于压力除以受力面积，公式为p=F/S。其中p是压强，F是压力，S是受力面积。'
          },
          {
            question: '压强的单位是什么？',
            answer: '帕斯卡或Pa',
            explanation: '压强的国际单位是帕斯卡，简称帕，符号Pa。1Pa=1N/m²。'
          }
        ]
      },
      {
        id: 2,
        title: '3.2 液体压强',
        expanded: false,
        content: `
          <h4>液体压强的特点</h4>
          <p>液体由于受到重力作用，对容器底部和侧壁都有压强</p>
          <p>液体内部压强的特点：</p>
          <ul>
            <li>液体对容器底部和侧壁都有压强</li>
            <li>液体内部向各个方向都有压强</li>
            <li>在同一深度，液体向各个方向的压强相等</li>
            <li>液体的压强随深度的增加而增大</li>
            <li>液体压强还与液体的密度有关</li>
          </ul>
          <p><strong>公式：p = ρgh</strong></p>
          <ul>
            <li>ρ — 液体密度，单位：kg/m³</li>
            <li>g — 重力加速度，取10N/kg</li>
            <li>h — 深度，单位：m</li>
          </ul>
        `,
        exercises: [
          {
            question: '液体压强的计算公式是什么？',
            answer: 'p=ρgh',
            explanation: '液体压强公式为p=ρgh，其中ρ是液体密度，g是重力加速度(10N/kg)，h是深度。'
          }
        ]
      }
    ]
  }
]);

const currentChapters = computed(() => {
  // 根据科目返回对应的章节
  if (currentSubject.value === 'math') {
    return mathChapters.value;
  } else if (currentSubject.value === 'physics') {
    return physicsChapters.value;
  }
  // 其他科目可以添加类似的数据
  return [];
});

// UI状态
const showNotesDialog = ref(false);
const showStudyStats = ref(false);
const showAddGoalDialog = ref(false);
const currentFontSize = ref<'small' | 'medium' | 'large' | 'xlarge'>('medium');
const fontSizeMap = {
  small: '14px',
  medium: '16px',
  large: '18px',
  xlarge: '20px'
};

// 笔记相关
const notes = ref<Note[]>([]);
const currentNote = ref<Note | null>(null);
const noteTitle = ref('');
const noteContent = ref('');
const noteTag = ref('');
const noteSearch = ref('');

// AI问答
const aiQuestion = ref('');
const aiLoading = ref(false);
const qaHistory = ref<QA[]>([]);

// 学习目标
const dailyGoals = ref<Goal[]>([]);
const newGoalTitle = ref('');

// 知识卡片
const mathKnowledgeCards: KnowledgeCard[] = [
  { id: 1, term: '函数', definition: '描述两个变量之间对应关系的数学概念，每个输入对应唯一的输出' },
  { id: 2, term: '定义域', definition: '函数中自变量x的取值范围' },
  { id: 3, term: '值域', definition: '函数中因变量y的取值范围' },
  { id: 4, term: '单调性', definition: '函数在某个区间内的增减性质' },
  { id: 5, term: '奇偶性', definition: '函数图像关于y轴或原点的对称性' }
];

const physicsKnowledgeCards: KnowledgeCard[] = [
  { id: 1, term: '力', definition: '物体对物体的作用，能改变物体的运动状态或使物体发生形变' },
  { id: 2, term: '重力', definition: '由于地球的吸引而使物体受到的力，方向总是竖直向下' },
  { id: 3, term: '摩擦力', definition: '阻碍物体相对运动的力，方向与相对运动方向相反' },
  { id: 4, term: '惯性', definition: '物体保持原来运动状态不变的性质，质量越大惯性越大' },
  { id: 5, term: '压强', definition: '物体单位面积上受到的压力，公式 p = F/S' },
  { id: 6, term: '二力平衡', definition: '同体、等大、反向、共线的两个力使物体保持平衡状态' }
];

const knowledgeCards = computed(() => {
  if (currentSubject.value === 'math') {
    return mathKnowledgeCards;
  } else if (currentSubject.value === 'physics') {
    return physicsKnowledgeCards;
  }
  return [];
});

// 学习统计
const totalStudyTime = ref(0);
const todayStudyTime = ref(0);
let studyStartTime = 0;

// ===== 计算属性 =====
const completedCount = computed(() => {
  return currentChapters.value.filter(ch => ch.completed).length;
});

const totalSections = computed(() => {
  return currentChapters.value.length;
});

const filteredNotes = computed(() => {
  if (!noteSearch.value) return notes.value;
  const keyword = noteSearch.value.toLowerCase();
  return notes.value.filter(note =>
    note.title.toLowerCase().includes(keyword) ||
    note.content.toLowerCase().includes(keyword)
  );
});

// ===== 方法 =====
// 章节相关
const handleSubjectChange = () => {
  currentChapter.value = null;
  ElMessage.success(`已切换到${subjects.value.find(s => s.id === currentSubject.value)?.name}`);
};

const selectChapter = (chapter: Chapter) => {
  currentChapter.value = chapter;
  // 展开第一个小节
  if (chapter.sections.length > 0) {
    chapter.sections[0].expanded = true;
  }
  saveProgress();
};

const toggleSection = (section: Section) => {
  section.expanded = !section.expanded;
};

const toggleChapterComplete = () => {
  if (!currentChapter.value) return;

  currentChapter.value.completed = !currentChapter.value.completed;
  if (currentChapter.value.completed) {
    currentChapter.value.progress = 100;
    ElMessage.success('恭喜完成本章学习！🎉');
  } else {
    currentChapter.value.progress = 0;
    ElMessage.info('已重置学习进度');
  }
  saveProgress();
};

const getDifficultyType = (difficulty: string) => {
  const map: any = {
    '基础': 'success',
    '中等': 'warning',
    '困难': 'danger'
  };
  return map[difficulty] || 'info';
};

// 练习题相关
const checkAnswer = (section: Section, exercise: Exercise) => {
  if (!exercise.userAnswer) {
    ElMessage.warning('请先输入答案');
    return;
  }

  exercise.checked = true;
  const userAns = exercise.userAnswer.trim().toLowerCase();
  const correctAns = exercise.answer.toLowerCase();

  exercise.correct = userAns === correctAns || userAns.includes(correctAns);

  if (exercise.correct) {
    ElMessage.success('回答正确！👍');
  } else {
    ElMessage.error('回答错误，请查看解析');
    exercise.showExplanation = true;
  }

  saveProgress();
};

// AI问答
const askAI = async () => {
  if (!aiQuestion.value.trim()) {
    ElMessage.warning('请输入问题');
    return;
  }

  aiLoading.value = true;

  try {
    // 构建上下文信息
    const context = currentChapter.value
      ? `当前正在学习：${currentChapter.value.name}`
      : '正在使用AI家教学习';

    const subjectName = subjects.value.find(s => s.id === currentSubject.value)?.name || '数学';

    // 调用真实的AI API
    const answer = await answerQuestion({
      question: aiQuestion.value,
      context: context,
      subject: subjectName
    });

    qaHistory.value.unshift({
      question: aiQuestion.value,
      answer: answer,
      timestamp: Date.now()
    });

    // 只保留最近10条
    if (qaHistory.value.length > 10) {
      qaHistory.value = qaHistory.value.slice(0, 10);
    }

    aiQuestion.value = '';
    ElMessage.success('AI已回答');
  } catch (error: any) {
    console.error('AI回答失败:', error);
    ElMessage.error(error.message || 'AI回答失败，请稍后重试');
  } finally {
    aiLoading.value = false;
  }
};

// 笔记相关
const openQuickNote = (section: Section) => {
  noteTitle.value = `${currentChapter.value?.name} - ${section.title}`;
  noteContent.value = '';
  noteTag.value = '';
  currentNote.value = null;
  showNotesDialog.value = true;
};

const selectNote = (note: Note) => {
  currentNote.value = note;
  noteTitle.value = note.title;
  noteContent.value = note.content;
  noteTag.value = note.tag || '';
};

const saveNote = () => {
  if (!noteTitle.value.trim() || !noteContent.value.trim()) {
    ElMessage.warning('请填写笔记标题和内容');
    return;
  }

  const now = Date.now();
  const preview = noteContent.value.substring(0, 50) + (noteContent.value.length > 50 ? '...' : '');

  if (currentNote.value) {
    // 更新现有笔记
    currentNote.value.title = noteTitle.value;
    currentNote.value.content = noteContent.value;
    currentNote.value.preview = preview;
    currentNote.value.tag = noteTag.value;
    currentNote.value.time = formatTime(now);
    currentNote.value.timestamp = now;
    ElMessage.success('笔记已更新');
  } else {
    // 新建笔记
    notes.value.unshift({
      id: now,
      title: noteTitle.value,
      content: noteContent.value,
      preview: preview,
      tag: noteTag.value,
      time: formatTime(now),
      chapterId: currentChapter.value?.id,
      timestamp: now
    });
    ElMessage.success('笔记已保存');
  }

  saveToLocalStorage();
  clearNote();
};

const clearNote = () => {
  noteTitle.value = '';
  noteContent.value = '';
  noteTag.value = '';
  currentNote.value = null;
};

// 学习目标
const addGoal = () => {
  showAddGoalDialog.value = true;
  newGoalTitle.value = '';
};

const confirmAddGoal = () => {
  if (!newGoalTitle.value.trim()) {
    ElMessage.warning('请输入目标内容');
    return;
  }

  dailyGoals.value.push({
    id: Date.now(),
    title: newGoalTitle.value,
    completed: false
  });

  saveProgress();
  showAddGoalDialog.value = false;
  ElMessage.success('目标已添加');
};

const removeGoal = (id: number) => {
  dailyGoals.value = dailyGoals.value.filter(g => g.id !== id);
  saveProgress();
};

// 知识卡片
const flipCard = (card: KnowledgeCard) => {
  card.flipped = !card.flipped;
};

// 设置相关
const handleCommand = (command: string) => {
  switch (command) {
    case 'fontSize':
      const sizes: Array<'small' | 'medium' | 'large' | 'xlarge'> = ['small', 'medium', 'large', 'xlarge'];
      const currentIndex = sizes.indexOf(currentFontSize.value);
      currentFontSize.value = sizes[(currentIndex + 1) % sizes.length];
      ElMessage.success(`字体大小：${currentFontSize.value}`);
      break;
    case 'theme':
      ElMessage.info('主题切换功能开发中...');
      break;
    case 'export':
      exportNotes();
      break;
  }
};

const exportNotes = () => {
  if (notes.value.length === 0) {
    ElMessage.warning('暂无笔记可导出');
    return;
  }

  let markdown = '# 我的学习笔记\n\n';
  markdown += `导出时间：${new Date().toLocaleString()}\n\n`;
  markdown += '---\n\n';

  notes.value.forEach((note, index) => {
    markdown += `## ${index + 1}. ${note.title}\n\n`;
    if (note.tag) {
      markdown += `**标签：** ${note.tag}\n\n`;
    }
    markdown += `${note.content}\n\n`;
    markdown += `*记录时间：${note.time}*\n\n`;
    markdown += '---\n\n';
  });

  const blob = new Blob([markdown], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `学习笔记_${Date.now()}.md`;
  a.click();
  URL.revokeObjectURL(url);

  ElMessage.success('笔记已导出');
};

// 工具函数
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp);
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 数据持久化
const saveProgress = () => {
  const progress = {
    currentSubject: currentSubject.value,
    chapters: currentChapters.value,
    goals: dailyGoals.value,
    totalStudyTime: totalStudyTime.value,
    lastSaveTime: Date.now()
  };
  localStorage.setItem('aiTutor_progress', JSON.stringify(progress));
};

const saveToLocalStorage = () => {
  localStorage.setItem('aiTutor_notes', JSON.stringify(notes.value));
};

const loadProgress = () => {
  try {
    const saved = localStorage.getItem('aiTutor_progress');
    if (saved) {
      const data = JSON.parse(saved);
      currentSubject.value = data.currentSubject || 'math';
      if (data.chapters) {
        mathChapters.value = data.chapters;
      }
      if (data.goals) {
        dailyGoals.value = data.goals;
      }
      totalStudyTime.value = data.totalStudyTime || 0;
    }

    const savedNotes = localStorage.getItem('aiTutor_notes');
    if (savedNotes) {
      notes.value = JSON.parse(savedNotes);
    }
  } catch (error) {
    console.error('加载数据失败:', error);
  }
};

// 学习时长统计
const startStudyTimer = () => {
  studyStartTime = Date.now();

  setInterval(() => {
    if (studyStartTime > 0) {
      const elapsed = Math.floor((Date.now() - studyStartTime) / 60000);
      todayStudyTime.value = elapsed;
      totalStudyTime.value = (totalStudyTime.value || 0) + 1;

      // 每分钟保存一次
      if (elapsed % 1 === 0) {
        saveProgress();
      }
    }
  }, 60000); // 每分钟更新
};

// 生命周期
onMounted(() => {
  loadProgress();
  startStudyTimer();
});

// 监听章节变化，自动保存
watch(() => currentChapters.value, () => {
  saveProgress();
}, { deep: true });
</script>

<style scoped>
.ai-tutor-page {
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
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.title-text p {
  margin: 4px 0 0;
  font-size: 14px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主体内容 */
.page-content {
  padding: 20px;
}

/* 课程列表 */
.course-list {
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.badge {
  margin-left: 8px;
}

.chapter-list {
  max-height: 600px;
  overflow-y: auto;
}

.chapter-item {
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.chapter-item:hover {
  border-color: #667eea;
  background: #f5f7fa;
}

.chapter-item.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea22, #764ba222);
}

.chapter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.chapter-name {
  font-weight: 500;
  font-size: 14px;
  flex: 1;
}

.chapter-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

/* 学习内容区 */
.learning-area {
  min-height: 600px;
}

.learning-content {
  line-height: 1.8;
}

.chapter-info {
  margin-bottom: 24px;
}

.section-block {
  margin-bottom: 24px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  cursor: pointer;
  transition: background 0.3s;
}

.section-header:hover {
  background: #e4e7ed;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-icon {
  transition: transform 0.3s;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

.section-content {
  padding: 20px;
}

.content-text {
  margin-bottom: 20px;
}

.content-text h4 {
  color: #667eea;
  margin-top: 16px;
}

.content-text ul {
  padding-left: 24px;
}

.content-text li {
  margin: 8px 0;
}

.examples, .exercises {
  margin-top: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.examples h4, .exercises h4 {
  margin-top: 0;
  color: #303133;
}

.example-item {
  padding: 12px;
  margin-bottom: 12px;
  background: white;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.exercise-item {
  padding: 16px;
  margin-bottom: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.question {
  font-weight: 500;
  margin-bottom: 12px;
  color: #303133;
}

.answer-input {
  margin-bottom: 12px;
}

.exercise-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.feedback {
  margin-top: 12px;
}

.explanation {
  margin-top: 12px;
}

.quick-note {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

/* AI助手 */
.ai-assistant {
  position: sticky;
  top: 20px;
}

.ai-qa {
  max-height: 400px;
  overflow-y: auto;
}

.qa-history {
  margin-top: 16px;
}

.qa-item {
  padding: 12px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.question-text {
  display: flex;
  gap: 8px;
  color: #409eff;
  font-weight: 500;
  margin-bottom: 8px;
}

.answer-text {
  display: flex;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

/* 学习目标 */
.goals-list {
  max-height: 200px;
  overflow-y: auto;
}

.goal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}

.goal-item:last-child {
  border-bottom: none;
}

/* 知识卡片 */
.cards-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.card-item {
  padding: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 8px;
  cursor: pointer;
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  transition: transform 0.3s;
}

.card-item:hover {
  transform: scale(1.05);
}

.card-front strong {
  font-size: 16px;
}

.card-back p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
}

/* 笔记对话框 */
.notes-container {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
  min-height: 500px;
}

.notes-list {
  border-right: 1px solid #e4e7ed;
  padding-right: 20px;
  max-height: 500px;
  overflow-y: auto;
}

.note-item {
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.note-item:hover {
  border-color: #667eea;
  background: #f5f7fa;
}

.note-item.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea22, #764ba222);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.note-preview {
  font-size: 13px;
  color: #909399;
  margin: 8px 0;
  line-height: 1.5;
}

.note-time {
  font-size: 12px;
  color: #c0c4cc;
}

.note-editor {
  display: flex;
  flex-direction: column;
}

.note-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

/* 学习统计 */
.study-stats {
  padding: 20px;
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
    justify-content: flex-end;
  }

  .notes-container {
    grid-template-columns: 1fr;
  }

  .notes-list {
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
    padding-right: 0;
    padding-bottom: 20px;
    margin-bottom: 20px;
    max-height: 200px;
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>
