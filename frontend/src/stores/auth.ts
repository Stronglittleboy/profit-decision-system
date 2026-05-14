import { computed, reactive } from 'vue';

export interface AuthUser {
  token: string;
  username: string;
  displayName: string;
  expireAt: string;
}

const AUTH_TOKEN_KEY = 'profit-decision-system-token';
const AUTH_USER_KEY = 'profit-decision-system-user';

function readString(key: string) {
  return localStorage.getItem(key) ?? '';
}

function readUser(): AuthUser | null {
  const raw = localStorage.getItem(AUTH_USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export const authState = reactive({
  token: readString(AUTH_TOKEN_KEY),
  user: readUser() as AuthUser | null
});

export const isAuthenticated = computed(() => Boolean(authState.token));

export function setSession(user: AuthUser) {
  authState.token = user.token;
  authState.user = user;
  localStorage.setItem(AUTH_TOKEN_KEY, user.token);
  localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
}

export function clearSession() {
  authState.token = '';
  authState.user = null;
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_USER_KEY);
}

export function getToken() {
  return authState.token;
}
