<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login } from '@/api/auth';
import { setSession } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const loading = ref(false);

const form = reactive({
  username: 'admin',
  password: '123456'
});

async function handleLogin() {
  loading.value = true;
  try {
    const response = await login(form);
    setSession(response.data.data);
    ElMessage.success('登录成功');
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
    await router.replace(redirect);
  } catch {
    ElMessage.error('用户名或密码错误');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-copy">
        <p class="eyebrow">Profit Decision System</p>
        <h1>登录后进入系统骨架。</h1>
        <p>
          这里先接通基础鉴权和首页框架，后续的业务模块会在同一套会话和布局上继续扩展。
        </p>
      </div>

      <el-card class="login-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>账号登录</span>
            <el-tag type="success" effect="dark">Demo</el-tag>
          </div>
        </template>

        <el-form :model="form" label-position="top" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="admin" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="123456" />
          </el-form-item>

          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            进入系统
          </el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.login-panel {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.8fr);
  gap: 20px;
}

.login-copy,
.login-card {
  border: 1px solid var(--panel-border);
  background: var(--panel);
  backdrop-filter: blur(18px);
  border-radius: 24px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.32);
}

.login-copy {
  padding: 40px;
}

.eyebrow {
  margin: 0 0 16px;
  color: var(--accent);
  letter-spacing: 0.18em;
  text-transform: uppercase;
  font-size: 12px;
}

h1 {
  margin: 0;
  font-size: clamp(2.2rem, 5vw, 4.8rem);
  line-height: 1;
}

.login-copy p:last-child {
  margin: 18px 0 0;
  max-width: 56ch;
  color: var(--muted);
  line-height: 1.8;
}

.login-card {
  padding: 8px 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.login-btn {
  width: 100%;
  margin-top: 6px;
}

@media (max-width: 900px) {
  .login-panel {
    grid-template-columns: 1fr;
  }
}
</style>
