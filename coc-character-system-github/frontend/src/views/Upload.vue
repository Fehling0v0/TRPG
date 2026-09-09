<template>
  <div class="upload-page">
    <div class="page-header">
      <h2>上传角色卡</h2>
    </div>

    <el-card shadow="never" class="upload-card">
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        action="#"
        :auto-upload="false"
        multiple
        accept=".xlsx,.xls"
        :on-change="handleChange"
        :on-remove="handleRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将 Excel 文件拖到此处，或<em>点击上传</em>（支持多选）
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .xlsx / .xls 格式的 COC 人物卡 Excel 文件，可一次选择多个文件
          </div>
        </template>
      </el-upload>

      <div v-if="fileList.length > 0" class="file-list-section">
        <div class="file-list-header">
          <span>待解析文件（{{ fileList.length }}）</span>
          <el-button type="primary" :loading="batchUploading" @click="handleBatchUpload">
            {{ batchUploading ? '解析中...' : '一键解析全部' }}
          </el-button>
        </div>
        <el-table :data="fileList" border size="small">
          <el-table-column label="文件名" min-width="200">
            <template #default="{ row }">
              <div class="file-info">
                <el-icon><Document /></el-icon>
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="大小" width="100">
            <template #default="{ row }">{{ formatFileSize(row.size) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'pending'" size="small" type="info">待解析</el-tag>
              <el-tag v-else-if="row.status === 'uploading'" size="small" type="primary">解析中</el-tag>
              <el-tag v-else-if="row.status === 'success'" size="small" type="success">成功</el-tag>
              <el-tag v-else-if="row.status === 'error'" size="small" type="danger">失败</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="信息" min-width="200">
            <template #default="{ row }">
              <span v-if="row.message" :class="row.status === 'error' ? 'error-msg' : 'success-msg'">{{ row.message }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row, $index }">
              <el-button size="small" type="danger" link @click="removeFile($index)" :disabled="row.status === 'uploading'">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="batchProgress > 0 && batchProgress < 100" class="progress-bar">
        <el-progress :percentage="batchProgress" />
      </div>
    </el-card>

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      :closable="false"
      style="margin-top: 16px"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document } from '@element-plus/icons-vue'
import { pcApi } from '../api/pc'

const router = useRouter()
const uploadRef = ref()
const fileList = ref([])
const batchUploading = ref(false)
const batchProgress = ref(0)
const errorMsg = ref('')

function handleChange(file, fileListRaw) {
  // el-upload 会传 fileList，我们自定义管理
  const existing = fileList.value.find(f => f.uid === file.uid)
  if (!existing) {
    fileList.value.push({
      uid: file.uid,
      name: file.name,
      size: file.size,
      raw: file.raw,
      status: 'pending',
      message: ''
    })
  }
  errorMsg.value = ''
}

function handleRemove(file) {
  const idx = fileList.value.findIndex(f => f.uid === file.uid)
  if (idx !== -1) {
    fileList.value.splice(idx, 1)
  }
}

function removeFile(index) {
  fileList.value.splice(index, 1)
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function handleBatchUpload() {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  batchUploading.value = true
  batchProgress.value = 0
  errorMsg.value = ''

  const total = fileList.value.length
  let successCount = 0
  let failCount = 0

  for (let i = 0; i < total; i++) {
    const item = fileList.value[i]
    if (item.status === 'success') continue

    item.status = 'uploading'
    item.message = ''

    try {
      const res = await pcApi.uploadExcel(item.raw)
      item.status = 'success'
      item.message = `角色卡ID: ${res.data?.pc_id || res.data?.id || '已创建'}`
      successCount++
    } catch (err) {
      item.status = 'error'
      item.message = err?.response?.data?.message || '解析失败'
      failCount++
    }

    batchProgress.value = Math.round(((i + 1) / total) * 100)
  }

  batchUploading.value = false

  if (failCount === 0) {
    ElMessage.success(`全部解析成功！共 ${successCount} 张角色卡已创建`)
    setTimeout(() => {
      router.push('/home/pc-list')
    }, 1000)
  } else {
    ElMessage.warning(`成功 ${successCount} 张，失败 ${failCount} 张`)
  }
}
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

.upload-card {
  border-radius: 12px;
  border: 1px solid var(--hairline);
}

.upload-card :deep(.el-card__body) {
  padding: 24px;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  padding: 48px 24px;
  border-color: var(--hairline-strong);
  border-radius: 10px;
  background: var(--bg-quiet);
  transition: border-color 0.2s, background-color 0.2s;
}

.upload-area :deep(.el-upload-dragger:hover),
.upload-area :deep(.el-upload-dragger.is-dragover) {
  border-color: var(--primary);
  background: var(--bg-card);
}

.upload-area :deep(.el-icon--upload) {
  color: var(--primary-soft);
  font-size: 44px;
  margin-bottom: 12px;
}

.upload-area :deep(.el-upload__text) {
  font-size: 14px;
  color: var(--text-regular);
}

.upload-area :deep(.el-upload__text em) {
  color: var(--primary);
  font-style: normal;
  font-weight: 500;
}

.upload-area :deep(.el-upload__tip) {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.file-list-section {
  margin-top: 24px;
}

.file-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--text-secondary);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-regular);
}

.error-msg {
  font-size: 13px;
  color: var(--el-color-danger);
}

.success-msg {
  font-size: 13px;
  color: var(--el-color-success);
}

.progress-bar {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .upload-card :deep(.el-card__body) {
    padding: 16px;
  }

  .upload-area :deep(.el-upload-dragger) {
    padding: 28px 16px;
  }

  .upload-area :deep(.el-icon--upload) {
    font-size: 36px;
    margin-bottom: 8px;
  }

  .file-list-header {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
