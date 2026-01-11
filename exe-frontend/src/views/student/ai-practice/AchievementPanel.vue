<template>
  <el-card class="achievement-panel" shadow="hover">
    <template #header>
      <div class="header-flex">
        <div class="header-left">
          <el-icon :size="20" color="#e6a23c"><Medal /></el-icon>
          <span class="header-title">成就系统</span>
        </div>
        <div class="header-right">
          <el-tag type="warning">
            Lv.{{ level }}
          </el-tag>
        </div>
      </div>
    </template>

    <div class="panel-body">
      <!-- 等级进度 -->
      <div class="level-section">
        <div class="level-info">
          <span class="level-label">等级 {{ level }}</span>
          <span class="xp-text">{{ totalXp }} / {{ nextLevelXp }} XP</span>
        </div>
        <el-progress
          :percentage="levelProgress"
          :stroke-width="16"
          :color="getLevelColor()"
        >
          <template #default="{ percentage }">
            <span class="progress-text">{{ percentage }}%</span>
          </template>
        </el-progress>
      </div>

      <!-- 成就标签页 -->
      <el-tabs v-model="activeTab" class="achievement-tabs">
        <!-- 已解锁成就 -->
        <el-tab-pane label="已解锁" name="unlocked">
          <div v-if="unlockedAchievements.length > 0" class="achievement-grid">
            <div
              v-for="achievement in unlockedAchievements"
              :key="achievement.id"
              class="achievement-item unlocked"
            >
              <div class="achievement-icon" :class="`rarity-${achievement.rarity}`">
                <span class="icon-emoji">{{ achievement.icon }}</span>
                <div class="rarity-glow"></div>
              </div>
              <div class="achievement-info">
                <div class="achievement-name">{{ achievement.name }}</div>
                <div class="achievement-desc">{{ achievement.description }}</div>
                <div class="achievement-reward">
                  <el-tag size="small" type="success">+{{ achievement.reward.xp }} XP</el-tag>
                  <el-tag v-if="achievement.reward.title" size="small" type="warning">
                    称号：{{ achievement.reward.title }}
                  </el-tag>
                </div>
                <div class="unlock-time">
                  <el-icon :size="12"><Clock /></el-icon>
                  <span>{{ formatTime(achievement.unlockedAt!) }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="还没有解锁任何成就，继续加油！" />
        </el-tab-pane>

        <!-- 未解锁成就 -->
        <el-tab-pane label="未解锁" name="locked">
          <div class="achievement-grid">
            <div
              v-for="achievement in lockedAchievements"
              :key="achievement.id"
              class="achievement-item locked"
            >
              <div class="achievement-icon locked-icon">
                <el-icon :size="32"><Lock /></el-icon>
              </div>
              <div class="achievement-info">
                <div class="achievement-name">{{ achievement.name }}</div>
                <div class="achievement-desc">{{ achievement.description }}</div>
                <div class="achievement-reward">
                  <el-tag size="small" type="info">+{{ achievement.reward.xp }} XP</el-tag>
                  <el-tag v-if="achievement.reward.title" size="small" type="info">
                    称号：{{ achievement.reward.title }}
                  </el-tag>
                </div>
                <!-- 进度条 -->
                <div v-if="getAchievementProgress(achievement.id) > 0" class="achievement-progress">
                  <el-progress
                    :percentage="getAchievementProgress(achievement.id)"
                    :stroke-width="4"
                    :show-text="false"
                  />
                  <span class="progress-label">
                    {{ getAchievementProgress(achievement.id) }}%
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 稀有度分类 -->
        <el-tab-pane label="稀有度" name="rarity">
          <div class="rarity-stats">
            <div
              v-for="rarity in rarities"
              :key="rarity.value"
              class="rarity-item"
            >
              <div class="rarity-header" :class="`rarity-${rarity.value}`">
                <span class="rarity-name">{{ rarity.label }}</span>
                <el-tag :type="rarity.tagType" size="small">
                  {{ getRarityCount(rarity.value) }} / {{ getRarityTotal(rarity.value) }}
                </el-tag>
              </div>
              <el-progress
                :percentage="getRarityPercentage(rarity.value)"
                :color="rarity.color"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Medal, Clock, Lock } from '@element-plus/icons-vue'
import { useAchievements } from '../hooks/useAchievements'

const {
  achievements,
  totalXp,
  level,
  levelProgress,
  allAchievements,
  getAchievementProgress,
  getRarityColor,
  getRarityLabel
} = useAchievements()

const activeTab = ref('unlocked')

/**
 * 下一级所需XP
 */
const nextLevelXp = computed(() => level.value * 1000)

/**
 * 已解锁成就
 */
const unlockedAchievements = computed(() => {
  return achievements.value.filter(a => a.unlockedAt)
})

/**
 * 未解锁成就
 */
