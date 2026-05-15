<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchHealth, type HealthPayload } from '@/api/health'
import { fetchDashboardSummary, type DashboardSummary } from '@/api/dashboard'
import { VALUE_PROPOSITION } from '@/constants/productCopy'

const router = useRouter()
const loading = ref(true)
const health = ref<HealthPayload | null>(null)
const data = ref<DashboardSummary | null>(null)

const meetingCollapse = ref<string[]>([])

const statusText = computed(() => health.value?.status ?? '未连接')

function fmt(v: number | undefined) {
  return (v ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function trendMax(trends: DashboardSummary['monthTrends']) {
  if (!trends?.length) return 1
  return Math.max(...trends.map((t) => Math.max(t.income, t.cost)), 1)
}

function barH(val: number, max: number) {
  return Math.max(4, (val / max) * 100) + '%'
}

function monthRange(): { start: string; end: string } {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth()
  const mm = String(m + 1).padStart(2, '0')
  const last = new Date(y, m + 1, 0).getDate()
  return {
    start: `${y}-${mm}-01`,
    end: `${y}-${mm}-${String(last).padStart(2, '0')}`,
  }
}

function goFactIncomeMonth() {
  const { start, end } = monthRange()
  void router.push({ path: '/fact-event', query: { type: 'income', startDate: start, endDate: end } })
}

function goFactCostMonth() {
  const { start, end } = monthRange()
  void router.push({ path: '/fact-event', query: { type: 'cost', startDate: start, endDate: end } })
}

function goReceivable() {
  void router.push({ path: '/receivable' })
}

function goReceivableOverdue() {
  void router.push({ path: '/receivable', query: { status: 'overdue' } })
}

function goCounterparty(name: string) {
  void router.push({ path: '/counterparty', query: { keyword: name } })
}

onMounted(async () => {
  try {
    const [hr, dr] = await Promise.all([fetchHealth(), fetchDashboardSummary()])
    health.value = hr.data.data
    data.value = dr.data.data
  } catch {
    ElMessage.warning('后端未启动，无法加载经营数据')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>经营总览</h2>
        <p class="sub">基于事实数据的实时经营看板</p>
        <p class="value-line">{{ VALUE_PROPOSITION }}</p>
      </div>
      <div class="status-chip">
        <span class="dot" :class="{ alive: statusText === 'UP' }"></span>
        <span>{{ statusText }}</span>
      </div>
    </div>

    <template v-if="data">
      <div class="metric-grid">
        <el-card shadow="never" class="mc clickable" @click="goFactIncomeMonth">
          <div class="mc-label">累计收入</div>
          <div class="mc-value income-c">¥{{ fmt(data.totalIncome) }}</div>
          <div class="mc-sub">本月 ¥{{ fmt(data.monthIncome) }} · 点击看本月收入明细</div>
        </el-card>
        <el-card shadow="never" class="mc clickable" @click="goFactCostMonth">
          <div class="mc-label">累计成本</div>
          <div class="mc-value cost-c">¥{{ fmt(data.totalCost) }}</div>
          <div class="mc-sub">本月 ¥{{ fmt(data.monthCost) }} · 点击看本月成本明细</div>
        </el-card>
        <el-card shadow="never" class="mc">
          <div class="mc-label">累计利润</div>
          <div class="mc-value" :class="data.totalProfit >= 0 ? 'income-c' : 'cost-c'">¥{{ fmt(data.totalProfit) }}</div>
          <div class="mc-sub">利润率 {{ data.profitRate }}%</div>
        </el-card>
        <el-card shadow="never" class="mc">
          <div class="mc-label">本月利润</div>
          <div class="mc-value" :class="data.monthProfit >= 0 ? 'income-c' : 'cost-c'">¥{{ fmt(data.monthProfit) }}</div>
        </el-card>
      </div>

      <div class="stat-grid">
        <el-card shadow="never" class="sc">
          <div class="sc-row"><span>项目总数</span><strong>{{ data.projectCount }}</strong></div>
          <div class="sc-row"><span>执行中</span><el-tag type="success" size="small">{{ data.activeProjectCount }}</el-tag></div>
        </el-card>
        <el-card shadow="never" class="sc">
          <div class="sc-row"><span>合同总数</span><strong>{{ data.contractCount }}</strong></div>
          <div class="sc-row"><span>生效中</span><el-tag type="success" size="small">{{ data.activeContractCount }}</el-tag></div>
        </el-card>
        <el-card shadow="never" class="sc clickable" @click="goReceivable">
          <div class="sc-row"><span>应收待收</span><strong class="warning-c">¥{{ fmt(data.receivableRemaining) }}</strong></div>
          <div class="sc-row">
            <span>逾期</span>
            <el-tag
              v-if="data.overdueReceivableCount > 0"
              type="danger"
              size="small"
              class="tag-link"
              @click.stop="goReceivableOverdue"
            >
              {{ data.overdueReceivableCount }} 笔
            </el-tag>
            <span v-else>0</span>
          </div>
          <div class="sc-hint">点击查看应收列表</div>
        </el-card>
        <el-card shadow="never" class="sc">
          <div class="sc-row"><span>应付待付</span><strong class="warning-c">¥{{ fmt(data.payableRemaining) }}</strong></div>
          <div class="sc-row"><span>逾期</span><el-tag v-if="data.overduePayableCount > 0" type="danger" size="small">{{ data.overduePayableCount }} 笔</el-tag><span v-else>0</span></div>
        </el-card>
      </div>

      <el-collapse v-model="meetingCollapse" class="meeting-collapse">
        <el-collapse-item title="本周经营例会（风险摘要）" name="brief">
          <ul class="meeting-list">
            <li>
              逾期应收：<strong>{{ data.overdueReceivableCount }}</strong> 笔 ·
              <el-button link type="primary" @click="goReceivableOverdue">打开逾期列表</el-button>
            </li>
            <li>
              应收待收余额：<strong class="warning-c">¥{{ fmt(data.receivableRemaining) }}</strong> ·
              <el-button link type="primary" @click="goReceivable">打开应收</el-button>
            </li>
            <li class="muted">待决策：决策建议功能规划中（后续版本接入决策引擎）</li>
          </ul>
        </el-collapse-item>
      </el-collapse>

      <div class="bottom-grid">
        <el-card shadow="never" class="trend-card">
          <template #header><strong>月度收入/成本趋势（近 6 月）</strong></template>
          <div class="chart-area" v-if="data.monthTrends?.length">
            <div class="bar-group" v-for="t in data.monthTrends" :key="t.month">
              <div class="bars">
                <div class="bar income-bar" :style="{ height: barH(t.income, trendMax(data.monthTrends)) }" :title="`收入 ¥${fmt(t.income)}`"></div>
                <div class="bar cost-bar" :style="{ height: barH(t.cost, trendMax(data.monthTrends)) }" :title="`成本 ¥${fmt(t.cost)}`"></div>
              </div>
              <div class="bar-label">{{ t.month.slice(5) }}月</div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
          <div class="legend"><span class="lg income-lg">收入</span><span class="lg cost-lg">成本</span></div>
        </el-card>

        <el-card shadow="never" class="customer-card">
          <template #header><strong>客户贡献 TOP 5</strong></template>
          <el-table :data="data.topCustomers" size="small" stripe v-if="data.topCustomers?.length">
            <el-table-column prop="counterpartyName" label="客户">
              <template #default="{ row }">
                <el-button v-if="row.counterpartyName" link type="primary" @click="goCounterparty(row.counterpartyName)">
                  {{ row.counterpartyName }}
                </el-button>
                <span v-else>(未关联)</span>
              </template>
            </el-table-column>
            <el-table-column label="收入" align="right">
              <template #default="{ row }"><span class="income-c">¥{{ fmt(row.income) }}</span></template>
            </el-table-column>
            <el-table-column label="成本" align="right">
              <template #default="{ row }"><span class="cost-c">¥{{ fmt(row.cost) }}</span></template>
            </el-table-column>
            <el-table-column label="利润" align="right">
              <template #default="{ row }">
                <strong :class="row.profit >= 0 ? 'income-c' : 'cost-c'">¥{{ fmt(row.profit) }}</strong>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </div>
    </template>
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
.value-line {
  margin: 10px 0 0;
  max-width: 720px;
  font-size: 14px;
  line-height: 1.55;
  color: var(--text);
}
.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  font-size: 13px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--warning, #e6a23c);
}
.dot.alive {
  background: var(--success, #67c23a);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.mc {
  text-align: center;
}
.mc.clickable {
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.mc.clickable:hover {
  box-shadow: 0 0 0 1px var(--accent, #409eff);
}
.mc-label {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 8px;
}
.mc-value {
  font-size: 26px;
  font-weight: 700;
}
.mc-sub {
  font-size: 12px;
  color: var(--muted);
  margin-top: 6px;
}
.income-c {
  color: #67c23a;
}
.cost-c {
  color: #f56c6c;
}
.warning-c {
  color: #e6a23c;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.sc {
  padding: 8px 0;
}
.sc.clickable {
  cursor: pointer;
}
.sc.clickable:hover {
  box-shadow: 0 0 0 1px var(--accent, #409eff);
}
.sc-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
}
.sc-hint {
  font-size: 11px;
  color: var(--muted);
  margin-top: 4px;
}
.tag-link {
  cursor: pointer;
}

.meeting-collapse {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}
.meeting-list {
  margin: 0;
  padding-left: 1.2em;
  line-height: 1.85;
}
.meeting-list .muted {
  color: var(--muted);
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
}

.chart-area {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 180px;
  padding: 10px 0;
}
.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}
.bars {
  display: flex;
  gap: 4px;
  align-items: flex-end;
  height: 150px;
}
.bar {
  width: 18px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s;
}
.income-bar {
  background: #67c23a;
}
.cost-bar {
  background: #f56c6c;
}
.bar-label {
  font-size: 11px;
  color: var(--muted);
  margin-top: 6px;
}
.legend {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 8px;
  font-size: 12px;
}
.lg {
  display: flex;
  align-items: center;
  gap: 4px;
}
.lg::before {
  content: '';
  display: block;
  width: 12px;
  height: 12px;
  border-radius: 2px;
}
.income-lg::before {
  background: #67c23a;
}
.cost-lg::before {
  background: #f56c6c;
}

@media (max-width: 900px) {
  .metric-grid,
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}
</style>
