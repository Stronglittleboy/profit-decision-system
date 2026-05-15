<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight } from '@element-plus/icons-vue'
import {
  fetchFactEventList,
  createFactEvent,
  reverseFactEvent,
  fetchAmortizationEntries,
  type FactEventVO,
  type FactEventForm,
  type FactEventQuery,
  type AmortizationEntryVO,
} from '@/api/factEvent'
import { fetchSubjectTree, type AccountSubjectTreeNode } from '@/api/accountSubject'
import { fetchCounterpartyList, type CounterpartyVO } from '@/api/counterparty'
import RevenueCostDisclaimer from '@/components/RevenueCostDisclaimer.vue'

const route = useRoute()
const loading = ref(false)
const list = ref<FactEventVO[]>([])

const query = reactive<FactEventQuery>({
  type: '',
  status: '',
  startDate: '',
  endDate: '',
})
const dateRange = ref<[string, string] | null>(null)

const subjectOptions = ref<{ id: number; code: string; name: string }[]>([])
const counterpartyOptions = ref<CounterpartyVO[]>([])

const incomeTotal = computed(() =>
  list.value
    .filter((e) => e.type === 'income' && e.status === 'valid')
    .reduce((sum, e) => sum + e.amount, 0),
)
const costTotal = computed(() =>
  list.value
    .filter((e) => e.type === 'cost' && e.status === 'valid')
    .reduce((sum, e) => sum + e.amount, 0),
)
const profitTotal = computed(() => incomeTotal.value - costTotal.value)

function flattenSubjects(nodes: AccountSubjectTreeNode[]): { id: number; code: string; name: string }[] {
  const result: { id: number; code: string; name: string }[] = []
  function walk(list: AccountSubjectTreeNode[]) {
    for (const n of list) {
      result.push({ id: n.id, code: n.code, name: n.name })
      if (n.children?.length) walk(n.children)
    }
  }
  walk(nodes)
  return result
}

async function loadOptions() {
  const [subRes, cpRes] = await Promise.all([
    fetchSubjectTree(),
    fetchCounterpartyList(),
  ])
  subjectOptions.value = flattenSubjects(subRes.data.data ?? [])
  counterpartyOptions.value = (cpRes.data.data ?? []).filter((c: CounterpartyVO) => c.enabled)
}

async function loadList() {
  loading.value = true
  try {
    if (dateRange.value) {
      query.startDate = dateRange.value[0]
      query.endDate = dateRange.value[1]
    } else {
      query.startDate = ''
      query.endDate = ''
    }
    const res = await fetchFactEventList(query)
    list.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.type = ''
  query.status = ''
  dateRange.value = null
  loadList()
}

async function handleReverse(row: FactEventVO) {
  const typeLabel = row.typeName
  const amount = formatMoney(row.amount)
  await ElMessageBox.confirm(
    `确认冲正这笔 ¥${amount} 的${typeLabel}记录？冲正后不可恢复。`,
    '冲正确认',
    { confirmButtonText: '确认冲正', cancelButtonText: '取消', type: 'warning' },
  )
  await reverseFactEvent(row.id)
  ElMessage.success('冲正成功')
  await loadList()
}

// --- 新增弹窗 ---
const dialogVisible = ref(false)
const formRef = ref()
const formData = reactive<FactEventForm>({
  type: 'income',
  amount: undefined,
  businessDate: '',
  accountingDate: '',
  subjectId: undefined,
  counterpartyId: undefined,
  costCategory: '',
  amortizeStart: '',
  amortizeEnd: '',
  amortizeMethod: '',
  invoiceNo: '',
  remark: '',
})

const formRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  businessDate: [{ required: true, message: '请选择业务日期', trigger: 'change' }],
  accountingDate: [{ required: true, message: '请选择会计日期', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择会计科目', trigger: 'change' }],
  counterpartyId: [{ required: true, message: '请选择往来方', trigger: 'change' }],
  invoiceNo: [{ max: 50, message: '最多 50 个字符', trigger: 'blur' }],
  remark: [{ max: 500, message: '最多 500 个字符', trigger: 'blur' }],
}

function openCreate() {
  Object.assign(formData, {
    type: 'income',
    amount: undefined,
    businessDate: '',
    accountingDate: '',
    subjectId: undefined,
    counterpartyId: undefined,
    costCategory: '',
    amortizeStart: '',
    amortizeEnd: '',
    amortizeMethod: '',
    invoiceNo: '',
    remark: '',
  })
  dialogVisible.value = true
}

const amortDialogVisible = ref(false)
const amortEntries = ref<AmortizationEntryVO[]>([])
const amortLoading = ref(false)
const amortRow = ref<FactEventVO | null>(null)

async function openAmortDetail(row: FactEventVO) {
  amortRow.value = row
  amortLoading.value = true
  amortDialogVisible.value = true
  try {
    const res = await fetchAmortizationEntries(row.id)
    amortEntries.value = res.data.data ?? []
  } finally {
    amortLoading.value = false
  }
}

function handleBusinessDateChange(val: string) {
  if (val && !formData.accountingDate) {
    formData.accountingDate = val
  }
}

const submitting = ref(false)
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createFactEvent(formData)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadList()
  } finally {
    submitting.value = false
  }
}

