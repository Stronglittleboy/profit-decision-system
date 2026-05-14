import { http } from './http';

export interface DashboardMetric {
  label: string;
  value: string;
  note: string;
}

export interface DashboardSummary {
  title: string;
  metrics: DashboardMetric[];
  nextSteps: string[];
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export function fetchDashboardSummary() {
  return http.get<ApiResponse<DashboardSummary>>('/dashboard/summary');
}
