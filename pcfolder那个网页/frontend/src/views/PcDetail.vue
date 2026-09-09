﻿<template>
  <div class="pc-detail" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="$router.push('/home/pc-list')">返回列表</el-button>
        <h2 v-if="!editing">{{ pcData.name || '角色卡详情' }}<span v-if="pcData.custom_fields?.name_note" class="header-name-note">{{ pcData.custom_fields.name_note }}</span></h2>
        <template v-else>
          <el-input v-model="pcData.name" placeholder="姓名" style="width: 200px" />
          <el-input v-model="pcData.custom_fields.name_note" placeholder="名字备注" style="width: 160px" />
        </template>
      </div>
      <div class="header-actions">
        <template v-if="!editing">
          <el-button type="primary" :icon="Edit" @click="editing = true">编辑</el-button>
          <el-button type="danger" :icon="Delete" @click="handleDelete">删除</el-button>
        </template>
        <template v-else>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </template>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 基本信息（含自定义字段） -->
      <el-col :span="24">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-flex">
              <span class="section-title">基本信息</span>
              <el-button v-if="editing" size="small" :icon="Plus" @click="customFieldDialogVisible = true">添加自定义字段</el-button>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <div class="field-row">
                <label>姓名</label>
                <el-input v-if="editing" v-model="pcData.name" size="small" />
                <span v-else>{{ pcData.name }}</span>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <div class="field-row">
                <label>性别</label>
                <el-input v-if="editing" v-model="pcData.gender" size="small" />
                <span v-else>{{ pcData.gender || '-' }}</span>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <div class="field-row">
                <label>年龄</label>
                <el-input-number v-if="editing" v-model="pcData.age" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ pcData.age || '-' }}</span>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <div class="field-row">
                <label>时代</label>
                <el-input v-if="editing" v-model="pcData.era" size="small" />
                <span v-else>{{ pcData.era || '-' }}</span>
              </div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <div class="field-row">
                <label>职业</label>
                <el-input v-if="editing" v-model="pcData.occupation" size="small" />
                <span v-else>{{ pcData.occupation || '-' }}</span>
              </div>
            </el-col>
          </el-row>
          <el-divider v-if="customFieldKeys.length > 0" />
          <el-row :gutter="20" v-if="customFieldKeys.length > 0">
            <el-col v-for="key in customFieldKeys" :key="key" :xs="12" :sm="8" :md="6" :lg="4" class="custom-field">
              <div class="field-row">
                <label>{{ key }}</label>
                <div class="custom-field-content">
                  <el-input v-if="editing" v-model="pcData.custom_fields[key]" size="small" />
                  <span v-else>{{ pcData.custom_fields[key] || '-' }}</span>
                  <el-button v-if="editing" type="danger" size="small" link @click="removeCustomField(key)">删除</el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 属性 -->
      <el-col :span="24">
        <el-card shadow="never" class="section-card">
          <template #header><span class="section-title">属性 (ATTRIBUTES)</span></template>
          <el-row :gutter="12">
            <el-col v-for="abbr in attributeKeys" :key="abbr" :xs="6" :sm="4" :md="3" :lg="2" class="attr-col">
              <div class="attr-item">
                <label>{{ attrLabel(abbr) }}</label>
                <el-input-number
                  v-if="editing"
                  v-model="pcData.attributes[abbr]"
                  size="small"
                  :min="0"
                  :max="abbr === 'san' ? 99 : abbr === 'db' ? undefined : 99"
                  controls-position="right"
                  style="width: 100%"
                />
                <span v-else class="attr-value">{{ pcData.attributes[abbr] ?? '-' }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 技能 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-flex">
          <span class="section-title">技能 (SKILLS)</span>
          <el-button v-if="editing" size="small" :icon="Plus" @click="openSkillDialog">添加新技能</el-button>
        </div>
      </template>
      <el-row :gutter="12">
        <el-col :xs="24" :md="12" class="skill-left-col">
          <el-table :data="skillLeftCol" border size="default" row-key="name" class="detail-table">
            <el-table-column v-if="editing" label="" width="50" align="center">
              <template #default="{ row }">
                <el-icon class="drag-handle" style="cursor: move"><DCaret /></el-icon>
              </template>
            </el-table-column>
            <el-table-column label="技能名" min-width="120">
              <template #default="{ row }">
                <el-input v-if="editing" v-model="row.name" size="small" @change="onSkillNameChange(row)" />
                <span v-else>{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="初始" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.initial" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.initial ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="成长" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.growth" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.growth ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="职业" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.occupation" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.occupation ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="兴趣" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.interest" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.interest ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="合计" width="70">
              <template #default="{ row }">
                <span>{{ row.value.total ?? (row.value.initial || 0) + (row.value.growth || 0) + (row.value.occupation || 0) + (row.value.interest || 0) }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="editing" label="操作" width="60">
              <template #default="{ row }">
                <el-button type="danger" size="small" link @click="removeSkill(row.name)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :xs="24" :md="12" class="skill-right-col">
          <el-table :data="skillRightCol" border size="default" row-key="name" class="detail-table">
            <el-table-column v-if="editing" label="" width="50" align="center">
              <template #default="{ row }">
                <el-icon class="drag-handle" style="cursor: move"><DCaret /></el-icon>
              </template>
            </el-table-column>
            <el-table-column label="技能名" min-width="120">
              <template #default="{ row }">
                <el-input v-if="editing" v-model="row.name" size="small" @change="onSkillNameChange(row)" />
                <span v-else>{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="初始" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.initial" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.initial ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="成长" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.growth" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.growth ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="职业" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.occupation" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.occupation ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="兴趣" width="70">
              <template #default="{ row }">
                <el-input-number v-if="editing" v-model="row.value.interest" size="small" :min="0" controls-position="right" style="width: 100%" />
                <span v-else>{{ row.value.interest ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="合计" width="70">
              <template #default="{ row }">
                <span>{{ row.value.total ?? (row.value.initial || 0) + (row.value.growth || 0) + (row.value.occupation || 0) + (row.value.interest || 0) }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="editing" label="操作" width="60">
              <template #default="{ row }">
                <el-button type="danger" size="small" link @click="removeSkill(row.name)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-card>

    <!-- 武器列表 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-flex">
          <span class="section-title">武器列表 (WEAPONS)</span>
          <el-button v-if="editing" size="small" :icon="Plus" @click="addWeapon">添加新武器</el-button>
        </div>
      </template>
      <el-table :data="pcData.weapons" border size="default" class="detail-table">
        <el-table-column label="武器名称" min-width="120">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.name" size="small" />
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.type" size="small" />
            <span v-else>{{ row.type || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="伤害" width="120">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.damage" size="small" />
            <span v-else>{{ row.damage || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="射程" width="100">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.range" size="small" />
            <span v-else>{{ row.range || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="120">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.note" size="small" />
            <span v-else>{{ row.note || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="editing" label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" size="small" link @click="pcData.weapons.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 随身物品 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-flex">
          <span class="section-title">随身物品 (ITEMS)</span>
          <el-button v-if="editing" size="small" :icon="Plus" @click="addItem">添加新物品</el-button>
        </div>
      </template>
      <el-table :data="pcData.items" border size="default" class="detail-table">
        <el-table-column label="位置" width="100">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.location" size="small" />
            <span v-else>{{ row.location || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.status" size="small" />
            <span v-else>{{ row.status || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物品名称" min-width="120">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.name" size="small" />
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="背包格" min-width="150">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.note" size="small" />
            <span v-else>{{ row.note || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="editing" label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" size="small" link @click="pcData.items.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 背景故事 -->
    <el-card shadow="never" class="section-card">
      <template #header><span class="section-title">背景故事 (BACKGROUND)</span></template>
      <template v-if="hasBackgroundText">
        <el-input
          v-if="editing"
          v-model="pcData.background.text"
          type="textarea"
          :rows="6"
          placeholder="请输入背景故事"
        />
        <div v-else class="bg-text">{{ pcData.background.text }}</div>
      </template>
      <template v-else>
        <el-row :gutter="16">
          <el-col v-for="key in backgroundKeys" :key="key" :xs="24" :md="12" class="bg-field">
            <label class="bg-label">{{ backgroundLabels[key] || key }}</label>
            <el-input
              v-if="editing"
              v-model="pcData.background[key]"
              type="textarea"
              :rows="3"
            />
            <div v-else class="bg-value">{{ pcData.background[key] || '-' }}</div>
          </el-col>
        </el-row>
      </template>
    </el-card>

    <!-- 经历模组 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-flex">
          <span class="section-title">经历模组 (EXPERIENCES)</span>
          <el-button v-if="editing" size="small" :icon="Plus" @click="addExperience">添加新经历模组</el-button>
        </div>
      </template>
      <el-row :gutter="12">
        <el-col v-for="(exp, idx) in pcData.experiences" :key="idx" :xs="24" :sm="12" :md="8" style="margin-bottom: 16px">
          <el-card shadow="hover" class="exp-card">
            <template #header>
              <div class="exp-card-header">
                <template v-if="editing">
                  <el-input v-model="exp.module_name" size="small" placeholder="模组名称" />
                  <el-button type="danger" size="small" link @click="pcData.experiences.splice(idx, 1)">删除</el-button>
                </template>
                <template v-else>
                  <span class="exp-title">{{ exp.module_name || '未命名' }}</span>
                  <span v-if="exp.ho" class="exp-ho">{{ exp.ho }}</span>
                  <el-tag v-if="exp.status" size="small" :type="statusTagType(exp.status)" class="exp-status-tag">{{ exp.status }}</el-tag>
                  <span v-if="formatExpDateRange(exp)" class="exp-date-inline">{{ formatExpDateRange(exp) }}</span>
                </template>
              </div>
            </template>
            <template v-if="editing">
              <div class="field-row" style="margin-bottom: 8px">
                <label>位置</label>
                <el-select v-model="exp.ho" size="small" style="width: 120px" allow-create filterable default-first-option clearable placeholder="选择或输入">
                  <el-option label="HO1" value="HO1" />
                  <el-option label="HO2" value="HO2" />
                  <el-option label="HO3" value="HO3" />
                  <el-option label="HO4" value="HO4" />
                </el-select>
              </div>
              <div class="field-row" style="margin-bottom: 8px">
                <label>开团</label>
                <div class="date-fields">
                  <el-input v-model="exp._startY" size="small" class="date-y" placeholder="年" maxlength="4" @input="syncDateParts(exp, 'start_date', 'start')" />
                  <el-select v-model="exp._startM" size="small" class="date-m" placeholder="月" clearable @change="syncDateParts(exp, 'start_date', 'start')">
                    <el-option v-for="m in 12" :key="m" :label="m" :value="m" />
                  </el-select>
                  <el-select v-model="exp._startD" size="small" class="date-d" placeholder="日" clearable @change="syncDateParts(exp, 'start_date', 'start')">
                    <el-option v-for="d in 31" :key="d" :label="d" :value="d" />
                  </el-select>
                </div>
              </div>
              <div class="field-row" style="margin-bottom: 8px">
                <label>结团</label>
                <div class="date-fields">
                  <el-input v-model="exp._endY" size="small" class="date-y" placeholder="年" maxlength="4" @input="syncDateParts(exp, 'end_date', 'end')" />
                  <el-select v-model="exp._endM" size="small" class="date-m" placeholder="月" clearable @change="syncDateParts(exp, 'end_date', 'end')">
                    <el-option v-for="m in 12" :key="m" :label="m" :value="m" />
                  </el-select>
                  <el-select v-model="exp._endD" size="small" class="date-d" placeholder="日" clearable @change="syncDateParts(exp, 'end_date', 'end')">
                    <el-option v-for="d in 31" :key="d" :label="d" :value="d" />
                  </el-select>
                </div>
              </div>
              <div class="field-row" style="margin-bottom: 8px">
                <label>状态</label>
                <el-select v-model="exp.status" size="small" style="width: 120px" allow-create filterable default-first-option clearable placeholder="选择或输入">
                  <el-option label="卫星中" value="卫星中" />
                  <el-option label="进行中" value="进行中" />
                  <el-option label="已结团" value="已结团" />
                  <el-option label="已散桌" value="已散桌" />
                  <el-option label="暂停中" value="暂停中" />
                </el-select>
              </div>
              <div class="field-row">
                <label>变更描述</label>
                <el-input v-model="exp.change_description" type="textarea" :rows="2" size="small" />
              </div>
            </template>
            <template v-else>
              <p class="exp-desc">{{ exp.change_description || '-' }}</p>
            </template>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="pcData.experiences.length === 0" description="暂无经历模组" />
    </el-card>

    <!-- 法术一览 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-flex">
          <span class="section-title">法术一览 (SPELLS)</span>
          <el-button v-if="editing" size="small" :icon="Plus" @click="addSpell">添加新法术</el-button>
        </div>
      </template>
      <el-table :data="pcData.spells" border size="default" class="detail-table">
        <el-table-column label="法术名称" min-width="120">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.name" size="small" />
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="消耗" width="120">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.cost" size="small" />
            <span v-else>{{ row.cost || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="效果描述" min-width="200">
          <template #default="{ row }">
            <el-input v-if="editing" v-model="row.effect" size="small" />
            <span v-else>{{ row.effect || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="editing" label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" size="small" link @click="pcData.spells.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加技能弹窗 -->
    <el-dialog v-model="skillDialogVisible" title="添加新技能" width="400px">
      <el-form label-width="80px">
        <el-form-item label="技能名称">
          <el-input v-model="newSkillName" placeholder="请输入技能名称" @keyup.enter="addSkill" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skillDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addSkill">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加自定义字段弹窗 -->
    <el-dialog v-model="customFieldDialogVisible" title="添加自定义字段" width="400px">
      <el-form label-width="80px">
        <el-form-item label="字段名">
          <el-input v-model="newCustomKey" placeholder="如：身高、体重" />
        </el-form-item>
        <el-form-item label="字段值">
          <el-input v-model="newCustomValue" placeholder="字段值" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="customFieldDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addCustomField">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Delete, Plus, DCaret } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { pcApi } from '../api/pc'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)

// 原始数据快照（取消编辑时恢复）
let snapshot = null

const pcData = reactive({
  id: null,
  pc_number: null,
  name: '',
  gender: '',
  age: null,
  era: '',
  occupation: '',
  attributes: {},
  skills: {},
  weapons: [],
  items: [],
  background: {},
  experiences: [],
  spells: [],
  custom_fields: {}
})

const ATTRIBUTE_LIST = ['str', 'con', 'siz', 'dex', 'app', 'int', 'pow', 'edu', 'luck', 'hp', 'san', 'db']

const ATTR_LABELS = {
  str: 'STR', con: 'CON', siz: 'SIZ', dex: 'DEX', app: 'APP', int: 'INT',
  pow: 'POW', edu: 'EDU', luck: 'LUCK', hp: 'HP', san: 'SAN', db: 'DB'
}

const ATTR_ORDER = ['str', 'con', 'siz', 'dex', 'app', 'int', 'pow', 'edu', 'luck', 'hp', 'san', 'db']

function attrLabel(abbr) {
  return ATTR_LABELS[abbr] || abbr.toUpperCase()
}

const backgroundLabels = {
  personal_description: '个人描述',
  ideology: '信仰/世界观',
  important_people: '重要人物',
  important_place: '重要地点',
  treasured_item: '珍视之物',
  traits: '特征',
  wounds_scars: '伤痕',
  text: '背景故事'
}

const attributeKeys = computed(() => {
  const keys = Object.keys(pcData.attributes || {})
  if (keys.length > 0) {
    // 按 ATTR_ORDER 排序，未知属性放最后
    return keys.sort((a, b) => {
      const ia = ATTR_ORDER.indexOf(a)
      const ib = ATTR_ORDER.indexOf(b)
      return (ia === -1 ? 999 : ia) - (ib === -1 ? 999 : ib)
    })
  }
  // 如果没有属性，返回默认列表用于编辑
  return editing.value ? ATTRIBUTE_LIST : []
})

const skillList = computed(() => {
  const skills = pcData.skills || {}
  let entries = Object.entries(skills).map(([name, value]) => ({ name, value }))
  // 非编辑模式下，隐藏成长、职业、兴趣均为 0 的技能
  if (!editing.value) {
    entries = entries.filter(
      (e) => (e.value.growth ?? 0) !== 0 || (e.value.occupation ?? 0) !== 0 || (e.value.interest ?? 0) !== 0
    )
  }
  return entries
})

// 左右双栏
const skillLeftCol = computed(() => {
  const half = Math.ceil(skillList.value.length / 2)
  return skillList.value.slice(0, half)
})

const skillRightCol = computed(() => {
  const half = Math.ceil(skillList.value.length / 2)
  return skillList.value.slice(half)
})

// 技能名修改：同步到 pcData.skills 的键
function onSkillNameChange(row) {
  const entries = skillList.value
  const newSkills = {}
  for (const e of entries) {
    newSkills[e.name] = e.value
  }
  pcData.skills = newSkills
}

// 同步拖拽后的顺序到 skillList
function syncSkillOrder(entries, newOrder) {
  const reordered = []
  for (const name of newOrder) {
    const entry = entries.find((e) => e.name === name)
    if (entry) reordered.push(entry)
  }
  skillList.value = reordered
  const newSkills = {}
  for (const e of reordered) {
    newSkills[e.name] = e.value
  }
  pcData.skills = newSkills
}

// 从 DOM 行中提取技能名（兼容 input 和纯文本）
function getRowName(tr) {
  const td = tr.children[1]
  if (!td) return ''
  const input = td.querySelector('input')
  if (input) return input.value.trim()
  return td.textContent.trim()
}

// 初始化拖拽
function initSortable() {
  const leftBody = document.querySelector('.skill-left-col .el-table__body tbody')
  const rightBody = document.querySelector('.skill-right-col .el-table__body tbody')
  const collectNames = () => {
    const leftNames = leftBody ? Array.from(leftBody.querySelectorAll('tr')).map(getRowName) : []
    const rightNames = rightBody ? Array.from(rightBody.querySelectorAll('tr')).map(getRowName) : []
    return [...leftNames, ...rightNames]
  }
  if (leftBody) {
    Sortable.create(leftBody, {
      handle: '.drag-handle',
      animation: 150,
      group: 'skills',
      onEnd() {
        syncSkillOrder(skillList.value, collectNames())
      }
    })
  }
  if (rightBody) {
    Sortable.create(rightBody, {
      handle: '.drag-handle',
      animation: 150,
      group: 'skills',
      onEnd() {
        syncSkillOrder(skillList.value, collectNames())
      }
    })
  }
}

const backgroundKeys = computed(() => {
  const keys = Object.keys(pcData.background || {}).filter((k) => k !== 'text')
  return keys
})

const hasBackgroundText = computed(() => {
  return !!pcData.background?.text
})

const customFieldKeys = computed(() => Object.keys(pcData.custom_fields || {}).filter((k) => k !== 'name_note'))

// 弹窗状态
const skillDialogVisible = ref(false)
const newSkillName = ref('')
const customFieldDialogVisible = ref(false)
const newCustomKey = ref('')
const newCustomValue = ref('')

function removeCustomField(key) {
  delete pcData.custom_fields[key]
  pcData.custom_fields = { ...pcData.custom_fields }
}

async function loadPcDetail() {
  loading.value = true
  try {
    const res = await pcApi.getPcDetail(route.params.id)
    Object.assign(pcData, res.data)
    // 确保 JSON 字段不为 null
    if (!pcData.attributes) pcData.attributes = {}
    if (!pcData.skills) pcData.skills = {}
    if (!pcData.weapons) pcData.weapons = []
    if (!pcData.items) pcData.items = []
    if (!pcData.background) pcData.background = {}
    if (!pcData.experiences) pcData.experiences = []
    if (!pcData.spells) pcData.spells = []
    if (!pcData.custom_fields) pcData.custom_fields = {}
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  if (snapshot) {
    Object.assign(pcData, JSON.parse(snapshot))
  }
  editing.value = false
}

async function handleSave() {
  saving.value = true
  try {
    // 清理日期临时字段后再提交
    stripDateParts()
    // 构建保存请求（只发送 PcSaveRequest 需要的字段）
    const saveData = {
      name: pcData.name,
      gender: pcData.gender,
      age: pcData.age,
      era: pcData.era,
      occupation: pcData.occupation,
      attributes: pcData.attributes,
      skills: pcData.skills,
      weapons: pcData.weapons,
      items: pcData.items,
      background: pcData.background,
      experiences: pcData.experiences,
      spells: pcData.spells,
      custom_fields: pcData.custom_fields
    }
    await pcApi.updatePc(pcData.id, saveData)
    ElMessage.success('保存成功')
    editing.value = false
    // 重新加载数据（获取服务端计算的 total 等）
    await loadPcDetail()
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此角色卡？此操作不可恢复。', '警告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await pcApi.deletePc(pcData.id)
    ElMessage.success('删除成功')
    router.push('/home/pc-list')
  } catch (err) {
    // 用户取消或错误已由拦截器处理
  }
}

function openSkillDialog() {
  newSkillName.value = ''
  skillDialogVisible.value = true
}

function addSkill() {
  const name = newSkillName.value.trim()
  if (!name) {
    ElMessage.warning('请输入技能名称')
    return
  }
  if (pcData.skills[name]) {
    ElMessage.warning('该技能已存在')
    return
  }
  pcData.skills[name] = { initial: 0, growth: 0, occupation: 0, interest: 0, total: 0 }
  skillDialogVisible.value = false
  newSkillName.value = ''
  ElMessage.success(`已添加技能：${name}`)
}

function removeSkill(name) {
  delete pcData.skills[name]
  // 触发响应式更新
  pcData.skills = { ...pcData.skills }
}

function addWeapon() {
  pcData.weapons.push({ name: '', type: '', damage: '', range: '', note: '' })
}

function addItem() {
  pcData.items.push({ name: '', location: '', status: '', note: '' })
}

function addExperience() {
  pcData.experiences.push({ module_name: '', ho: '', change_description: '', start_date: '', end_date: '', status: '' })
}

function statusTagType(status) {
  const map = { '卫星中': 'info', '进行中': 'success', '已结团': '', '已散桌': 'danger', '暂停中': 'warning' }
  return map[status] || 'info'
}

// ── 经历模组日期：手动输入年 + 月/日下拉，可只填年或年月 ──

// 进入编辑模式时：把存储的 start_date/end_date 拆成 _startY/_startM/_startD 临时字段
function initDateParts() {
  pcData.experiences.forEach((exp) => {
    parseDateToParts(exp, 'start_date', 'start')
    parseDateToParts(exp, 'end_date', 'end')
  })
}

function parseDateToParts(exp, dateKey, prefix) {
  const v = exp[dateKey] ? String(exp[dateKey]) : ''
  const y = v.slice(0, 4) || ''
  const m = v.length >= 7 ? parseInt(v.slice(5, 7), 10) : null
  const d = v.length >= 10 ? parseInt(v.slice(8, 10), 10) : null
  exp[`_${prefix}Y`] = y
  exp[`_${prefix}M`] = Number.isInteger(m) ? m : null
  exp[`_${prefix}D`] = Number.isInteger(d) ? d : null
}

// 年/月/日变化时重新拼装 start_date/end_date
// 年必须填；月/日留空则保存为 YYYY 或 YYYY-MM
function syncDateParts(exp, dateKey, prefix) {
  const y = (exp[`_${prefix}Y`] || '').toString().trim()
  if (!/^\d{1,4}$/.test(y)) {
    exp[dateKey] = ''
    return
  }
  const year = y.padStart(4, '0')
  const m = exp[`_${prefix}M`]
  const d = exp[`_${prefix}D`]
  if (m == null || m === '') {
    exp[dateKey] = year
  } else if (d == null || d === '') {
    exp[dateKey] = `${year}-${String(m).padStart(2, '0')}`
  } else {
    exp[dateKey] = `${year}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
}

// 保存前：清理临时字段，不随数据提交
function stripDateParts() {
  pcData.experiences.forEach((exp) => {
    delete exp._startY
    delete exp._startM
    delete exp._startD
    delete exp._endY
    delete exp._endM
    delete exp._endD
  })
}

// 格式化中文日期：值长度决定精度 4→x年 7→x年x月 10→x年x月x日
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

// 显示「x年x月x日 至 x年x月x日」，缺失的日期显示为 -
function formatExpDateRange(exp) {
  const hasStart = !!exp.start_date
  const hasEnd = !!exp.end_date
  if (!hasStart && !hasEnd) return ''
  const s = hasStart ? formatDateCN(exp.start_date) : '-'
  const e = hasEnd ? formatDateCN(exp.end_date) : '-'
  return `${s} 至 ${e}`
}

function addSpell() {
  pcData.spells.push({ name: '', cost: '', effect: '' })
}

function addCustomField() {
  const key = newCustomKey.value.trim()
  if (!key) {
    ElMessage.warning('请输入字段名')
    return
  }
  if (pcData.custom_fields[key] !== undefined) {
    ElMessage.warning('该字段已存在')
    return
  }
  pcData.custom_fields[key] = newCustomValue.value
  // 触发响应式
  pcData.custom_fields = { ...pcData.custom_fields }
  customFieldDialogVisible.value = false
  newCustomKey.value = ''
  newCustomValue.value = ''
  ElMessage.success(`已添加字段：${key}`)
}

// 监听编辑模式变化，保存快照
watch(editing, (val) => {
  if (val) {
    snapshot = JSON.stringify(pcData)
    initDateParts()
    nextTick(() => initSortable())
  }
})

onMounted(loadPcDetail)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  min-width: 0;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
}

.header-name-note {
  font-size: 13px;
  font-weight: normal;
  color: var(--text-secondary);
  margin-left: 6px;
  letter-spacing: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.section-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 1px solid var(--hairline);
}

.section-card :deep(.el-card__header) {
  padding: 16px 24px;
  background: var(--bg-quiet);
  border-bottom: 1px solid var(--hairline);
}

.section-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.exp-card {
  border: 1px solid var(--hairline);
  border-radius: 10px;
}

.exp-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid var(--hairline);
}

.exp-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.exp-card-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.exp-ho {
  font-size: 13px;
  color: var(--text-secondary);
}

.exp-status-tag {
  flex-shrink: 0;
}

.exp-date-inline {
  font-size: 13px;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

.date-fields {
  display: flex;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.date-fields .date-y {
  width: 64px;
  flex: 0 0 auto;
}

.date-fields .date-m {
  width: 68px;
  flex: 0 0 auto;
}

.date-fields .date-d {
  width: 68px;
  flex: 0 0 auto;
}

.exp-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--ink);
  letter-spacing: 0.5px;
}

.exp-desc {
  font-size: 13px;
  color: var(--text-regular);
  margin: 0;
  line-height: 1.7;
  white-space: pre-wrap;
}

.exp-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.exp-date {
  font-size: 12px;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

.section-title {
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 2px;
  color: var(--text-secondary);
}

.detail-table :deep(.el-table__cell) {
  font-size: 15px;
}

.detail-table :deep(.el-table__header .cell) {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--text-secondary);
}

.card-header-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.field-row label {
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--text-secondary);
  white-space: nowrap;
  min-width: 40px;
}

.attr-col {
  margin-bottom: 12px;
}

.attr-item {
  text-align: center;
}

.attr-item label {
  display: block;
  font-size: 12px;
  letter-spacing: 1.5px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.attr-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.bg-field {
  margin-bottom: 16px;
}

.bg-label {
  display: block;
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.bg-value {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.7;
  white-space: pre-wrap;
}

.bg-text {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.9;
  white-space: pre-wrap;
}

.custom-field {
  margin-bottom: 12px;
}

.custom-field-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

@media (max-width: 768px) {
  .page-header h2 {
    font-size: 20px;
  }

  .section-card :deep(.el-card__body) {
    padding: 16px;
  }

  .section-card :deep(.el-card__header) {
    padding: 12px 16px;
  }
}
</style>
