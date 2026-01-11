<template>
  <div class="ai-practice-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon :size="32" color="#667eea"><Edit /></el-icon>
        <div class="title-text">
          <h1>AI练习生成</h1>
          <p>智能生成个性化练习题，针对性提升学习效果</p>
        </div>
      </div>
      <div class="header-right">
        <el-badge :value="favoriteQuestions.length" :hidden="favoriteQuestions.length === 0" class="header-badge">
          <el-button :icon="StarFilled" @click="showFavoriteDialog = true">
            收藏题目
          </el-button>
        </el-badge>
        <el-button :icon="TrendCharts" @click="showStatsDialog = true">
          统计分析
        </el-button>
        <el-button :icon="DocumentCopy" @click="showHistoryDialog = true">
          历史记录
        </el-button>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="page-content">
      <el-row :gutter="20">
        <!-- 左侧：生成配置 -->
        <el-col :xs="24" :lg="8">
          <el-card class="config-card" shadow="hover">
            <template #header>
              <span>🎯 练习配置</span>
            </template>

            <el-form :model="practiceForm" label-width="100px">
              <!-- 生成方式 -->
              <el-form-item label="生成方式">
                <el-radio-group v-model="practiceForm.mode" @change="handleModeChange">
                  <el-radio value="weakness">薄弱知识点</el-radio>
                  <el-radio value="knowledge">指定知识点</el-radio>
                  <el-radio value="wrong">错题重练</el-radio>
                  <el-radio value="custom">自定义</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 练习模式 -->
              <el-form-item label="练习模式">
                <el-radio-group v-model="practiceForm.practiceMode">
                  <el-radio value="normal">常规模式</el-radio>
                  <el-radio value="challenge">闯关模式</el-radio>
                  <el-radio value="timed">计时模式</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 计时设置（仅计时模式显示） -->
              <el-form-item label="倒计时" v-if="practiceForm.practiceMode === 'timed'">
                <el-input-number
                  v-model="practiceForm.timeLimit"
                  :min="5"
                  :max="120"
                  :step="5"
                  style="width: 100%"
                />
                <span style="margin-left: 8px">分钟</span>
              </el-form-item>

              <!-- 科目选择 -->
              <el-form-item label="科目" v-if="practiceForm.mode !== 'wrong'">
                <el-select
                  v-model="practiceForm.subject"
                  placeholder="请选择科目"
                  style="width: 100%"
                  :loading="subjectsLoading"
                >
                  <el-option
                    v-for="subject in allSubjects"
                    :key="subject.id"
                    :label="subject.name"
                    :value="subject.id.toString()"
                  >
                    <span>{{ subject.name }}</span>
                    <span v-if="subject.knowledgePointCount || subject.questionCount" style="float: right; color: #8492a6; font-size: 12px; margin-left: 10px;">
                      {{ subject.knowledgePointCount }}个知识点 | {{ subject.questionCount }}道题
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>

              <!-- 知识点选择（薄弱知识点模式） -->
              <el-form-item label="知识点" v-if="practiceForm.mode === 'weakness'">
                <el-select
                  v-model="practiceForm.weaknessPoints"
                  multiple
                  placeholder="自动推荐薄弱知识点"
                  style="width: 100%"
                >
                  <el-option
                    v-for="point in weaknessKnowledgePoints"
                    :key="point.id"
                    :label="`${point.name} (掌握度${point.masteryRate}%)`"
                    :value="point.id"
                  >
                    <span style="float: left">{{ point.name }}</span>
                    <el-progress
                      :percentage="point.masteryRate"
                      :color="getMasteryColor(point.masteryRate)"
                      :stroke-width="4"
                      :show-text="false"
                      style="width: 80px; float: right; margin-top: 8px"
                    />
                  </el-option>
                </el-select>
              </el-form-item>

              <!-- 知识点选择（指定知识点模式） -->
              <el-form-item label="知识点" v-if="practiceForm.mode === 'knowledge'">
                <el-cascader
                  v-model="practiceForm.knowledgePoints"
                  :options="knowledgePointTree"
                  :props="{ multiple: true, checkStrictly: true }"
                  placeholder="请选择知识点"
                  style="width: 100%"
                  clearable
                />
              </el-form-item>

              <!-- 题目数量 -->
              <el-form-item label="题目数量" v-if="practiceForm.mode !== 'wrong'">
                <el-slider
                  v-model="practiceForm.questionCount"
                  :min="5"
                  :max="50"
                  :step="5"
                  show-stops
                  :marks="{ 10: '10题', 20: '20题', 30: '30题', 40: '40题' }"
                />
              </el-form-item>

              <!-- 难度级别 -->
              <el-form-item label="难度级别" v-if="practiceForm.mode !== 'wrong'">
                <el-radio-group v-model="practiceForm.difficulty">
                  <el-radio value="easy">简单</el-radio>
                  <el-radio value="medium">中等</el-radio>
                  <el-radio value="hard">困难</el-radio>
                  <el-radio value="auto">自适应</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 题型选择 -->
              <el-form-item label="题型" v-if="practiceForm.mode !== 'wrong'">
                <el-checkbox-group v-model="practiceForm.questionTypes">
                  <el-checkbox value="single">单选题</el-checkbox>
                  <el-checkbox value="multiple">多选题</el-checkbox>
                  <el-checkbox value="blank">填空题</el-checkbox>
                  <el-checkbox value="calculation">计算题</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <!-- 生成按钮 -->
              <el-form-item>
                <el-button
                  type="primary"
                  :icon="MagicStick"
                  :loading="generating"
                  @click="generatePractice"
                  style="width: 100%"
                  size="large"
                >
                  {{ generating ? '正在生成中...' : 'AI智能生成练习' }}
                </el-button>
              </el-form-item>
            </el-form>

            <!-- 成就徽章系统 -->
            <el-divider>成就徽章</el-divider>
            <div class="achievement-badges">
              <div
                v-for="badge in achievementBadges"
                :key="badge.id"
                :class="['badge-item', { unlocked: badge.unlocked }]"
              >
                <el-tooltip :content="badge.description" placement="top">
                  <div class="badge-content">
                    <el-icon :size="32" :color="badge.unlocked ? badge.color : '#ccc'">
                      <component :is="badge.icon" />
                    </el-icon>
                    <span class="badge-name">{{ badge.name }}</span>
                    <el-progress
                      v-if="!badge.unlocked"
                      :percentage="badge.progress"
                      :show-text="false"
                      :stroke-width="3"
                    />
                  </div>
                </el-tooltip>
              </div>
            </div>

            <!-- 统计概览 -->
            <el-divider>练习统计</el-divider>
            <div class="stats-overview">
              <div class="stat-item">
                <div class="stat-value">{{ totalPracticeCount }}</div>
                <div class="stat-label">累计练习</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ totalQuestionCount }}</div>
                <div class="stat-label">累计题数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ continuousDays }}</div>
                <div class="stat-label">连续天数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ averageAccuracy }}%</div>
                <div class="stat-label">平均正确率</div>
              </div>
            </div>

            <!-- AI推荐 -->
            <el-divider>AI推荐</el-divider>
            <div class="ai-recommendations">
              <div
                v-for="(rec, index) in aiRecommendations"
                :key="index"
                class="recommendation-item"
                @click="applyRecommendation(rec)"
              >
                <el-icon :color="rec.color"><Star /></el-icon>
                <div class="rec-content">
                  <h4>{{ rec.title }}</h4>
                  <p>{{ rec.description }}</p>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：生成结果 -->
        <el-col :xs="24" :lg="16">
          <!-- 空状态 -->
          <el-card v-if="!generatedQuestions.length && !generating && !isPracticing" class="empty-card" shadow="hover">
            <el-empty description="请配置练习参数后点击生成">
              <template #image>
                <div class="empty-icon">
                  <el-icon :size="120" color="#909399"><Document /></el-icon>
                </div>
              </template>
            </el-empty>
          </el-card>

          <!-- 生成中 -->
          <el-card v-if="generating" class="generating-card" shadow="hover">
            <div class="generating-content">
              <el-icon class="loading-icon" :size="80" color="#667eea"><Loading /></el-icon>
              <h2>AI正在为你生成练习题...</h2>
              <p>预计需要 {{ estimatedTime }} 秒</p>
              <el-progress :percentage="generatingProgress" :stroke-width="12" />
            </div>
          </el-card>

          <!-- 生成结果（未开始练习） -->
          <div v-if="generatedQuestions.length > 0 && !generating && !isPracticing" class="result-section">
            <el-card class="result-header" shadow="hover">
              <div class="result-info">
                <div class="info-left">
                  <h3>📝 已生成 {{ generatedQuestions.length }} 道题目</h3>
                  <p>预计完成时间：{{ estimatedCompletionTime }} 分钟</p>
                </div>
                <div class="info-right">
                  <el-button :icon="Download" @click="downloadPractice">
                    下载练习
                  </el-button>
                  <el-button type="primary" :icon="Promotion" @click="startPractice">
                    开始练习
                  </el-button>
                </div>
              </div>
            </el-card>

            <!-- 题目列表 -->
            <el-card
              v-for="(question, index) in generatedQuestions"
              :key="index"
              class="question-card"
              shadow="hover"
            >
              <div class="question-header">
                <div class="header-tags">
                  <el-tag>{{ getQuestionTypeName(question.type) }}</el-tag>
                  <el-tag type="warning">{{ getDifficultyName(question.difficulty) }}</el-tag>
                  <el-tag type="info">{{ question.knowledgePoint }}</el-tag>
                </div>
                <el-button
                  :icon="isFavorite(question.id) ? StarFilled : Star"
                  :type="isFavorite(question.id) ? 'warning' : 'default'"
                  text
                  @click="toggleFavorite(question)"
                >
                  {{ isFavorite(question.id) ? '已收藏' : '收藏' }}
                </el-button>
              </div>
              <div class="question-content">
                <h4>{{ index + 1 }}. {{ question.content }}</h4>
                <div class="question-options" v-if="question.options">
                  <div
                    v-for="(option, optIndex) in question.options"
                    :key="optIndex"
                    class="option-item"
                  >
                    {{ option }}
                  </div>
                </div>
              </div>
              <div class="question-footer">
                <el-button text :icon="View" @click="showAnswer(question)">
                  查看答案
                </el-button>
                <el-button text :icon="ChatDotRound" @click="askAiAboutQuestion(question)">
                  AI讲解
                </el-button>
              </div>
            </el-card>
          </div>

          <!-- 练习模式 -->
          <div v-if="isPracticing" class="practice-section">
            <!-- 练习控制栏 -->
            <el-card class="practice-control" shadow="hover">
              <div class="control-content">
                <div class="control-left">
                  <h3>
                    {{ practiceForm.practiceMode === 'challenge' ? '闯关模式' :
                       practiceForm.practiceMode === 'timed' ? '计时模式' : '常规模式' }}
                  </h3>
                  <el-tag type="info">第 {{ currentQuestionIndex + 1 }} / {{ generatedQuestions.length }} 题</el-tag>
                </div>
                <div class="control-right">
                  <div v-if="practiceForm.practiceMode === 'timed'" class="timer">
                    <el-icon><Timer /></el-icon>
                    <span :class="{ 'time-warning': remainingTime < 300 }">
                      {{ formatTime(remainingTime) }}
                    </span>
                  </div>
                  <el-button @click="showAnswerSheet = true" :icon="List">
                    答题卡
                  </el-button>
                  <el-button @click="submitPractice" type="primary" :icon="Check">
                    提交答案
                  </el-button>
                  <el-button @click="exitPractice" :icon="CloseBold">
                    退出练习
                  </el-button>
                </div>
              </div>
              <!-- 进度条 -->
              <el-progress
                :percentage="practiceProgress"
                :stroke-width="8"
                :color="progressColors"
                class="practice-progress"
              />
            </el-card>

            <!-- 当前题目 -->
            <el-card class="current-question-card" shadow="hover">
              <div class="question-header">
                <div class="header-tags">
                  <el-tag>{{ getQuestionTypeName(currentQuestion.type) }}</el-tag>
                  <el-tag type="warning">{{ getDifficultyName(currentQuestion.difficulty) }}</el-tag>
                  <el-tag type="info">{{ currentQuestion.knowledgePoint }}</el-tag>
                </div>
                <div class="header-actions">
                  <el-button
                    :icon="isFavorite(currentQuestion.id) ? StarFilled : Star"
                    :type="isFavorite(currentQuestion.id) ? 'warning' : 'default'"
                    text
                    @click="toggleFavorite(currentQuestion)"
                  >
                    {{ isFavorite(currentQuestion.id) ? '已收藏' : '收藏' }}
                  </el-button>
                  <el-button
                    :icon="isMarked(currentQuestion.id) ? Flag : Flag"
                    :type="isMarked(currentQuestion.id) ? 'danger' : 'default'"
                    text
                    @click="toggleMark(currentQuestion.id)"
                  >
                    {{ isMarked(currentQuestion.id) ? '已标记' : '标记' }}
                  </el-button>
                </div>
              </div>

              <div class="question-content">
                <h3>{{ currentQuestionIndex + 1 }}. {{ currentQuestion.content }}</h3>

                <!-- 单选题 -->
                <el-radio-group
                  v-if="currentQuestion.type === 'single'"
                  v-model="userAnswers[currentQuestion.id]"
                  class="answer-options"
                >
                  <el-radio
                    v-for="(option, index) in currentQuestion.options"
                    :key="index"
                    :value="option.charAt(0)"
                    class="answer-option"
                  >
                    {{ option }}
                  </el-radio>
                </el-radio-group>

                <!-- 多选题 -->
                <el-checkbox-group
                  v-if="currentQuestion.type === 'multiple'"
                  v-model="userAnswers[currentQuestion.id]"
                  class="answer-options"
                >
                  <el-checkbox
                    v-for="(option, index) in currentQuestion.options"
                    :key="index"
                    :value="option.charAt(0)"
                    class="answer-option"
                  >
                    {{ option }}
                  </el-checkbox>
                </el-checkbox-group>

                <!-- 填空题 / 计算题 -->
                <el-input
                  v-if="currentQuestion.type === 'blank' || currentQuestion.type === 'calculation'"
                  v-model="userAnswers[currentQuestion.id]"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入答案"
                  class="answer-input"
                />
              </div>

              <div class="question-navigation">
                <el-button
                  :disabled="currentQuestionIndex === 0"
                  @click="previousQuestion"
                  :icon="ArrowLeft"
                >
                  上一题
                </el-button>
                <el-button
                  v-if="practiceForm.practiceMode !== 'challenge'"
                  :disabled="currentQuestionIndex === generatedQuestions.length - 1"
                  @click="nextQuestion"
                  :icon="ArrowRight"
                >
                  下一题
                </el-button>
                <el-button
                  v-if="practiceForm.practiceMode === 'challenge'"
                  type="primary"
                  @click="checkCurrentAnswer"
                  :icon="Check"
                  :disabled="!userAnswers[currentQuestion.id]"
                >
                  检查答案
                </el-button>

                <!-- AI批改按钮（仅主观题显示） -->
                <el-button
                  v-if="currentQuestion.type === 'calculation' || currentQuestion.type === 'blank'"
                  type="success"
                  @click="requestAiGrading"
                  :loading="aiGrading"
                  :disabled="!userAnswers[currentQuestion.id]"
                >
                  🤖 请AI批改
                </el-button>
              </div>
            </el-card>

            <!-- AI批改结果面板 -->
            <el-card v-if="showAiGrading && aiGradingResult" class="ai-grading-card" shadow="hover">
              <template #header>
                <div class="grading-header">
                  <span>🤖 AI智能批改</span>
                  <el-button text @click="showAiGrading = false">关闭</el-button>
                </div>
              </template>

              <div class="grading-content">
                <!-- 流式显示的批改内容 -->
                <div v-if="aiGrading" class="grading-streaming">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <p>AI正在批改中...</p>
                  <div class="streaming-text" v-html="renderMarkdown(streamingContent)"></div>
                </div>

                <!-- 完整批改结果 -->
                <div v-else class="grading-result" v-html="renderMarkdown(aiGradingResult)"></div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 答题卡对话框 -->
    <el-dialog
      v-model="showAnswerSheet"
      title="答题卡"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="answer-sheet">
        <div
          v-for="(question, index) in generatedQuestions"
          :key="question.id"
          :class="[
            'answer-sheet-item',
            {
              current: index === currentQuestionIndex,
              answered: userAnswers[question.id],
              marked: isMarked(question.id)
            }
          ]"
          @click="jumpToQuestion(index)"
        >
          <span class="item-number">{{ index + 1 }}</span>
          <el-icon v-if="isMarked(question.id)" class="mark-icon"><Flag /></el-icon>
        </div>
      </div>
      <template #footer>
        <div class="answer-sheet-legend">
          <div class="legend-item">
            <span class="legend-box current"></span>
            <span>当前题目</span>
          </div>
          <div class="legend-item">
            <span class="legend-box answered"></span>
            <span>已作答</span>
          </div>
          <div class="legend-item">
            <span class="legend-box marked"></span>
            <span>已标记</span>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog
      v-model="showStatsDialog"
      title="统计分析"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeStatsTab">
        <el-tab-pane label="总体统计" name="overall">
          <div class="stats-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="stats-chart-container">
                  <h4>答题情况分布</h4>
                  <div ref="answerDistChart" class="chart-box"></div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stats-chart-container">
                  <h4>题型正确率</h4>
                  <div ref="typeAccuracyChart" class="chart-box"></div>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="24">
                <div class="stats-chart-container">
                  <h4>每日练习趋势</h4>
                  <div ref="dailyTrendChart" class="chart-box" style="height: 300px"></div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="本周统计" name="week">
          <div class="stats-content">
            <el-row :gutter="20">
              <el-col :span="6" v-for="day in weekStats" :key="day.date">
                <el-card shadow="hover">
                  <div class="day-stat">
                    <div class="day-label">{{ day.label }}</div>
                    <div class="day-value">{{ day.count }}题</div>
                    <div class="day-accuracy">正确率 {{ day.accuracy }}%</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="本月统计" name="month">
          <div class="stats-content">
            <div class="month-summary">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-statistic title="本月练习次数" :value="monthStats.practiceCount" suffix="次" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="本月累计题数" :value="monthStats.totalQuestions" suffix="题" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="平均正确率" :value="monthStats.avgAccuracy" suffix="%" :precision="1" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="累计时长" :value="monthStats.totalTime" suffix="分钟" />
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 收藏题目对话框 -->
    <el-dialog
      v-model="showFavoriteDialog"
      title="收藏的题目"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="favoriteQuestions.length === 0" class="empty-favorite">
        <el-empty description="暂无收藏的题目" />
      </div>
      <div v-else class="favorite-list">
        <el-card
          v-for="(question, index) in favoriteQuestions"
          :key="question.id"
          class="favorite-question-card"
          shadow="hover"
        >
          <div class="question-header">
            <div class="header-tags">
              <el-tag>{{ getQuestionTypeName(question.type) }}</el-tag>
              <el-tag type="warning">{{ getDifficultyName(question.difficulty) }}</el-tag>
              <el-tag type="info">{{ question.knowledgePoint }}</el-tag>
            </div>
            <el-button
              :icon="StarFilled"
              type="warning"
              text
              @click="removeFavorite(question.id)"
            >
              取消收藏
            </el-button>
          </div>
          <div class="question-content">
            <h4>{{ index + 1 }}. {{ question.content }}</h4>
            <div class="question-options" v-if="question.options">
              <div
                v-for="(option, optIndex) in question.options"
                :key="optIndex"
                class="option-item"
              >
                {{ option }}
              </div>
            </div>
          </div>
          <div class="question-footer">
            <el-button text :icon="View" @click="showAnswer(question)">
              查看答案
            </el-button>
            <el-button text :icon="ChatDotRound" @click="askAiAboutQuestion(question)">
              AI讲解
            </el-button>
            <span class="favorite-date">收藏于: {{ question.favoriteDate }}</span>
          </div>
        </el-card>
      </div>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="历史生成记录"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div class="history-filters">
        <el-date-picker
          v-model="historyDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 300px"
          @change="filterHistory"
        />
        <el-input
          v-model="historySearchKeyword"
          placeholder="搜索知识点或科目"
          :prefix-icon="Search"
          style="width: 300px; margin-left: 12px"
          @input="filterHistory"
          clearable
        />
      </div>

      <el-table :data="filteredHistory" style="width: 100%; margin-top: 20px">
        <el-table-column prop="date" label="生成时间" width="180" />
        <el-table-column prop="subject" label="科目" width="100" />
        <el-table-column prop="mode" label="模式" width="120" />
        <el-table-column prop="questionCount" label="题目数" width="100" align="center" />
        <el-table-column prop="difficulty" label="难度" width="100" />
        <el-table-column label="完成情况" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.completed" type="success">已完成</el-tag>
            <el-tag v-else type="info">未完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="正确率" width="100">
          <template #default="{ row }">
            <span v-if="row.accuracy !== null">{{ row.accuracy }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="viewHistoryDetail(row)">查看详情</el-button>
            <el-button text size="small" type="primary" @click="redoHistory(row)">重做</el-button>
            <el-button text size="small" type="danger" @click="deleteHistory(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 历史记录详情对话框 -->
    <el-dialog
      v-model="showHistoryDetailDialog"
      title="练习详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedHistory" class="history-detail">
        <div class="detail-summary">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="生成时间">{{ selectedHistory.date }}</el-descriptions-item>
            <el-descriptions-item label="科目">{{ selectedHistory.subject }}</el-descriptions-item>
            <el-descriptions-item label="模式">{{ selectedHistory.mode }}</el-descriptions-item>
            <el-descriptions-item label="题目数量">{{ selectedHistory.questionCount }}</el-descriptions-item>
            <el-descriptions-item label="难度">{{ selectedHistory.difficulty }}</el-descriptions-item>
            <el-descriptions-item label="用时">{{ selectedHistory.timeSpent || '-' }}</el-descriptions-item>
            <el-descriptions-item label="正确率">
              <el-tag v-if="selectedHistory.accuracy !== null" :type="getAccuracyType(selectedHistory.accuracy)">
                {{ selectedHistory.accuracy }}%
              </el-tag>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="得分">
              {{ selectedHistory.score || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag v-if="selectedHistory.completed" type="success">已完成</el-tag>
              <el-tag v-else type="info">未完成</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider>题目列表</el-divider>

        <div class="detail-questions">
          <el-card
            v-for="(question, index) in selectedHistory.questions"
            :key="question.id"
            class="detail-question-card"
            shadow="hover"
          >
            <div class="question-header">
              <div class="header-tags">
                <el-tag>{{ getQuestionTypeName(question.type) }}</el-tag>
                <el-tag type="warning">{{ getDifficultyName(question.difficulty) }}</el-tag>
                <el-tag type="info">{{ question.knowledgePoint }}</el-tag>
              </div>
              <el-tag v-if="question.isCorrect !== undefined" :type="question.isCorrect ? 'success' : 'danger'">
                {{ question.isCorrect ? '正确' : '错误' }}
              </el-tag>
            </div>
            <div class="question-content">
              <h4>{{ index + 1 }}. {{ question.content }}</h4>
              <div class="question-options" v-if="question.options">
                <div
                  v-for="(option, optIndex) in question.options"
                  :key="optIndex"
                  :class="[
                    'option-item',
                    {
                      'correct-answer': isCorrectOption(question, option),
                      'wrong-answer': isWrongAnswer(question, option)
                    }
                  ]"
                >
                  {{ option }}
                </div>
              </div>
              <div v-if="question.userAnswer" class="answer-info">
                <p><strong>你的答案：</strong>{{ question.userAnswer }}</p>
                <p><strong>正确答案：</strong>{{ question.answer }}</p>
                <div class="answer-explanation" v-if="question.showExplanation">
                  <el-divider content-position="left">答案解析</el-divider>
                  <p>{{ question.explanation }}</p>
                </div>
              </div>
            </div>
            <div class="question-footer">
              <el-button
                text
                :icon="question.showExplanation ? ArrowUp : ArrowDown"
                @click="question.showExplanation = !question.showExplanation"
              >
                {{ question.showExplanation ? '收起解析' : '展开解析' }}
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </el-dialog>

    <!-- 完成统计对话框 -->
    <el-dialog
      v-model="showResultDialog"
      title="练习完成"
      width="700px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="result-summary">
        <div class="result-icon">
          <el-icon :size="80" :color="resultIconColor">
            <component :is="resultIcon" />
          </el-icon>
        </div>
        <h2>{{ resultTitle }}</h2>
        <p class="result-message">{{ resultMessage }}</p>

        <div class="result-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ practiceResult.totalQuestions }}</div>
                <div class="stat-label">总题数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value" style="color: #67C23A">{{ practiceResult.correctCount }}</div>
                <div class="stat-label">正确</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value" style="color: #F56C6C">{{ practiceResult.wrongCount }}</div>
                <div class="stat-label">错误</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value" style="color: #409EFF">{{ practiceResult.accuracy }}%</div>
                <div class="stat-label">正确率</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="result-chart">
          <div ref="resultPieChart" class="chart-box" style="height: 300px"></div>
        </div>

        <div class="result-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="练习时长">{{ practiceResult.timeSpent }}</el-descriptions-item>
            <el-descriptions-item label="平均用时">{{ practiceResult.avgTime }}</el-descriptions-item>
            <el-descriptions-item label="得分">{{ practiceResult.score }}</el-descriptions-item>
            <el-descriptions-item label="超越">{{ practiceResult.beatPercentage }}%的用户</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 新解锁的成就 -->
        <div v-if="newUnlockedBadges.length > 0" class="new-badges">
          <el-divider>恭喜解锁新成就</el-divider>
          <div class="badge-list">
            <div v-for="badge in newUnlockedBadges" :key="badge.id" class="new-badge-item">
              <el-icon :size="48" :color="badge.color">
                <component :is="badge.icon" />
              </el-icon>
              <div class="badge-name">{{ badge.name }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="result-actions">
          <el-button @click="viewAnswerAnalysis">查看解析</el-button>
          <el-button type="primary" @click="closeResultDialog">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
import {
  Edit, DocumentCopy, MagicStick, Star, Document, Loading,
  Download, Promotion, View, ChatDotRound, StarFilled, TrendCharts,
  Timer, List, Check, CloseBold, ArrowLeft, ArrowRight, Flag,
  Search, ArrowUp, ArrowDown, Trophy, Medal, Finished, WarningFilled
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { marked } from 'marked';
import { markRaw } from 'vue';
import { analyzeAnswerStream } from '@/api/ai';
import { useStudentAuthStore } from '@/stores/studentAuth';
import { fetchPracticeQuestions } from '@/api/question';
import type { Question as ApiQuestion } from '@/api/question';
// 【新增】导入科目API
import { fetchPracticeSubjects } from '@/api/studentAuth';
import type { Subject } from '@/api/subject';

const studentAuthStore = useStudentAuthStore();

// ==================== 类型定义 ====================
interface Question {
  id: string;
  type: 'single' | 'multiple' | 'blank' | 'calculation';
  difficulty: 'easy' | 'medium' | 'hard' | 'auto';
  knowledgePoint: string;
  content: string;
  options?: string[];
  answer: string;
  explanation: string;
  favoriteDate?: string;
  userAnswer?: string;
  isCorrect?: boolean;
  showExplanation?: boolean;
}

interface PracticeHistory {
  id: string;
  date: string;
  subject: string;
  mode: string;
  questionCount: number;
  difficulty: string;
  completed: boolean;
  accuracy: number | null;
  score?: string;
  timeSpent?: string;
  questions?: Question[];
}

interface AchievementBadge {
  id: string;
  name: string;
  description: string;
  icon: any;
  color: string;
  unlocked: boolean;
  progress: number;
  requirement: number;
}

interface PracticeResult {
  totalQuestions: number;
  correctCount: number;
  wrongCount: number;
  accuracy: number;
  timeSpent: string;
  avgTime: string;
  score: string;
  beatPercentage: number;
}

// ==================== 响应式数据 ====================
// 【新增】科目列表
const allSubjects = ref<Subject[]>([]);
const subjectsLoading = ref(false);

// 练习配置表单
const practiceForm = ref({
  mode: 'weakness' as 'weakness' | 'knowledge' | 'wrong' | 'custom',
  practiceMode: 'normal' as 'normal' | 'challenge' | 'timed',
  timeLimit: 30,
  subject: '', // 【修改】默认为空，等待加载后设置
  weaknessPoints: [] as number[],
  knowledgePoints: [] as any[],
  questionCount: 20,
  difficulty: 'auto' as 'easy' | 'medium' | 'hard' | 'auto',
  questionTypes: ['single', 'multiple'] as string[]
});

// 薄弱知识点
const weaknessKnowledgePoints = ref([
  { id: 1, name: '函数与导数', masteryRate: 65 },
  { id: 2, name: '牛顿运动定律', masteryRate: 55 },
  { id: 3, name: '电磁感应', masteryRate: 70 },
  { id: 4, name: '有机化学', masteryRate: 68 }
]);

// 知识点树
const knowledgePointTree = ref([
  {
    value: 'function',
    label: '函数',
    children: [
      { value: 'linear', label: '一次函数' },
      { value: 'quadratic', label: '二次函数' },
      { value: 'derivative', label: '导数' }
    ]
  },
  {
    value: 'geometry',
    label: '几何',
    children: [
      { value: 'triangle', label: '三角形' },
      { value: 'circle', label: '圆' }
    ]
  }
]);

// AI推荐
const aiRecommendations = ref([
  {
    title: '针对薄弱点强化',
    description: '根据你的错题记录，推荐函数与导数相关练习',
    color: '#E6A23C',
    config: { mode: 'weakness', subject: 'math', questionCount: 15 }
  },
  {
    title: '全面复习套餐',
    description: '涵盖所有知识点的综合练习',
    color: '#409EFF',
    config: { mode: 'custom', questionCount: 30, difficulty: 'medium' }
  },
  {
    title: '错题重练',
    description: '针对历史错题进行专项训练',
    color: '#F56C6C',
    config: { mode: 'wrong', questionCount: 20 }
  }
]);

// 生成状态
const generating = ref(false);
const generatingProgress = ref(0);
const estimatedTime = ref(10);

// 生成的题目
const generatedQuestions = ref<Question[]>([]);
const estimatedCompletionTime = ref(0);

// 练习状态
const isPracticing = ref(false);
const currentQuestionIndex = ref(0);
const userAnswers = ref<Record<string, any>>({});
const markedQuestions = ref<Set<string>>(new Set());
const practiceStartTime = ref<number>(0);
const remainingTime = ref(0);
let timerInterval: number | null = null;

// 收藏的题目
const favoriteQuestions = ref<Question[]>([]);

// 对话框显示状态
const showHistoryDialog = ref(false);
const showFavoriteDialog = ref(false);
const showStatsDialog = ref(false);
const showAnswerSheet = ref(false);
const showHistoryDetailDialog = ref(false);
const showResultDialog = ref(false);

// AI批改相关
const aiGrading = ref(false);
const showAiGrading = ref(false);
const aiGradingResult = ref('');
const streamingContent = ref('');
const currentGradingQuestion = ref<Question | null>(null);

// 历史记录
const practiceHistory = ref<PracticeHistory[]>([]);
const historyDateRange = ref<[Date, Date] | null>(null);
const historySearchKeyword = ref('');
const selectedHistory = ref<PracticeHistory | null>(null);

// 统计相关
const activeStatsTab = ref('overall');
const totalPracticeCount = ref(0);
const totalQuestionCount = ref(0);
const continuousDays = ref(0);
const averageAccuracy = ref(0);
const weekStats = ref([
  { date: '2026-01-04', label: '周一', count: 15, accuracy: 85 },
  { date: '2026-01-05', label: '周二', count: 20, accuracy: 78 },
  { date: '2026-01-06', label: '周三', count: 18, accuracy: 82 },
  { date: '2026-01-07', label: '周四', count: 22, accuracy: 88 },
  { date: '2026-01-08', label: '周五', count: 16, accuracy: 80 },
  { date: '2026-01-09', label: '周六', count: 25, accuracy: 90 },
  { date: '2026-01-10', label: '周日', count: 20, accuracy: 86 }
]);
const monthStats = ref({
  practiceCount: 28,
  totalQuestions: 560,
  avgAccuracy: 84.5,
  totalTime: 1680
});

// 成就徽章
const achievementBadges = ref<AchievementBadge[]>([
  {
    id: 'first_practice',
    name: '初来乍到',
    description: '完成第一次练习',
    icon: markRaw(Medal),
    color: '#E6A23C',
    unlocked: true,
    progress: 100,
    requirement: 1
  },
  {
    id: 'continuous_7',
    name: '坚持不懈',
    description: '连续练习7天',
    icon: markRaw(Trophy),
    color: '#409EFF',
    unlocked: false,
    progress: 60,
    requirement: 7
  },
  {
    id: 'total_100',
    name: '百题斩',
    description: '累计完成100道题',
    icon: markRaw(Finished),
    color: '#67C23A',
    unlocked: false,
    progress: 75,
    requirement: 100
  },
  {
    id: 'accuracy_90',
    name: '学霸之路',
    description: '单次练习正确率达90%',
    icon: markRaw(StarFilled),
    color: '#F56C6C',
    unlocked: false,
    progress: 50,
    requirement: 90
  }
]);

// 练习结果
const practiceResult = ref<PracticeResult>({
  totalQuestions: 0,
  correctCount: 0,
  wrongCount: 0,
  accuracy: 0,
  timeSpent: '0分0秒',
  avgTime: '0秒/题',
  score: '0',
  beatPercentage: 0
});

const newUnlockedBadges = ref<AchievementBadge[]>([]);

// ECharts 图表实例
const answerDistChart = ref<HTMLElement | null>(null);
const typeAccuracyChart = ref<HTMLElement | null>(null);
const dailyTrendChart = ref<HTMLElement | null>(null);
const resultPieChart = ref<HTMLElement | null>(null);

// ==================== 计算属性 ====================
const currentQuestion = computed(() => {
  return generatedQuestions.value[currentQuestionIndex.value];
});

// 监听当前题目变化，确保多选题答案初始化为数组
watch(currentQuestion, (newQuestion) => {
  if (newQuestion && newQuestion.type === 'multiple') {
    if (!userAnswers.value[newQuestion.id]) {
      userAnswers.value[newQuestion.id] = [];
    } else if (!Array.isArray(userAnswers.value[newQuestion.id])) {
      // 如果已存在但不是数组，转换为数组
      userAnswers.value[newQuestion.id] = [userAnswers.value[newQuestion.id]];
    }
  }
}, { immediate: true });

const practiceProgress = computed(() => {
  const answered = Object.keys(userAnswers.value).length;
  return Math.round((answered / generatedQuestions.value.length) * 100);
});

const progressColors = computed(() => {
  return [
    { color: '#f56c6c', percentage: 30 },
    { color: '#e6a23c', percentage: 60 },
    { color: '#67c23a', percentage: 100 }
  ];
});

const filteredHistory = computed(() => {
  let result = [...practiceHistory.value];

  // 日期筛选
  if (historyDateRange.value) {
    const [start, end] = historyDateRange.value;
    result = result.filter(item => {
      const itemDate = new Date(item.date);
      return itemDate >= start && itemDate <= end;
    });
  }

  // 关键词搜索
  if (historySearchKeyword.value) {
    const keyword = historySearchKeyword.value.toLowerCase();
    result = result.filter(item =>
      item.subject.toLowerCase().includes(keyword) ||
      item.mode.toLowerCase().includes(keyword)
    );
  }

  return result;
});

const resultIcon = computed(() => {
  const accuracy = practiceResult.value.accuracy;
  if (accuracy >= 90) return Trophy;
  if (accuracy >= 70) return Medal;
  if (accuracy >= 60) return Finished;
  return WarningFilled;
});

const resultIconColor = computed(() => {
  const accuracy = practiceResult.value.accuracy;
  if (accuracy >= 90) return '#F56C6C';
  if (accuracy >= 70) return '#E6A23C';
  if (accuracy >= 60) return '#409EFF';
  return '#909399';
});

const resultTitle = computed(() => {
  const accuracy = practiceResult.value.accuracy;
  if (accuracy >= 90) return '太棒了！';
  if (accuracy >= 70) return '做得不错！';
  if (accuracy >= 60) return '继续加油！';
  return '需要加强练习';
});

const resultMessage = computed(() => {
  const accuracy = practiceResult.value.accuracy;
  if (accuracy >= 90) return '你已经掌握得非常好了，继续保持！';
  if (accuracy >= 70) return '掌握得还不错，再接再厉！';
  if (accuracy >= 60) return '有进步的空间，继续努力！';
  return '建议针对薄弱知识点进行强化训练';
});

// ==================== 生命周期 ====================
// 【新增】加载科目列表
const loadSubjects = async () => {
  subjectsLoading.value = true;
  try {
    const res = await fetchPracticeSubjects();
    if (res.code === 200) {
      allSubjects.value = res.data || [];
      // 如果有科目，设置第一个为默认值
      if (allSubjects.value.length > 0 && !practiceForm.value.subject) {
        practiceForm.value.subject = allSubjects.value[0].id.toString();
      }
    } else {
      ElMessage.error('加载科目列表失败');
    }
  } catch (error) {
    console.error('加载科目失败:', error);
    ElMessage.error('加载科目列表失败');
  } finally {
    subjectsLoading.value = false;
  }
};

onMounted(() => {
  loadSubjects(); // 【新增】加载科目
  loadFromLocalStorage();
  calculateStatistics();
});

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }
});

// ==================== 方法 ====================
// 获取掌握度颜色
const getMasteryColor = (rate: number) => {
  if (rate >= 80) return '#67C23A';
  if (rate >= 60) return '#E6A23C';
  return '#F56C6C';
};

// 模式切换
const handleModeChange = () => {
  const modeNames: Record<string, string> = {
    weakness: '薄弱知识点',
    knowledge: '指定知识点',
    wrong: '错题重练',
    custom: '自定义'
  };
  ElMessage.info(`已切换到${modeNames[practiceForm.value.mode]}模式`);
};

// 生成练习
const generatePractice = async () => {
  if (practiceForm.value.mode !== 'wrong' && practiceForm.value.questionTypes.length === 0) {
    ElMessage.warning('请至少选择一种题型');
    return;
  }

  generating.value = true;
  generatingProgress.value = 0;

  // 进度模拟
  const interval = setInterval(() => {
    if (generatingProgress.value < 90) {
      generatingProgress.value += 10;
    }
  }, 300);

  try {
    // 题型映射：前端 -> 后端 (1-单选, 2-多选, 3-填空, 5-主观, 6-计算)
    const typeMap: Record<string, number> = {
      'single': 1,
      'multiple': 2,
      'blank': 3,
      'calculation': 6
    };

    const questionCount = practiceForm.value.mode === 'wrong' ? 10 : practiceForm.value.questionCount;

    // 如果选择了多种题型，需要分别获取并合并
    let allQuestions: ApiQuestion[] = [];

    if (practiceForm.value.questionTypes.length > 0) {
      // 每种题型获取相应数量的题目
      const countPerType = Math.ceil(questionCount / practiceForm.value.questionTypes.length);

      for (const type of practiceForm.value.questionTypes) {
        const questionType = typeMap[type];
        if (!questionType) continue;

        generatingProgress.value = 30 + (practiceForm.value.questionTypes.indexOf(type) / practiceForm.value.questionTypes.length) * 50;

        const response = await fetchPracticeQuestions({
          current: 1,
          size: countPerType,
          questionType: questionType,
          subjectId: getSubjectId(practiceForm.value.subject)
        });

        if (response.code === 200 && response.data) {
          allQuestions = allQuestions.concat(response.data);
        }
      }
    } else {
      // 如果没有选择题型，获取所有类型
      const response = await fetchPracticeQuestions({
        current: 1,
        size: questionCount,
        subjectId: getSubjectId(practiceForm.value.subject)
      });

      if (response.code === 200 && response.data) {
        allQuestions = response.data;
      }
    }

    generatingProgress.value = 90;

    if (allQuestions.length === 0) {
      ElMessage.warning('未找到符合条件的题目，请调整筛选条件或联系管理员添加题目');
      return;
    }

    // 随机打乱题目顺序
    allQuestions = shuffleArray(allQuestions).slice(0, questionCount);

    // 转换为前端格式
    generatedQuestions.value = allQuestions.map((q, index) => ({
      id: `q_${q.id}_${index}`,
      type: getQuestionTypeKey(q.questionType),
      difficulty: estimateDifficulty(),
      knowledgePoint: q.knowledgePointIds && q.knowledgePointIds.length > 0
        ? `知识点${q.knowledgePointIds[0]}`
        : '综合练习',
      content: q.content,
      imageUrl: q.imageUrl,
      options: parseOptions(q.options),
      answer: q.answer,
      answerImageUrl: q.answerImageUrl,
      explanation: q.description || '暂无解析'
    }));

    generatingProgress.value = 100;
    estimatedCompletionTime.value = generatedQuestions.value.length * 2;

    ElMessage.success(`成功生成${generatedQuestions.value.length}道练习题！`);
  } catch (error: any) {
    console.error('生成练习失败:', error);
    ElMessage.error('生成失败: ' + (error.message || '请重试'));
  } finally {
    clearInterval(interval);
    generating.value = false;
  }
};

// 辅助函数：获取科目ID
const getSubjectId = (subject: string): number | undefined => {
  // 【修改】直接将字符串转换为数字ID（因为现在存储的就是ID）
  const subjectId = parseInt(subject, 10);
  return isNaN(subjectId) ? undefined : subjectId;
};

// 辅助函数：将后端题型转换为前端类型
const getQuestionTypeKey = (questionType: number): string => {
  const typeMap: Record<number, string> = {
    1: 'single',      // 单选
    2: 'multiple',    // 多选
    3: 'blank',       // 填空
    4: 'judge',       // 判断
    5: 'subjective',  // 主观
    6: 'calculation'  // 计算
  };
  return typeMap[questionType] || 'single';
};

// 辅助函数：解析选项
const parseOptions = (options: string | any[]): string[] => {
  if (Array.isArray(options)) {
    return options.map((opt: any) =>
      typeof opt === 'string' ? opt : `${opt.key}. ${opt.value}`
    );
  }
  if (typeof options === 'string') {
    try {
      const parsed = JSON.parse(options);
      if (Array.isArray(parsed)) {
        return parsed.map((opt: any) =>
          typeof opt === 'string' ? opt : `${opt.key}. ${opt.value}`
        );
      }
    } catch (e) {
      console.warn('选项解析失败:', e);
    }
  }
  return [];
};

// 辅助函数：随机打乱数组
const shuffleArray = <T>(array: T[]): T[] => {
  const newArray = [...array];
  for (let i = newArray.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [newArray[i], newArray[j]] = [newArray[j], newArray[i]];
  }
  return newArray;
};

// 辅助函数：估算难度（可以根据题目统计数据改进）
const estimateDifficulty = (): string => {
  const difficulties = ['easy', 'medium', 'hard'];
  return difficulties[Math.floor(Math.random() * difficulties.length)];
};

// 应用推荐
const applyRecommendation = (rec: any) => {
  Object.assign(practiceForm.value, rec.config);
  ElMessage.success('已应用推荐配置');
};

// 获取题型名称
const getQuestionTypeName = (type: string) => {
  const names: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    blank: '填空题',
    calculation: '计算题'
  };
  return names[type] || type;
};

