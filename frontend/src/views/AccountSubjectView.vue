<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  fetchSubjectTree,
  createSubject,
  updateSubject,
  deleteSubject,
  toggleSubjectStatus,
  type AccountSubjectTreeNode,
  type AccountSubjectForm
} from '@/api/accountSubject'

// ─── 科目类型 & 借贷方向字典 ───
const typeOptions = [
  { value: 'asset', label: '资产类' },
  { value: 'liability', label: '负债类' },
  { value: 'equity', label: '权益类' },
  { value: 'cost', label: '成本类' },
  { value: 'profit_loss', label: '损益类' }
]

const debitCreditOptions = [
  { value: 'debit', label: '借' },
  { value: 'credit', label: '贷' }
]

// ─── 页面状态 ───
const status = ref<'init' | 'loading' | 'ready' | 'error'>('init')
const errorMessage = ref('')
const treeData = ref<AccountSubjectTreeNode[]>([])
const expandedKeys = ref<number[]>([])
const keyword = ref('')

// ─── 弹窗状态 ───
const dialog = reactive({
  visible: false,
  mode: 'create' as 'create' | 'createChild' | 'edit',
  submitting: false,
  editingId: null as number | null
})

const formRef = ref<FormInstance>()
const form = reactive<AccountSubjectForm>({
  code: '',
  name: '',
  parentId: null,
  type: '',
  debitCredit: '',
  sort: 1,
  remark: ''
})

const rules: FormRules = {
  code: [
    { required: true, message: '请输入科目编码', trigger: 'blur' },
    { min: 1, max: 50, message: '长度 1-50 个字符', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '只允许字母和数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入科目名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度 1-100 个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择科目类型', trigger: 'change' }],
  debitCredit: [{ required: true, message: '请选择借贷方向', trigger: 'change' }],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' },
    { type: 'number', min: 1, max: 9999, message: '范围 1-9999', trigger: 'blur' }
  ],
  remark: [{ max: 200, message: '最多 200 个字符', trigger: 'blur' }]
}

// ─── 启停操作追踪 ───
const togglingId = ref<number | null>(null)

// ─── 计算属性 ───
const dialogTitle = computed(() => {
  switch (dialog.mode) {
    case 'create':
      return '新增科目'
    case 'createChild':
      return '新增下级科目'
    case 'edit':
      return '编辑科目'
  }
})

const isCodeReadonly = computed(() => dialog.mode === 'edit')

// 树形选择器数据：编辑时排除自身及子孙节点
const parentTreeData = computed(() => {
  if (dialog.mode !== 'edit' || !dialog.editingId) return treeData.value
  return filterTree(treeData.value, dialog.editingId)
})

function filterTree(nodes: AccountSubjectTreeNode[], excludeId: number): AccountSubjectTreeNode[] {
  return nodes
    .filter((n) => n.id !== excludeId)
    .map((n) => ({
      ...n,
      children: filterTree(n.children, excludeId)
    }))
}

function collectRootIds(nodes: AccountSubjectTreeNode[]): number[] {
  return nodes.map((n) => n.id)
}

// ─── 数据加载 ───
async function fetchTree(kw?: string) {
  status.value = 'loading'
  try {
    const res = await fetchSubjectTree(kw)
    treeData.value = res.data.data
    expandedKeys.value = kw ? collectAllIds(treeData.value) : collectRootIds(treeData.value)
    status.value = 'ready'
  } catch (e: any) {
    errorMessage.value = e?.response?.data?.message || e?.message || '加载失败'
    status.value = 'error'
  }
}

function collectAllIds(nodes: AccountSubjectTreeNode[]): number[] {
  const ids: number[] = []
  const walk = (list: AccountSubjectTreeNode[]) => {
    for (const n of list) {
      ids.push(n.id)
      if (n.children?.length) walk(n.children)
    }
  }
  walk(nodes)
  return ids
}

// ─── 搜索 ───
function handleSearch() {
  fetchTree(keyword.value || undefined)
}

function handleReset() {
  keyword.value = ''
  fetchTree()
}

