<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { fetchBudgetList, createBudget, approveBudget, refreshBudget, deleteBudget, type BudgetVO, type BudgetForm } from '@/api/budget'

const loading = ref(false)
const list = ref<BudgetVO[]>([])
const periodFilter = ref('')
const categoryFilter = ref('')

const overBudgetCount = computed(() => list.value.filter(b => b.overBudget && b.status === 'approved').length)

async function loadList() {
  loading.value = true
  try { list.value = (await fetchBudgetList(periodFilter.value || undefined, categoryFilter.value || undefined)).data.data ?? [] }
  finally { loading.value = false }
}

function handleReset() { periodFilter.value = ''; categoryFilter.value = ''; loadList() }

const dialogVisible = ref(false)
const formRef = ref()
const formData = reactive<BudgetForm>({ period: '', category: '', plannedAmount: undefined, remark: '' })
const formRules = {
  period: [{ required: true, message: '请选择月份', trigger: 'change' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  plannedAmount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
}

function openCreate() {
  const now = new Date()
  const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  Object.assign(formData, { period: ym, category: '', plannedAmount: undefined, remark: '' })
  dialogVisible.value = true
}

const submitting = ref(false)
async function handleSubmit() {
  if (!await formRef.value?.validate().catch(() => false)) return
  submitting.value = true
  try { await createBudget(formData); ElMessage.success('创建成功'); dialogVisible.value = false; await loadList() }
  finally { submitting.value = false }
}

async function handleApprove(row: BudgetVO) {
  await ElMessageBox.confirm(`确认批准 ${row.period} ${row.categoryName} 的预算？`, '批准', { type: 'info' })
  await approveBudget(row.id); ElMessage.success('已批准'); await loadList()
}

async function handleRefresh() {
  if (!periodFilter.value) { ElMessage.warning('请先选择月份'); return }
  const res = await refreshBudget(periodFilter.value)
  ElMessage.success(`已刷新 ${res.data.data?.affected ?? 0} 条预算实际数据`); await loadList()
}

async function handleDelete(row: BudgetVO) {
  await ElMessageBox.confirm(`确认删除 ${row.period} ${row.categoryName}？`, '删除', { type: 'warning' })
  await deleteBudget(row.id); ElMessage.success('删除成功'); await loadList()
}

function execTag(rate: number, over: boolean) { return over ? 'danger' : rate > 80 ? 'warning' : 'success' }
function statusTag(s: string) { return s === 'approved' ? 'success' : 'info' }
function fmt(v: number) { return v?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '0.00' }

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div><h2>预算管理</h2><p class="sub">编制预算、监控执行、超标预警</p></div>
      <div style="display:flex;gap:10px">
        <el-button :icon="Refresh" @click="handleRefresh">刷新实际数据</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">编制预算</el-button>
      </div>
    </div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="月份"><el-date-picker v-model="periodFilter" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:160px" @change="loadList" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="categoryFilter" clearable placeholder="全部" style="width:130px" @change="loadList">
            <el-option label="收入" value="income" /><el-option label="固定成本" value="fixed_cost" /><el-option label="变动成本" value="variable_cost" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-alert v-if="overBudgetCount > 0" :title="`${overBudgetCount} 项预算超标！`" type="error" show-icon :closable="false" style="margin-bottom:16px" />

    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="period" label="月份" width="100" />
        <el-table-column prop="categoryName" label="类别" width="100" />
        <el-table-column label="预算" width="140" align="right"><template #default="{row}">¥{{ fmt(row.plannedAmount) }}</template></el-table-column>
        <el-table-column label="实际" width="140" align="right"><template #default="{row}">¥{{ fmt(row.actualAmount) }}</template></el-table-column>
        <el-table-column label="执行率" width="120" align="center">
          <template #default="{row}">
            <el-tag :type="execTag(row.executionRate, row.overBudget)" size="small">{{ row.executionRate }}%</el-tag>
            <span v-if="row.overBudget" style="color:#f56c6c;margin-left:4px;font-size:11px">超标</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center"><template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{ row.statusName }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120"><template #default="{row}">{{ row.remark || '-' }}</template></el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status==='draft'" link type="success" size="small" @click="handleApprove(row)">批准</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编制预算" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="月份" prop="period"><el-date-picker v-model="formData.period" type="month" value-format="YYYY-MM" style="width:100%" /></el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="formData.category" style="width:100%">
            <el-option label="收入" value="income" /><el-option label="固定成本" value="fixed_cost" /><el-option label="变动成本" value="variable_cost" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额" prop="plannedAmount"><el-input-number v-model="formData.plannedAmount" :min="0" :precision="2" :controls="false" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 28px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 22px; }
.page-header h2 { margin: 0 0 4px; }
.sub { margin: 0; color: var(--muted); font-size: 13px; }
.filter-card { margin-bottom: 18px; }
</style>
