import { http } from './http';

export interface HealthPayload {
  service: string;
  status: string;
  time: string;
  message: string;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export const fetchHealth = () => http.get<ApiResponse<HealthPayload>>('/health');
