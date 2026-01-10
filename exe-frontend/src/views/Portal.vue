<template>
  <!-- Phase 5: Accessibility - Skip to Content -->
  <a href="#main-content" class="skip-to-content">跳转到主要内容</a>

  <div class="portal-wrapper" @keydown="handleKeyDown" role="main" aria-label="教学管理系统门户">
    <!-- Phase 4: Particle Background Canvas -->
    <canvas ref="particleCanvasRef" class="particle-canvas" aria-hidden="true"></canvas>

    <!-- Phase 4: Loading Skeleton -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-content">
        <el-skeleton :rows="1" animated class="skeleton-header" />
        <div class="skeleton-cards">
          <el-skeleton :rows="5" animated class="skeleton-card" />
          <el-skeleton :rows="5" animated class="skeleton-card" />
        </div>
        <div class="skeleton-small-cards">
          <el-skeleton :rows="3" animated class="skeleton-small-card" />
          <el-skeleton :rows="3" animated class="skeleton-small-card" />
          <el-skeleton :rows="3" animated class="skeleton-small-card" />
        </div>
      </div>
    </div>

    <div v-else class="portal-container" id="main-content">
      <!-- Header with User Status -->
      <div class="portal-header" role="banner">
        <div class="header-content">
          <div class="title-section">
            <h1 class="portal-title">综合系统平台</h1>
            <p class="portal-subtitle">选择您要访问的系统</p>
          </div>

          <!-- Right Section: Search + Notifications + User -->
          <div class="header-right">
            <!-- Search Bar -->
            <div class="search-bar" role="search">
              <el-input
                ref="searchInputRef"
                v-model="searchQuery"
                placeholder="搜索系统或功能..."
                :prefix-icon="Search"
                clearable
                aria-label="搜索系统或功能"
                @input="handleSearch"
                @focus="showSearchResults = true"
                @blur="handleSearchBlur"
              >
                <template #suffix>
                  <el-tag size="small" class="search-hint">/</el-tag>
                </template>
              </el-input>

              <!-- Search Results Dropdown -->
              <transition name="fade">
                <div v-if="showSearchResults && searchQuery" class="search-results">
                  <div v-if="filteredSystems.length > 0" class="results-list">
                    <div
                      v-for="system in filteredSystems"
                      :key="system.id"
                      class="result-item"
                      @mousedown="handleResultClick(system)"
                    >
                      <el-icon :size="20" :color="system.color">
                        <component :is="system.icon" />
                      </el-icon>
                      <div class="result-content">
                        <div class="result-name">{{ system.name }}</div>
                        <div class="result-desc">{{ system.description }}</div>
                      </div>
                      <el-tag v-if="system.category" size="small">{{ system.category }}</el-tag>
                    </div>
                  </div>
                  <div v-else class="no-results">
                    <el-icon :size="40" color="#909399"><Search /></el-icon>
                    <p>未找到相关系统</p>
                  </div>
                </div>
              </transition>
            </div>

            <!-- Notification Center -->
            <el-popover
              :visible="showNotifications"
              placement="bottom-end"
              :width="360"
              trigger="click"
              @update:visible="showNotifications = $event"
            >
              <template #reference>
                <el-badge :value="unreadNotifications" :max="99" :hidden="unreadNotifications === 0">
                  <el-button circle :icon="Bell" @click="showNotifications = !showNotifications" />
                </el-badge>
              </template>

              <div class="notification-panel">
                <div class="notification-header">
                  <span class="notification-title">通知中心</span>
                  <el-button text size="small" @click="markAllAsRead">全部已读</el-button>
                </div>
                <div class="notification-list">
                  <div
                    v-for="notification in notifications"
                    :key="notification.id"
                    class="notification-item"
                    :class="{ unread: !notification.read }"
                    @click="handleNotificationClick(notification)"
                  >
                    <el-icon :size="20" :color="getNotificationColor(notification.type)">
                      <component :is="getNotificationIcon(notification.type)" />
                    </el-icon>
                    <div class="notification-content">
                      <div class="notification-text">{{ notification.title }}</div>
                      <div class="notification-time">{{ notification.time }}</div>
                    </div>
                  </div>
                </div>
                <div v-if="notifications.length === 0" class="no-notifications">
                  <el-icon :size="40" color="#909399"><Bell /></el-icon>
                  <p>暂无通知</p>
                </div>
              </div>
            </el-popover>

            <!-- Help Button -->
            <el-tooltip content="快捷键帮助 (?)" placement="bottom">
              <el-button circle :icon="QuestionFilled" @click="showHelpDialog = true" />
            </el-tooltip>

            <!-- Phase 5: Theme Toggle Button -->
            <el-tooltip :content="`当前: ${userPreferences.theme === 'light' ? '浅色' : userPreferences.theme === 'dark' ? '深色' : '自动'}模式`" placement="bottom">
              <el-button circle @click="toggleTheme">
                <el-icon v-if="userPreferences.theme === 'light'">
                  <svg viewBox="0 0 1024 1024" width="1em" height="1em">
                    <path fill="currentColor" d="M512 704a192 192 0 1 0 0-384 192 192 0 0 0 0 384zm0 64a256 256 0 1 1 0-512 256 256 0 0 1 0 512zm0-704a32 32 0 0 1 32 32v64a32 32 0 0 1-64 0V96a32 32 0 0 1 32-32zm0 768a32 32 0 0 1 32 32v64a32 32 0 1 1-64 0v-64a32 32 0 0 1 32-32zM195.2 195.2a32 32 0 0 1 45.248 0l45.248 45.248a32 32 0 1 1-45.248 45.248L195.2 240.448a32 32 0 0 1 0-45.248zm543.104 543.104a32 32 0 0 1 45.248 0l45.248 45.248a32 32 0 0 1-45.248 45.248l-45.248-45.248a32 32 0 0 1 0-45.248zM64 512a32 32 0 0 1 32-32h64a32 32 0 0 1 0 64H96a32 32 0 0 1-32-32zm768 0a32 32 0 0 1 32-32h64a32 32 0 1 1 0 64h-64a32 32 0 0 1-32-32zM195.2 828.8a32 32 0 0 1 0-45.248l45.248-45.248a32 32 0 0 1 45.248 45.248L240.448 828.8a32 32 0 0 1-45.248 0zm543.104-543.104a32 32 0 0 1 0-45.248l45.248-45.248a32 32 0 0 1 45.248 45.248l-45.248 45.248a32 32 0 0 1-45.248 0z"/>
                  </svg>
                </el-icon>
                <el-icon v-else-if="userPreferences.theme === 'dark'">
                  <svg viewBox="0 0 1024 1024" width="1em" height="1em">
                    <path fill="currentColor" d="M240.448 240.448a384 384 0 1 0 559.424 525.696 448 448 0 0 1-542.016-542.08 390.592 390.592 0 0 0-17.408 16.384zm181.056 362.048a384 384 0 0 0 525.632 16.384A448 448 0 1 1 405.056 76.8a384 384 0 0 0 16.448 525.696z"/>
                  </svg>
                </el-icon>
                <el-icon v-else>
                  <svg viewBox="0 0 1024 1024" width="1em" height="1em">
                    <path fill="currentColor" d="M512 128a32 32 0 0 1 32 32v96a32 32 0 0 1-64 0v-96a32 32 0 0 1 32-32zm0 640a32 32 0 0 1 32 32v96a32 32 0 1 1-64 0v-96a32 32 0 0 1 32-32zm448-288a32 32 0 0 1-32 32h-96a32 32 0 0 1 0-64h96a32 32 0 0 1 32 32zM288 480a32 32 0 0 1-32 32h-96a32 32 0 0 1 0-64h96a32 32 0 0 1 32 32zm23.424-201.024a32 32 0 0 1 0 45.248l-67.84 67.84a32 32 0 0 1-45.248-45.248l67.84-67.84a32 32 0 0 1 45.248 0zm535.424 535.424a32 32 0 0 1 0 45.248l-67.84 67.84a32 32 0 1 1-45.248-45.248l67.84-67.84a32 32 0 0 1 45.248 0z"/>
                  </svg>
                </el-icon>
              </el-button>
            </el-tooltip>

            <!-- Phase 5: Settings Button -->
            <el-tooltip content="设置" placement="bottom">
              <el-button circle @click="showSettingsPanel = true">
                <el-icon>
                  <svg viewBox="0 0 1024 1024" width="1em" height="1em">
                    <path fill="currentColor" d="M600.704 64a32 32 0 0 1 30.464 22.208l35.2 109.376c14.784 7.232 28.928 15.36 42.432 24.512l112.384-24.192a32 32 0 0 1 34.432 15.36L944.32 364.8a32 32 0 0 1-4.032 37.504l-77.12 85.12a357.12 357.12 0 0 1 0 49.024l77.12 85.248a32 32 0 0 1 4.032 37.504l-88.704 153.6a32 32 0 0 1-34.432 15.296L708.8 803.904c-13.44 9.088-27.648 17.28-42.368 24.512l-35.264 109.376A32 32 0 0 1 600.704 960H423.296a32 32 0 0 1-30.464-22.208L357.632 828.48a351.616 351.616 0 0 1-42.56-24.64l-112.896 24.256a32 32 0 0 1-34.432-15.36L79.04 659.328a32 32 0 0 1 4.032-37.504l77.12-85.248a357.12 357.12 0 0 1 0-48.896l-77.12-85.248A32 32 0 0 1 79.04 364.8l88.704-153.6a32 32 0 0 1 34.432-15.296L315.072 220.096c13.376-9.152 27.456-17.28 42.24-24.64l35.2-109.312A32 32 0 0 1 423.232 64H600.64zm-23.424 64H446.72l-36.352 113.088-24.512 11.968a294.113 294.113 0 0 0-34.816 20.096l-22.656 15.36-116.224-25.088-65.28 113.152 79.68 88.192-1.92 27.136a293.12 293.12 0 0 0 0 40.192l1.92 27.136-79.808 88.192 65.344 113.152 116.224-25.024 22.656 15.296a294.113 294.113 0 0 0 34.816 20.096l24.512 11.968L446.72 896h130.688l36.48-113.152 24.448-11.904a288.282 288.282 0 0 0 34.752-20.096l22.592-15.296 116.288 25.024 65.28-113.152-79.744-88.192 1.92-27.136a293.12 293.12 0 0 0 0-40.256l-1.92-27.136 79.808-88.128-65.344-113.152-116.288 24.96-22.592-15.232a287.616 287.616 0 0 0-34.752-20.096l-24.448-11.904L577.344 128zM512 320a192 192 0 1 1 0 384 192 192 0 0 1 0-384zm0 64a128 128 0 1 0 0 256 128 128 0 0 0 0-256z"/>
                  </svg>
                </el-icon>
              </el-button>
            </el-tooltip>

            <!-- User Info Section -->
            <div class="user-section">
              <div v-if="currentUser" class="user-info">
                <el-avatar :size="40" :src="currentUser.avatar">
                  {{ currentUser.name.charAt(0) }}
                </el-avatar>
                <div class="user-details">
                  <span class="user-name">{{ currentUser.name }}</span>
                  <span class="user-role">{{ currentUser.role }}</span>
                </div>
                <el-dropdown @command="handleUserCommand" class="user-dropdown">
                  <el-button circle :icon="ArrowDown" size="small" text />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="switchRole">
                        <el-icon><Switch /></el-icon>
                        切换身份
                      </el-dropdown-item>
                      <el-dropdown-item command="logout" divided>
                        <el-icon><SwitchButton /></el-icon>
                        退出登录
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <div v-else class="login-section">
                <el-button type="primary" size="default" @click="showLoginDialog = true">
                  <el-icon><User /></el-icon>
                  登录
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Access (if user logged in) -->
        <transition name="fade">
          <div v-if="currentUser && recentAccess.length > 0" class="recent-access">
            <div class="recent-header">
              <el-icon><Clock /></el-icon>
              <span>最近访问</span>
            </div>
            <div class="recent-items">
              <div
                v-for="item in recentAccess"
                :key="item.id"
                class="recent-item"
                @click="navigateTo(item.path)"
              >
                <el-icon :size="16" :color="item.color">
                  <component :is="item.icon" />
                </el-icon>
                <span class="recent-name">{{ item.name }}</span>
                <span class="recent-time">{{ item.time }}</span>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 核心系统区 -->
      <section class="core-systems">
        <h2 class="section-title">核心系统</h2>
        <div class="core-grid">
          <!-- 试题管理系统 -->
          <div
            class="system-card core-card"
            :class="{
              recommended: isRecommended('admin'),
              'card-animate': isAnimationReady,
              [`popularity-${getSystemPopularity(coreSystems[0].id)}`]: getSystemPopularity(coreSystems[0].id)
            }"
            :style="{ background: coreSystems[0].gradient, animationDelay: '0.1s' }"
            @click="navigateTo(coreSystems[0].path)"
          >
            <div class="card-decoration"></div>
            <div class="card-shortcut">1</div>

            <!-- System Status Badge -->
            <el-badge
              :value="getSystemStatus(coreSystems[0].id)"
              :type="getSystemStatusType(coreSystems[0].id)"
              class="status-badge"
            />

            <!-- Recommended Badge -->
            <div v-if="isRecommended('admin')" class="recommended-badge">
              <el-icon><Star /></el-icon>
              推荐
            </div>

            <!-- Phase 4: Visit Statistics -->
            <div v-if="getVisitCount(coreSystems[0].id) > 0" class="visit-count">
              <el-icon><Clock /></el-icon>
              <span>{{ getVisitCount(coreSystems[0].id) }}次访问</span>
            </div>

            <!-- Phase 4: Popularity Indicator -->
            <div v-if="getSystemPopularity(coreSystems[0].id)" class="popularity-indicator">
              <span class="fire-icon">🔥</span>
            </div>

            <div class="card-content">
              <div class="card-icon">
                <el-icon :size="64">
                  <component :is="coreSystems[0].icon" />
                </el-icon>
              </div>
              <h3 class="card-title">{{ coreSystems[0].name }}</h3>
              <p class="card-subtitle">{{ coreSystems[0].subtitle }}</p>
              <p class="card-description">{{ coreSystems[0].description }}</p>
              <div class="card-features">
                <span v-for="feature in coreSystems[0].features" :key="feature" class="feature-tag">
                  {{ feature }}
                </span>
              </div>

              <!-- Quick Login Buttons -->
              <div class="card-actions" @click.stop>
                <el-button
                  v-if="!authStore.isAuthenticated"
                  type="primary"
                  size="default"
                  class="action-btn"
                  @click="quickLogin('teacher')"
                >
                  <el-icon><UserFilled /></el-icon>
                  教师登录
                </el-button>
                <el-button
                  v-else
                  type="primary"
                  size="default"
                  class="action-btn"
                  @click="navigateTo(coreSystems[0].path)"
                >
                  进入系统
                  <el-icon class="ml-1"><Right /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <!-- 在线学习系统 -->
          <div
            class="system-card core-card"
            :class="{
              recommended: isRecommended('student'),
              'card-animate': isAnimationReady,
              [`popularity-${getSystemPopularity(coreSystems[1].id)}`]: getSystemPopularity(coreSystems[1].id)
            }"
            :style="{ background: coreSystems[1].gradient, animationDelay: '0.2s' }"
            @click="navigateTo(coreSystems[1].path)"
          >
            <div class="card-decoration"></div>
            <div class="card-shortcut">2</div>

            <!-- System Status Badge -->
            <el-badge
              :value="getSystemStatus(coreSystems[1].id)"
              :type="getSystemStatusType(coreSystems[1].id)"
              class="status-badge"
            />

            <!-- Recommended Badge -->
            <div v-if="isRecommended('student')" class="recommended-badge">
              <el-icon><Star /></el-icon>
              推荐
            </div>

            <!-- Phase 4: Visit Statistics -->
            <div v-if="getVisitCount(coreSystems[1].id) > 0" class="visit-count">
              <el-icon><Clock /></el-icon>
              <span>{{ getVisitCount(coreSystems[1].id) }}次访问</span>
            </div>

            <!-- Phase 4: Popularity Indicator -->
            <div v-if="getSystemPopularity(coreSystems[1].id)" class="popularity-indicator">
              <span class="fire-icon">🔥</span>
            </div>

            <div class="card-content">
              <div class="card-icon">
                <el-icon :size="64">
                  <component :is="coreSystems[1].icon" />
                </el-icon>
              </div>
              <h3 class="card-title">{{ coreSystems[1].name }}</h3>
              <p class="card-subtitle">{{ coreSystems[1].subtitle }}</p>
              <p class="card-description">{{ coreSystems[1].description }}</p>
              <div class="card-features">
                <span v-for="feature in coreSystems[1].features" :key="feature" class="feature-tag">
                  {{ feature }}
                </span>
              </div>

              <!-- Quick Login Buttons -->
              <div class="card-actions" @click.stop>
                <el-button
                  v-if="!studentAuthStore.isAuthenticated"
                  type="success"
                  size="default"
                  class="action-btn"
                  @click="quickLogin('student')"
                >
                  <el-icon><UserFilled /></el-icon>
                  学生登录
                </el-button>
                <el-button
                  v-else
                  type="success"
                  size="default"
                  class="action-btn"
                  @click="navigateTo(coreSystems[1].path)"
                >
                  进入系统
                  <el-icon class="ml-1"><Right /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Phase 4: 智能推荐区 -->
      <section v-if="getRecommendedSystems.length > 0" class="recommended-systems">
        <h2 class="section-title">
          <el-icon><Star /></el-icon>
          智能推荐 <span class="subtitle">基于您的使用习惯</span>
        </h2>
        <div class="recommended-grid">
          <div
            v-for="(system, index) in getRecommendedSystems"
            :key="system.id"
            class="system-card recommended-card card-animate"
            :style="{ background: system.gradient, animationDelay: `${0.3 + index * 0.1}s` }"
            @click="navigateTo(system.path || system.url || '')"
          >
            <div class="recommend-badge">
              <el-icon><Star /></el-icon>
              推荐
            </div>

            <div class="visit-count-large">
              <el-icon><Clock /></el-icon>
              <span>{{ system.visitCount }}次</span>
            </div>

            <div class="card-icon-medium">
              <el-icon :size="48">
                <component :is="system.icon" />
              </el-icon>
            </div>
            <h3 class="card-title-medium">{{ system.name }}</h3>
            <p class="card-description-medium">{{ system.description }}</p>
          </div>
        </div>
      </section>

      <!-- 扩展系统区 -->
      <section class="extended-systems">
        <h2 class="section-title">扩展系统</h2>
        <div class="extended-grid">
          <div
            v-for="system in extendedSystems"
            :key="system.id"
            class="system-card extended-card"
            :style="{ background: system.gradient }"
            @click="handleSystemClick(system)"
          >
            <!-- System Status Badge -->
            <el-badge
              :value="getSystemStatus(system.id)"
              :type="getSystemStatusType(system.id)"
              class="status-badge-small"
            />

            <div class="card-icon-small">
              <el-icon :size="40">
                <component :is="system.icon" />
              </el-icon>
            </div>
            <h3 class="card-title-small">{{ system.name }}</h3>
            <p class="card-description-small">{{ system.description }}</p>
            <el-tag v-if="system.tag" :type="system.tagType" size="small" class="system-tag">
              {{ system.tag }}
            </el-tag>
          </div>
        </div>
      </section>

      <!-- 外部资源区 -->
      <section class="external-resources">
        <h2 class="section-title">学习资源</h2>
        <div class="resource-grid">
          <a
            v-for="resource in externalResources"
            :key="resource.id"
            :href="resource.url"
            target="_blank"
            class="resource-link"
            @click.prevent="openExternal(resource.url)"
          >
            <el-icon :size="20" :color="resource.color">
              <component :is="resource.icon" />
            </el-icon>
            <span class="resource-name">{{ resource.name }}</span>
            <el-icon :size="14" class="external-icon">
              <TopRight />
            </el-icon>
          </a>
        </div>
      </section>

      <!-- Footer -->
      <div class="portal-footer">
        <p>© 2026 综合系统平台 | 为教育赋能</p>
        <span class="footer-hint">按 <kbd>?</kbd> 查看快捷键</span>
      </div>
    </div>

    <!-- Login Dialog -->
    <el-dialog
      v-model="showLoginDialog"
      :title="loginDialogTitle"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoginDialog = false">取消</el-button>
        <el-button type="primary" @click="handleLogin" :loading="loginLoading">
          登录
        </el-button>
      </template>
    </el-dialog>

    <!-- Help Dialog - Keyboard Shortcuts -->
    <el-dialog
      v-model="showHelpDialog"
      title="快捷键帮助"
      width="500px"
    >
      <div class="help-content">
        <div class="help-section">
          <h3>导航快捷键</h3>
          <div class="shortcut-list">
            <div class="shortcut-item">
              <kbd>1</kbd>
              <span>跳转到试题管理系统</span>
            </div>
            <div class="shortcut-item">
              <kbd>2</kbd>
              <span>跳转到在线学习系统</span>
            </div>
          </div>
        </div>

        <div class="help-section">
          <h3>搜索快捷键</h3>
          <div class="shortcut-list">
            <div class="shortcut-item">
              <kbd>/</kbd>
              <span>聚焦搜索框</span>
            </div>
            <div class="shortcut-item">
              <kbd>Esc</kbd>
              <span>清空搜索 / 关闭对话框</span>
            </div>
          </div>
        </div>

        <div class="help-section">
          <h3>其他快捷键</h3>
          <div class="shortcut-list">
            <div class="shortcut-item">
              <kbd>?</kbd>
              <span>显示此帮助</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelpDialog = false">知道了</el-button>
      </template>
    </el-dialog>

    <!-- Announcement Dialog -->
    <el-dialog
      v-model="showAnnouncement"
      title="系统公告"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="announcement-content">
        <div class="announcement-icon">
          <el-icon :size="60" color="#409eff"><InfoFilled /></el-icon>
        </div>
        <h2>{{ currentAnnouncement.title }}</h2>
        <p class="announcement-text">{{ currentAnnouncement.content }}</p>
        <div class="announcement-meta">
          <span>发布时间: {{ currentAnnouncement.date }}</span>
        </div>
      </div>
      <template #footer>
        <el-checkbox v-model="dontShowAnnouncement">不再显示</el-checkbox>
        <el-button type="primary" @click="closeAnnouncement">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- Phase 5: Settings Panel Dialog -->
    <el-dialog
      v-model="showSettingsPanel"
      title="个性化设置"
      width="600px"
    >
      <div class="settings-content">
        <div class="settings-section">
          <h3>主题设置</h3>
          <el-radio-group v-model="userPreferences.theme" @change="updatePreference('theme', userPreferences.theme)">
            <el-radio-button label="light">浅色模式</el-radio-button>
            <el-radio-button label="dark">深色模式</el-radio-button>
            <el-radio-button label="auto">自动切换</el-radio-button>
          </el-radio-group>
          <p class="settings-tip">自动模式：18:00-6:00 自动切换为深色</p>
        </div>

        <div class="settings-section">
          <h3>界面选项</h3>
          <div class="settings-item">
            <span>粒子背景</span>
            <el-switch
              v-model="userPreferences.particlesEnabled"
              @change="updatePreference('particlesEnabled', userPreferences.particlesEnabled)"
            />
          </div>
          <div class="settings-item">
            <span>动画效果</span>
            <el-switch
              v-model="userPreferences.animationsEnabled"
              @change="updatePreference('animationsEnabled', userPreferences.animationsEnabled)"
            />
          </div>
          <div class="settings-item">
            <span>紧凑模式</span>
            <el-switch
              v-model="userPreferences.compactMode"
              @change="updatePreference('compactMode', userPreferences.compactMode)"
            />
          </div>
        </div>

        <div class="settings-section">
          <h3>字体大小</h3>
          <el-radio-group v-model="userPreferences.fontSize" @change="updatePreference('fontSize', userPreferences.fontSize)">
            <el-radio-button label="small">小</el-radio-button>
            <el-radio-button label="medium">中</el-radio-button>
            <el-radio-button label="large">大</el-radio-button>
          </el-radio-group>
        </div>

        <div class="settings-section">
          <h3>高级功能</h3>
          <div class="settings-buttons">
            <el-button @click="showPerformanceMonitor = !showPerformanceMonitor">
              <el-icon><DataBoard /></el-icon>
              性能监控
            </el-button>
            <el-button @click="showDataVisualization = true">
              <el-icon><DataBoard /></el-icon>
              访问数据分析
            </el-button>
            <el-button @click="showHeatmap = true">
              <el-icon><DataBoard /></el-icon>
              使用热力图
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showSettingsPanel = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Phase 5: Performance Monitor Panel -->
    <div v-if="showPerformanceMonitor" class="performance-monitor">
      <div class="performance-header">
        <span>性能监控</span>
        <el-button text @click="showPerformanceMonitor = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="performance-stats">
        <div class="stat-item">
          <span class="stat-label">FPS:</span>
          <span class="stat-value" :class="{ 'stat-good': fps >= 50, 'stat-warning': fps >= 30 && fps < 50, 'stat-bad': fps < 30 }">
            {{ fps }}
          </span>
        </div>
        <div class="stat-item">
          <span class="stat-label">加载时间:</span>
          <span class="stat-value">{{ loadTime.toFixed(0) }}ms</span>
        </div>
      </div>
    </div>

    <!-- Phase 5: Data Visualization Dialog -->
    <el-dialog
      v-model="showDataVisualization"
      title="访问数据分析"
      width="800px"
    >
      <div class="data-visualization-content">
        <div class="chart-section">
          <h3>7天访问趋势</h3>
          <div class="trend-chart">
            <div class="trend-bars">
              <div
                v-for="(item, index) in getVisitTrendData"
                :key="index"
                class="trend-bar-item"
              >
                <div class="trend-bar-wrapper">
                  <div
                    class="trend-bar"
                    :style="{ height: `${(item.count / Math.max(...getVisitTrendData.map(d => d.count))) * 100}%` }"
                  >
                    <span class="trend-count">{{ item.count }}</span>
                  </div>
                </div>
                <span class="trend-label">{{ item.date }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="chart-section">
          <h3>系统使用分布</h3>
          <div class="usage-distribution">
            <div
              v-for="(item, index) in getSystemUsageDistribution"
              :key="index"
              class="usage-item"
            >
              <div class="usage-header">
                <span class="usage-name">{{ item.name }}</span>
                <span class="usage-percent">{{ item.percentage }}%</span>
              </div>
              <div class="usage-bar">
                <div
                  class="usage-bar-fill"
                  :style="{ width: `${item.percentage}%`, backgroundColor: item.color }"
                ></div>
              </div>
              <span class="usage-count">访问次数: {{ item.count }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDataVisualization = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Phase 5: System Usage Heatmap Dialog -->
    <el-dialog
      v-model="showHeatmap"
      title="系统使用热力图"
      width="900px"
    >
      <div class="heatmap-content">
        <div class="heatmap-header">
          <h3>最近30天活动概览</h3>
          <p class="heatmap-description">根据您的系统访问频率生成的热力图，颜色越深表示访问越频繁</p>
        </div>

        <div class="heatmap-container">
          <!-- Day Labels -->
          <div class="heatmap-days">
            <span class="day-label">周日</span>
            <span class="day-label">周一</span>
            <span class="day-label">周二</span>
            <span class="day-label">周三</span>
            <span class="day-label">周四</span>
            <span class="day-label">周五</span>
            <span class="day-label">周六</span>
          </div>

          <!-- Heatmap Grid -->
          <div class="heatmap-grid">
            <div
              v-for="cell in getHeatmapData"
              :key="`${cell.week}-${cell.day}`"
              class="heatmap-cell"
              :class="`level-${cell.level}`"
              :style="{
                gridColumn: cell.week + 1,
                gridRow: cell.day + 1
              }"
              :title="`${cell.date}: ${cell.count} 次访问`"
            >
              <span class="cell-tooltip">{{ cell.count }}</span>
            </div>
          </div>

          <!-- Legend -->
          <div class="heatmap-legend">
            <span class="legend-label">少</span>
            <div class="legend-item level-0"></div>
            <div class="legend-item level-1"></div>
            <div class="legend-item level-2"></div>
            <div class="legend-item level-3"></div>
            <div class="legend-item level-4"></div>
            <span class="legend-label">多</span>
          </div>
        </div>

        <div class="heatmap-stats">
          <div class="stat-card">
            <div class="stat-icon">📊</div>
            <div class="stat-info">
              <span class="stat-label">总访问</span>
              <span class="stat-value">{{ getHeatmapData.reduce((sum, cell) => sum + cell.count, 0) }}次</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">🔥</div>
            <div class="stat-info">
              <span class="stat-label">最高单日</span>
              <span class="stat-value">{{ Math.max(...getHeatmapData.map(cell => cell.count)) }}次</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📈</div>
            <div class="stat-info">
              <span class="stat-label">日均访问</span>
              <span class="stat-value">{{ Math.round(getHeatmapData.reduce((sum, cell) => sum + cell.count, 0) / 30) }}次</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showHeatmap = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, markRaw } from 'vue';