function formatMoney(val: number) {
  return val?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '0.00'
}

function typeTagType(type: string) {
  return type === 'income' ? 'success' : 'danger'
}

function statusTagType(status: string) {
  return status === 'valid' ? '' : 'info'
}

function rowClassName({ row }: { row: FactEventVO }) {
  return row.status === 'reversed' ? 'reversed-row' : ''
}

function applyRouteQuery() {
  const q = route.query
  const t = typeof q.type === 'string' ? q.type : ''
  const s = typeof q.status === 'string' ? q.status : ''
  const sd = typeof q.startDate === 'string' ? q.startDate : ''
  const ed = typeof q.endDate === 'string' ? q.endDate : ''
  if (t) query.type = t
  if (s) query.status = s
  if (sd && ed) {
    dateRange.value = [sd, ed]
  } else if (sd || ed) {
    dateRange.value = [sd || '', ed || '']
  }
}

onMounted(() => {
  applyRouteQuery()
  loadOptions()
  loadList()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>收支记录</h2>
        <p class="sub">经营事实唯一入口 — 所有收入和成本都在这里录入</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增记录</el-button>
    </div>

    <RevenueCostDisclaimer />

    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width: 120px">
            <el-option label="收入" value="income" />
            <el-option label="成本" value="cost" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="有效" value="valid" />
            <el-option label="已冲正" value="reversed" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="loadList">搜索</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 汇总卡片 -->
    <div class="summary-cards">
      <el-card shadow="never" class="summary-card income-card">
        <div class="summary-label">收入合计</div>
        <div class="summary-value income-value">¥{{ formatMoney(incomeTotal) }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card cost-card">
        <div class="summary-label">成本合计</div>
        <div class="summary-value cost-value">¥{{ formatMoney(costTotal) }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card profit-card">
        <div class="summary-label">利润</div>
        <div class="summary-value" :class="profitTotal >= 0 ? 'income-value' : 'cost-value'">
          ¥{{ formatMoney(profitTotal) }}
        </div>
      </el-card>
    </div>

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" :row-class-name="rowClassName" stripe>
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'income' ? 'income-value' : 'cost-value'">
              ¥{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="businessDate" label="业务日期" width="120" />
        <el-table-column label="会计科目" min-width="140">
          <template #default="{ row }">
            <span class="text-muted">{{ row.subjectCode }}</span>
            {{ row.subjectName }}
          </template>
        </el-table-column>
        <el-table-column prop="counterpartyName" label="往来方" min-width="120" />
        <el-table-column label="成本类别" width="100">
          <template #default="{ row }">
            {{ row.costCategoryName ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="分摊" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.amortizeMonths > 0" type="warning" size="small">{{ row.amortizeMonths }}个月</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceNo" label="发票号" width="130">
          <template #default="{ row }">
            {{ row.invoiceNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.amortizeMonths > 0" link type="primary" size="small" @click="openAmortDetail(row)">分摊明细</el-button>
            <el-button
              v-if="row.status === 'valid'"
              link
              type="warning"
              size="small"
              @click="handleReverse(row)"
            >
              冲正
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增收支记录" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio-button value="income">收入</el-radio-button>
            <el-radio-button value="cost">成本</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="formData.amount"
            :min="0.01"
            :precision="2"
            :controls="false"
            style="width: 100%"
            placeholder="请输入金额"
          />
        </el-form-item>
        <el-form-item label="业务日期" prop="businessDate">
          <el-date-picker
            v-model="formData.businessDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
            @change="handleBusinessDateChange"
          />
        </el-form-item>
        <el-form-item label="会计日期" prop="accountingDate">
          <el-date-picker
            v-model="formData.accountingDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="默认与业务日期相同"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会计科目" prop="subjectId">
          <el-select
            v-model="formData.subjectId"
            filterable
            placeholder="搜索并选择科目"
            style="width: 100%"
          >
            <el-option
              v-for="s in subjectOptions"
              :key="s.id"
              :label="`${s.code} ${s.name}`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="往来方" prop="counterpartyId">
          <el-select
            v-model="formData.counterpartyId"
            filterable
            placeholder="搜索并选择往来方"
            style="width: 100%"
          >
            <el-option
              v-for="c in counterpartyOptions"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.type === 'cost'" label="成本类别">
          <el-select v-model="formData.costCategory" clearable placeholder="选择成本类别" style="width: 100%">
            <el-option label="固定成本" value="fixed" />
            <el-option label="变动成本" value="variable" />
            <el-option label="直接成本" value="direct" />
            <el-option label="间接成本" value="indirect" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.type === 'cost' && formData.costCategory === 'fixed'" label="跨期分摊">
          <el-date-picker v-model="formData.amortizeStart" type="month" value-format="YYYY-MM-DD" placeholder="起始月" style="width:48%" />
          <span style="display:inline-block;width:4%;text-align:center">—</span>
          <el-date-picker v-model="formData.amortizeEnd" type="month" value-format="YYYY-MM-DD" placeholder="截止月" style="width:48%" />
        </el-form-item>
        <el-form-item label="发票号" prop="invoiceNo">
          <el-input v-model="formData.invoiceNo" placeholder="可选" maxlength="50" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
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

    <!-- 分摊明细弹窗 -->
    <el-dialog v-model="amortDialogVisible" title="分摊明细" width="500px" destroy-on-close>
      <p v-if="amortRow">总额 ¥{{ formatMoney(amortRow.amount) }}，线性分摊 {{ amortRow.amortizeMonths }} 个月（{{ amortRow.amortizeStart?.substring(0,7) }} ~ {{ amortRow.amortizeEnd?.substring(0,7) }}）</p>
      <el-table :data="amortEntries" v-loading="amortLoading" stripe size="small">
        <el-table-column prop="period" label="月份" width="120" />
        <el-table-column label="分摊金额" align="right"><template #default="{row}">¥{{ formatMoney(row.amount) }}</template></el-table-column>
      </el-table>
      <template #footer><el-button @click="amortDialogVisible=false">关闭</el-button></template>
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
  grid-template-columns: repeat(3, 1fr);
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

.income-value {
  color: #67c23a;
}

.cost-value {
  color: #f56c6c;
}

.text-muted {
  color: var(--muted);
  margin-right: 6px;
  font-size: 12px;
}

:deep(.reversed-row) {
  opacity: 0.45;
}
</style>
