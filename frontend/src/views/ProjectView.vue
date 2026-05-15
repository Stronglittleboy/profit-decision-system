<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight } from '@element-plus/icons-vue'
import {
  fetchProjectList,
  createProject,
  updateProject,
  deleteProject,
  transitionProject,
  toggleProjectEnabled,
  fetchProjectPnl,
  type ProjectVO,
  type ProjectForm,
  type ProjectPnlVO,
} from '@/api/project'

const loading = ref(false)
const list = ref<ProjectVO[]>([])
const keyword = ref('')
const statusFilter = ref('')

const counts = computed(() => {
  const all = list.value.length
  const executing = list.value.filter((p) => p.status === 'executing').length
  const planning = list.value.filter((p) => p.status === 'planning').length
  const completed = list.value.filter((p) => p.status === 'completed').length
  return { all, executing, planning, completed }
})

async function loadList() {
  loading.value = true
  try {
    const res = await fetchProjectList(keyword.value || undefined, statusFilter.value || undefined)
    list.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = ''
  loadList()
}

// --- 新增/编辑弹窗 ---
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const formData = reactive<ProjectForm>({
  code: '',
  name: '',
  budget: undefined,
  startDate: '',
  endDate: '',
  manager: '',
  description: '',
})

const formRules = {
  code: [
    { required: true, message: '请输入项目编号', trigger: 'blur' },
    { max: 50, message: '最多 50 个字符', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 100, message: '最多 100 个字符', trigger: 'blur' },
  ],
  budget: [{ required: true, message: '请输入总预算', trigger: 'blur' }],
  manager: [{ max: 50, message: '最多 50 个字符', trigger: 'blur' }],
  description: [{ max: 500, message: '最多 500 个字符', trigger: 'blur' }],
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  Object.assign(formData, {
    code: '',
    name: '',
    budget: undefined,
    startDate: '',
    endDate: '',
    manager: '',
    description: '',
  })
  dialogVisible.value = true
}

function openEdit(row: ProjectVO) {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(formData, {
    code: row.code,
    name: row.name,
    budget: row.budget,
    startDate: row.startDate ?? '',
    endDate: row.endDate ?? '',
    manager: row.manager ?? '',
    description: row.description ?? '',
  })
  dialogVisible.value = true
}

const submitting = ref(false)
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      await updateProject(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createProject(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    submitting.value = false
  }
}

// --- 删除 ---
async function handleDelete(row: ProjectVO) {
  await ElMessageBox.confirm(`确认删除项目「${row.name}」？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await deleteProject(row.id)
  ElMessage.success('删除成功')
  await loadList()
}

// --- 状态流转 ---
const transitionLabels: Record<string, string> = {
  start: '启动',
  complete: '完成',
  suspend: '暂停',
  resume: '恢复',
}

function getAvailableActions(status: string): string[] {
  switch (status) {
    case 'planning':
      return ['start']
    case 'executing':
      return ['complete', 'suspend']
    case 'suspended':
      return ['resume']
    default:
      return []
  }
}

async function handleTransition(row: ProjectVO, action: string) {
  const label = transitionLabels[action] ?? action
  await ElMessageBox.confirm(`确认将项目「${row.name}」${label}？`, '操作确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info',
  })
  await transitionProject(row.id, action)
  ElMessage.success(`${label}成功`)
  await loadList()
}

// --- 启停 ---
async function handleToggleEnabled(row: ProjectVO, val: boolean) {
  try {
    await toggleProjectEnabled(row.id, val)
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {
    row.enabled = !val
  }
}

// --- 辅助 ---
function statusTagType(status: string) {
  switch (status) {
    case 'planning':
      return 'warning'
    case 'executing':
      return 'success'
    case 'completed':
      return 'info'
    case 'suspended':
      return 'danger'
    default:
      return ''
  }
}

function formatMoney(val: number) {
  return val?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '0.00'
}

function formatDateRange(start: string | null, end: string | null) {
  if (start && end) return `${start} ~ ${end}`
  if (start) return `${start} ~`
  if (end) return `~ ${end}`
  return '-'
}

// --- 盈亏明细 ---
const pnlVisible = ref(false)
const pnlLoading = ref(false)
const pnlData = ref<ProjectPnlVO | null>(null)
async function openPnl(row: ProjectVO) {
  pnlVisible.value = true
  pnlLoading.value = true
  try {
    const res = await fetchProjectPnl(row.id)
    pnlData.value = res.data.data ?? null
  } finally { pnlLoading.value = false }
}

onMounted(() => loadList())
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>项目管理</h2>
        <p class="sub">管理项目全生命周期 — 规划、执行、完成、暂停</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增项目</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="搜索">
          <el-input
            v-model="keyword"
            placeholder="编号或名称"
            clearable
            style="width: 200px"
            @keyup.enter="loadList"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" clearable placeholder="全部" style="width: 120px">
            <el-option label="规划中" value="planning" />
            <el-option label="进行中" value="executing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已暂停" value="suspended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="loadList">搜索</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <div class="summary-cards">
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">全部</div>
        <div class="summary-value">{{ counts.all }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">进行中</div>
        <div class="summary-value" style="color: #67c23a">{{ counts.executing }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">规划中</div>
        <div class="summary-value" style="color: #e6a23c">{{ counts.planning }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">已完成</div>
        <div class="summary-value" style="color: #909399">{{ counts.completed }}</div>
      </el-card>
    </div>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="code" label="编号" width="150" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预算" width="140" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.budget) }}
          </template>
        </el-table-column>
        <el-table-column label="起止日期" width="220">
          <template #default="{ row }">
            {{ formatDateRange(row.startDate, row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="利润" width="140" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.totalProfit >= 0 ? '#67c23a' : '#f56c6c' }">¥{{ formatMoney(row.totalProfit ?? 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预算执行" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="(row.budgetExecutionRate ?? 0) > 100 ? 'danger' : (row.budgetExecutionRate ?? 0) > 80 ? 'warning' : 'success'" size="small">{{ row.budgetExecutionRate ?? 0 }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="经理" width="100">
          <template #default="{ row }">{{ row.manager || '-' }}</template>
        </el-table-column>
        <el-table-column label="启用" width="70" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              size="small"
              @change="(val: boolean) => handleToggleEnabled(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openPnl(row)">盈亏</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-for="action in getAvailableActions(row.status)"
              :key="action"
              link
              type="primary"
              size="small"
              @click="handleTransition(row, action)"
            >
              {{ transitionLabels[action] }}
            </el-button>
            <el-button
              v-if="row.status !== 'completed'"
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑项目' : '新增项目'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="项目编号" prop="code">
          <el-input v-model="formData.code" :disabled="isEdit" placeholder="如 PRJ-2026-003" maxlength="50" />
        </el-form-item>
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入项目名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="总预算" prop="budget">
          <el-input-number
            v-model="formData.budget"
            :min="0"
            :precision="2"
            :controls="false"
            style="width: 100%"
            placeholder="请输入预算金额"
          />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="项目经理" prop="manager">
          <el-input v-model="formData.manager" placeholder="可选" maxlength="50" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="可选"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 盈亏明细弹窗 -->
    <el-dialog v-model="pnlVisible" title="项目盈亏分析" width="560px" destroy-on-close>
      <div v-loading="pnlLoading">
        <template v-if="pnlData">
          <h3 style="margin:0 0 16px">{{ pnlData.projectName }}</h3>
          <div class="pnl-grid">
            <div class="pnl-item"><span>收入</span><strong style="color:#67c23a">¥{{ formatMoney(pnlData.totalIncome) }}</strong></div>
            <div class="pnl-item"><span>成本</span><strong style="color:#f56c6c">¥{{ formatMoney(pnlData.totalCost) }}</strong></div>
            <div class="pnl-item"><span>利润</span><strong :style="{color: pnlData.totalProfit>=0?'#67c23a':'#f56c6c'}">¥{{ formatMoney(pnlData.totalProfit) }}</strong></div>
            <div class="pnl-item"><span>利润率</span><strong>{{ pnlData.profitRate }}%</strong></div>
            <div class="pnl-item"><span>预算</span><strong>¥{{ formatMoney(pnlData.budget ?? 0) }}</strong></div>
            <div class="pnl-item"><span>预算执行率</span><el-tag :type="pnlData.budgetExecutionRate > 100 ? 'danger' : 'success'" size="small">{{ pnlData.budgetExecutionRate }}%</el-tag></div>
          </div>
          <el-divider />
          <h4 style="margin:0 0 12px">成本结构</h4>
          <el-table v-if="pnlData.costBreakdown?.length" :data="pnlData.costBreakdown" size="small" stripe>
            <el-table-column prop="categoryName" label="类别" />
            <el-table-column label="金额" align="right"><template #default="{row}">¥{{ formatMoney(row.amount) }}</template></el-table-column>
          </el-table>
          <el-empty v-else description="暂无成本记录" :image-size="50" />
          <el-collapse class="meeting-focus">
            <el-collapse-item title="本会讨论焦点（模板）" name="focus">
              <ul class="focus-list">
                <li><strong>目标偏差：</strong>本期收入/利润相对计划偏差的主要原因？</li>
                <li><strong>费用结构：</strong>哪类成本上升最快、是否与产出匹配？</li>
                <li><strong>感谢与浪费：</strong>哪些投入证明有效，哪些可削减？</li>
                <li><strong>下月挑战：</strong>最需要攻坚的一项与客户/资源动作？</li>
              </ul>
            </el-collapse-item>
          </el-collapse>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 28px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 22px;
}

.page-header h2 {
  margin: 0 0 4px;
}

.sub {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
}

.filter-card {
  margin-bottom: 18px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 18px;
}

.summary-card {
  text-align: center;
}

.summary-label {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
}
.pnl-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 12px; }
.pnl-item { display: flex; flex-direction: column; gap: 4px; }
.pnl-item span { font-size: 12px; color: var(--muted); }
.meeting-focus {
  margin-top: 16px;
}
.focus-list {
  margin: 0;
  padding-left: 1.2em;
  line-height: 1.75;
  font-size: 13px;
}
</style>
