import axios from 'axios';
import { clearSession, getToken } from '@/stores/auth';

export const http = axios.create({
  baseURL: '/api',
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers = config.headers ?? {};
    (config.headers as Record<string, string>).Authorization = `Bearer ${token}`;
  }
  return config;
});

function redirectToLoginIfNeeded() {
  clearSession();
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

http.interceptors.response.use(
  (response) => {
    // 历史行为：部分业务异常以 HTTP 200 + body.code=401 返回，axios 不会进错误分支
    const payload = response.data as { code?: number; message?: string } | undefined;
    if (payload && typeof payload === 'object' && payload.code === 401) {
      redirectToLoginIfNeeded();
      return Promise.reject(new Error(payload.message || '未登录或登录已过期'));
    }
    return response;
  },
  (error) => {
    if (error?.response?.status === 401) {
      redirectToLoginIfNeeded();
    }
    return Promise.reject(error);
  }
);
