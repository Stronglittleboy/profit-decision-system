<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  House,
  SwitchButton,
  Notebook,
  User,
  Coin,
  FolderOpened,
  Document,
  Download,
  Upload,
  DataAnalysis,
  TrendCharts,
  QuestionFilled,
  Calendar,
} from '@element-plus/icons-vue';
import { authState, clearSession } from '@/stores/auth';
import { logout } from '@/api/auth';

const router = useRouter();

const displayName = computed(() => authState.user?.displayName ?? '管理员');

async function handleLogout() {
  try {
    await logout();
  } finally {
    clearSession();
    await router.push('/login');
  }
}
</script>

<template>
  <el-container class="shell">
    <el-aside class="sidebar" width="248px">
      <div class="brand">
        <div class="brand-mark">P</div>
        <div>
          <strong>飞牛经营系统</strong>
          <p>Spring Boot + Vue 3</p>
        </div>
      </div>

      <el-menu router class="menu" :default-active="$route.path">
        <el-sub-menu index="grp-meeting">
          <template #title><span class="submenu-title">经营例会</span></template>
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/meeting">
            <el-icon><Calendar /></el-icon>
            <span>会议清单</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="grp-project">
          <template #title><span class="submenu-title">项目经营体</span></template>
          <el-menu-item index="/project">
            <el-icon><FolderOpened /></el-icon>
            <span>项目管理</span>
          </el-menu-item>
          <el-menu-item index="/contract">
            <el-icon><Document /></el-icon>
            <span>合同管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="grp-customer">
          <template #title><span class="submenu-title">客户与往来</span></template>
          <el-menu-item index="/counterparty">
            <el-icon><User /></el-icon>
            <span>往来方</span>
          </el-menu-item>
          <el-menu-item index="/customer-analysis">
            <el-icon><TrendCharts /></el-icon>
            <span>客户分析</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="grp-funds">
          <template #title><span class="submenu-title">资金与预算</span></template>
          <el-menu-item index="/fact-event">
            <el-icon><Coin /></el-icon>
            <span>收支记录</span>
          </el-menu-item>
          <el-menu-item index="/receivable">
            <el-icon><Download /></el-icon>
            <span>应收账款</span>
          </el-menu-item>
          <el-menu-item index="/payable">
            <el-icon><Upload /></el-icon>
            <span>应付账款</span>
          </el-menu-item>
          <el-menu-item index="/budget">
            <el-icon><DataAnalysis /></el-icon>
            <span>预算管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="grp-master">
          <template #title><span class="submenu-title">主数据</span></template>
          <el-menu-item index="/account-subject">
            <el-icon><Notebook /></el-icon>
            <span>会计科目</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="grp-help">
          <template #title><span class="submenu-title">系统与帮助</span></template>
          <el-menu-item index="/help">
            <el-icon><QuestionFilled /></el-icon>
            <span>使用说明</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <p class="title">当前已进入基础框架</p>
          <p class="subtitle">后续业务模块都将在这个布局上展开</p>
        </div>
        <div class="user-area">
          <span class="user-tag">{{ displayName }}</span>
          <el-button text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.shell {
  min-height: 100vh;
}

.sidebar {
  padding: 22px 18px;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(8, 15, 28, 0.82);
  backdrop-filter: blur(20px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  color: white;
  font-weight: 800;
}

.brand p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.menu {
  border-right: none;
  background: transparent;
}

.submenu-title {
  font-weight: 600;
  font-size: 13px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(10, 18, 32, 0.72);
  backdrop-filter: blur(20px);
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.subtitle {
  margin: 6px 0 0;
  color: var(--muted);
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-tag {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text);
}

.main {
  padding: 0;
}

@media (max-width: 900px) {
  .shell {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }
}
</style>