// 获取难度名称
const getDifficultyName = (difficulty: string) => {
  const names: Record<string, string> = {
    easy: '简单',
    medium: '中等',
    hard: '困难',
    auto: '自适应'
  };
  return names[difficulty] || difficulty;
};

// 查看答案
const showAnswer = (question: Question) => {
  ElMessageBox.alert(
    `<p><strong>答案：</strong>${question.answer}</p>
     <p><strong>解析：</strong>${question.explanation}</p>`,
    '题目答案',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '知道了'
    }
  );
};

// AI讲解
const askAiAboutQuestion = (question: Question) => {
  ElMessage.info('AI讲解功能开发中...');
};

// Markdown渲染
const renderMarkdown = (text: string): string => {
  if (!text) return '';
  return marked(text) as string;
};

// AI批改功能
const requestAiGrading = async () => {
  const question = currentQuestion.value;
  const userAnswer = userAnswers.value[question.id];

  if (!userAnswer || !userAnswer.trim()) {
    ElMessage.warning('请先完成答题');
    return;
  }

  // 检查是否配置了AI Key
  if (!studentAuthStore.aiKey) {
    ElMessage.warning('请先在个人设置中配置AI API Key');
    return;
  }

  aiGrading.value = true;
  showAiGrading.value = true;
  streamingContent.value = '';
  aiGradingResult.value = '';
  currentGradingQuestion.value = question;

  try {
    analyzeAnswerStream(
      {
        questionId: parseInt(question.id.split('_')[1]) || 0,
        questionType: question.type === 'calculation' ? 6 : 3,
        questionContent: question.content,
        correctAnswer: question.answer || '参考答案',
        userAnswer: userAnswer,
        maxScore: 100
      },
      // onChunk - 流式接收数据
      (chunk: string) => {
        streamingContent.value += chunk;
      },
      // onComplete - 完成
      () => {
        aiGrading.value = false;
        aiGradingResult.value = streamingContent.value;
        ElNotification({
          title: '✅ 批改完成',
          message: 'AI批改已完成，请查看详细反馈',
          type: 'success',
          duration: 3000
        });
      },
      // onError - 错误
      (error: Error) => {
        aiGrading.value = false;
        showAiGrading.value = false;
        ElMessage.error('AI批改失败: ' + error.message);
      }
    );
  } catch (error: any) {
    aiGrading.value = false;
    showAiGrading.value = false;
    ElMessage.error('AI批改请求失败: ' + error.message);
  }
};


