<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { fetchDashboardSummary, type DashboardSummary } from '@/api/dashboard'

const router = useRouter()
const loading = ref(true)
const data = ref<DashboardSummary | null>(null)

function fmt(v: number | undefined) {
  return (v ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function goReceivableOverdue() {
  void router.push({ path: '/receivable', query: { status: 'overdue' } })
}

function goReceivable() {
  void router.push({ path: '/receivable' })
}

function goPayableOverdue() {
  void router.push({ path: '/payable', query: { status: 'overdue' } })
}

onMounted(async () => {
  try {
    const res = await fetchDashboardSummary()
    data.value = res.data.data ?? null
  } catch {
    ElMessage.warning('无法加载会议摘要数据')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>会议清单（风险摘要）</h2>
        <p class="sub">
          与首页「本周经营例会」折叠卡数据源一致（仪表盘聚合）；适合投屏或单独标签页周会使用。
        </p>
      </div>
    </div>

    <template v-if="data">
      <el-card shadow="never" class="risk-card">
        <template #header>
          <span class="card-title"><el-icon><Warning /></el-icon> 开场核对项</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="逾期应收笔数">
            <strong>{{ data.overdueReceivableCount }}</strong>
            <el-button link type="primary" @click="goReceivableOverdue">查看列表</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="应收待收余额">
            <strong class="warn">¥{{ fmt(data.receivableRemaining) }}</strong>
            <el-button link type="primary" @click="goReceivable">打开应收</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="应付待付余额">
            <strong class="warn">¥{{ fmt(data.payableRemaining) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="逾期应付笔数">
            <strong>{{ data.overduePayableCount }}</strong>
            <el-button v-if="data.overduePayableCount > 0" link type="primary" @click="goPayableOverdue">
              查看列表
            </el-button>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="decision-card">
        <template #header><strong>待决策</strong></template>
        <p class="placeholder">决策建议功能规划中；本期请在线下记录结论并回填至项目/合同备注。</p>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.page {
  padding: 28px;
  max-width: 720px;
}
.page-header h2 {
  margin: 0 0 8px;
}
.sub {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.55;
}
.risk-card {
  margin-bottom: 18px;
}
.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.warn {
  color: var(--warning, #e6a23c);
}
.decision-card .placeholder {
  margin: 0;
  color: var(--muted);
  line-height: 1.65;
}
</style>
