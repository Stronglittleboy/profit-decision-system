<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight } from '@element-plus/icons-vue'
import {
  fetchContractList,
  createContract,
  updateContract,
  deleteContract,
  transitionContract,
  type ContractVO,
  type ContractForm,
} from '@/api/contract'
import { fetchCounterpartyList, type CounterpartyVO } from '@/api/counterparty'
import { fetchProjectList, type ProjectVO } from '@/api/project'

const loading = ref(false)
const list = ref<ContractVO[]>([])
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')

const counterpartyOptions = ref<CounterpartyVO[]>([])
const projectOptions = ref<ProjectVO[]>([])

async function loadOptions() {
  const [cpRes, pjRes] = await Promise.all([
    fetchCounterpartyList(),
    fetchProjectList(),
  ])
  counterpartyOptions.value = (cpRes.data.data ?? []).filter((c: CounterpartyVO) => c.enabled)
  projectOptions.value = pjRes.data.data ?? []
}

async function loadList() {
  loading.value = true
  try {
    const res = await fetchContractList(
      keyword.value || undefined,
      typeFilter.value || undefined,
      statusFilter.value || undefined,
    )
    list.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

function handleReset() {
  keyword.value = ''
  typeFilter.value = ''
  statusFilter.value = ''
  loadList()
}

// --- 新增/编辑弹窗 ---
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const formData = reactive<ContractForm>({
  code: '',
  name: '',
  counterpartyId: undefined,
  projectId: undefined,
  type: 'sales',
  amount: undefined,
  signDate: '',
  startDate: '',
  endDate: '',
  remark: '',
})

const formRules = {
  code: [
    { required: true, message: '请输入合同编号', trigger: 'blur' },
    { max: 50, message: '最多 50 个字符', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入合同名称', trigger: 'blur' },
    { max: 200, message: '最多 200 个字符', trigger: 'blur' },
  ],
  counterpartyId: [{ required: true, message: '请选择往来方', trigger: 'change' }],
  type: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  remark: [{ max: 500, message: '最多 500 个字符', trigger: 'blur' }],
}

function resetForm() {
  Object.assign(formData, {
    code: '', name: '', counterpartyId: undefined, projectId: undefined,
    type: 'sales', amount: undefined, signDate: '', startDate: '', endDate: '', remark: '',
  })
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: ContractVO) {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(formData, {
    code: row.code,
    name: row.name,
    counterpartyId: row.counterpartyId,
    projectId: row.projectId ?? undefined,
    type: row.type,
    amount: row.amount,
    signDate: row.signDate ?? '',
    startDate: row.startDate ?? '',
    endDate: row.endDate ?? '',
    remark: row.remark ?? '',
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
      await updateContract(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createContract(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    submitting.value = false
  }
}

// --- 删除 ---
async function handleDelete(row: ContractVO) {
  await ElMessageBox.confirm(`确认删除合同「${row.name}」？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning',
  })
  await deleteContract(row.id)
  ElMessage.success('删除成功')
  await loadList()
}

// --- 状态流转 ---
const transitionLabels: Record<string, string> = {
  activate: '生效', complete: '完成', terminate: '终止',
}

function getAvailableActions(status: string): string[] {
  switch (status) {
    case 'draft': return ['activate', 'terminate']
    case 'active': return ['complete', 'terminate']
    default: return []
  }
}

async function handleTransition(row: ContractVO, action: string) {
  const label = transitionLabels[action]
  await ElMessageBox.confirm(`确认将合同「${row.name}」${label}？`, '操作确认', {
    confirmButtonText: '确认', cancelButtonText: '取消', type: 'info',
  })
  await transitionContract(row.id, action)
  ElMessage.success(`${label}成功`)
  await loadList()
}

// --- 辅助 ---
function statusTagType(status: string) {
  switch (status) {
    case 'draft': return 'warning'
    case 'active': return 'success'
    case 'completed': return 'info'
    case 'terminated': return 'danger'
    default: return ''
  }
}

function typeTagType(type: string) {
  switch (type) {
    case 'sales': return 'success'
    case 'purchase': return 'warning'
    case 'service': return ''
    default: return ''
  }
}

function formatMoney(val: number) {
  return val?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '0.00'
}

onMounted(() => {
  loadOptions()
  loadList()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>合同管理</h2>
        <p class="sub">管理与往来方的契约关系 — 销售、采购、服务合同</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增合同</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="搜索">
          <el-input v-model="keyword" placeholder="编号或名称" clearable style="width: 200px" @keyup.enter="loadList" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="typeFilter" clearable placeholder="全部" style="width: 120px">
            <el-option label="销售合同" value="sales" />
            <el-option label="采购合同" value="purchase" />
            <el-option label="服务合同" value="service" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" clearable placeholder="全部" style="width: 120px">
            <el-option label="草稿" value="draft" />
            <el-option label="生效" value="active" />
            <el-option label="已完成" value="completed" />
            <el-option label="已终止" value="terminated" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="loadList">搜索</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="code" label="编号" width="150" />
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="counterpartyName" label="往来方" width="120" />
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="signDate" label="签约日" width="110">
          <template #default="{ row }">{{ row.signDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="项目" width="120">
          <template #default="{ row }">{{ row.projectName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'draft'" link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-for="action in getAvailableActions(row.status)"
              :key="action"
              link
              :type="action === 'terminate' ? 'danger' : 'primary'"
              size="small"
              @click="handleTransition(row, action)"
            >{{ transitionLabels[action] }}</el-button>
            <el-button
              v-if="row.status === 'draft'"
              link type="danger" size="small"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑合同' : '新增合同'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="合同编号" prop="code">
          <el-input v-model="formData.code" :disabled="isEdit" placeholder="如 CT-2026-004" maxlength="50" />
        </el-form-item>
        <el-form-item label="合同名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入合同名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="合同类型" prop="type">
          <el-select v-model="formData.type" style="width: 100%">
            <el-option label="销售合同" value="sales" />
            <el-option label="采购合同" value="purchase" />
            <el-option label="服务合同" value="service" />
          </el-select>
        </el-form-item>
        <el-form-item label="往来方" prop="counterpartyId">
          <el-select v-model="formData.counterpartyId" filterable placeholder="搜索并选择" style="width: 100%">
            <el-option v-for="c in counterpartyOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="formData.projectId" filterable clearable placeholder="可选" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="`${p.code} ${p.name}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0.01" :precision="2" :controls="false" style="width: 100%" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="签约日期">
          <el-date-picker v-model="formData.signDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="formData.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker v-model="formData.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="可选" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
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