// 开始练习
const startPractice = () => {
  isPracticing.value = true;
  currentQuestionIndex.value = 0;
  userAnswers.value = {};
  markedQuestions.value.clear();
  practiceStartTime.value = Date.now();

  // 如果是计时模式，启动倒计时
  if (practiceForm.value.practiceMode === 'timed') {
    remainingTime.value = practiceForm.value.timeLimit * 60;
    startTimer();
  }

  ElMessage.success('开始练习！');
};

// 启动计时器
const startTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }

  timerInterval = window.setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--;
    } else {
      clearInterval(timerInterval!);
      ElMessage.warning('时间到！自动提交答案');
      submitPractice();
    }
  }, 1000);
};

// 格式化时间
const formatTime = (seconds: number) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 上一题
const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--;
  }
};

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < generatedQuestions.value.length - 1) {
    currentQuestionIndex.value++;
  }
};

// 智能匹配填空题答案（支持多空题）
const checkFillBlankAnswer = (correctAnswer: string, userAnswer: string): boolean => {
  // 统一转换中文逗号为英文逗号
  const correctLower = correctAnswer.toLowerCase().trim().replace(/，/g, ',');
  const userLower = userAnswer.toLowerCase().trim().replace(/，/g, ',');

  if (!correctLower.includes('###')) {
    // 单答案，直接匹配
    return userLower === correctLower;
  }

  const parts = correctLower.split('###').map(ans => ans.trim());

  // 检查是否为多空题格式（某些部分包含逗号，某些不包含）
  const templatePart = parts.find(p => p.includes(','));

  if (templatePart && userLower.includes(',')) {
    // 多空题：模板格式 "固定部分，可变部分1###可变部分2###可变部分3"
    const templateFields = templatePart.split(',').map(f => f.trim());
    const userFields = userLower.split(',').map(f => f.trim());

    if (templateFields.length !== userFields.length) {
      // 空格数量不匹配，尝试完整匹配
      return parts.some(ans => userLower === ans);
    }

    // 逐个字段检查
    return templateFields.every((templateField, index) => {
      const userField = userFields[index];

      // 收集这个位置的所有可能答案
      const possibleAnswers: string[] = [];

      parts.forEach(p => {
        if (p.includes(',')) {
          // 从完整答案中提取对应字段
          const fields = p.split(',').map(f => f.trim());
          if (fields[index]) {
            possibleAnswers.push(fields[index]);
          }
        } else if (index === templateFields.length - 1) {
          // 单个词可能是最后一个字段的替代答案
          possibleAnswers.push(p);
        }
      });

      // 检查用户输入是否在可能答案中
      return possibleAnswers.includes(userField);
    });
  } else {
    // 单空多答案：直接匹配任意一个
    return parts.some(ans => userLower === ans);
  }
};

