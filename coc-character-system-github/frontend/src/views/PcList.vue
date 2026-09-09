﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="pc-list">
    <div class="page-header">
      <h2>角色卡列表</h2>
      <div class="header-actions">
        <el-button
          :type="sortMode ? 'primary' : 'default'"
          :icon="sortMode ? Check : DCaret"
          @click="toggleSortMode"
        >{{ sortMode ? '完成排序' : '手动排序' }}</el-button>
        <el-popover placement="bottom" :width="280" trigger="click">
          <template #reference>
            <el-button :icon="Setting">自定义显示字段</el-button>
          </template>
          <div class="column-selector">
            <p class="selector-title">勾选要显示的字段</p>
            <el-checkbox-group v-model="selectedFields">
              <el-checkbox
                v-for="col in allColumns"
                :key="col.prop"
                :label="col.prop"
              >
                {{ col.label }}
              </el-checkbox>
            </el-checkbox-group>
            <el-divider />
            <p class="selector-title">已添加自定义字段</p>
            <div v-if="customFieldList.length > 0" class="custom-field-list">
              <el-tag
                v-for="f in customFieldList"
                :key="f"
                closable
                size="small"
                @close="removeCustomField(f)"
                style="margin: 0 4px 4px 0"
              >
                {{ f }}
              </el-tag>
            </div>
            <div v-else class="empty-hint">暂无自定义字段</div>
            <div class="custom-field-search">
              <el-input
                v-model="customFieldInput"
                placeholder="输入字段名搜索"
                size="small"
                clearable
                @keyup.enter="searchCustomField"
              >
                <template #append>
                  <el-button :icon="Search" @click="searchCustomField" />
                </template>
              </el-input>
            </div>
            <div class="selector-actions">
              <el-button size="small" @click="resetFields">恢复默认</el-button>
              <el-button size="small" type="primary" @click="saveFields">保存</el-button>
            </div>
          </div>
        </el-popover>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建角色卡</el-button>
      </div>
    </div>

    <div v-loading="loading" ref="cardGridRef" class="card-grid" :class="{ 'sort-mode': sortMode }">
      <el-card
        v-for="pc in pcList"
        :key="pc.id"
        :data-id="pc.id"
        shadow="hover"
        class="pc-card"
        @click="handleCardClick(pc)"
      >
        <template #header>
          <div class="card-header">
            <span class="pc-name">
              {{ pc.name || '未命名' }}
              <span v-if="pc.gender" class="gender-icon">{{ genderSymbol(pc.gender) }}</span>
              <span v-if="getNameNote(pc)" class="name-note">{{ getNameNote(pc) }}</span>
            </span>
            <span v-if="sortMode" class="card-drag-handle" title="拖动调整顺序">
              <el-icon><Rank /></el-icon>
            </span>
          </div>
        </template>
        <div class="card-body">
          <div class="tag-row">
            <el-tag
              v-for="col in standardDisplayFields.filter((c) => c.prop !== 'experience_modules' && c.prop !== 'experience_modules_detail')"
              :key="col.prop"
              class="tag-default"
            >
              <template v-if="col.prop === 'updated_at' || col.prop === 'created_at'">{{ formatDate(pc[col.prop]) }}</template>
              <template v-else>{{ pc[col.prop] ?? '-' }}</template>
            </el-tag>
            <el-tag
              v-for="f in customFieldList"
              :key="f"
              v-show="pcIdsWithField(f).length > 0"
              class="tag-default"
            >{{ getCustomFieldValue(pc, f) }}</el-tag>
          </div>
          <div v-if="standardDisplayFields.some((c) => c.prop === 'experience_modules') && getExperienceList(pc).length > 0" class="tag-row">
            <el-tag
              v-for="(exp, idx) in getExperienceList(pc)"
              :key="'exp-' + idx"
              class="tag-experience"
            >{{ exp }}</el-tag>
          </div>
          <div v-if="standardDisplayFields.some((c) => c.prop === 'experience_modules_detail') && getExperienceDetailList(pc).length > 0" class="exp-detail-list">
            <div v-for="(exp, idx) in getExperienceDetailList(pc)" :key="'expd-' + idx" class="exp-detail-item">
              <div class="exp-detail-line1">
                <span class="exp-detail-name">{{ exp.name || '未命名' }}</span>
                <span v-if="exp.ho" class="exp-detail-ho">{{ exp.ho }}</span>
                <span v-if="exp.status" class="exp-detail-status">{{ exp.status }}</span>
              </div>
              <div v-if="exp.dateRange" class="exp-detail-line2">{{ exp.dateRange }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!loading && pcList.length === 0" description="暂无角色卡，点击「新建角色卡」创建" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Setting, Plus, Search, DCaret, Rank, Check } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { pcApi } from '../api/pc'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const pcList = ref([])

// 自定义字段搜索
const customFieldInput = ref('')
// 完整角色卡数据缓存（用于取自定义字段值）
const pcDetailCache = ref({})
// 每个自定义字段对应的含此字段的角色卡 ID 列表
const customFieldPcIds = ref({})

// 所有可选字段
const allColumns = [
  { prop: 'gender', label: '性别' },
  { prop: 'age', label: '年龄' },
  { prop: 'era', label: '年代' },
  { prop: 'occupation', label: '职业' },
  { prop: 'experience_modules', label: '经历模组（简略）' },
  { prop: 'experience_modules_detail', label: '经历模组（详情）' },
  { prop: 'created_at', label: '创建时间' },
  { prop: 'updated_at', label: '更新时间' }
]

const defaultFields = ['gender', 'age', 'era', 'occupation']

// 所有标准字段的 prop 集合
const standardProps = new Set(allColumns.map((c) => c.prop))

const selectedFields = ref([...defaultFields])

const savedFields = computed(() => {
  const fields = userStore.user?.display_fields
  if (fields && Array.isArray(fields) && fields.length > 0) {
    return fields
  }
  return defaultFields
})

// selectedFields 中的标准字段（排除 gender，已在头部展示）
const standardDisplayFields = computed(() => {
  return selectedFields.value
    .filter((prop) => standardProps.has(prop) && prop !== 'gender')
    .map((prop) => allColumns.find((col) => col.prop === prop))
    .filter(Boolean)
})

// selectedFields 中的自定义字段（不在 allColumns 中的）
const customFieldList = computed(() => {
  return selectedFields.value.filter((prop) => !standardProps.has(prop))
})

function pcIdsWithField(field) {
  return customFieldPcIds.value[field] || []
}

function genderSymbol(gender) {
  if (!gender) return ''
  const g = gender.toLowerCase()
  if (g === '男' || g === 'male' || g === 'm') return '\u2642'
  if (g === '女' || g === 'female' || g === 'f') return '\u2640'
  return ''
}

async function loadPcList() {
  loading.value = true
  try {
    const res = await pcApi.getPcList()
    pcList.value = res.data || []
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
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

function handleCardClick(pc) {
  if (sortMode.value) {
    ElMessage.info('排序模式中，请拖动卡片标题栏右侧图标调整顺序')
    return
  }
  router.push(`/home/pc-detail/${pc.id}`)
}

// ── 手动排序（SortableJS 拖拽，顺序自动保存到后端） ──
const cardGridRef = ref(null)
const sortMode = ref(false)
let cardSortable = null

function toggleSortMode() {
  sortMode.value = !sortMode.value
  if (sortMode.value) {
    nextTick(() => initCardSortable())
  } else {
    destroyCardSortable()
  }
}

function destroyCardSortable() {
  if (cardSortable) {
    cardSortable.destroy()
    cardSortable = null
  }
}

function initCardSortable() {
  const grid = cardGridRef.value
  if (!grid) return
  destroyCardSortable()
  cardSortable = Sortable.create(grid, {
    handle: '.card-drag-handle',
    draggable: '.pc-card',
    animation: 150,
    onEnd: handleCardDragEnd
  })
}

// 拖拽结束后：从 DOM 读取新顺序 → 同步 pcList → 保存到后端
function handleCardDragEnd() {
  const grid = cardGridRef.value
  if (!grid) return
  const ids = Array.from(grid.querySelectorAll('.pc-card')).map((el) => Number(el.dataset.id))
  if (ids.length !== pcList.value.length || ids.some((id) => !id)) return

  const oldIds = pcList.value.map((pc) => pc.id)
  if (ids.join(',') === oldIds.join(',')) return // 顺序未变

  const byId = new Map(pcList.value.map((pc) => [pc.id, pc]))
  pcList.value = ids.map((id) => byId.get(id)).filter(Boolean)
  savePcOrder(ids)
}

async function savePcOrder(ids) {
  try {
    await pcApi.reorderPcs(ids)
  } catch (err) {
    // 错误已由拦截器处理
  }
}

onUnmounted(() => {
  destroyCardSortable()
})

async function handleCreate() {
  try {
    const res = await pcApi.createPc({ name: '新角色卡' })
    ElMessage.success('角色卡已创建')
    router.push(`/home/pc-detail/${res.data.id}`)
  } catch (err) {
    // 错误已由拦截器处理
  }
}

function resetFields() {
  selectedFields.value = [...defaultFields]
}

async function saveFields() {
  try {
    await pcApi.updateDisplayFields(selectedFields.value)
    if (userStore.user) {
      userStore.user.display_fields = [...selectedFields.value]
      localStorage.setItem('user', JSON.stringify(userStore.user))
    }
    ElMessage.success('显示字段配置已保存')
  } catch (err) {
    // 错误已由拦截器处理
  }
}

async function searchCustomField() {
  const field = customFieldInput.value.trim()
  if (!field) {
    ElMessage.warning('请输入字段名')
    return
  }
  try {
    const res = await pcApi.searchField(field)
    const ids = res.data || []
    customFieldPcIds.value[field] = ids

    // 加载有此字段的卡的完整数据，以获取字段值
    for (const pcId of ids) {
      if (!pcDetailCache.value[pcId]) {
        const detailRes = await pcApi.getPcDetail(pcId)
        pcDetailCache.value[pcId] = detailRes.data
      }
    }

    if (ids.length === 0) {
      ElMessage.info(`没有角色卡包含字段"${field}"`)
    } else {
      // 添加到 selectedFields（去重）
      if (!selectedFields.value.includes(field)) {
        selectedFields.value.push(field)
      }
      ElMessage.success(`找到 ${ids.length} 张卡包含字段"${field}"，已添加到显示字段`)
    }
    customFieldInput.value = ''
  } catch (err) {
    // 错误已由拦截器处理
  }
}

function removeCustomField(field) {
  selectedFields.value = selectedFields.value.filter((f) => f !== field)
  delete customFieldPcIds.value[field]
}

function getCustomFieldValue(pc, field) {
  if (!field) return '-'
  const detail = pcDetailCache.value[pc.id]
  if (!detail) return '-'
  const target = field.toLowerCase()
  // 在 custom_fields 中查找
  const cf = detail.custom_fields || {}
  for (const key of Object.keys(cf)) {
    if (key.toLowerCase() === target) return cf[key] ?? '-'
  }
  // 在 background 中查找
  const bg = detail.background || {}
  for (const key of Object.keys(bg)) {
    if (key.toLowerCase() === target) return bg[key] ?? '-'
  }
  // 在 attributes 中查找
  const attr = detail.attributes || {}
  for (const key of Object.keys(attr)) {
    if (key.toLowerCase() === target) return attr[key] ?? '-'
  }
  return '-'
}

// 获取名字备注
function getNameNote(pc) {
  const detail = pcDetailCache.value[pc.id]
  if (!detail) return ''
  return detail.custom_fields?.name_note || ''
}

// 获取经历模组列表（每条一个元素：模组名(位置)）
function getExperienceList(pc) {
  const detail = pcDetailCache.value[pc.id]
  if (!detail || !detail.experiences || detail.experiences.length === 0) return []
  return detail.experiences
    .map((e) => {
      const name = e.module_name || ''
      const ho = e.ho || ''
      if (name && ho) return `${name}（${ho}）`
      return name || ''
    })
    .filter((n) => n)
}

// 获取经历模组名称及位置（逗号分隔，用于旧引用兼容）
function getExperienceModules(pc) {
  const list = getExperienceList(pc)
  return list.length > 0 ? list.join(', ') : '-'
}

// 获取经历模组详情列表（每条含 name/ho/status/dateRange）
function getExperienceDetailList(pc) {
  const detail = pcDetailCache.value[pc.id]
  if (!detail || !detail.experiences || detail.experiences.length === 0) return []
  return detail.experiences.map((e) => {
    const dateRange = formatExpDateRange(e)
    return {
      name: e.module_name || '',
      ho: e.ho || '',
      status: e.status || '',
      dateRange
    }
  }).filter(e => e.name || e.ho || e.status || e.dateRange)
}

// 按值长度格式化中文日期：4→x年 7→x年x月 10→x年x月x日
function formatDateCN(value) {
  if (!value) return ''
  const v = String(value)
  const year = v.slice(0, 4)
  if (v.length <= 4) return `${year}年`
  const month = parseInt(v.slice(5, 7), 10)
  if (v.length <= 7) return `${year}年${month}月`
  const day = parseInt(v.slice(8, 10), 10)
  return `${year}年${month}月${day}日`
}

// 「x年x月x日 至 x年x月x日」，缺失日期显示为 -
function formatExpDateRange(e) {
  const hasStart = !!e.start_date
  const hasEnd = !!e.end_date
  if (!hasStart && !hasEnd) return ''
  const s = hasStart ? formatDateCN(e.start_date) : '-'
  const end = hasEnd ? formatDateCN(e.end_date) : '-'
  return `${s} 至 ${end}`
}

function statusTagType(status) {
  const map = { '卫星中': 'info', '进行中': 'success', '已结团': '', '已散桌': 'danger', '暂停中': 'warning' }
  return map[status] || 'info'
}

// 加载所有角色卡的完整数据（用于名字备注和自定义字段）
async function loadAllDetails() {
  for (const pc of pcList.value) {
    if (!pcDetailCache.value[pc.id]) {
      try {
        const detailRes = await pcApi.getPcDetail(pc.id)
        pcDetailCache.value[pc.id] = detailRes.data
      } catch (err) {
        // 错误已由拦截器处理
      }
    }
  }
}

// 加载自定义字段数据（从已保存的 display_fields 中识别自定义字段并搜索）
async function loadCustomFields() {
  const customFields = selectedFields.value.filter((f) => !standardProps.has(f))
  for (const field of customFields) {
    if (customFieldPcIds.value[field]) continue // 已加载
    try {
      const res = await pcApi.searchField(field)
      const ids = res.data || []
      customFieldPcIds.value[field] = ids
    } catch (err) {
      // 错误已由拦截器处理
    }
  }
}

onMounted(async () => {
  // 拉取最新用户信息（含 display_fields），保证刷新后配置恢复
  try {
    await userStore.fetchCurrentUser()
  } catch (err) {
    // 静默失败，使用本地缓存
  }
  selectedFields.value = [...savedFields.value]
  await loadPcList()
  await loadAllDetails()
  await loadCustomFields()
})
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

/* 瀑布流布局：CSS 多列，卡片高度不一自动填列 */
.card-grid {
  column-count: 4;
  column-gap: 16px;
  min-height: 120px;
}

.pc-card {
  break-inside: avoid;
  page-break-inside: avoid;
  margin: 0 0 16px;
  cursor: pointer;
  border-radius: 12px;
  border: 1px solid var(--hairline);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.pc-card:hover {
  transform: translateY(-2px);
  border-color: var(--hairline-strong);
  box-shadow: var(--shadow-hover);
}

/* ── 手动排序 ── */
.sort-mode .pc-card {
  cursor: default;
}

.card-drag-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 6px;
  cursor: grab;
  color: var(--text-secondary);
  flex-shrink: 0;
  transition: color 0.2s, background-color 0.2s;
}

.card-drag-handle:hover {
  color: var(--primary);
  background-color: var(--bg-page);
}

.card-drag-handle:active {
  cursor: grabbing;
}

.card-grid .sortable-ghost {
  opacity: 0.35;
}

.pc-card :deep(.el-card__header) {
  padding: 16px 20px 12px;
  border-bottom: none;
}

.pc-card :deep(.el-card__body) {
  padding: 0 20px 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pc-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--ink);
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.gender-icon {
  font-size: 15px;
  color: var(--text-secondary);
}

.name-note {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: normal;
  letter-spacing: 0;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-default {
  margin: 0;
  height: auto;
  line-height: 1.6;
  padding: 3px 10px;
  white-space: nowrap;
  font-size: 13px;
  background-color: var(--bg-page);
  border-color: transparent;
  color: var(--text-regular);
  border-radius: 4px;
}

.tag-experience {
  margin: 0;
  height: auto;
  line-height: 1.6;
  padding: 3px 10px;
  white-space: nowrap;
  font-size: 13px;
  background-color: #3d5f55;
  border-color: #3d5f55;
  color: #f7f6f2;
  border-radius: 4px;
}

.exp-detail-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exp-detail-item {
  background: #3d5f55;
  border-radius: 8px;
  padding: 10px 14px;
  color: #f7f6f2;
  font-size: 13px;
  width: 100%;
}

.exp-detail-line1 {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.exp-detail-name {
  font-weight: 500;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.exp-detail-ho {
  font-size: 14px;
  opacity: 0.7;
}

.exp-detail-status {
  font-size: 14px;
  opacity: 0.75;
}

.exp-detail-line2 {
  font-size: 13px;
  margin-top: 4px;
  opacity: 0.6;
  font-variant-numeric: tabular-nums;
}

.column-selector .selector-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.column-selector .el-checkbox {
  display: block;
  margin-left: 0;
  margin-bottom: 6px;
}

.custom-field-list {
  margin-bottom: 10px;
}

.empty-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.custom-field-search {
  margin-bottom: 12px;
}

.selector-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 14px;
  border-top: 1px solid var(--hairline);
  padding-top: 12px;
}

@media (max-width: 1400px) {
  .card-grid {
    column-count: 3;
  }
}

@media (max-width: 1000px) {
  .card-grid {
    column-count: 2;
  }
}

@media (max-width: 680px) {
  .card-grid {
    column-count: 1;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .page-header h2 {
    font-size: 20px;
  }

  .header-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  /* 自定义字段弹窗全宽 */
  .pc-list :deep(.el-popover.el-popper) {
    width: 92vw !important;
    max-width: 320px !important;
  }
}
</style>
