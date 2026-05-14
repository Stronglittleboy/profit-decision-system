import { http } from './http';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  displayName: string;
  expireAt: string;
}

export interface CurrentUser {
  username: string;
  displayName: string;
  expireAt: string;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export function login(payload: LoginRequest) {
  return http.post<ApiResponse<LoginResponse>>('/auth/login', payload);
}

export function fetchCurrentUser() {
  return http.get<ApiResponse<CurrentUser>>('/auth/me');
}

export function logout() {
  return http.post<ApiResponse<void>>('/auth/logout');
}