// 检查当前答案（闯关模式）
const checkCurrentAnswer = () => {
  const question = currentQuestion.value;
  const userAnswer = userAnswers.value[question.id];

  // 检查答案是否正确
  let isCorrect = false;
  if (question.type === 'multiple') {
    const sortedUserAnswer = Array.isArray(userAnswer) ? userAnswer.sort().join('') : '';
    const sortedCorrectAnswer = question.answer.split('').sort().join('');
    isCorrect = sortedUserAnswer === sortedCorrectAnswer;
  } else {
    // 使用智能匹配函数
    isCorrect = checkFillBlankAnswer(question.answer, userAnswer);
  }

  if (isCorrect) {
    ElMessage.success('回答正确！进入下一题');
    if (currentQuestionIndex.value < generatedQuestions.value.length - 1) {
      currentQuestionIndex.value++;
    } else {
      ElMessage.info('已完成所有题目！');
      submitPractice();
    }
  } else {
    ElMessageBox.confirm(
      `回答错误！正确答案是：${question.answer}<br><br>${question.explanation}`,
      '答案解析',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '重新作答',
        cancelButtonText: '查看解析',
        type: 'error'
      }
    ).catch(() => {});
  }
};

// 跳转到指定题目
const jumpToQuestion = (index: number) => {
  currentQuestionIndex.value = index;
  showAnswerSheet.value = false;
};