import { useRouter } from 'vue-router';
import {
  Management,
  School,
  Box,
  DataBoard,
  Share,
  Reading,
  Document,
  VideoPlay,
  Right,
  TopRight,
  ArrowDown,
  Switch,
  SwitchButton,
  User,
  UserFilled,
  Clock,
  Star,
  Search,
  Bell,
  QuestionFilled,
  InfoFilled,
  Warning,
  SuccessFilled,
  Close
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { useStudentAuthStore } from '@/stores/studentAuth';

// Phase 8: Portal API 集成
import * as portalApi from '@/api/portal';
import type {
  SystemVisitStats,
  VisitTrendDataPoint,
  HeatmapCell,
  RecentAccessRecord
} from '@/api/portal';

// Phase 7: 性能优化工具
import { debounce, rafThrottle } from '@/utils/performance';

const router = useRouter();
const authStore = useAuthStore();
const studentAuthStore = useStudentAuthStore();

// Refs
const searchInputRef = ref();
const searchQuery = ref('');
const showSearchResults = ref(false);
const showNotifications = ref(false);
const showHelpDialog = ref(false);
const showAnnouncement = ref(false);
const dontShowAnnouncement = ref(false);

// Phase 4: Loading & Animation
const isLoading = ref(true);
const isAnimationReady = ref(false);

// Phase 4: Visit Statistics
const systemVisitStats = ref<Record<string, number>>({});

// Phase 4: Particle Background
const particleCanvasRef = ref<HTMLCanvasElement | null>(null);
// Phase 7: 动画帧ID追踪,用于清理
let particleAnimationId: number | null = null;
let fpsAnimationId: number | null = null;

// Phase 5: Theme System
const isDarkMode = ref(false);
const showSettingsPanel = ref(false);

// Phase 5: User Preferences
interface UserPreferences {
  theme: 'light' | 'dark' | 'auto';
  particlesEnabled: boolean;
  animationsEnabled: boolean;
  compactMode: boolean;
  fontSize: 'small' | 'medium' | 'large';
}

const userPreferences = ref<UserPreferences>({
  theme: 'light',
  particlesEnabled: true,
  animationsEnabled: true,
  compactMode: false,
  fontSize: 'medium'
});

// Phase 5: Performance Monitor
const showPerformanceMonitor = ref(false);
const fps = ref(60);
const loadTime = ref(0);

// Phase 5: Data Visualization
const showDataVisualization = ref(false);

// Phase 5: System Usage Heatmap
const showHeatmap = ref(false);

// Phase 5: Card Flip Animation
const flippedCards = ref<Set<string>>(new Set());

// ========== Phase 8: 实际数据集成 ==========
// 加载状态
const isLoadingVisitStats = ref(false);
const isLoadingHeatmap = ref(false);
const isLoadingTrend = ref(false);

// 数据刷新标志
const lastDataRefresh = ref<number>(0);
const DATA_REFRESH_INTERVAL = 5 * 60 * 1000; // 5分钟刷新一次

// 离线访问记录队列（用于离线时缓存）
const offlineVisitQueue = ref<any[]>([]);

// Login Dialog
const showLoginDialog = ref(false);
const loginDialogTitle = ref('登录');
const loginType = ref<'teacher' | 'student'>('teacher');
const loginForm = ref({
  username: '',
  password: ''
});
const loginLoading = ref(false);

// 核心系统配置
const coreSystems = [
  {
    id: 'admin',
    name: '试题管理系统',
    subtitle: '教师 / 管理员端',
    description: '高效管理科目、知识点、试题与试卷，支持AI智能出题和成绩统计分析',
    icon: markRaw(Management),
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    path: '/home',
    features: ['题库管理', '试卷生成', 'AI出题', '成绩统计'],
    status: 'online',
    category: '核心系统',
    color: '#667eea'
  },
  {
    id: 'student',
    name: '在线学习系统',
    subtitle: '学生端',
    description: '在线练习、AI学习助手、知识对战，让学习更高效、更有趣',
    icon: markRaw(School),
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    path: '/student/dashboard',
    features: ['在线练习', 'AI助手', '知识对战', '错题本'],
    status: 'online',
    category: '核心系统',
    color: '#11998e'
  }
];

// 扩展系统配置
const extendedSystems = [
  {
    id: 'warehouse',
    name: '仓库管理系统',
    description: '学校物资设备资产管理',
    icon: markRaw(Box),
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    url: 'http://localhost:8081',
    type: 'external' as const,
    tag: '外部系统',
    tagType: 'warning' as const,
    status: 'offline',
    category: '扩展系统',
    color: '#fa709a'
  },
  {
    id: 'datascreen',
    name: '数据大屏',
    description: '可视化数据展示',
    icon: markRaw(DataBoard),
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    path: '/data-screen',
    type: 'internal' as const,
    tag: '需要权限',
    tagType: 'info' as const,
    status: 'online',
    category: '扩展系统',
    color: '#4facfe'
  },
  {
    id: 'knowledge-graph',
    name: '知识图谱',
    description: '可视化知识结构编辑',
    icon: markRaw(Share),
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    path: '/knowledge-graph',
    type: 'internal' as const,
    status: 'online',
    category: '扩展系统',
    color: '#a8edea'
  }
];

// 外部教育资源配置
const externalResources = [
  {
    id: 'smartedu',
    name: '国家智慧教育平台',
    url: 'https://www.smartedu.cn/',
    icon: markRaw(Reading),
    color: '#e74c3c',
    description: '国家级教育资源平台'
  },
  {
    id: 'mooc',
    name: '中国大学MOOC',
    url: 'https://www.icourse163.org/',
    icon: markRaw(VideoPlay),
    color: '#3498db',
    description: '优质在线课程平台'
  },
  {
    id: 'docs',
    name: '系统使用手册',
    url: '#',
    icon: markRaw(Document),
    color: '#95a5a6',
    description: '系统操作文档'
  }
];

// 通知数据
const notifications = ref([
  {
    id: 1,
    type: 'info',
    title: '系统升级通知：导航页已优化，新增搜索和快捷键功能',
    time: '5分钟前',
    read: false
  },
  {
    id: 2,
    type: 'warning',
    title: '维护通知：仓库管理系统将于今晚22:00进行维护',
    time: '1小时前',
    read: false
  },
  {
    id: 3,
    type: 'success',
    title: 'AI学习助手功能已上线，欢迎体验！',
    time: '2小时前',
    read: true
  }
]);

// 系统公告
const currentAnnouncement = ref({
  title: '欢迎使用综合系统平台 v3.05',
  content: '我们进行了全面的导航页优化，新增了全局搜索、通知中心、快捷键等功能。按 "/" 可快速搜索系统，按 "1" 或 "2" 可快速跳转到核心系统。按 "?" 查看所有快捷键。',
  date: '2026-01-10'
});

// 最近访问记录
const recentAccess = ref<any[]>([]);

// 当前用户信息
const currentUser = computed(() => {
  if (authStore.isAuthenticated && authStore.user) {
    return {
      name: authStore.user.nickName || '教师',
      role: '教师/管理员',
      avatar: authStore.user.avatar || ''
    };
  } else if (studentAuthStore.isAuthenticated && studentAuthStore.student) {
    return {
      name: studentAuthStore.student.name || '学生',
      role: '学生',
      avatar: studentAuthStore.student.avatar || ''
    };
  }
  return null;
});

// 未读通知数量
const unreadNotifications = computed(() => {
  return notifications.value.filter(n => !n.read).length;
});

// 所有系统（用于搜索）
const allSystems = computed(() => {
  return [
    ...coreSystems.map(s => ({ ...s, path: s.path || '' })),
    ...extendedSystems.map(s => ({ ...s, path: s.path || s.url || '' })),
    ...externalResources
  ];
});

// 搜索过滤结果
const filteredSystems = computed(() => {
  if (!searchQuery.value) return [];

  const query = searchQuery.value.toLowerCase();
  return allSystems.value.filter(system =>
    system.name.toLowerCase().includes(query) ||
    system.description?.toLowerCase().includes(query) ||
    system.category?.toLowerCase().includes(query)
  ).slice(0, 5);  // 最多显示5个结果
});

// 搜索处理
// Phase 7: 使用防抖优化搜索性能
const handleSearch = debounce(() => {
  showSearchResults.value = true;
}, 200);

const handleSearchBlur = () => {
  // 延迟关闭，以便点击结果项
  setTimeout(() => {
    showSearchResults.value = false;
  }, 200);
};

const handleResultClick = (system: any) => {
  searchQuery.value = '';
  showSearchResults.value = false;
  if (system.path) {
    if (system.type === 'external') {
      openExternal(system.path);
    } else {
      navigateTo(system.path);
    }
  } else if (system.url) {
    openExternal(system.url);
  }
};

// 通知处理
const getNotificationIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'info': InfoFilled,
    'warning': Warning,
    'success': SuccessFilled
  };
  return iconMap[type] || InfoFilled;
};

const getNotificationColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'info': '#409eff',
    'warning': '#e6a23c',
    'success': '#67c23a'
  };
  return colorMap[type] || '#409eff';
};

const handleNotificationClick = (notification: any) => {
  notification.read = true;
  showNotifications.value = false;
  ElMessage.info('通知已标记为已读');
};

const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true);
  ElMessage.success('所有通知已标记为已读');
};

// 键盘快捷键处理
const handleKeyDown = (e: KeyboardEvent) => {
  // 如果正在输入，忽略快捷键
  if ((e.target as HTMLElement).tagName === 'INPUT' ||
      (e.target as HTMLElement).tagName === 'TEXTAREA') {
    if (e.key === 'Escape') {
      // ESC键清空搜索或关闭对话框
      if (searchQuery.value) {
        searchQuery.value = '';
        showSearchResults.value = false;
        searchInputRef.value?.blur();
      } else {
        showLoginDialog.value = false;
        showHelpDialog.value = false;
        showAnnouncement.value = false;
      }
    }
    return;
  }

  switch (e.key) {
    case '/':
      e.preventDefault();
      searchInputRef.value?.focus();
      break;
    case '1':
      e.preventDefault();
      navigateTo(coreSystems[0].path);
      break;
    case '2':
      e.preventDefault();
      navigateTo(coreSystems[1].path);
      break;
    case '?':
      e.preventDefault();
      showHelpDialog.value = true;
      break;
    case 'Escape':
      searchQuery.value = '';
      showSearchResults.value = false;
      showLoginDialog.value = false;
      showHelpDialog.value = false;
      showAnnouncement.value = false;
      break;
  }
};

