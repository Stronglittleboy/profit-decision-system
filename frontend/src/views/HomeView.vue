<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchHealth, type HealthPayload } from '@/api/health';
import { fetchDashboardSummary, type DashboardSummary } from '@/api/dashboard';

const loading = ref(true);
const health = ref<HealthPayload | null>(null);
const dashboard = ref<DashboardSummary | null>(null);

const cards = [
  {
    title: '后端',
    value: 'Spring Boot + JDK 21',
    note: 'Maven / MyBatis-Plus / Lombok / Hutool'
  },
  {
    title: '前端',
    value: 'Vue 3 + Router + Element Plus',
    note: 'Vite 构建，模块化页面结构'
  },
  {
    title: '数据',
    value: 'MySQL + Redis',
    note: '统一本地开发与生产配置'
  }
];

const displayCards = computed(() =>
  dashboard.value?.metrics.map((item) => ({
    title: item.label,
    value: item.value,
    note: item.note
  })) ?? cards
);

const pipelineSteps = computed(
  () =>
    dashboard.value?.nextSteps ?? [
      '建立后端基础工程和统一返回值。',
      '建立前端基础工程和路由骨架。',
      '补充公共配置、异常处理、日志和联调接口。',
      '再进入业务模块开发。'
    ]
);

const statusText = computed(() => health.value?.status ?? '未连接');

onMounted(async () => {
  try {
    const [healthResponse, summaryResponse] = await Promise.all([
      fetchHealth(),
      fetchDashboardSummary()
    ]);
    health.value = healthResponse.data.data;
    dashboard.value = summaryResponse.data.data;
  } catch {
    ElMessage.warning('后端未启动，当前仅展示前端骨架');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="page-shell" v-loading="loading">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">Profit Decision System</p>
        <h1>新的技术方案已经落地为可启动工程骨架。</h1>
        <p class="lead">
          后端使用 Spring Boot + JDK 21 + Maven，前端使用 Vue 3 + Vue Router + Element Plus。
          这是后续业务模块开发的基础底座。
        </p>
      </div>

      <div class="hero-status">
        <div class="status-chip">
          <span class="dot" :class="{ alive: statusText === 'UP' }"></span>
          <span>{{ statusText }}</span>
        </div>
        <p v-if="health" class="status-meta">{{ health.message }}</p>
        <p v-else class="status-meta">等待后端启动后自动联通。</p>
      </div>
    </section>

    <section class="card-grid">
      <el-card v-for="card in displayCards" :key="card.title" class="info-card" shadow="never">
        <p class="card-title">{{ card.title }}</p>
        <h3>{{ card.value }}</h3>
        <p class="card-note">{{ card.note }}</p>
      </el-card>
    </section>

    <section class="pipeline">
      <el-card class="pipeline-card" shadow="never">
        <template #header>
          <div class="pipeline-header">
            <span>{{ dashboard?.title ?? '当前实施顺序' }}</span>
            <el-tag type="info" effect="dark">Init</el-tag>
          </div>
        </template>
        <ol>
          <li v-for="step in pipelineSteps" :key="step">{{ step }}</li>
        </ol>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  gap: 20px;
  align-items: stretch;
}

.hero-copy,
.hero-status,
.info-card,
.pipeline-card {
  border: 1px solid var(--panel-border);
  background: var(--panel);
  backdrop-filter: blur(18px);
  border-radius: 22px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.28);
}

.hero-copy {
  padding: 34px;
}

.eyebrow {
  margin: 0 0 14px;
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  max-width: 12ch;
  font-size: clamp(2.2rem, 5vw, 4.8rem);
  line-height: 0.98;
}

.lead {
  margin: 18px 0 0;
  max-width: 58ch;
  color: var(--muted);
  font-size: 1.02rem;
  line-height: 1.8;
}

.hero-status {
  padding: 28px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16px;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  color: var(--text);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--warning);
  box-shadow: 0 0 0 6px rgba(245, 158, 11, 0.12);
}

.dot.alive {
  background: var(--success);
  box-shadow: 0 0 0 6px rgba(54, 211, 153, 0.12);
}

.status-meta,
.card-note {
  margin: 0;
  color: var(--muted);
  line-height: 1.7;
}

.card-grid {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.info-card {
  overflow: hidden;
}

.card-title {
  margin: 0 0 10px;
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.info-card h3 {
  margin: 0 0 8px;
  font-size: 1.2rem;
}

.pipeline {
  margin-top: 20px;
}

.pipeline-card {
  padding: 4px 0;
}

.pipeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

ol {
  margin: 0;
  padding-left: 20px;
  color: var(--muted);
  line-height: 1.9;
}

@media (max-width: 900px) {
  .hero,
  .card-grid {
    grid-template-columns: 1fr;
  }

  h1 {
    max-width: none;
  }
}
</style>
