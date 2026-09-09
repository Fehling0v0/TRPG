<template>
  <el-container class="main-layout">
    <!-- 移动端遮罩层 -->
    <div v-if="isMobile && mobileSidebarOpen" class="sidebar-overlay" @click="closeMobileSidebar" />

    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar" :class="{ 'sidebar-mobile': isMobile, 'sidebar-mobile-open': isMobile && mobileSidebarOpen }">
      <div class="brand">
        <span v-if="collapsed && !isMobile" class="brand-mark">PC</span>
        <span v-else class="brand-name">PC档案夹</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        :collapse="collapsed && !isMobile"
        :collapse-transition="false"
        class="sidebar-menu"
        @select="onMenuSelect"
      >
        <el-menu-item index="/home/pc-list">
          <el-icon><Collection /></el-icon>
          <template #title>角色卡列表</template>
        </el-menu-item>
        <el-menu-item index="/home/upload">
          <el-icon><Upload /></el-icon>
          <template #title>上传角色卡</template>
        </el-menu-item>
        <el-menu-item index="/home/ho-table">
          <el-icon><Grid /></el-icon>
          <template #title>HO 表</template>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/home/admin">
          <el-icon><Setting /></el-icon>
          <template #title>管理员后台</template>
        </el-menu-item>
      </el-menu>
      <div v-if="!collapsed" class="sidebar-foot">玩家角色档案</div>
    </el-aside>

    <el-container class="body-container">
      <!-- 顶部导航 -->
      <el-header class="header" height="60px">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <span class="date-text">{{ dateText }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-dropdown">
              <span class="avatar">{{ initial }}</span>
          <span v-if="!isMobile" class="username">{{ userStore.user?.username }}</span>
          <el-tag v-if="userStore.isAdmin && !isMobile" size="small" effect="plain" class="admin-chip">管理员</el-tag>
              <el-icon class="caret"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="change-password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 修改密码弹窗 -->
      <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false" append-to-body>
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="6-100 个字符" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="changingPassword" @click="submitChangePassword">确认修改</el-button>
        </template>
      </el-dialog>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Collection,
  Upload,
  Grid,
  Setting,
  ArrowDown,
  Fold,
  Expand
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { authApi } from '../api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 响应式侧边栏：
//   手机端（<768px）：抽屉式，默认隐藏，点击汉堡按钮弹出全尺寸侧边栏+遮罩
//   平板端（768-992px）：折叠为 64px 图标栏
//   桌面端（≥992px）：全尺寸 220px
const isMobile = ref(window.innerWidth < 768)
const isNarrow = ref(window.innerWidth >= 768 && window.innerWidth < 992)
const manualCollapsed = ref(false)
const mobileSidebarOpen = ref(false)

const collapsed = computed(() => isNarrow.value || manualCollapsed.value)
const sidebarWidth = computed(() => {
  if (isMobile.value) return '220px' // 抽屉模式始终全尺寸
  return collapsed.value ? '64px' : '220px'
})

function toggleCollapse() {
  if (isMobile.value) {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
  } else {
    manualCollapsed.value = !manualCollapsed.value
  }
}

function closeMobileSidebar() {
  mobileSidebarOpen.value = false
}

function onMenuSelect() {
  // 移动端选择菜单后自动关闭抽屉
  if (isMobile.value) {
    mobileSidebarOpen.value = false
  }
}

function onResize() {
  const w = window.innerWidth
  const wasMobile = isMobile.value
  isMobile.value = w < 768
  isNarrow.value = w >= 768 && w < 992
  // 离开手机端时关闭抽屉
  if (wasMobile && !isMobile.value) {
    mobileSidebarOpen.value = false
  }
  // 切到桌面时恢复展开
  if (w >= 992) {
    manualCollapsed.value = false
  }
}
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const dateText = new Date().toLocaleDateString('zh-CN', {
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const initial = computed(() => (userStore.user?.username || '?').slice(0, 1).toUpperCase())

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'change-password') {
    openPasswordDialog()
  }
}

// ── 修改密码 ──
const passwordDialogVisible = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度需在 6-100 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function openPasswordDialog() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
  passwordDialogVisible.value = true
}

async function submitChangePassword() {
  try {
    await passwordFormRef.value?.validate()
  } catch (err) {
    return
  }
  changingPassword.value = true
  try {
    await authApi.changePassword({
      old_password: passwordForm.oldPassword,
      new_password: passwordForm.newPassword
    })
    passwordDialogVisible.value = false
    ElMessage.success('密码修改成功，下次登录请使用新密码')
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped>
.main-layout {
  height: 100%;
}

/* ── 侧边栏 ── */
.sidebar {
  background: var(--bg-card);
  border-right: 1px solid var(--hairline);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.25s ease;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}

.brand-name {
  font-family: var(--font-serif);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 4px;
  color: var(--ink);
  white-space: nowrap;
  margin-right: -4px; /* 抵消字间距 */
}

.brand-mark {
  font-family: var(--font-serif);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--primary);
}

.sidebar-menu {
  border-right: none;
  background-color: transparent;
  padding: 12px 10px;
  flex: 1;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  margin: 3px 0;
  border-radius: 8px;
  color: var(--text-regular);
  font-size: 14px;
  letter-spacing: 0.5px;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: var(--text-secondary);
  transition: color 0.2s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: var(--bg-page);
  color: var(--ink);
}

.sidebar-menu :deep(.el-menu-item:hover .el-icon) {
  color: var(--ink);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--primary-tint);
  color: var(--primary);
  font-weight: 600;
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--primary);
}

.sidebar-foot {
  padding: 20px 0 18px;
  text-align: center;
  font-size: 11px;
  letter-spacing: 3px;
  color: var(--text-placeholder);
  white-space: nowrap;
  flex-shrink: 0;
}

/* ── 移动端抽屉式侧边栏 ── */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(26, 25, 24, 0.35);
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.sidebar-mobile {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 1001;
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  box-shadow: 2px 0 12px rgba(26, 25, 24, 0.1);
}

.sidebar-mobile-open {
  transform: translateX(0);
}

/* ── 顶栏 ── */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-card);
  border-bottom: 1px solid var(--hairline);
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: var(--ink);
}

.date-text {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
  outline: none;
}

.user-dropdown:hover {
  background-color: var(--bg-page);
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--primary-tint);
  color: var(--primary);
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.username {
  font-size: 14px;
  color: var(--ink);
  font-weight: 500;
}

.admin-chip {
  background: transparent;
  border-color: var(--hairline-strong);
  color: var(--primary);
  font-weight: 500;
}

.caret {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ── 内容区 ── */
.body-container {
  min-width: 0; /* 防止表格/横向滚动撑破布局 */
}

.main-content {
  background-color: var(--bg-page);
  overflow-y: auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .main-content {
    padding: 12px;
  }

  .date-text {
    display: none;
  }

  .header {
    padding: 0 12px;
  }

  .header-left {
    gap: 8px;
  }

  .user-dropdown {
    gap: 6px;
    padding: 4px 6px;
  }

  /* 弹窗全宽 */
  .main-layout :deep(.el-dialog) {
    width: 92% !important;
    margin: 16px auto !important;
  }
}
</style>
