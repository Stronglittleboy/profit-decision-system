<script setup lang="ts">
import {
  VALUE_PROPOSITION,
  CALIBER_BLOCK_FULL,
  DISCLAIMER_SHORT,
} from '@/constants/productCopy'
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2>使用说明与边界</h2>
      <p class="lead">{{ VALUE_PROPOSITION }}</p>
    </div>

    <el-card shadow="never" class="block">
      <template #header><strong>一、经营数字说明</strong></template>
      <p>
        仪表盘与经营类汇总为系统中 <strong>收支记录（fact_event）</strong> 在
        <strong>有效状态</strong>下的聚合快照，用于例会讨论与趋势观察。
      </p>
    </el-card>

    <el-card shadow="never" class="block">
      <template #header><strong>二、三层边界（我们做什么 / 不做什么）</strong></template>
      <ul class="boundary-list">
        <li>
          <strong>明确要做：</strong>经营事实录入与可追溯；项目维度盈亏与结构成本；应收应付等资金侧辅助视图；管理层摘要与钻取（服务周会开场，不替代月末结账）。
        </li>
        <li>
          <strong>明确不做：</strong>不替代总账、税务申报与审计底稿；不承诺多组织法定合并、阿米巴内部定价、人事绩效；不输出自动经营决策（系统可辅助录入与解释，拍板仍由人完成）。
        </li>
        <li>
          <strong>灰区：</strong>客户排名等仅作改善讨论工具，不作 KPI 引擎；与 Excel 并行时，建议以
          <strong>本系统作为经营例会数字底座</strong>、Excel 作为个人草稿（最终以财务书面制度为准）。
        </li>
      </ul>
    </el-card>

    <el-card shadow="never" class="block">
      <template #header><strong>三、与应收应付的关系（口径）</strong></template>
      <p class="caliber">{{ CALIBER_BLOCK_FULL }}</p>
    </el-card>

    <el-card shadow="never" class="block disclaimer-card">
      <template #header><strong>四、免责声明</strong></template>
      <p>{{ DISCLAIMER_SHORT }}</p>
    </el-card>

    <footer class="meta-footer">
      文档：product-spec-v2 · 版本 <strong>2.0</strong> · 修订日期 2026-05-15
    </footer>
  </div>
</template>

<style scoped>
.page {
  padding: 28px;
  max-width: 920px;
}
.page-header h2 {
  margin: 0 0 12px;
}
.lead {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: var(--text);
}
.block {
  margin-bottom: 18px;
}
.block p,
.caliber {
  margin: 0;
  line-height: 1.65;
  font-size: 14px;
  color: var(--text);
}
.boundary-list {
  margin: 0;
  padding-left: 1.2em;
  line-height: 1.7;
  font-size: 14px;
}
.boundary-list li {
  margin-bottom: 10px;
}
.disclaimer-card {
  border-left: 3px solid var(--warning, #e6a23c);
}
.meta-footer {
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
}
</style>