const lockedAchievements = computed(() => {
  return allAchievements.filter(
    a => !achievements.value.find(ua => ua.id === a.id)
  )
})

/**
 * 稀有度列表
 */
const rarities = [
  { value: 'common', label: '普通', tagType: '', color: '#909399' },
  { value: 'rare', label: '稀有', tagType: 'primary', color: '#409eff' },
  { value: 'epic', label: '史诗', tagType: 'warning', color: '#9c27b0' },
  { value: 'legendary', label: '传说', tagType: 'danger', color: '#ff6b00' }
]

/**
 * 获取等级颜色
 */
const getLevelColor = () => {
  if (levelProgress.value >= 80) return '#67c23a'
  if (levelProgress.value >= 50) return '#409eff'
  return '#e6a23c'
}

/**
 * 获取稀有度数量
 */
const getRarityCount = (rarity: string): number => {
  return achievements.value.filter(a => a.rarity === rarity).length
}

/**
 * 获取稀有度总数
 */
const getRarityTotal = (rarity: string): number => {
  return allAchievements.filter(a => a.rarity === rarity).length
}

/**
 * 获取稀有度百分比
 */
const getRarityPercentage = (rarity: string): number => {
  const total = getRarityTotal(rarity)
  if (total === 0) return 0
  const count = getRarityCount(rarity)
  return Math.round((count / total) * 100)
}

/**
 * 格式化时间
 */
const formatTime = (date: Date): string => {
  const now = new Date()
  const diff = now.getTime() - new Date(date).getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天解锁'
  if (days === 1) return '昨天解锁'
  if (days < 7) return `${days}天前解锁`
  if (days < 30) return `${Math.floor(days / 7)}周前解锁`

  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped lang="scss">
.achievement-panel {
  .header-flex {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .header-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .panel-body {
    .level-section {
      margin-bottom: 24px;
      padding: 16px;
      background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
      border-radius: 8px;

      .level-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;

        .level-label {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }

        .xp-text {
          font-size: 14px;
          color: #606266;
        }
      }

      .progress-text {
        font-size: 12px;
        font-weight: 600;
      }
    }

    .achievement-tabs {
      :deep(.el-tabs__content) {
        padding: 0;
      }

      .achievement-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 16px;
        margin-top: 16px;

        .achievement-item {
          display: flex;
          gap: 12px;
          padding: 16px;
          border-radius: 8px;
          transition: all 0.3s;

          &.unlocked {
            background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);
            border: 2px solid #b3d8ff;

            &:hover {
              transform: translateY(-4px);
              box-shadow: 0 8px 16px rgba(64, 158, 255, 0.2);
            }
          }

          &.locked {
            background: #f5f7fa;
            border: 2px dashed #dcdfe6;
            opacity: 0.7;
          }

          .achievement-icon {
            position: relative;
            flex-shrink: 0;
            width: 64px;
            height: 64px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 36px;

            &.rarity-common {
              background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
            }

            &.rarity-rare {
              background: linear-gradient(135deg, #e6f0ff 0%, #b3d8ff 100%);
            }

            &.rarity-epic {
              background: linear-gradient(135deg, #f3e8ff 0%, #d3adf7 100%);
            }

            &.rarity-legendary {
              background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
            }

            &.locked-icon {
              background: #f5f7fa;
              color: #909399;
            }

            .rarity-glow {
              position: absolute;
              inset: -2px;
              border-radius: 12px;
              opacity: 0.3;
              pointer-events: none;
            }
          }

          .achievement-info {
            flex: 1;

            .achievement-name {
              font-size: 15px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 6px;
            }

            .achievement-desc {
              font-size: 13px;
              color: #606266;
              margin-bottom: 8px;
              line-height: 1.4;
            }

            .achievement-reward {
              display: flex;
              gap: 6px;
              flex-wrap: wrap;
              margin-bottom: 8px;
            }

            .unlock-time {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 12px;
              color: #909399;
            }

            .achievement-progress {
              margin-top: 8px;
              display: flex;
              align-items: center;
              gap: 8px;

              .progress-label {
                font-size: 12px;
                color: #606266;
                flex-shrink: 0;
              }
            }
          }
        }
      }

      .rarity-stats {
        margin-top: 16px;

        .rarity-item {
          padding: 16px;
          margin-bottom: 16px;
          background: #f5f7fa;
          border-radius: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .rarity-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .rarity-name {
              font-size: 15px;
              font-weight: 600;
            }

            &.rarity-common .rarity-name {
              color: #909399;
            }

            &.rarity-rare .rarity-name {
              color: #409eff;
            }

            &.rarity-epic .rarity-name {
              color: #9c27b0;
            }

            &.rarity-legendary .rarity-name {
              color: #ff6b00;
            }
          }
        }
      }
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .achievement-panel {
    .panel-body {
      .achievement-tabs {
        .achievement-grid {
          grid-template-columns: 1fr;
        }
      }
    }
  }
}
</style>
