<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  fetchCounterpartyList,
  createCounterparty,
  updateCounterparty,
  deleteCounterparty,
  toggleCounterpartyStatus,
  type CounterpartyVO,
  type CounterpartyForm
} from '@/api/counterparty'

const typeOptions = [
  { value: 'customer', label: '客户' },
  { value: 'supplier', label: '供应商' },
  { value: 'both', label: '双重' }
]

const creditLevelOptions = [
  { value: 'A', label: 'A级' },
  { value: 'B', label: 'B级' },
  { value: 'C', label: 'C级' },
  { value: 'D', label: 'D级' }
]

// ─── 页面状态 ───
const status = ref<'init' | 'loading' | 'ready' | 'error'>('init')
const errorMessage = ref('')
const tableData = ref<CounterpartyVO[]>([])
const keyword = ref('')
const filterType = ref('')

// ─── 弹窗状态 ───
const dialog = reactive({
  visible: false,
  mode: 'create' as 'create' | 'edit',
  submitting: false,
  editingId: null as number | null
})

const formRef = ref<FormInstance>()
const form = reactive<CounterpartyForm>({
  name: '',
  type: '',
  contact: '',
  phone: '',
  address: '',
  taxNo: '',
  creditLevel: '',
  remark: ''
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度 1-100 个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  contact: [{ max: 100, message: '最多 100 个字符', trigger: 'blur' }],
  phone: [{ max: 20, message: '最多 20 个字符', trigger: 'blur' }],
  address: [{ max: 200, message: '最多 200 个字符', trigger: 'blur' }],
  taxNo: [{ max: 50, message: '最多 50 个字符', trigger: 'blur' }],
  remark: [{ max: 200, message: '最多 200 个字符', trigger: 'blur' }]
}

const togglingId = ref<number | null>(null)

// ─── 数据加载 ───
async function fetchList() {
  status.value = 'loading'
  try {
    const res = await fetchCounterpartyList(
      keyword.value || undefined,
      filterType.value || undefined
    )
    tableData.value = res.data.data
    status.value = 'ready'
  } catch (e: any) {
    errorMessage.value = e?.response?.data?.message || e?.message || '加载失败'
    status.value = 'error'
  }
}

function handleSearch() {
  fetchList()
}

function handleReset() {
  keyword.value = ''
  filterType.value = ''
  fetchList()
}

// ─── 弹窗 ───
function resetForm() {
  form.name = ''
  form.type = ''
  form.contact = ''
  form.phone = ''
  form.address = ''
  form.taxNo = ''
  form.creditLevel = ''
  form.remark = ''
  dialog.editingId = null
}

function openCreate() {
  resetForm()
  dialog.mode = 'create'
  dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row: CounterpartyVO) {
  resetForm()
  dialog.mode = 'edit'
  dialog.editingId = row.id
  form.name = row.name
  form.type = row.type
  form.contact = row.contact ?? ''
  form.phone = row.phone ?? ''
  form.address = row.address ?? ''
  form.taxNo = row.taxNo ?? ''
  form.creditLevel = row.creditLevel ?? ''
  form.remark = row.remark ?? ''
  dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleDialogClose() {
  dialog.visible = false
  dialog.submitting = false
  resetForm()
}

async function handleSave() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  dialog.submitting = true
  try {
    const payload = { ...form, creditLevel: form.creditLevel || undefined } as any
    if (dialog.mode === 'edit' && dialog.editingId) {
      await updateCounterparty(dialog.editingId, payload)
    } else {
      await createCounterparty(payload)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    resetForm()
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    dialog.submitting = false
  }
}

// ─── 删除 ───
async function handleDelete(row: CounterpartyVO) {
  try {
    await ElMessageBox.confirm(`确认删除往来方「${row.name}」？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await deleteCounterparty(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
  }
}

// ─── 启停 ───
async function handleToggleStatus(row: CounterpartyVO) {
  const newVal = !row.enabled
  togglingId.value = row.id
  try {
    await toggleCounterpartyStatus(row.id, newVal)
    ElMessage.success('状态已更新')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '状态更新失败')
  } finally {
    togglingId.value = null
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="page-container">
    <div v-if="status === 'error'" class="error-placeholder">
      <el-empty :description="errorMessage || '加载失败'">
        <el-button type="primary" @click="fetchList()">重新加载</el-button>
      </el-empty>
    </div>

    <template v-else>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索名称 / 联系人 / 电话"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="filterType"
          placeholder="全部类型"
          clearable
          style="width: 130px"
        >
          <el-option
            v-for="opt in typeOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增往来方</el-button>
      </div>

      <el-card shadow="never" class="table-card">
        <el-table
          v-loading="status === 'loading'"
          :data="tableData"
          stripe
          border
        >
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="typeName" label="类型" width="100" />
          <el-table-column prop="contact" label="联系人" width="110" />
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column prop="taxNo" label="税号" width="160" />
          <el-table-column prop="creditLevelName" label="信用等级" width="100" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.enabled"
                :disabled="togglingId === row.id"
                :loading="togglingId === row.id"
                @change="handleToggleStatus(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty
          v-if="status === 'ready' && tableData.length === 0"
          description="暂无数据"
          style="padding: 40px 0"
        />
      </el-card>
    </template>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.mode === 'create' ? '新增往来方' : '编辑往来方'"
      width="560px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        :disabled="dialog.submitting"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option
              v-for="opt in typeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="选填" maxlength="100" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" maxlength="20" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="选填" maxlength="200" />
        </el-form-item>
        <el-form-item label="税号" prop="taxNo">
          <el-input v-model="form.taxNo" placeholder="选填" maxlength="50" />
        </el-form-item>
        <el-form-item label="信用等级" prop="creditLevel">
          <el-select v-model="form.creditLevel" placeholder="选填" clearable style="width: 100%">
            <el-option
              v-for="opt in creditLevelOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="选填"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  padding: 24px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.error-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