// 公告处理
const closeAnnouncement = () => {
  showAnnouncement.value = false;
  if (dontShowAnnouncement.value) {
    localStorage.setItem('hideAnnouncement', 'true');
  }
};

// 系统状态
const getSystemStatus = (systemId: string) => {
  const system = [...coreSystems, ...extendedSystems].find(s => s.id === systemId);
  if (!system || !system.status) return '';

  const statusMap: Record<string, string> = {
    'online': '在线',
    'offline': '离线',
    'maintenance': '维护中'
  };

  return statusMap[system.status] || '';
};

const getSystemStatusType = (systemId: string) => {
  const system = [...coreSystems, ...extendedSystems].find(s => s.id === systemId);
  if (!system || !system.status) return 'info';

  const typeMap: Record<string, any> = {
    'online': 'success',
    'offline': 'danger',
    'maintenance': 'warning'
  };

  return typeMap[system.status] || 'info';
};

// 推荐系统判断
const isRecommended = (systemType: 'admin' | 'student') => {
  if (systemType === 'admin') {
    return authStore.isAuthenticated;
  } else {
    return studentAuthStore.isAuthenticated;
  }
};

// 快速登录
const quickLogin = (type: 'teacher' | 'student') => {
  loginType.value = type;
  loginDialogTitle.value = type === 'teacher' ? '教师登录' : '学生登录';
  loginForm.value = { username: '', password: '' };
  showLoginDialog.value = true;
};

// 处理登录
const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }

  loginLoading.value = true;
  try {
    if (loginType.value === 'teacher') {
      await authStore.login({
        username: loginForm.value.username,
        password: loginForm.value.password
      });
      ElMessage.success('登录成功');
      showLoginDialog.value = false;
      router.push('/home');
    } else {
      await studentAuthStore.login({
        studentNumber: loginForm.value.username,
        password: loginForm.value.password
      });
      ElMessage.success('登录成功');
      showLoginDialog.value = false;
      router.push('/student/dashboard');
    }

    recordAccess(loginType.value === 'teacher' ? 'admin' : 'student');
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名和密码');
  } finally {
    loginLoading.value = false;
  }
};

// 用户菜单命令
const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    if (authStore.isAuthenticated) {
      authStore.logout();
    } else if (studentAuthStore.isAuthenticated) {
      studentAuthStore.logout();
    }
    ElMessage.success('已退出登录');
    recentAccess.value = [];
  } else if (command === 'switchRole') {
    ElMessage.info('请使用其他账号登录');
    if (authStore.isAuthenticated) {
      authStore.logout();
    } else if (studentAuthStore.isAuthenticated) {
      studentAuthStore.logout();
    }
    showLoginDialog.value = true;
  }
};

// 导航到内部系统
const navigateTo = (path: string) => {
  const system = [...coreSystems, ...extendedSystems].find(s => s.path === path);

  // 检查是否需要特定的登录状态
  if (path === '/student/dashboard' || path.startsWith('/student/')) {
    // 学生端，检查学生登录状态
    if (!studentAuthStore.isAuthenticated) {
      // 如果当前是教师登录状态，提示需要切换账号
      if (authStore.isAuthenticated) {
        ElMessageBox.confirm(
          '学生端需要使用学生账号登录。是否退出当前账号并切换到学生登录？',
          '提示',
          {
            confirmButtonText: '切换登录',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          authStore.logout();
          showLoginDialog.value = true;
          loginType.value = 'student';
        }).catch(() => {
          // 用户取消
        });
      } else {
        ElMessage.warning('请先使用学生账号登录');
        showLoginDialog.value = true;
        loginType.value = 'student';
      }
      return;
    }
  } else if (path === '/home' || path.startsWith('/home')) {
    // 管理端，检查管理员登录状态
    if (!authStore.isAuthenticated) {
      // 如果当前是学生登录状态，提示需要切换账号
      if (studentAuthStore.isAuthenticated) {
        ElMessageBox.confirm(
          '管理端需要使用教师/管理员账号登录。是否退出当前账号并切换到教师登录？',
          '提示',
          {
            confirmButtonText: '切换登录',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          studentAuthStore.logout();
          showLoginDialog.value = true;
          loginType.value = 'teacher';
        }).catch(() => {
          // 用户取消
        });
      } else {
        ElMessage.warning('请先使用教师/管理员账号登录');
        showLoginDialog.value = true;
        loginType.value = 'teacher';
      }
      return;
    }
  }

  // 记录访问
  if (system) {
    recordAccess(system.id);
  }

  router.push(path);
};

// 处理系统卡片点击
const handleSystemClick = (system: any) => {
  if (system.type === 'external') {
    openExternal(system.url);
  } else {
    navigateTo(system.path);
  }
};

// 打开外部链接
const openExternal = (url: string) => {
  if (url === '#') {
    ElMessage.info('文档正在完善中，敬请期待！');
    return;
  }
  window.open(url, '_blank');
};

/**
 * 记录访问 (Phase 8: API 集成版本)
 * 同时上报到后端 API 和更新本地缓存
 */