// 退出练习
const exitPractice = () => {
  ElMessageBox.confirm(
    '确定要退出练习吗？当前进度不会保存。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    isPracticing.value = false;
    if (timerInterval) {
      clearInterval(timerInterval);
    }
    ElMessage.info('已退出练习');
  }).catch(() => {});
};

// 提交练习
const submitPractice = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
  }

  // 计算结果
  const endTime = Date.now();
  const timeSpentSeconds = Math.floor((endTime - practiceStartTime.value) / 1000);
  const timeSpentMinutes = Math.floor(timeSpentSeconds / 60);
  const timeSpentSecondsRemainder = timeSpentSeconds % 60;

  let correctCount = 0;
  const questionsWithResults = generatedQuestions.value.map(question => {
    const userAnswer = userAnswers.value[question.id];
    let isCorrect = false;

    if (question.type === 'multiple') {
      const sortedUserAnswer = Array.isArray(userAnswer) ? userAnswer.sort().join('') : '';
      const sortedCorrectAnswer = question.answer.split('').sort().join('');
      isCorrect = sortedUserAnswer === sortedCorrectAnswer;
    } else {
      // 使用智能匹配函数
      isCorrect = userAnswer ? checkFillBlankAnswer(question.answer, userAnswer) : false;
    }

    if (isCorrect) {
      correctCount++;
    }

    return {
      ...question,
      userAnswer: Array.isArray(userAnswer) ? userAnswer.join('') : userAnswer,
      isCorrect
    };
  });

  const wrongCount = generatedQuestions.value.length - correctCount;
  const accuracy = Math.round((correctCount / generatedQuestions.value.length) * 100);

  practiceResult.value = {
    totalQuestions: generatedQuestions.value.length,
    correctCount,
    wrongCount,
    accuracy,
    timeSpent: `${timeSpentMinutes}分${timeSpentSecondsRemainder}秒`,
    avgTime: `${Math.round(timeSpentSeconds / generatedQuestions.value.length)}秒/题`,
    score: `${correctCount * 5}`,
    beatPercentage: Math.min(95, 50 + accuracy / 2)
  };

  // 保存到历史记录
  const historyRecord: PracticeHistory = {
    id: `history_${Date.now()}`,
    date: new Date().toLocaleString('zh-CN'),
    subject: practiceForm.value.subject === 'math' ? '数学' : practiceForm.value.subject,
    mode: practiceForm.value.mode === 'weakness' ? '薄弱知识点' :
          practiceForm.value.mode === 'knowledge' ? '指定知识点' :
          practiceForm.value.mode === 'wrong' ? '错题重练' : '自定义',
    questionCount: generatedQuestions.value.length,
    difficulty: getDifficultyName(practiceForm.value.difficulty),
    completed: true,
    accuracy,
    score: practiceResult.value.score,
    timeSpent: practiceResult.value.timeSpent,
    questions: questionsWithResults
  };

  practiceHistory.value.unshift(historyRecord);
  saveToLocalStorage();

  // 检查是否解锁新成就
  checkAchievements(accuracy);

  // 更新统计数据
  calculateStatistics();

  isPracticing.value = false;
  showResultDialog.value = true;

  // 渲染结果图表
  nextTick(() => {
    renderResultChart();
  });
};

