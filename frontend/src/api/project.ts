import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface ProjectVO {
  id: number
  code: string
  name: string
  status: string
  statusName: string
  budget: number
  startDate: string | null
  endDate: string | null
  manager: string | null
  description: string | null
  enabled: boolean
  createdAt: string
  updatedAt: string
  totalIncome: number
  totalCost: number
  totalProfit: number
  profitRate: number
  budgetExecutionRate: number
}

export interface CostBreakdown {
  category: string
  categoryName: string
  amount: number
}

export interface ProjectPnlVO {
  projectId: number
  projectName: string
  totalIncome: number
  totalCost: number
  totalProfit: number
  profitRate: number
  budget: number
  budgetExecutionRate: number
  costBreakdown: CostBreakdown[]
}

export interface ProjectForm {
  code: string
  name: string
  budget: number | undefined
  startDate: string
  endDate: string
  manager: string
  description: string
}

export function fetchProjectList(keyword?: string, status?: string) {
  const params: Record<string, string> = {}
  if (keyword) params.keyword = keyword
  if (status) params.status = status
  return http.get<ApiResponse<ProjectVO[]>>('/project', { params })
}

export function fetchProjectDetail(id: number) {
  return http.get<ApiResponse<ProjectVO>>(`/project/${id}`)
}

export function createProject(data: ProjectForm) {
  return http.post<ApiResponse<ProjectVO>>('/project', data)
}

export function updateProject(id: number, data: ProjectForm) {
  return http.put<ApiResponse<ProjectVO>>(`/project/${id}`, data)
}

export function deleteProject(id: number) {
  return http.delete<ApiResponse<void>>(`/project/${id}`)
}

export function transitionProject(id: number, action: string) {
  return http.post<ApiResponse<void>>(`/project/${id}/transition`, { action })
}

export function toggleProjectEnabled(id: number, enabled: boolean) {
  return http.patch<ApiResponse<void>>(`/project/${id}/status`, { enabled })
}

export function fetchProjectPnl(id: number) {
  return http.get<ApiResponse<ProjectPnlVO>>(`/project/${id}/pnl`)
}
