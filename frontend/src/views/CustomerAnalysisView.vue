<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchCustomerRank, type CustomerRankVO } from '@/api/analysis'
import { CUSTOMER_ANALYSIS_ALERT, CUSTOMER_ANALYSIS_FOOTER } from '@/constants/productCopy'

const loading = ref(false)
const list = ref<CustomerRankVO[]>([])
const dateRange = ref<[string, string] | null>(null)

async function loadData() {
  loading.value = true
  try {
    const sd = dateRange.value?.[0] || undefined
    const ed = dateRange.value?.[1] || undefined
    const res = await fetchCustomerRank(sd, ed)
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '查询失败')
      list.value = []
      return
    }
    list.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

function fmt(v: number) {
  return v?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '0.00'
}
function profitColor(v: number) {
  return v >= 0 ? '#67c23a' : '#f56c6c'
}
function rateTag(v: number) {
  return v >= 20 ? 'success' : v >= 0 ? 'warning' : 'danger'
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>客户贡献分析</h2>
        <p class="sub">基于收支记录聚合的客户收入、成本、利润排行</p>
      </div>
    </div>
    <el-alert type="info" :closable="false" show-icon class="purpose-alert" title="用途说明">
      <p class="alert-body">{{ CUSTOMER_ANALYSIS_ALERT }}</p>
    </el-alert>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" style="width:280px" @change="loadData" />
        </el-form-item>
        <el-form-item><el-button @click="dateRange=null; loadData()">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="#" type="index" width="50" />
        <el-table-column prop="counterpartyName" label="客户" min-width="160">
          <template #default="{row}">{{ row.counterpartyName || '(未关联)' }}</template>
        </el-table-column>
        <el-table-column label="收入" align="right" width="160"><template #default="{row}"><span style="color:#67c23a">¥{{ fmt(row.income) }}</span></template></el-table-column>
        <el-table-column label="成本" align="right" width="160"><template #default="{row}"><span style="color:#f56c6c">¥{{ fmt(row.cost) }}</span></template></el-table-column>
        <el-table-column label="利润" align="right" width="160"><template #default="{row}"><strong :style="{color: profitColor(row.profit)}">¥{{ fmt(row.profit) }}</strong></template></el-table-column>
        <el-table-column label="利润率" align="center" width="110"><template #default="{row}"><el-tag :type="rateTag(row.profitRate)" size="small">{{ row.profitRate }}%</el-tag></template></el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无数据" />
    </el-card>
    <p class="page-footer-note">{{ CUSTOMER_ANALYSIS_FOOTER }}</p>
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
.purpose-alert {
  margin-bottom: 18px;
}
.alert-body {
  margin: 4px 0 0;
  line-height: 1.6;
  font-size: 14px;
}
.filter-card {
  margin-bottom: 18px;
}
.page-footer-note {
  margin: 16px 0 0;
  font-size: 13px;
  color: var(--muted);
  line-height: 1.6;
}
</style>