// 检查成就解锁
const checkAchievements = (accuracy: number) => {
  newUnlockedBadges.value = [];

  achievementBadges.value.forEach(badge => {
    if (badge.unlocked) return;

    let shouldUnlock = false;

    switch (badge.id) {
      case 'first_practice':
        shouldUnlock = totalPracticeCount.value >= 1;
        break;
      case 'continuous_7':
        shouldUnlock = continuousDays.value >= 7;
        break;
      case 'total_100':
        shouldUnlock = totalQuestionCount.value >= 100;
        break;
      case 'accuracy_90':
        shouldUnlock = accuracy >= 90;
        break;
    }

    if (shouldUnlock) {
      badge.unlocked = true;
      badge.progress = 100;
      newUnlockedBadges.value.push(badge);
    }
  });

  saveToLocalStorage();
};

// 查看答案解析
const viewAnswerAnalysis = () => {
  showResultDialog.value = false;

  // 查看最新的历史记录详情
  if (practiceHistory.value.length > 0) {
    selectedHistory.value = practiceHistory.value[0];
    selectedHistory.value.questions?.forEach(q => {
      q.showExplanation = false;
    });
    showHistoryDetailDialog.value = true;
  }
};

// 关闭结果对话框
const closeResultDialog = () => {
  showResultDialog.value = false;
};

