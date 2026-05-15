import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface BudgetVO {
  id: number; period: string; category: string; categoryName: string
  plannedAmount: number; actualAmount: number; executionRate: number; overBudget: boolean
  status: string; statusName: string; remark: string | null; createdAt: string
}

export interface BudgetForm {
  period: string; category: string; plannedAmount: number | undefined; remark: string
}

export const fetchBudgetList = (period?: string, category?: string, status?: string) => {
  const p: Record<string, string> = {}
  if (period) p.period = period; if (category) p.category = category; if (status) p.status = status
  return http.get<ApiResponse<BudgetVO[]>>('/budget', { params: p })
}
export const createBudget = (d: BudgetForm) => http.post<ApiResponse<BudgetVO>>('/budget', d)
export const updateBudget = (id: number, d: BudgetForm) => http.put<ApiResponse<BudgetVO>>(`/budget/${id}`, d)
export const approveBudget = (id: number) => http.post<ApiResponse<void>>(`/budget/${id}/approve`)
export const refreshBudget = (period: string) => http.post<ApiResponse<{ affected: number }>>('/budget/refresh', { period })
export const deleteBudget = (id: number) => http.delete<ApiResponse<void>>(`/budget/${id}`)