const recordAccess = async (systemId: string) => {
  const system = [...coreSystems, ...extendedSystems].find(s => s.id === systemId);
  if (!system) return;

  const accessItem = {
    id: systemId,
    name: system.name,
    path: system.path || '',
    icon: system.icon,
    color: system.color || system.gradient.match(/#[0-9a-f]{6}/i)?.[0] || '#409eff',
    time: '刚刚',
    timestamp: Date.now()
  };

  // 更新本地最近访问列表
  recentAccess.value = recentAccess.value.filter(item => item.id !== systemId);
  recentAccess.value.unshift(accessItem);
  recentAccess.value = recentAccess.value.slice(0, 3);

  localStorage.setItem('portalRecentAccess', JSON.stringify(recentAccess.value));

  // Phase 8: 上报访问记录到后端 API
  const visitRecord = {
    systemId: systemId,
    systemName: system.name,
    visitTime: new Date().toISOString(),
    userId: authStore.user?.id || studentAuthStore.student?.id,
    userType: authStore.isAuthenticated ? 'admin' : (studentAuthStore.isAuthenticated ? 'student' : undefined)
  };

  try {
    // 尝试上报到后端
    await portalApi.recordVisit(visitRecord);

    // 成功后，尝试同步离线队列
    await syncOfflineVisits();
  } catch (error) {
    console.error('Failed to record visit to API, adding to offline queue:', error);

    // 失败则添加到离线队列
    offlineVisitQueue.value.push(visitRecord);
    localStorage.setItem('offlineVisitQueue', JSON.stringify(offlineVisitQueue.value));
  }

  // Phase 4: 同时记录访问统计
  incrementVisitCount(systemId);
};

/**
 * 加载最近访问记录
 * 优先从 API 获取，失败则从 localStorage 读取缓存
 */
const loadRecentAccess = async () => {
  try {
    // 从 API 获取数据
    const response = await portalApi.fetchRecentAccess(3);

    if (response.code === 200 && response.data) {
      recentAccess.value = response.data.map((item: RecentAccessRecord) => ({
        id: item.id,
        name: item.name,
        icon: item.icon,
        time: item.time,
        path: item.path,
        gradient: item.gradient,
        timestamp: Date.now() // 临时使用当前时间
      }));

      // 缓存到 localStorage
      localStorage.setItem('portalRecentAccess', JSON.stringify(recentAccess.value));
    } else {
      throw new Error('API response invalid');
    }
  } catch (error) {
    console.error('Failed to load recent access from API, using cache:', error);

    // API 失败，从 localStorage 读取缓存
    const saved = localStorage.getItem('portalRecentAccess');
    if (saved) {
      try {
        const data = JSON.parse(saved);
        recentAccess.value = data.map((item: any) => ({
          ...item,
          time: formatAccessTime(item.timestamp)
        }));
      } catch (e) {
        // ignore
      }
    }
  }
};

// 格式化访问时间
const formatAccessTime = (timestamp: number) => {
  const now = Date.now();
  const diff = now - timestamp;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return '较早';
};

// 检查是否显示公告
const checkAnnouncement = () => {
  const hideAnnouncement = localStorage.getItem('hideAnnouncement');
  if (!hideAnnouncement) {
    // 延迟显示公告
    setTimeout(() => {
      showAnnouncement.value = true;
    }, 1000);
  }
};

// ========== Phase 8: 访问统计功能 (API集成版本) ==========

/**
 * 加载访问统计数据
 * 优先从 API 获取，失败则从 localStorage 读取缓存
 */
// Phase 7 & 8: 访问统计加载 - 优化缓存策略
const loadVisitStats = async () => {
  isLoadingVisitStats.value = true;

  try {
    // Phase 7: 先检查缓存有效性,避免不必要的API请求
    const cachedTimestamp = localStorage.getItem('portalVisitStats_timestamp');
    const cacheAge = cachedTimestamp ? Date.now() - parseInt(cachedTimestamp) : Infinity;

    // 如果缓存在5分钟内,优先使用缓存
    if (cacheAge < DATA_REFRESH_INTERVAL) {
      const saved = localStorage.getItem('portalVisitStats');
      if (saved) {
        try {
          systemVisitStats.value = JSON.parse(saved);
          isLoadingVisitStats.value = false;
          return; // 使用缓存,跳过API请求
        } catch (e) {
          console.warn('Failed to parse cached visit stats');
        }
      }
    }

    // 缓存过期或无效,从 API 获取数据
    const response = await portalApi.fetchSystemVisitStats(30);

    if (response.code === 200 && response.data) {
      // 将 API 数据转换为 systemVisitStats 格式
      const stats: Record<string, number> = {};
      response.data.forEach((item: SystemVisitStats) => {
        stats[item.systemId] = item.visitCount;
      });

      systemVisitStats.value = stats;

      // 缓存到 localStorage
      localStorage.setItem('portalVisitStats', JSON.stringify(stats));
      localStorage.setItem('portalVisitStats_timestamp', Date.now().toString());

      lastDataRefresh.value = Date.now();
    } else {
      throw new Error('API response invalid');
    }
  } catch (error) {
    console.error('Failed to load visit stats from API, using cache:', error);

    // API 失败，从 localStorage 读取缓存
    const saved = localStorage.getItem('portalVisitStats');
    if (saved) {
      try {
        systemVisitStats.value = JSON.parse(saved);
      } catch (e) {
        systemVisitStats.value = {};
      }
    }
  } finally {
    isLoadingVisitStats.value = false;
  }
};

/**
 * 同步离线访问记录队列
 * 将离线期间积累的访问记录批量上报到后端
 */
const syncOfflineVisits = async () => {
  if (offlineVisitQueue.value.length === 0) return;

  try {
    // 批量上报离线记录
    await portalApi.batchRecordVisits(offlineVisitQueue.value);

    // 成功后清空队列
    offlineVisitQueue.value = [];
    localStorage.removeItem('offlineVisitQueue');

    console.log('Offline visit queue synced successfully');
  } catch (error) {
    console.error('Failed to sync offline visit queue:', error);
  }
};

/**
 * 检查并刷新数据
 * 如果距离上次刷新超过指定间隔，则重新加载数据
 */
const checkAndRefreshData = async () => {
  const now = Date.now();

  if (now - lastDataRefresh.value > DATA_REFRESH_INTERVAL) {
    console.log('Refreshing portal data...');
    await Promise.all([
      loadVisitStats(),
      loadRecentAccess()
    ]);
  }
};

/**
 * 初始化离线队列
 * 从 localStorage 加载未同步的访问记录
 */
const initOfflineQueue = () => {
  const saved = localStorage.getItem('offlineVisitQueue');
  if (saved) {
    try {
      offlineVisitQueue.value = JSON.parse(saved);
    } catch (e) {
      offlineVisitQueue.value = [];
    }
  }
};

// 保存访问统计
const saveVisitStats = () => {
  localStorage.setItem('portalVisitStats', JSON.stringify(systemVisitStats.value));
};

// 增加访问计数
const incrementVisitCount = (systemId: string) => {
  if (!systemVisitStats.value[systemId]) {
    systemVisitStats.value[systemId] = 0;
  }
  systemVisitStats.value[systemId]++;
  saveVisitStats();
};

// 获取系统热度等级
const getSystemPopularity = (systemId: string) => {
  const count = systemVisitStats.value[systemId] || 0;
  if (count === 0) return '';
  if (count < 5) return 'low';
  if (count < 15) return 'medium';
  if (count < 30) return 'high';
  return 'very-high';
};

// 获取系统访问次数
const getVisitCount = (systemId: string) => {
  return systemVisitStats.value[systemId] || 0;
};

// 获取推荐系统（基于访问频率）
const getRecommendedSystems = computed(() => {
  const allSystemsArray = [...coreSystems, ...extendedSystems];

  // 按访问次数排序
  const sorted = allSystemsArray
    .map(system => ({
      ...system,
      visitCount: systemVisitStats.value[system.id] || 0
    }))
    .sort((a, b) => b.visitCount - a.visitCount);

  // 返回访问次数 > 0 的前3个系统
  return sorted.filter(s => s.visitCount > 0).slice(0, 3);
});

// ========== Phase 4: 粒子背景动画 ==========
// Phase 7: 优化后的粒子背景 - 降低粒子数量、优化性能
const initParticleBackground = () => {
  const canvas = particleCanvasRef.value;
  if (!canvas) return;

  const ctx = canvas.getContext('2d', { alpha: true });
  if (!ctx) return;

  // 清理之前的动画
  if (particleAnimationId) {
    cancelAnimationFrame(particleAnimationId);
    particleAnimationId = null;
  }

  // 设置画布大小
  const resize = () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  };
  resize();

  // Phase 7: 使用RAF节流优化resize
  const throttledResize = rafThrottle(resize);
  window.addEventListener('resize', throttledResize);

  // Phase 7: 减少粒子数量,从50降至30,提升性能
  const particleCount = 30;

  // 粒子数组
  const particles: Array<{
    x: number;
    y: number;
    size: number;
    speedX: number;
    speedY: number;
    opacity: number;
  }> = [];

  // 创建粒子
  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      size: Math.random() * 3 + 1,
      speedX: (Math.random() - 0.5) * 0.5,
      speedY: (Math.random() - 0.5) * 0.5,
      opacity: Math.random() * 0.5 + 0.2
    });
  }

  // Phase 7: 优化连线性能 - 预设最大连接距离
  const MAX_DISTANCE = 120;
  const MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;

  // 动画循环
  const animate = () => {
    if (!userPreferences.value.particlesEnabled) {
      // 清理动画
      if (particleAnimationId) {
        cancelAnimationFrame(particleAnimationId);
        particleAnimationId = null;
      }
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      return;
    }

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    particles.forEach(particle => {
      // 更新位置
      particle.x += particle.speedX;
      particle.y += particle.speedY;

      // 边界检测
      if (particle.x < 0 || particle.x > canvas.width) particle.speedX *= -1;
      if (particle.y < 0 || particle.y > canvas.height) particle.speedY *= -1;

      // 绘制粒子
      ctx.beginPath();
      ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(255, 255, 255, ${particle.opacity})`;
      ctx.fill();
    });

    // Phase 7: 优化连线绘制 - 使用距离平方避免sqrt计算
    particles.forEach((p1, i) => {
      particles.slice(i + 1).forEach(p2 => {
        const dx = p1.x - p2.x;
        const dy = p1.y - p2.y;
        const distanceSq = dx * dx + dy * dy;

        if (distanceSq < MAX_DISTANCE_SQ) {
          const distance = Math.sqrt(distanceSq);
          ctx.beginPath();
          ctx.moveTo(p1.x, p1.y);
          ctx.lineTo(p2.x, p2.y);
          ctx.strokeStyle = `rgba(255, 255, 255, ${(1 - distance / MAX_DISTANCE) * 0.15})`;
          ctx.lineWidth = 0.5;
          ctx.stroke();
        }
      });
    });

    particleAnimationId = requestAnimationFrame(animate);
  };

  particleAnimationId = requestAnimationFrame(animate);

  // 返回清理函数
  return () => {
    window.removeEventListener('resize', throttledResize);
    if (particleAnimationId) {
      cancelAnimationFrame(particleAnimationId);
      particleAnimationId = null;
    }
  };
};

// ========== Phase 4: 加载动画 ==========

// 模拟加载过程
const simulateLoading = async () => {
  const startTime = performance.now();

  // 模拟数据加载
  await new Promise(resolve => setTimeout(resolve, 800));
  isLoading.value = false;

  // 记录加载时间
  loadTime.value = Math.round(performance.now() - startTime);

  // 延迟启动卡片动画
  await nextTick();
  setTimeout(() => {
    isAnimationReady.value = true;
  }, 100);
};

// ========== Phase 5: 主题系统 ==========

// 加载用户偏好
const loadUserPreferences = () => {
  const saved = localStorage.getItem('portalUserPreferences');
  if (saved) {
    try {
      const preferences = JSON.parse(saved);
      userPreferences.value = { ...userPreferences.value, ...preferences };

      // 应用主题
      applyTheme(preferences.theme || 'light');
    } catch (e) {
      console.error('Failed to load preferences:', e);
    }
  }
};

// 保存用户偏好
const saveUserPreferences = () => {
  localStorage.setItem('portalUserPreferences', JSON.stringify(userPreferences.value));
};

// 应用主题
const applyTheme = (theme: 'light' | 'dark' | 'auto') => {
  let shouldBeDark = false;

  if (theme === 'auto') {
    // 自动模式：根据系统时间判断（18:00-6:00 为深色）
    const hour = new Date().getHours();
    shouldBeDark = hour >= 18 || hour < 6;
  } else {
    shouldBeDark = theme === 'dark';
  }

  isDarkMode.value = shouldBeDark;

  // 添加/移除dark-mode class
  if (shouldBeDark) {
    document.documentElement.classList.add('dark-mode');
  } else {
    document.documentElement.classList.remove('dark-mode');
  }
};

// 切换主题
const toggleTheme = () => {
  const themes: Array<'light' | 'dark' | 'auto'> = ['light', 'dark', 'auto'];
  const currentIndex = themes.indexOf(userPreferences.value.theme);
  const nextIndex = (currentIndex + 1) % themes.length;

  userPreferences.value.theme = themes[nextIndex];
  applyTheme(themes[nextIndex]);
  saveUserPreferences();

  ElMessage.success(`已切换到${themes[nextIndex] === 'light' ? '浅色' : themes[nextIndex] === 'dark' ? '深色' : '自动'}模式`);
};

// 更新偏好设置
const updatePreference = (key: keyof UserPreferences, value: any) => {
  (userPreferences.value as any)[key] = value;
  saveUserPreferences();

  // 特殊处理
  if (key === 'theme') {
    applyTheme(value);
  } else if (key === 'particlesEnabled') {
    if (!value) {
      // 停止粒子动画
      const canvas = particleCanvasRef.value;
      if (canvas) {
        const ctx = canvas.getContext('2d');
        ctx?.clearRect(0, 0, canvas.width, canvas.height);
      }
    } else {
      // 重新启动粒子动画
      setTimeout(() => initParticleBackground(), 100);
    }
  }
};

// ========== Phase 5: 性能监控 ==========

// Phase 7: 优化后的FPS计算 - 使用RAF ID追踪
let lastFrameTime = performance.now();
let frameCount = 0;

const calculateFPS = () => {
  frameCount++;
  const now = performance.now();

  if (now >= lastFrameTime + 1000) {
    fps.value = Math.round((frameCount * 1000) / (now - lastFrameTime));
    frameCount = 0;
    lastFrameTime = now;
  }

  if (showPerformanceMonitor.value) {
    fpsAnimationId = requestAnimationFrame(calculateFPS);
  } else {
    // 清理动画帧
    if (fpsAnimationId) {
      cancelAnimationFrame(fpsAnimationId);
      fpsAnimationId = null;
    }
  }
};

// 开始监控性能
const startPerformanceMonitor = () => {
  showPerformanceMonitor.value = true;
  lastFrameTime = performance.now();
  frameCount = 0;
  if (!fpsAnimationId) {
    fpsAnimationId = requestAnimationFrame(calculateFPS);
  }
};

// 停止监控性能
const stopPerformanceMonitor = () => {
  showPerformanceMonitor.value = false;
  if (fpsAnimationId) {
    cancelAnimationFrame(fpsAnimationId);
    fpsAnimationId = null;
  }
};

// ========== Phase 8: 数据可视化 (API集成版本) ==========

// 访问趋势数据（使用 ref 而不是 computed，因为需要从 API 加载）
const visitTrendData = ref<Array<{ date: string; count: number }>>([]);

/**
 * 加载访问趋势数据
 * 优先从 API 获取，失败则生成模拟数据
 */
const loadVisitTrendData = async () => {
  isLoadingTrend.value = true;

  try {
    // 从 API 获取数据
    const response = await portalApi.fetchVisitTrend(7);

    if (response.code === 200 && response.data) {
      visitTrendData.value = response.data.map((item: VisitTrendDataPoint) => ({
        date: formatDate(item.date),
        count: item.count
      }));

      // 缓存到 localStorage
      localStorage.setItem('portalVisitTrend', JSON.stringify(visitTrendData.value));
      localStorage.setItem('portalVisitTrend_timestamp', Date.now().toString());
    } else {
      throw new Error('API response invalid');
    }
  } catch (error) {
    console.error('Failed to load visit trend from API, using cache or fallback:', error);

    // API 失败，先尝试从缓存读取
    const saved = localStorage.getItem('portalVisitTrend');
    if (saved) {
      try {
        visitTrendData.value = JSON.parse(saved);
        return;
      } catch (e) {
        // ignore
      }
    }

    // 缓存也没有，生成模拟数据
    const days = 7;
    const data: Array<{ date: string; count: number }> = [];
    const today = new Date();

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      const dateStr = `${date.getMonth() + 1}/${date.getDate()}`;

      // 模拟数据
      const count = Math.floor(Math.random() * 10) + i * 2;
      data.push({ date: dateStr, count });
    }

    visitTrendData.value = data;
  } finally {
    isLoadingTrend.value = false;
  }
};

/**
 * 格式化日期为 MM/DD 格式
 */
const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}/${date.getDate()}`;
};

// 为了保持向后兼容，提供一个 computed 属性
const getVisitTrendData = computed(() => visitTrendData.value);

// 获取系统使用分布
const getSystemUsageDistribution = computed(() => {
  const systems = [...coreSystems, ...extendedSystems];
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#c71585'];

  const dataWithCount = systems.map((system, index) => ({
    name: system.name,
    count: systemVisitStats.value[system.id] || 0,
    color: colors[index % colors.length]
  })).filter(item => item.count > 0);

  const total = dataWithCount.reduce((sum, item) => sum + item.count, 0);

  return dataWithCount.map(item => ({
    ...item,
    percentage: total > 0 ? Math.round((item.count / total) * 100) : 0
  }));
});

// ========== Phase 8: System Usage Heatmap (API集成版本) ==========
interface HeatmapCell {
  date: string;
  day: number;
  week: number;
  count: number;
  level: number; // 0-4, 用于颜色强度
}

// 热力图数据（使用 ref 而不是 computed）
const heatmapData = ref<HeatmapCell[]>([]);

/**
 * 加载热力图数据
 * 优先从 API 获取，失败则生成模拟数据
 */
const loadHeatmapData = async () => {
  isLoadingHeatmap.value = true;

  try {
    // 从 API 获取数据
    const response = await portalApi.fetchHeatmapData(30);

    if (response.code === 200 && response.data) {
      heatmapData.value = response.data;

      // 缓存到 localStorage
      localStorage.setItem('portalHeatmap', JSON.stringify(heatmapData.value));
      localStorage.setItem('portalHeatmap_timestamp', Date.now().toString());
    } else {
      throw new Error('API response invalid');
    }
  } catch (error) {
    console.error('Failed to load heatmap from API, using cache or fallback:', error);

    // API 失败，先尝试从缓存读取
    const saved = localStorage.getItem('portalHeatmap');
    if (saved) {
      try {
        heatmapData.value = JSON.parse(saved);
        return;
      } catch (e) {
        // ignore
      }
    }

    // 缓存也没有，生成模拟数据
    const days = 30;
    const data: HeatmapCell[] = [];
    const today = new Date();

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);

      const dayOfWeek = date.getDay(); // 0 (Sunday) - 6 (Saturday)
      const weekNumber = Math.floor(i / 7);

      // 模拟访问数据 (0-20次访问)
      const count = Math.floor(Math.random() * 21);

      // 根据访问次数计算热度等级 (0-4)
      let level = 0;
      if (count > 15) level = 4;
      else if (count > 10) level = 3;
      else if (count > 5) level = 2;
      else if (count > 0) level = 1;

      data.push({
        date: `${date.getMonth() + 1}/${date.getDate()}`,
        day: dayOfWeek,
        week: weekNumber,
        count,
        level
      });
    }

    heatmapData.value = data;
  } finally {
    isLoadingHeatmap.value = false;
  }
};