// 收藏相关
const isFavorite = (questionId: string) => {
  return favoriteQuestions.value.some(q => q.id === questionId);
};

const toggleFavorite = (question: Question) => {
  const index = favoriteQuestions.value.findIndex(q => q.id === question.id);

  if (index > -1) {
    favoriteQuestions.value.splice(index, 1);
    ElMessage.success('已取消收藏');
  } else {
    favoriteQuestions.value.unshift({
      ...question,
      favoriteDate: new Date().toLocaleString('zh-CN')
    });
    ElMessage.success('收藏成功');
  }

  saveToLocalStorage();
};

const removeFavorite = (questionId: string) => {
  const index = favoriteQuestions.value.findIndex(q => q.id === questionId);
  if (index > -1) {
    favoriteQuestions.value.splice(index, 1);
    ElMessage.success('已取消收藏');
    saveToLocalStorage();
  }
};

// 标记相关
const isMarked = (questionId: string) => {
  return markedQuestions.value.has(questionId);
};

const toggleMark = (questionId: string) => {
  if (markedQuestions.value.has(questionId)) {
    markedQuestions.value.delete(questionId);
    ElMessage.info('已取消标记');
  } else {
    markedQuestions.value.add(questionId);
    ElMessage.success('已标记为疑难题');
  }
};

// 下载练习
const downloadPractice = () => {
  ElMessage.info('下载功能开发中...');
};

// 历史记录相关
const filterHistory = () => {
  // 过滤逻辑已在 computed 中实现
};

const viewHistoryDetail = (record: PracticeHistory) => {
  selectedHistory.value = record;
  selectedHistory.value.questions?.forEach(q => {
    q.showExplanation = false;
  });
  showHistoryDetailDialog.value = true;
};

const redoHistory = (record: PracticeHistory) => {
  if (record.questions) {
    generatedQuestions.value = record.questions.map(q => ({
      ...q,
      userAnswer: undefined,
      isCorrect: undefined,
      showExplanation: undefined
    }));
    showHistoryDialog.value = false;
    ElMessage.success('已加载历史题目，可以重新练习');
  }
};

const deleteHistory = (record: PracticeHistory) => {
  ElMessageBox.confirm('确定删除这条记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = practiceHistory.value.findIndex(item => item.id === record.id);
    if (index > -1) {
      practiceHistory.value.splice(index, 1);
      saveToLocalStorage();
      calculateStatistics();
      ElMessage.success('删除成功');
    }
  }).catch(() => {});
};

const isCorrectOption = (question: Question, option: string) => {
  if (!question.userAnswer) return false;
  const optionLetter = option.charAt(0);
  return question.answer.includes(optionLetter);
};

const isWrongAnswer = (question: Question, option: string) => {
  if (!question.userAnswer || question.isCorrect) return false;
  const optionLetter = option.charAt(0);
  return question.userAnswer.includes(optionLetter) && !question.answer.includes(optionLetter);
};

const getAccuracyType = (accuracy: number) => {
  if (accuracy >= 90) return 'success';
  if (accuracy >= 70) return 'warning';
  return 'danger';
};

// 计算统计数据
const calculateStatistics = () => {
  totalPracticeCount.value = practiceHistory.value.filter(h => h.completed).length;

  totalQuestionCount.value = practiceHistory.value
    .filter(h => h.completed)
    .reduce((sum, h) => sum + h.questionCount, 0);

  const accuracies = practiceHistory.value
    .filter(h => h.completed && h.accuracy !== null)
    .map(h => h.accuracy as number);

  if (accuracies.length > 0) {
    averageAccuracy.value = Math.round(
      accuracies.reduce((sum, acc) => sum + acc, 0) / accuracies.length
    );
  }

  // 计算连续天数（简化版）
  continuousDays.value = Math.min(7, totalPracticeCount.value);

  // 更新成就进度
  updateAchievementProgress();
};

// 更新成就进度
const updateAchievementProgress = () => {
  achievementBadges.value.forEach(badge => {
    if (badge.unlocked) {
      badge.progress = 100;
      return;
    }

    switch (badge.id) {
      case 'first_practice':
        badge.progress = Math.min(100, totalPracticeCount.value * 100);
        break;
      case 'continuous_7':
        badge.progress = Math.min(100, (continuousDays.value / 7) * 100);
        break;
      case 'total_100':
        badge.progress = Math.min(100, (totalQuestionCount.value / 100) * 100);
        break;
      case 'accuracy_90':
        badge.progress = Math.min(100, (averageAccuracy.value / 90) * 100);
        break;
    }
  });
};

// LocalStorage 持久化
const saveToLocalStorage = () => {
  try {
    localStorage.setItem('ai_practice_history', JSON.stringify(practiceHistory.value));
    localStorage.setItem('ai_practice_favorites', JSON.stringify(favoriteQuestions.value));
    localStorage.setItem('ai_practice_achievements', JSON.stringify(achievementBadges.value));
  } catch (error) {
    console.error('保存数据失败:', error);
  }
};

const loadFromLocalStorage = () => {
  try {
    const historyData = localStorage.getItem('ai_practice_history');
    if (historyData) {
      practiceHistory.value = JSON.parse(historyData);
    }

    const favoritesData = localStorage.getItem('ai_practice_favorites');
    if (favoritesData) {
      favoriteQuestions.value = JSON.parse(favoritesData);
    }

    const achievementsData = localStorage.getItem('ai_practice_achievements');
    if (achievementsData) {
      const loadedBadges = JSON.parse(achievementsData);
      // 合并已解锁状态
      achievementBadges.value.forEach(badge => {
        const loaded = loadedBadges.find((b: AchievementBadge) => b.id === badge.id);
        if (loaded) {
          badge.unlocked = loaded.unlocked;
          badge.progress = loaded.progress;
        }
      });
    }
  } catch (error) {
    console.error('加载数据失败:', error);
  }
};

// 渲染统计图表
const renderResultChart = () => {
  if (!resultPieChart.value) return;

  const chart = echarts.init(resultPieChart.value);
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        name: '答题情况',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}题'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: practiceResult.value.correctCount, name: '正确', itemStyle: { color: '#67C23A' } },
          { value: practiceResult.value.wrongCount, name: '错误', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  };

  chart.setOption(option);
};

// 监听统计对话框打开，渲染图表
watch(showStatsDialog, (newVal) => {
  if (newVal) {
    nextTick(() => {
      renderStatsCharts();
    });
  }
});