// ─── 弹窗操作 ───
function resetForm() {
  form.code = ''
  form.name = ''
  form.parentId = null
  form.type = ''
  form.debitCredit = ''
  form.sort = 1
  form.remark = ''
  dialog.editingId = null
}

function openCreate() {
  resetForm()
  dialog.mode = 'create'
  dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}

function openCreateChild(row: AccountSubjectTreeNode) {
  resetForm()
  form.parentId = row.id
  dialog.mode = 'createChild'
  dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row: AccountSubjectTreeNode) {
  resetForm()
  dialog.mode = 'edit'
  dialog.editingId = row.id
  form.code = row.code
  form.name = row.name
  form.parentId = row.parentId
  form.type = row.type
  form.debitCredit = row.debitCredit
  form.sort = row.sort
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
    if (dialog.mode === 'edit' && dialog.editingId) {
      await updateSubject(dialog.editingId, { ...form })
    } else {
      await createSubject({ ...form })
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    resetForm()
    fetchTree()
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '保存失败'
    ElMessage.error(msg)
  } finally {
    dialog.submitting = false
  }
}

// ─── 删除 ───
async function handleDelete(row: AccountSubjectTreeNode) {
  try {
    await ElMessageBox.confirm(`确认删除科目「${row.code} - ${row.name}」？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await deleteSubject(row.id)
    ElMessage.success('删除成功')
    fetchTree()
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '删除失败'
    ElMessage.error(msg)
  }
}

// ─── 启停 ───
async function handleToggleStatus(row: AccountSubjectTreeNode) {
  const newVal = !row.enabled
  togglingId.value = row.id
  try {
    await toggleSubjectStatus(row.id, newVal)
    ElMessage.success('状态已更新')
    fetchTree()
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '状态更新失败'
    ElMessage.error(msg)
  } finally {
    togglingId.value = null
  }
}

// ─── 生命周期 ───
onMounted(() => {
  fetchTree()
})
</script>

<template>
  <div class="page-container">
    <!-- 错误状态 -->
    <div v-if="status === 'error'" class="error-placeholder">
      <el-empty :description="errorMessage || '加载失败'">
        <el-button type="primary" @click="fetchTree()">重新加载</el-button>
      </el-empty>
    </div>

    <template v-else>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索科目编码或名称"
          clearable
          style="width: 280px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增科目</el-button>
      </div>

      <!-- 树形表格 -->
      <el-card shadow="never" class="table-card">
        <el-table
          v-loading="status === 'loading'"
          :data="treeData"
          row-key="id"
          :default-expand-all="false"
          :expand-row-keys="expandedKeys.map(String)"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          stripe
          border
        >
          <el-table-column prop="code" label="编码" width="140" />
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="typeName" label="类型" width="100" />
          <el-table-column prop="debitCreditName" label="方向" width="80" />
          <el-table-column prop="level" label="层级" width="80" align="center" />
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
          <el-table-column prop="sort" label="排序" width="80" align="center" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link type="primary" size="small" @click="openCreateChild(row)"
                >新增下级</el-button
              >
              <el-button link type="danger" size="small" @click="handleDelete(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>

        <el-empty
          v-if="status === 'ready' && treeData.length === 0"
          description="暂无数据"
          style="padding: 40px 0"
        />
      </el-card>
    </template>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialogTitle"
      width="560px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        :disabled="dialog.submitting"
      >
        <el-form-item label="科目编码" prop="code">
          <el-input
            v-model="form.code"
            :disabled="isCodeReadonly"
            placeholder="请输入科目编码"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="科目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入科目名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="父级科目" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentTreeData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            :disabled="dialog.mode === 'createChild'"
            clearable
            check-strictly
            placeholder="无（根科目）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="科目类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择科目类型" style="width: 100%">
            <el-option
              v-for="opt in typeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="借贷方向" prop="debitCredit">
          <el-select v-model="form.debitCredit" placeholder="请选择借贷方向" style="width: 100%">
            <el-option
              v-for="opt in debitCreditOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="1" :max="9999" style="width: 100%" />
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