// 为了保持向后兼容，提供一个 computed 属性
const getHeatmapData = computed(() => heatmapData.value);

// ========== Phase 5: Card Flip Animation ==========
const toggleCardFlip = (cardId: string, event?: Event) => {
  // 阻止卡片点击事件冒泡
  if (event) {
    event.stopPropagation();
  }

  const newFlipped = new Set(flippedCards.value);
  if (newFlipped.has(cardId)) {
    newFlipped.delete(cardId);
  } else {
    newFlipped.add(cardId);
  }
  flippedCards.value = newFlipped;
};

const isCardFlipped = (cardId: string) => {
  return flippedCards.value.has(cardId);
};

onMounted(async () => {
  // Phase 8: 初始化离线队列
  initOfflineQueue();

  // Phase 8: 优先加载数据（异步并发）
  await Promise.all([
    loadRecentAccess(),
    loadVisitStats(),
    loadVisitTrendData(),
    loadHeatmapData()
  ]);

  // Phase 8: 同步离线访问记录队列
  syncOfflineVisits();

  // Phase 5: 加载用户偏好
  loadUserPreferences();

  // 检查公告
  checkAnnouncement();

  // 模拟加载（显示骨架屏）
  simulateLoading();

  // 延迟初始化粒子背景，避免影响首屏加载
  setTimeout(() => {
    if (userPreferences.value.particlesEnabled) {
      initParticleBackground();
    }
  }, 1500);

  // 自动模式下定时检查时间
  if (userPreferences.value.theme === 'auto') {
    setInterval(() => {
      applyTheme('auto');
    }, 60000); // 每分钟检查一次
  }

  // Phase 8: 定期刷新数据（每5分钟检查一次）
  setInterval(() => {
    checkAndRefreshData();
  }, 60000); // 每分钟检查一次是否需要刷新
});

// Phase 7: 组件卸载时清理所有动画和事件监听
onUnmounted(() => {
  // 清理粒子背景动画
  if (particleAnimationId) {
    cancelAnimationFrame(particleAnimationId);
    particleAnimationId = null;
  }

  // 清理FPS监控动画
  if (fpsAnimationId) {
    cancelAnimationFrame(fpsAnimationId);
    fpsAnimationId = null;
  }

  // 清理事件监听
  window.removeEventListener('resize', () => {});
});
</script>

<style scoped lang="scss">
// ========== Phase 4: Particle Background ==========
.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.4;
}

// ========== Phase 4: Loading Skeleton ==========
.loading-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;

  .loading-content {
    .skeleton-header {
      margin-bottom: 40px;
      :deep(.el-skeleton__item) {
        height: 80px;
        border-radius: 24px;
      }
    }

    .skeleton-cards {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 24px;
      margin-bottom: 40px;

      .skeleton-card {
        :deep(.el-skeleton__item) {
          height: 300px;
          border-radius: 24px;
        }
      }
    }

    .skeleton-small-cards {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;

      .skeleton-small-card {
        :deep(.el-skeleton__item) {
          height: 180px;
          border-radius: 20px;
        }
      }
    }
  }
}

// ========== Phase 4: Card Stagger Animation ==========
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-animate {
  opacity: 0;
  animation: fadeInUp 0.6s ease-out forwards;
}

// ========== Phase 4: Visit Statistics ==========
.visit-count {
  position: absolute;
  bottom: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  color: white;
  font-size: 12px;
  font-weight: 600;
  z-index: 5;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.35);
    transform: scale(1.05);
  }
}

.visit-count-large {
  position: absolute;
  top: 15px;
  right: 15px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  color: white;
  font-size: 13px;
  font-weight: 700;
  z-index: 5;
}

// ========== Phase 4: Popularity Indicator ==========
.popularity-indicator {
  position: absolute;
  top: 80px;
  left: 20px;
  z-index: 5;
  animation: pulse 1.5s ease-in-out infinite;

  .fire-icon {
    font-size: 28px;
    filter: drop-shadow(0 2px 8px rgba(255, 100, 0, 0.6));
  }
}

// Popularity level animations
.popularity-low .popularity-indicator .fire-icon {
  font-size: 20px;
  animation: flickerLow 2s ease-in-out infinite;
}

.popularity-medium .popularity-indicator .fire-icon {
  font-size: 24px;
  animation: flickerMedium 1.5s ease-in-out infinite;
}

.popularity-high .popularity-indicator .fire-icon {
  font-size: 28px;
  animation: flickerHigh 1s ease-in-out infinite;
}

.popularity-very-high .popularity-indicator .fire-icon {
  font-size: 32px;
  animation: flickerVeryHigh 0.8s ease-in-out infinite;
}

@keyframes flickerLow {
  0%, 100% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 0.9; transform: scale(1.05); }
}

@keyframes flickerMedium {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

@keyframes flickerHigh {
  0%, 100% { opacity: 0.9; transform: scale(1) rotate(-5deg); }
  50% { opacity: 1; transform: scale(1.15) rotate(5deg); }
}

@keyframes flickerVeryHigh {
  0%, 100% { opacity: 1; transform: scale(1) rotate(-10deg); }
  25% { transform: scale(1.2) rotate(5deg); }
  50% { transform: scale(1.1) rotate(-5deg); }
  75% { transform: scale(1.2) rotate(10deg); }
}

// ========== Phase 4: Recommended Systems Section ==========
.recommended-systems {
  margin-bottom: 40px;
  animation: fadeIn 1.1s ease;

  .section-title {
    display: flex;
    align-items: center;
    gap: 12px;

    .subtitle {
      font-size: 1rem;
      font-weight: 400;
      color: rgba(255, 255, 255, 0.8);
      margin-left: 8px;
    }
  }

  .recommended-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .recommended-card {
    position: relative;
    padding: 24px 20px;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.2);
    text-align: center;
    color: white;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
      opacity: 0;
      transition: opacity 0.4s ease;
    }

    &:hover {
      transform: translateY(-8px) scale(1.02);
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);

      &::before {
        opacity: 1;
      }

      .recommend-badge {
        transform: scale(1.1);
      }
    }

    .recommend-badge {
      position: absolute;
      top: 15px;
      left: 15px;
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 10px;
      background: linear-gradient(135deg, #ffd700, #ffed4e);
      color: #8b4513;
      font-size: 12px;
      font-weight: 700;
      border-radius: 15px;
      box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
      transition: all 0.3s ease;
      z-index: 5;
    }

    .card-icon-medium {
      margin-bottom: 16px;
      opacity: 0.95;
    }

    .card-title-medium {
      font-size: 1.2rem;
      font-weight: 700;
      margin-bottom: 10px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }

    .card-description-medium {
      font-size: 0.9rem;
      opacity: 0.9;
      line-height: 1.5;
    }
  }
}

// ========== Phase 4: Enhanced 3D Hover Effects ==========
.core-card {
  perspective: 1000px;
  transform-style: preserve-3d;

  &:hover {
    transform: translateY(-12px) scale(1.02) rotateX(2deg);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  }

  &:active {
    transform: translateY(-8px) scale(0.98);
  }
}