const renderStatsCharts = () => {
  // 答题情况分布
  if (answerDistChart.value) {
    const chart1 = echarts.init(answerDistChart.value);
    chart1.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '70%',
        data: [
          { value: totalQuestionCount.value * 0.8, name: '正确', itemStyle: { color: '#67C23A' } },
          { value: totalQuestionCount.value * 0.2, name: '错误', itemStyle: { color: '#F56C6C' } }
        ]
      }]
    });
  }

  // 题型正确率
  if (typeAccuracyChart.value) {
    const chart2 = echarts.init(typeAccuracyChart.value);
    chart2.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: ['单选题', '多选题', '填空题', '计算题'] },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{
        data: [85, 78, 82, 88],
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      }]
    });
  }

  // 每日练习趋势
  if (dailyTrendChart.value) {
    const chart3 = echarts.init(dailyTrendChart.value);
    chart3.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['题目数量', '正确率'] },
      xAxis: { type: 'category', data: weekStats.value.map(d => d.label) },
      yAxis: [
        { type: 'value', name: '题目数量', position: 'left' },
        { type: 'value', name: '正确率(%)', position: 'right', max: 100 }
      ],
      series: [
        {
          name: '题目数量',
          type: 'bar',
          data: weekStats.value.map(d => d.count),
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '正确率',
          type: 'line',
          yAxisIndex: 1,
          data: weekStats.value.map(d => d.accuracy),
          itemStyle: { color: '#67C23A' }
        }
      ]
    });
  }
};
</script>

<style scoped>
.ai-practice-page {
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
  border-bottom: 1px solid var(--border-color);
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
  color: var(--text-secondary);
}

.header-right {
  display: flex;
  gap: 12px;
}

.header-badge {
  margin-right: 0;
}

/* 主要内容 */
.page-content {
  padding: 20px;
}

/* 配置卡片 */
.config-card {
  border-radius: 12px;
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}

/* 成就徽章 */
.achievement-badges {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.badge-item {
  padding: 12px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  transition: all 0.3s;
}

.badge-item.unlocked {
  background: linear-gradient(135deg, #fff4e6 0%, #ffe7ba 100%);
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
}

.badge-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.badge-name {
  font-size: 12px;
  font-weight: 500;
  text-align: center;
}

/* 统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-item {
  padding: 12px;
  background: linear-gradient(135deg, #667eea11 0%, #764ba211 100%);
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

/* AI推荐 */
.ai-recommendations {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommendation-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #667eea11 0%, #764ba211 100%);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.recommendation-item:hover {
  background: linear-gradient(135deg, #667eea22 0%, #764ba222 100%);
  transform: translateX(4px);
}

.rec-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: var(--text-primary);
}

.rec-content p {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 空状态 */
.empty-card {
  border-radius: 12px;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  opacity: 0.3;
}

/* 生成中 */
.generating-card {
  border-radius: 12px;
  min-height: 400px;
}

.generating-content {
  text-align: center;
  padding: 60px 20px;
}

.loading-icon {
  animation: rotate 2s linear infinite;
  margin-bottom: 24px;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.generating-content h2 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
}

.generating-content p {
  margin: 0 0 24px 0;
  color: var(--text-secondary);
}

/* 结果部分 */
.result-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-header {
  border-radius: 12px;
}

.result-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-left h3 {
  margin: 0 0 4px 0;
  color: var(--text-primary);
}

.info-left p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.info-right {
  display: flex;
  gap: 12px;
}

/* 题目卡片 */
.question-card,
.favorite-question-card,
.detail-question-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.question-card:hover,
.favorite-question-card:hover,
.detail-question-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.question-content h4 {
  margin: 0 0 16px 0;
  line-height: 1.6;
  color: var(--text-primary);
}

.question-content h3 {
  margin: 0 0 20px 0;
  line-height: 1.6;
  color: var(--text-primary);
  font-size: 18px;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.option-item {
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.option-item.correct-answer {
  background: #f0f9ff;
  border: 1px solid #67C23A;
  color: #67C23A;
}

.option-item.wrong-answer {
  background: #fef0f0;
  border: 1px solid #F56C6C;
  color: #F56C6C;
}

.question-footer {
  display: flex;
  gap: 12px;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.favorite-date {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 练习部分 */
.practice-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.practice-control {
  border-radius: 12px;
}

.control-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.control-left h3 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
}

.control-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.timer {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
}

.time-warning {
  color: #F56C6C;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.practice-progress {
  margin-top: 16px;
}

.current-question-card {
  border-radius: 12px;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 20px 0;
}

.answer-option {
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.answer-option:hover {
  background: #f0f2f5;
  border-color: #667eea;
}

.answer-input {
  margin: 20px 0;
}

.question-navigation {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

/* 答题卡 */
.answer-sheet {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 12px;
  padding: 20px;
}

.answer-sheet-item {
  position: relative;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.answer-sheet-item:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.answer-sheet-item.current {
  background: #667eea;
  color: white;
}

.answer-sheet-item.answered {
  background: #67C23A;
  color: white;
}

.answer-sheet-item.marked {
  background: #F56C6C;
  color: white;
}

.mark-icon {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 12px;
}

.answer-sheet-legend {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-box {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.legend-box.current {
  background: #667eea;
}

.legend-box.answered {
  background: #67C23A;
}

.legend-box.marked {
  background: #F56C6C;
}

/* 统计分析 */
.stats-content {
  padding: 20px 0;
}

.stats-chart-container h4 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
}

.chart-box {
  height: 250px;
}

.day-stat {
  text-align: center;
}

.day-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.day-value {
  font-size: 24px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 4px;
}

.day-accuracy {
  font-size: 14px;
  color: var(--text-secondary);
}

.month-summary {
  padding: 20px;
}

/* 收藏题目 */
.empty-favorite {
  padding: 40px 0;
}

.favorite-list {
  max-height: 600px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 历史记录 */
.history-filters {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.history-detail {
  max-height: 700px;
  overflow-y: auto;
}

.detail-summary {
  margin-bottom: 20px;
}

.detail-questions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-info {
  margin-top: 16px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.answer-info p {
  margin: 4px 0;
  font-size: 14px;
}

.answer-explanation {
  margin-top: 12px;
}

.answer-explanation p {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 完成统计 */
.result-summary {
  text-align: center;
  padding: 20px;
}

.result-icon {
  margin-bottom: 20px;
}

.result-summary h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: var(--text-primary);
}

.result-message {
  margin: 0 0 30px 0;
  font-size: 16px;
  color: var(--text-secondary);
}

.result-stats {
  margin-bottom: 30px;
}

.stat-box {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 12px;
}

.stat-box .stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-box .stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.result-chart {
  margin: 30px 0;
}

.result-detail {
  margin-top: 30px;
}

.new-badges {
  margin-top: 30px;
}

.badge-list {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.new-badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(135deg, #fff4e6 0%, #ffe7ba 100%);
  border-radius: 12px;
}

.new-badge-item .badge-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* AI批改卡片样式 */
.ai-grading-card {
  margin-top: 20px;
  border-left: 4px solid #67c23a;
}

.grading-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.grading-content {
  .grading-streaming {
    text-align: center;
    padding: 20px;

    .el-icon {
      font-size: 32px;
      color: #409eff;
      margin-bottom: 12px;
    }

    p {
      color: #606266;
      margin-bottom: 20px;
    }

    .streaming-text {
      text-align: left;
      background: #f5f7fa;
      padding: 16px;
      border-radius: 8px;
      min-height: 100px;
      line-height: 1.6;

      :deep(h2) {
        color: #303133;
        font-size: 18px;
        margin: 16px 0 8px 0;
        border-bottom: 2px solid #409eff;
        padding-bottom: 8px;
      }

      :deep(h3) {
        color: #606266;
        font-size: 16px;
        margin: 12px 0 6px 0;
      }

      :deep(ul), :deep(ol) {
        margin: 8px 0;
        padding-left: 24px;
      }

      :deep(li) {
        margin: 4px 0;
        line-height: 1.8;
      }

      :deep(p) {
        margin: 8px 0;
      }

      :deep(code) {
        background: #e1f3d8;
        padding: 2px 6px;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
      }
    }
  }

  .grading-result {
    background: #f5f7fa;
    padding: 20px;
    border-radius: 8px;
    line-height: 1.8;

    :deep(h2) {
      color: #303133;
      font-size: 20px;
      margin: 20px 0 12px 0;
      border-bottom: 2px solid #67c23a;
      padding-bottom: 8px;

      &:first-child {
        margin-top: 0;
      }
    }

    :deep(h3) {
      color: #606266;
      font-size: 16px;
      margin: 16px 0 8px 0;
    }

    :deep(ul), :deep(ol) {
      margin: 12px 0;
      padding-left: 28px;
    }

    :deep(li) {
      margin: 6px 0;
      line-height: 2;
    }

    :deep(p) {
      margin: 12px 0;
      color: #606266;
    }

    :deep(strong) {
      color: #303133;
      font-weight: 600;
    }

    :deep(code) {
      background: #e1f3d8;
      padding: 3px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      color: #67c23a;
      font-weight: 500;
    }
  }
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
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .config-card {
    position: static;
    margin-bottom: 16px;
    max-height: none;
  }

  .result-info,
  .control-content {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .info-right,
  .control-right {
    width: 100%;
  }

  .info-right .el-button,
  .control-right .el-button {
    flex: 1;
  }

  .answer-sheet {
    grid-template-columns: repeat(5, 1fr);
  }

  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .achievement-badges {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
