<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>管理员后台</h2>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="tab-actions">
          <el-button :icon="Refresh" @click="loadUsers">刷新</el-button>
        </div>
        <el-card shadow="never" class="admin-card">
        <el-table :data="users" v-loading="usersLoading" border size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                {{ row.role }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="pc_count" label="角色卡数" width="100" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button size="small" type="warning" @click="handleResetPassword(row)">
                重置密码
              </el-button>
              <el-button
                size="small"
                type="danger"
                :disabled="row.role === 'ADMIN'"
                @click="handleDeleteUser(row)"
              >
                删除用户
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 邀请码管理 -->
      <el-tab-pane label="邀请码管理" name="invites">
        <div class="tab-actions">
          <el-button type="primary" :icon="Plus" @click="handleCreateCode">生成邀请码</el-button>
          <el-button :icon="Refresh" @click="loadInviteCodes">刷新</el-button>
        </div>
        <el-card shadow="never" class="admin-card">
        <el-table :data="inviteCodes" v-loading="codesLoading" border size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="code" label="邀请码" min-width="160">
            <template #default="{ row }">
              <code class="code-text">{{ row.code }}</code>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.used ? 'success' : 'warning'" size="small">
                {{ row.used ? '已使用' : '未使用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="created_by" label="创建者 ID" width="100" />
          <el-table-column prop="used_by" label="使用者 ID" width="100">
            <template #default="{ row }">{{ row.used_by || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">{{ formatDate(row.created_at) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                size="small"
                type="danger"
                :disabled="row.used"
                @click="handleDeleteCode(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 重置密码结果弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="密码已重置" width="400px">
      <div class="password-result">
        <p>新密码：</p>
        <el-input :model-value="newPassword" readonly>
          <template #append>
            <el-button @click="copyPassword">复制</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>

    <!-- 新邀请码弹窗 -->
    <el-dialog v-model="codeDialogVisible" title="邀请码已生成" width="400px">
      <div class="password-result">
        <p>邀请码：</p>
        <el-input :model-value="newCode" readonly>
          <template #append>
            <el-button @click="copyCode">复制</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '../api/admin'

const activeTab = ref('users')

// 用户管理
const users = ref([])
const usersLoading = ref(false)

// 邀请码管理
const inviteCodes = ref([])
const codesLoading = ref(false)

// 弹窗
const passwordDialogVisible = ref(false)
const newPassword = ref('')
const codeDialogVisible = ref(false)
const newCode = ref('')

async function loadUsers() {
  usersLoading.value = true
  try {
    const res = await adminApi.getUsers()
    users.value = res.data || []
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    usersLoading.value = false
  }
}

async function loadInviteCodes() {
  codesLoading.value = true
  try {
    const res = await adminApi.getInviteCodes()
    inviteCodes.value = res.data || []
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    codesLoading.value = false
  }
}

async function handleCreateCode() {
  try {
    const res = await adminApi.createInviteCode()
    // 后端返回 InviteCodeView 对象，取 code 字段
    newCode.value = res.data?.code ?? ''
    codeDialogVisible.value = true
    await loadInviteCodes()
  } catch (err) {
    // 错误已由拦截器处理
  }
}

async function handleDeleteCode(row) {
  try {
    await ElMessageBox.confirm(`确定删除邀请码 "${row.code}"？`, '警告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await adminApi.deleteInviteCode(row.id)
    ElMessage.success('删除成功')
    await loadInviteCodes()
  } catch (err) {
    // 用户取消或错误已由拦截器处理
  }
}

async function handleDeleteUser(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}"？该用户的所有角色卡将一并删除。`, '警告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await adminApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    await loadUsers()
  } catch (err) {
    // 用户取消或错误已由拦截器处理
  }
}

async function handleResetPassword(row) {
  try {
    const res = await adminApi.resetPassword(row.id)
    newPassword.value = res.data.new_password
    passwordDialogVisible.value = true
  } catch (err) {
    // 错误已由拦截器处理
  }
}

function copyPassword() {
  navigator.clipboard.writeText(newPassword.value)
  ElMessage.success('已复制到剪贴板')
}

function copyCode() {
  navigator.clipboard.writeText(newCode.value)
  ElMessage.success('已复制到剪贴板')
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadUsers()
  loadInviteCodes()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
}

.admin-card {
  border-radius: 12px;
  border: 1px solid var(--hairline);
}

.admin-card :deep(.el-card__body) {
  padding: 0;
}

.admin-card :deep(.el-table) {
  border-radius: 12px;
}

.tab-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.code-text {
  font-family: 'SFMono-Regular', Consolas, 'Courier New', monospace;
  font-size: 14px;
  color: var(--primary);
  font-weight: 600;
  letter-spacing: 1px;
}

.password-result p {
  font-size: 14px;
  color: var(--text-regular);
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .tab-actions {
    flex-wrap: wrap;
  }

  .page-header h2 {
    font-size: 20px;
  }

  /* 表格横向滚动 */
  .admin-card :deep(.el-table) {
    overflow-x: auto;
  }
}
</style>