// ========== Phase 4: Ripple Effect (Micro-interaction) ==========
.system-card {
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.4);
    transform: translate(-50%, -50%);
    transition: width 0.6s ease, height 0.6s ease;
  }

  &:active::after {
    width: 300%;
    height: 300%;
  }
}

.portal-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  padding: 40px 20px;
  position: relative;
  overflow-x: hidden;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.portal-container {
  max-width: 1400px;
  margin: 0 auto;
}

// ========== Header Section ==========
.portal-header {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 32px 40px;
  margin-bottom: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: fadeIn 0.6s ease;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .title-section {
      flex: 0 0 auto;

      .portal-title {
        font-size: 2.5rem;
        font-weight: 700;
        color: white;
        margin: 0 0 8px 0;
        text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      }

      .portal-subtitle {
        font-size: 1rem;
        color: rgba(255, 255, 255, 0.9);
        margin: 0;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;
      justify-content: flex-end;
      max-width: 800px;
    }
  }
}

// ========== Search Bar ==========
.search-bar {
  position: relative;
  flex: 1;
  max-width: 400px;

  :deep(.el-input) {
    .el-input__wrapper {
      background: rgba(255, 255, 255, 0.95);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.3);
      transition: all 0.3s ease;

      &:hover, &.is-focus {
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
        border-color: #667eea;
      }
    }

    .el-input__inner {
      color: #333;
      font-size: 14px;
    }
  }

  .search-hint {
    background: #f0f0f0;
    color: #666;
    border: none;
    font-family: monospace;
    font-weight: 600;
  }
}

.search-results {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000;

  .results-list {
    padding: 8px;
  }

  .result-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: #f5f7fa;
      transform: translateX(4px);
    }

    .result-content {
      flex: 1;

      .result-name {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }

      .result-desc {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .no-results {
    padding: 40px 20px;
    text-align: center;
    color: #909399;

    p {
      margin-top: 12px;
      font-size: 14px;
    }
  }
}

// ========== Notification Center ==========
.notification-panel {
  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    margin-bottom: 12px;
    border-bottom: 1px solid #ebeef5;

    .notification-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .notification-list {
    max-height: 400px;
    overflow-y: auto;
  }

  .notification-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    margin-bottom: 8px;

    &:hover {
      background: #f5f7fa;
    }

    &.unread {
      background: #ecf5ff;

      &:hover {
        background: #d9ecff;
      }

      .notification-content .notification-text {
        font-weight: 600;
      }
    }

    .notification-content {
      flex: 1;

      .notification-text {
        font-size: 14px;
        color: #303133;
        margin-bottom: 4px;
        line-height: 1.5;
      }

      .notification-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .no-notifications {
    padding: 40px 20px;
    text-align: center;
    color: #909399;

    p {
      margin-top: 12px;
      font-size: 14px;
    }
  }
}

// ========== User Section ==========
.user-section {
  flex: 0 0 auto;

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    background: rgba(255, 255, 255, 0.2);
    padding: 8px 12px 8px 8px;
    border-radius: 50px;
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .user-details {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .user-name {
        font-size: 14px;
        font-weight: 600;
        color: white;
      }

      .user-role {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.8);
      }
    }

    .user-dropdown {
      :deep(.el-button) {
        background: transparent;
        border: none;
        color: white;

        &:hover {
          background: rgba(255, 255, 255, 0.2);
        }
      }
    }
  }

  .login-section {
    :deep(.el-button) {
      background: rgba(255, 255, 255, 0.95);
      color: #667eea;
      border: none;
      font-weight: 600;
      padding: 12px 24px;

      &:hover {
        background: white;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
      }
    }
  }
}

// ========== Recent Access Section ==========
.recent-access {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);

  .recent-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    color: rgba(255, 255, 255, 0.9);
    font-size: 14px;
    font-weight: 600;
  }

  .recent-items {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }

  .recent-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    backdrop-filter: blur(10px);

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .recent-name {
      font-size: 14px;
      color: white;
      font-weight: 500;
    }

    .recent-time {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.7);
    }
  }
}

// ========== Section Styles ==========
.section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: white;
  margin-bottom: 24px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.6s ease;
}

// ========== Core Systems Grid ==========
.core-systems {
  margin-bottom: 40px;
  animation: fadeIn 0.8s ease;
}

.core-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.core-card {
  position: relative;
  padding: 40px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.3);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
    opacity: 0;
    transition: opacity 0.4s ease;
  }

  &:hover {
    transform: translateY(-12px) scale(1.02);
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.25);

    &::before {
      opacity: 1;
    }

    .card-decoration {
      transform: scale(1.3) rotate(45deg);
      opacity: 0.3;
    }

    .card-shortcut {
      transform: scale(1.1);
      background: rgba(255, 255, 255, 0.95);
    }
  }

  &.recommended {
    border-color: #ffd700;
    box-shadow: 0 8px 32px rgba(255, 215, 0, 0.3);

    &:hover {
      box-shadow: 0 16px 48px rgba(255, 215, 0, 0.4);
    }
  }

  .card-decoration {
    position: absolute;
    top: -50px;
    right: -50px;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    transition: all 0.6s ease;
  }

  .card-shortcut {
    position: absolute;
    top: 20px;
    left: 20px;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(10px);
    border-radius: 8px;
    color: white;
    font-size: 16px;
    font-weight: 700;
    font-family: monospace;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .status-badge {
    position: absolute;
    top: 20px;
    right: 20px;
    z-index: 10;

    :deep(.el-badge__content) {
      font-size: 12px;
      padding: 4px 8px;
      border: 2px solid white;
    }
  }

  .recommended-badge {
    position: absolute;
    top: 70px;
    right: 20px;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    background: linear-gradient(135deg, #ffd700, #ffed4e);
    color: #8b4513;
    font-size: 13px;
    font-weight: 700;
    border-radius: 20px;
    box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
    animation: pulse 2s ease infinite;
  }

  .card-content {
    position: relative;
    z-index: 1;
    color: white;

    .card-icon {
      margin-bottom: 20px;
      opacity: 0.95;
    }

    .card-title {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 8px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }

    .card-subtitle {
      font-size: 1rem;
      margin-bottom: 12px;
      opacity: 0.9;
    }

    .card-description {
      font-size: 0.95rem;
      line-height: 1.6;
      margin-bottom: 20px;
      opacity: 0.95;
    }

    .card-features {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 24px;

      .feature-tag {
        padding: 6px 14px;
        background: rgba(255, 255, 255, 0.25);
        backdrop-filter: blur(10px);
        border-radius: 20px;
        font-size: 0.85rem;
        font-weight: 500;
      }
    }

    .card-actions {
      display: flex;
      gap: 12px;

      .action-btn {
        flex: 1;
        padding: 12px 24px;
        font-size: 1rem;
        font-weight: 600;
        border: none;
        border-radius: 12px;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
        }

        .ml-1 {
          margin-left: 4px;
        }
      }
    }
  }
}

// ========== Extended Systems Grid ==========
.extended-systems {
  margin-bottom: 40px;
  animation: fadeIn 1s ease;
}

.extended-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.extended-card {
  position: relative;
  padding: 28px 24px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
  color: white;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
  }

  .status-badge-small {
    position: absolute;
    top: 12px;
    right: 12px;

    :deep(.el-badge__content) {
      font-size: 10px;
      padding: 2px 6px;
      border: 1px solid white;
    }
  }

  .card-icon-small {
    margin-bottom: 16px;
    opacity: 0.95;
  }

  .card-title-small {
    font-size: 1.3rem;
    font-weight: 700;
    margin-bottom: 12px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }

  .card-description-small {
    font-size: 0.9rem;
    opacity: 0.9;
    margin-bottom: 12px;
  }

  .system-tag {
    border: 1px solid rgba(255, 255, 255, 0.3);
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    color: white;
    font-weight: 600;
  }
}

// ========== External Resources ==========
.external-resources {
  margin-bottom: 40px;
  animation: fadeIn 1.2s ease;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.resource-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateX(8px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }

  .resource-name {
    flex: 1;
    font-size: 1rem;
    font-weight: 600;
  }

  .external-icon {
    opacity: 0.7;
  }
}

// ========== Footer ==========
.portal-footer {
  text-align: center;
  padding: 32px 20px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  animation: fadeIn 1.4s ease;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;

  .footer-hint {
    font-size: 0.85rem;
    color: rgba(255, 255, 255, 0.7);

    kbd {
      display: inline-block;
      padding: 2px 8px;
      margin: 0 4px;
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 4px;
      font-family: monospace;
      font-size: 0.85rem;
      font-weight: 600;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }
}

// ========== Help Dialog ==========
.help-content {
  .help-section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
    }
  }

  .shortcut-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .shortcut-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background: #e4e7ed;
    }

    kbd {
      display: inline-block;
      min-width: 40px;
      padding: 6px 12px;
      background: white;
      border: 2px solid #dcdfe6;
      border-radius: 6px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
      font-weight: 700;
      color: #606266;
      text-align: center;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }

    span {
      flex: 1;
      font-size: 14px;
      color: #606266;
    }
  }
}

// ========== Announcement Dialog ==========
.announcement-content {
  text-align: center;
  padding: 20px;

  .announcement-icon {
    margin-bottom: 20px;
  }

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 16px;
  }

  .announcement-text {
    font-size: 15px;
    color: #606266;
    line-height: 1.8;
    margin-bottom: 20px;
    text-align: left;
  }

  .announcement-meta {
    display: flex;
    justify-content: center;
    gap: 12px;
    font-size: 13px;
    color: #909399;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
}

// ========== Animations ==========
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

