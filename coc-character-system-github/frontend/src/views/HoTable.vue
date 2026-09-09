﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="ho-table-page">
    <div class="page-header">
      <h2>HO 表</h2>
      <div class="header-actions">
        <el-button :icon="Document" @click="exportJson" :loading="exportingJson">导出 JSON</el-button>
        <el-button type="primary" :icon="Download" @click="exportImage" :loading="exporting">导出为图片</el-button>
      </div>
    </div>

    <div ref="tableRef" v-loading="loading" element-loading-text="加载中..." class="ho-table-container">
      <div class="ho-columns">
        <div v-for="col in columns" :key="col.key" class="ho-column">
          <div class="ho-column-header">{{ col.label }}</div>
          <div class="ho-column-body">
            <div v-for="pc in col.cards" :key="pc.id" class="ho-pc-card" @click="goDetail(pc.id)">
              <div class="ho-pc-name">
                {{ pc.expModule || '未命名' }}
                <el-tag v-if="pc.expStatus" size="small" :type="statusTagType(pc.expStatus)" class="ho-status-tag">{{ pc.expStatus }}</el-tag>
              </div>
              <div class="ho-pc-exp">
                <span v-if="pc.gender" class="gender-icon">{{ pc.gender === '男' ? '♂' : '♀' }}</span>
                <span v-if="pc.name">{{ pc.name }}</span>
              </div>
            </div>
            <div v-if="col.cards.length === 0" class="ho-empty">无</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Document } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import { pcApi } from '../api/pc'

const router = useRouter()
const tableRef = ref()
const exporting = ref(false)
const exportingJson = ref(false)
const loading = ref(false)
const allPcs = ref([])

onMounted(async () => {
  await loadPcs()
})

async function loadPcs() {
  loading.value = true
  try {
    const res = await pcApi.getPcList()
    const list = res.data?.data || res.data || []
    // 获取每张卡的完整数据以读取经历模组
    const details = await Promise.all(
      list.map(pc => pcApi.getPcDetail(pc.id).then(r => r.data?.data || r.data).catch(() => null))
    )
    allPcs.value = details.filter(d => d)
  } catch (err) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 按 HO 分列，一个角色多个模组全部列出
const columns = computed(() => {
  const colMap = new Map() // ho -> entries
  const noHoEntries = []

  allPcs.value.forEach(pc => {
    const exps = pc.experiences || []
    if (exps.length === 0) {
      // 无经历模组的角色归入无HO
      noHoEntries.push({
        id: pc.id,
        name: pc.name,
        gender: pc.gender,
        expModule: '',
        expStatus: '',
        startDate: ''
      })
      return
    }
    // 遍历所有经历模组
    exps.forEach(exp => {
      const entry = {
        id: pc.id,
        name: pc.name,
        gender: pc.gender,
        expModule: exp.module_name || '',
        expStatus: exp.status || '',
        startDate: exp.start_date || ''
      }
      if (exp.ho) {
        if (!colMap.has(exp.ho)) colMap.set(exp.ho, [])
        colMap.get(exp.ho).push(entry)
      } else {
        noHoEntries.push(entry)
      }
    })
  })

  // 按 HO 名称排序
  const sortedKeys = [...colMap.keys()].sort()
  const cols = sortedKeys.map(ho => ({
    key: ho,
    label: ho,
    cards: colMap.get(ho).sort((a, b) => {
      // 按开团时间排序（空值排最后）
      if (!a.startDate && !b.startDate) return 0
      if (!a.startDate) return 1
      if (!b.startDate) return -1
      return a.startDate.localeCompare(b.startDate)
    })
  }))
  if (noHoEntries.length > 0) {
    cols.push({
      key: 'no-ho',
      label: '无HO',
      cards: noHoEntries.sort((a, b) => {
        if (!a.startDate && !b.startDate) return 0
        if (!a.startDate) return 1
        if (!b.startDate) return -1
        return a.startDate.localeCompare(b.startDate)
      })
    })
  }
  return cols
})

function statusTagType(status) {
  const map = { '卫星中': 'info', '进行中': 'success', '已结团': '', '已散桌': 'danger', '暂停中': 'warning' }
  return map[status] || 'info'
}

function goDetail(id) {
  router.push(`/home/pc-detail/${id}`)
}

function exportJson() {
  exportingJson.value = true
  try {
    const colorPalette = [0, 1, 2, 3, 5, 6, 7, 8, 9, 10]
    let colorIdx = 0
    const jsonColumns = columns.value.map((col) => {
      const entries = col.cards.map((pc) => {
        const genderSym = pc.gender === '男' ? '♂' : pc.gender === '女' ? '♀' : ''
        return {
          module: pc.expModule || '',
          character: genderSym + (pc.name || '未命名'),
          status: pc.expStatus || ''
        }
      })
      return {
        name: col.label,
        colorIdx: colorPalette[colorIdx++ % colorPalette.length],
        entries
      }
    })
    const nextColorIdx = colorIdx
    const data = {
      version: 4,
      columns: jsonColumns,
      nextColorIdx
    }
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const link = document.createElement('a')
    link.download = 'HO表.json'
    link.href = URL.createObjectURL(blob)
    link.click()
    ElMessage.success('JSON 导出成功')
  } catch (err) {
    ElMessage.error('导出失败')
  } finally {
    exportingJson.value = false
  }
}

async function exportImage() {
  if (!tableRef.value) return
  exporting.value = true
  try {
    const canvas = await html2canvas(tableRef.value, {
      backgroundColor: '#f4f3ef',
      scale: 2
    })
    const link = document.createElement('a')
    link.download = 'HO表.png'
    link.href = canvas.toDataURL('image/png')
    link.click()
    ElMessage.success('导出成功')
  } catch (err) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.ho-table-container {
  overflow-x: auto;
  padding-bottom: 8px;
}

.ho-columns {
  display: flex;
  gap: 14px;
  min-height: 400px;
  align-items: flex-start;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 8px;
}

.ho-column {
  min-width: 180px;
  flex: 0 0 auto;
  background: var(--bg-card);
  border: 1px solid var(--hairline);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ho-column-header {
  background: #3d5f55;
  color: #f7f6f2;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 2px;
  padding: 12px 16px;
  border-bottom: none;
  text-align: center;
}

.ho-column-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.ho-pc-card {
  background: var(--bg-quiet);
  border: 1px solid var(--hairline);
  border-radius: 8px;
  padding: 10px 14px;
  cursor: pointer;
  transition: border-color 0.2s, background-color 0.2s, box-shadow 0.2s;
}

.ho-pc-card:hover {
  background: var(--bg-card);
  border-color: var(--hairline-strong);
  box-shadow: var(--shadow-hover);
}

.ho-pc-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.ho-status-tag {
  flex-shrink: 0;
  margin-left: 2px;
}

.gender-icon {
  font-size: 13px;
  color: var(--text-secondary);
}

.ho-pc-info {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.ho-pc-exp {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-regular);
  margin-top: 4px;
  flex-wrap: wrap;
}

.ho-empty {
  text-align: center;
  color: var(--text-placeholder);
  font-size: 13px;
  padding: 28px 0;
  letter-spacing: 2px;
}

@media (max-width: 768px) {
  .page-header h2 {
    font-size: 20px;
  }

  .page-header {
    gap: 10px;
  }

  .header-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .ho-column {
    min-width: 150px;
  }

  .ho-column-header {
    font-size: 13px;
    padding: 10px 12px;
  }

  .ho-pc-card {
    padding: 8px 10px;
  }
}
</style>
