<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight } from '@element-plus/icons-vue'
import {
  fetchReceivableList, createReceivable, recordReceivablePayment,
  fetchReceivablePayments, markReceivableOverdue, batchReceivableOverdue,
  deleteReceivable,
  type ReceivableVO, type ReceivableForm, type PaymentRecordForm, type PaymentRecordVO
} from '@/api/receivable'
import { fetchCounterpartyList, type CounterpartyVO } from '@/api/counterparty'
import { fetchContractList, type ContractVO } from '@/api/contract'

const loading = ref(false)
const list = ref<ReceivableVO[]>([])
const keyword = ref('')
const statusFilter = ref('')
const cpOptions = ref<CounterpartyVO[]>([])
const ctOptions = ref<ContractVO[]>([])

const totalAmount = computed(() => list.value.reduce((s, r) => s + r.amount, 0))
const totalPaid = computed(() => list.value.reduce((s, r) => s + r.paidAmount, 0))
const totalRemaining = computed(() => totalAmount.value - totalPaid.value)
const overdueCount = computed(() => list.value.filter(r => r.status === 'overdue').length)

async function loadOptions() {
  const [cp, ct] = await Promise.all([fetchCounterpartyList(), fetchContractList()])
  cpOptions.value = (cp.data.data ?? []).filter((c: CounterpartyVO) => c.enabled)
  ctOptions.value = ct.data.data ?? []
}

async function loadList() {
  loading.value = true
  try { list.value = (await fetchReceivableList(keyword.value || undefined, statusFilter.value || undefined)).data.data ?? [] }
  finally { loading.value = false }
}

function handleReset() { keyword.value = ''; statusFilter.value = ''; loadList() }

const dialogVisible = ref(false)
const formRef = ref()
const formData = reactive<ReceivableForm>({ code: '', counterpartyId: undefined, contractId: undefined, amount: undefined, dueDate: '', remark: '' })
const formRules = {
  code: [{ required: true, message: '请输入单据编号', trigger: 'blur' }, { max: 50, message: '最多 50 字符', trigger: 'blur' }],
  counterpartyId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  dueDate: [{ required: true, message: '请选择到期日', trigger: 'change' }],
}

function openCreate() {
  Object.assign(formData, { code: '', counterpartyId: undefined, contractId: undefined, amount: undefined, dueDate: '', remark: '' })
  dialogVisible.value = true
}

const submitting = ref(false)
async function handleSubmit() {
  if (!await formRef.value?.validate().catch(() => false)) return
  submitting.value = true
  try { await createReceivable(formData); ElMessage.success('创建成功'); dialogVisible.value = false; await loadList() }
  finally { submitting.value = false }
}

const payDialogVisible = ref(false)
const payingRow = ref<ReceivableVO | null>(null)
const payForm = reactive<PaymentRecordForm>({ amount: undefined, payDate: '', remark: '' })

function openPay(row: ReceivableVO) {
  payingRow.value = row
  Object.assign(payForm, { amount: undefined, payDate: new Date().toISOString().slice(0, 10), remark: '' })
  payDialogVisible.value = true
}

async function handlePay() {
  if (!payForm.amount || payForm.amount <= 0) { ElMessage.warning('请输入有效金额'); return }
  if (!payForm.payDate) { ElMessage.warning('请选择日期'); return }
  await recordReceivablePayment(payingRow.value!.id, payForm)
  ElMessage.success('登记成功'); payDialogVisible.value = false; await loadList()
}

const historyDialogVisible = ref(false)
const historyRow = ref<ReceivableVO | null>(null)
const historyRecords = ref<PaymentRecordVO[]>([])
const historyLoading = ref(false)

async function openHistory(row: ReceivableVO) {
  historyRow.value = row; historyLoading.value = true; historyDialogVisible.value = true
  try { historyRecords.value = (await fetchReceivablePayments(row.id)).data.data ?? [] }
  finally { historyLoading.value = false }
}

async function handleOverdue(row: ReceivableVO) {
  await ElMessageBox.confirm(`确认将「${row.code}」标记为逾期？`, '操作确认', { type: 'warning' })
  await markReceivableOverdue(row.id); ElMessage.success('已标记逾期'); await loadList()
}

async function handleBatchOverdue() {
  await ElMessageBox.confirm('将所有已过到期日但未结清的应收标记为逾期？', '批量逾期', { type: 'warning' })
  const res = await batchReceivableOverdue()
  ElMessage.success(`完成，共标记 ${res.data.data?.affected ?? 0} 笔`); await loadList()
}

async function handleDelete(row: ReceivableVO) {
  await ElMessageBox.confirm(`确认删除「${row.code}」？`, '删除确认', { type: 'warning' })
  await deleteReceivable(row.id); ElMessage.success('删除成功'); await loadList()
}

function agingTag(days: number) { return days > 90 ? 'danger' : days > 30 ? 'warning' : '' }
function statusTag(s: string) { return s === 'paid' ? 'success' : s === 'overdue' ? 'danger' : s === 'partial' ? '' : 'warning' }
function fmt(v: number) { return v?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '0.00' }