// ========== Transitions ==========
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// ========== Responsive Design ==========
@media (max-width: 1024px) {
  .portal-wrapper {
    padding: 20px 16px;
  }

  .portal-header {
    padding: 24px;

    .header-content {
      flex-direction: column;
      gap: 20px;
      align-items: stretch;

      .title-section {
        text-align: center;

        .portal-title {
          font-size: 2rem;
        }
      }

      .header-right {
        flex-direction: column;
        max-width: 100%;
        gap: 12px;

        .search-bar {
          max-width: 100%;
        }
      }
    }
  }

  .core-grid {
    grid-template-columns: 1fr;
  }

  .extended-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .resource-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .portal-header .header-content .title-section .portal-title {
    font-size: 1.8rem;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .core-card {
    padding: 28px 24px;

    .card-content {
      .card-title {
        font-size: 1.6rem;
      }

      .card-subtitle {
        font-size: 0.9rem;
      }

      .card-description {
        font-size: 0.85rem;
      }
    }

    .card-shortcut {
      width: 32px;
      height: 32px;
      font-size: 14px;
    }
  }

  .extended-grid {
    grid-template-columns: 1fr;
  }

  .extended-card {
    padding: 24px 20px;

    .card-title-small {
      font-size: 1.2rem;
    }
  }
}

@media (max-width: 480px) {
  .portal-wrapper {
    padding: 16px 12px;
  }

  .portal-header {
    padding: 20px;
    border-radius: 16px;

    .header-content {
      .title-section .portal-title {
        font-size: 1.5rem;
      }

      .header-right {
        .user-section .user-info {
          padding: 6px 10px 6px 6px;

          .user-details {
            display: none;
          }
        }
      }
    }

    .recent-access {
      .recent-items {
        flex-direction: column;

        .recent-item {
          width: 100%;
        }
      }
    }
  }

  .core-card {
    border-radius: 16px;
    padding: 24px 20px;

    .card-content {
      .card-title {
        font-size: 1.4rem;
      }

      .card-features {
        .feature-tag {
          font-size: 0.75rem;
          padding: 4px 10px;
        }
      }
    }
  }

  .extended-card {
    border-radius: 16px;
  }

  .resource-link {
    padding: 16px 20px;
    border-radius: 12px;
  }
}

/* ========== Phase 5: Dark Mode Styles ========== */
:root {
  --bg-primary: #ffffff;
  --bg-secondary: #f5f7fa;
  --bg-card: #ffffff;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-placeholder: #909399;
  --border-color: #dcdfe6;
  --shadow-light: rgba(0, 0, 0, 0.1);
  --shadow-medium: rgba(0, 0, 0, 0.15);
}

.dark-mode {
  --bg-primary: #1a1a1a;
  --bg-secondary: #2d2d2d;
  --bg-card: #2a2a2a;
  --text-primary: #e0e0e0;
  --text-secondary: #b0b0b0;
  --text-placeholder: #707070;
  --border-color: #404040;
  --shadow-light: rgba(255, 255, 255, 0.05);
  --shadow-medium: rgba(255, 255, 255, 0.1);

  .portal-container {
    background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
    color: var(--text-primary);
  }

  .portal-header {
    background: rgba(42, 42, 42, 0.95);
    border-bottom-color: var(--border-color);
  }

  .core-card,
  .extended-card {
    background: var(--bg-card);
    border-color: var(--border-color);
    box-shadow: 0 4px 12px var(--shadow-light);

    &:hover {
      box-shadow: 0 8px 24px var(--shadow-medium);
    }
  }

  .search-bar input {
    background: var(--bg-secondary);
    border-color: var(--border-color);
    color: var(--text-primary);

    &::placeholder {
      color: var(--text-placeholder);
    }
  }

  .search-results {
    background: var(--bg-card);
    border-color: var(--border-color);
  }

  .recent-item {
    background: var(--bg-secondary);
    border-color: var(--border-color);
  }

  .announcement-content {
    background: var(--bg-card);
  }
}

/* ========== Phase 5: Settings Panel Styles ========== */
.settings-content {
  padding: 20px 0;

  .settings-section {
    margin-bottom: 30px;

    h3 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .settings-tip {
      margin-top: 10px;
      font-size: 13px;
      color: #909399;
    }

    .settings-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      span {
        font-size: 14px;
        color: #606266;
      }
    }

    .settings-buttons {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
  }
}

.dark-mode .settings-content {
  .settings-section {
    h3 {
      color: var(--text-primary);
    }

    .settings-item {
      border-bottom-color: var(--border-color);

      span {
        color: var(--text-secondary);
      }
    }
  }
}

/* ========== Phase 5: Performance Monitor Styles ========== */
.performance-monitor {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 2000;
  min-width: 200px;

  .performance-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e0e0e0;

    span {
      font-weight: 600;
      font-size: 14px;
      color: #303133;
    }

    .el-button {
      padding: 4px;
    }
  }

  .performance-stats {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .stat-label {
        font-size: 13px;
        color: #606266;
      }

      .stat-value {
        font-size: 16px;
        font-weight: 600;
        font-family: 'Courier New', monospace;

        &.stat-good {
          color: #67c23a;
        }

        &.stat-warning {
          color: #e6a23c;
        }

        &.stat-bad {
          color: #f56c6c;
        }
      }
    }
  }
}

.dark-mode .performance-monitor {
  background: rgba(42, 42, 42, 0.95);

  .performance-header {
    border-bottom-color: var(--border-color);

    span {
      color: var(--text-primary);
    }
  }

  .performance-stats {
    .stat-item {
      .stat-label {
        color: var(--text-secondary);
      }
    }
  }
}

/* ========== Phase 5: Data Visualization Styles ========== */
.data-visualization-content {
  padding: 20px;

  .chart-section {
    margin-bottom: 40px;

    &:last-child {
      margin-bottom: 0;
    }

    h3 {
      margin: 0 0 20px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  /* Trend Chart */
  .trend-chart {
    padding: 20px;
    background: #f9f9f9;
    border-radius: 12px;

    .trend-bars {
      display: flex;
      justify-content: space-around;
      align-items: flex-end;
      height: 200px;
      gap: 8px;

      .trend-bar-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        .trend-bar-wrapper {
          flex: 1;
          width: 100%;
          display: flex;
          align-items: flex-end;
          justify-content: center;

          .trend-bar {
            width: 60%;
            min-height: 20px;
            background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
            border-radius: 4px 4px 0 0;
            position: relative;
            transition: all 0.3s ease;
            display: flex;
            align-items: flex-start;
            justify-content: center;
            padding-top: 6px;

            &:hover {
              transform: translateY(-4px);
              box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
            }

            .trend-count {
              font-size: 12px;
              font-weight: 600;
              color: white;
            }
          }
        }

        .trend-label {
          font-size: 12px;
          color: #606266;
          white-space: nowrap;
        }
      }
    }
  }

  /* Usage Distribution */
  .usage-distribution {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .usage-item {
      .usage-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .usage-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
        }

        .usage-percent {
          font-size: 14px;
          font-weight: 600;
          color: #409eff;
        }
      }

      .usage-bar {
        width: 100%;
        height: 24px;
        background: #f0f0f0;
        border-radius: 12px;
        overflow: hidden;
        margin-bottom: 6px;

        .usage-bar-fill {
          height: 100%;
          background: #409eff;
          transition: width 0.6s ease;
          border-radius: 12px;
        }
      }

      .usage-count {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.dark-mode .data-visualization-content {
  .chart-section h3 {
    color: var(--text-primary);
  }

  .trend-chart {
    background: var(--bg-secondary);

    .trend-bars .trend-bar-item .trend-label {
      color: var(--text-secondary);
    }
  }

  .usage-distribution {
    .usage-item {
      .usage-header .usage-name {
        color: var(--text-primary);
      }

      .usage-bar {
        background: var(--bg-secondary);
      }

      .usage-count {
        color: var(--text-placeholder);
      }
    }
  }
}

/* ========== Phase 5: Card Flip Animation Styles ========== */
.core-card {
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);

  &.card-flipped {
    .card-flip-container {
      transform: rotateY(180deg);
    }
  }

  .card-flip-container {
    position: relative;
    width: 100%;
    height: 100%;
    transform-style: preserve-3d;
    transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .card-face {
    position: absolute;
    width: 100%;
    height: 100%;
    backface-visibility: hidden;
    -webkit-backface-visibility: hidden;
    border-radius: 24px;
    overflow: hidden;
  }

  .card-front {
    transform: rotateY(0deg);
  }

  .card-back {
    transform: rotateY(180deg);
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 40px;
    color: white;
    display: flex;
    flex-direction: column;

    .card-back-content {
      display: flex;
      flex-direction: column;
      height: 100%;
      gap: 20px;

      .back-title {
        font-size: 1.8rem;
        font-weight: 700;
        margin: 0 0 10px 0;
        color: white;
      }

      .back-info {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .info-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          background: rgba(255, 255, 255, 0.1);
          border-radius: 12px;
          backdrop-filter: blur(10px);

          .el-icon {
            font-size: 24px;
            color: rgba(255, 255, 255, 0.9);
          }

          .info-details {
            display: flex;
            flex-direction: column;
            gap: 4px;

            .info-label {
              font-size: 12px;
              opacity: 0.8;
            }

            .info-value {
              font-size: 16px;
              font-weight: 600;
            }
          }
        }
      }

      .back-features {
        flex: 1;

        h4 {
          font-size: 1.2rem;
          margin: 0 0 10px 0;
          color: white;
        }

        ul {
          margin: 0;
          padding-left: 20px;
          list-style-type: disc;

          li {
            margin-bottom: 6px;
            font-size: 14px;
            line-height: 1.5;
            opacity: 0.9;
          }
        }
      }

      .back-actions {
        display: flex;
        justify-content: center;
        margin-top: auto;
      }
    }
  }

  .card-flip-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    z-index: 10;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.1);
    }

    &.back {
      background: rgba(0, 0, 0, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.2);

      &:hover {
        background: rgba(0, 0, 0, 0.3);
      }
    }

    .el-icon {
      font-size: 18px;
    }
  }
}

.dark-mode .core-card {
  .card-back {
    background: linear-gradient(135deg, #2d2d2d 0%, #1a1a1a 100%);

    .card-back-content {
      .back-title {
        color: var(--text-primary);
      }

      .back-info .info-item {
        background: rgba(255, 255, 255, 0.05);
      }
    }
  }
}

/* ========== Phase 5: Heatmap Styles ========== */
.heatmap-content {
  padding: 20px;

  .heatmap-header {
    margin-bottom: 30px;
    text-align: center;

    h3 {
      margin: 0 0 10px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }

    .heatmap-description {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }

  .heatmap-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 30px;

    .heatmap-days {
      display: grid;
      grid-template-rows: repeat(7, 1fr);
      gap: 4px;
      margin-right: 12px;
      padding-left: 40px;

      .day-label {
        font-size: 12px;
        color: #606266;
        display: flex;
        align-items: center;
        height: 16px;
      }
    }

    .heatmap-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      grid-template-rows: repeat(7, 1fr);
      gap: 4px;
      min-height: 140px;

      .heatmap-cell {
        width: 100%;
        aspect-ratio: 1;
        border-radius: 3px;
        transition: all 0.2s ease;
        cursor: pointer;
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;

        &.level-0 {
          background: #ebedf0;
        }

        &.level-1 {
          background: #c6e48b;
        }

        &.level-2 {
          background: #7bc96f;
        }

        &.level-3 {
          background: #239a3b;
        }

        &.level-4 {
          background: #196127;
        }

        &:hover {
          transform: scale(1.1);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          z-index: 1;

          .cell-tooltip {
            opacity: 1;
            transform: translateY(-100%) scale(1);
          }
        }

        .cell-tooltip {
          position: absolute;
          top: -8px;
          left: 50%;
          transform: translateX(-50%) translateY(-100%) scale(0.8);
          background: rgba(0, 0, 0, 0.8);
          color: white;
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 12px;
          white-space: nowrap;
          opacity: 0;
          pointer-events: none;
          transition: all 0.2s ease;
        }
      }
    }

    .heatmap-legend {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      margin-top: 20px;

      .legend-label {
        font-size: 12px;
        color: #909399;
      }

      .legend-item {
        width: 16px;
        height: 16px;
        border-radius: 3px;

        &.level-0 {
          background: #ebedf0;
        }

        &.level-1 {
          background: #c6e48b;
        }

        &.level-2 {
          background: #7bc96f;
        }

        &.level-3 {
          background: #239a3b;
        }

        &.level-4 {
          background: #196127;
        }
      }
    }
  }

  .heatmap-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: #f9f9f9;
      border-radius: 12px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        font-size: 32px;
      }

      .stat-info {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .stat-label {
          font-size: 12px;
          color: #909399;
        }

        .stat-value {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }
}

.dark-mode .heatmap-content {
  .heatmap-header h3 {
    color: var(--text-primary);
  }

  .heatmap-container {
    .heatmap-days .day-label {
      color: var(--text-secondary);
    }

    .heatmap-grid .heatmap-cell {
      &.level-0 {
        background: #2d2d2d;
      }
    }
  }

  .heatmap-stats .stat-card {
    background: var(--bg-secondary);

    .stat-info {
      .stat-label {
        color: var(--text-placeholder);
      }

      .stat-value {
        color: var(--text-primary);
      }
    }
  }
}

/* ========== Phase 5: Accessibility Enhancements ========== */
/* Skip to Content Link */
.skip-to-content {
  position: fixed;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  background: #409eff;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  z-index: 10000;
  transition: top 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);

  &:focus {
    top: 20px;
    outline: 3px solid #fff;
    outline-offset: 2px;
  }
}

/* Enhanced Focus Indicators */
*:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
  border-radius: 4px;
}

.el-button:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}

.core-card:focus-visible,
.extended-card:focus-visible {
  outline: 3px solid #409eff;
  outline-offset: 4px;
}

.search-bar input:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 0;
}

/* High Contrast Mode Support */
@media (prefers-contrast: high) {
  .core-card,
  .extended-card {
    border: 2px solid currentColor;
  }

  .feature-tag {
    border: 1px solid currentColor;
  }
}

/* Reduced Motion Support */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }

  .card-flip-container {
    transition: none !important;
  }

  .core-card,
  .extended-card {
    transform: none !important;
  }
}

/* Screen Reader Only Content */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
</style>