onMounted(() => { loadOptions(); loadList() })
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div><h2>应收账款</h2><p class="sub">管理客户欠款 — 登记回款、跟踪逾期</p></div>
      <div style="display:flex;gap:10px">
        <el-button @click="handleBatchOverdue">批量标记逾期</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增应收</el-button>
      </div>
    </div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="搜索"><el-input v-model="keyword" placeholder="单据编号" clearable style="width:180px" @keyup.enter="loadList" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" clearable placeholder="全部" style="width:120px">
            <el-option label="待收" value="pending" /><el-option label="部分" value="partial" />
            <el-option label="已结清" value="paid" /><el-option label="逾期" value="overdue" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button :icon="Search" type="primary" @click="loadList">搜索</el-button><el-button :icon="RefreshRight" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <div class="summary-cards">
      <el-card shadow="never" class="sc"><div class="sl">应收总额</div><div class="sv">¥{{ fmt(totalAmount) }}</div></el-card>
      <el-card shadow="never" class="sc"><div class="sl">已收</div><div class="sv" style="color:#67c23a">¥{{ fmt(totalPaid) }}</div></el-card>
      <el-card shadow="never" class="sc"><div class="sl">待收</div><div class="sv" style="color:#e6a23c">¥{{ fmt(totalRemaining) }}</div></el-card>
      <el-card shadow="never" class="sc"><div class="sl">逾期</div><div class="sv" style="color:#f56c6c">{{ overdueCount }} 笔</div></el-card>
    </div>
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="code" label="编号" width="140" />
        <el-table-column prop="counterpartyName" label="客户" width="120" />
        <el-table-column label="合同" width="120"><template #default="{row}">{{ row.contractName || '-' }}</template></el-table-column>
        <el-table-column label="总额" width="130" align="right"><template #default="{row}">¥{{ fmt(row.amount) }}</template></el-table-column>
        <el-table-column label="已收" width="130" align="right"><template #default="{row}"><span style="color:#67c23a">¥{{ fmt(row.paidAmount) }}</span></template></el-table-column>
        <el-table-column label="待收" width="130" align="right"><template #default="{row}"><span style="color:#e6a23c">¥{{ fmt(row.remaining) }}</span></template></el-table-column>
        <el-table-column prop="dueDate" label="到期日" width="110" />
        <el-table-column label="账龄" width="80" align="center"><template #default="{row}"><el-tag :type="agingTag(row.agingDays)" size="small">{{ row.agingDays }}天</el-tag></template></el-table-column>
        <el-table-column label="状态" width="80" align="center"><template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{ row.statusName }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openHistory(row)">流水</el-button>
            <el-button v-if="row.status!=='paid'" link type="primary" size="small" @click="openPay(row)">登记回款</el-button>
            <el-button v-if="row.status!=='paid'&&row.status!=='overdue'" link type="warning" size="small" @click="handleOverdue(row)">标记逾期</el-button>
            <el-button v-if="row.status!=='paid'" link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增应收" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="单据编号" prop="code"><el-input v-model="formData.code" maxlength="50" /></el-form-item>
        <el-form-item label="客户" prop="counterpartyId"><el-select v-model="formData.counterpartyId" filterable style="width:100%"><el-option v-for="c in cpOptions" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="关联合同"><el-select v-model="formData.contractId" filterable clearable style="width:100%"><el-option v-for="c in ctOptions" :key="c.id" :label="`${c.code} ${c.name}`" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="应收金额" prop="amount"><el-input-number v-model="formData.amount" :min="0.01" :precision="2" :controls="false" style="width:100%" /></el-form-item>
        <el-form-item label="到期日" prop="dueDate"><el-date-picker v-model="formData.dueDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="payDialogVisible" title="登记回款" width="420px" destroy-on-close>
      <p>单据：{{ payingRow?.code }}，待收余额：¥{{ fmt(payingRow?.remaining ?? 0) }}</p>
      <el-form label-width="80px">
        <el-form-item label="回款金额"><el-input-number v-model="payForm.amount" :min="0.01" :max="payingRow?.remaining" :precision="2" :controls="false" style="width:100%" placeholder="本次回款金额" /></el-form-item>
        <el-form-item label="收款日期"><el-date-picker v-model="payForm.payDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="payForm.remark" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="payDialogVisible=false">取消</el-button><el-button type="primary" @click="handlePay">确认</el-button></template>
    </el-dialog>

    <el-dialog v-model="historyDialogVisible" title="收款流水" width="500px" destroy-on-close>
      <p>单据：{{ historyRow?.code }}</p>
      <el-table :data="historyRecords" v-loading="historyLoading" stripe size="small">
        <el-table-column prop="payDate" label="日期" width="120" />
        <el-table-column label="金额" align="right"><template #default="{row}">¥{{ fmt(row.amount) }}</template></el-table-column>
        <el-table-column prop="remark" label="备注"><template #default="{row}">{{ row.remark || '-' }}</template></el-table-column>
      </el-table>
      <template #footer><el-button @click="historyDialogVisible=false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 28px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 22px; }
.page-header h2 { margin: 0 0 4px; }
.sub { margin: 0; color: var(--muted); font-size: 13px; }
.filter-card { margin-bottom: 18px; }
.summary-cards { display: grid; grid-template-columns: repeat(4,1fr); gap: 18px; margin-bottom: 18px; }
.sc { text-align: center; }
.sl { font-size: 13px; color: var(--muted); margin-bottom: 8px; }
.sv { font-size: 24px; font-weight: 700; }
</style>
