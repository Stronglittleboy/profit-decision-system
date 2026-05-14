import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface ReceivableVO {
  id: number; code: string; counterpartyId: number; counterpartyName: string
  contractId: number | null; contractName: string | null
  amount: number; paidAmount: number; remaining: number
  dueDate: string; status: string; statusName: string
  remark: string | null; createdAt: string
}

export interface ReceivableForm {
  code: string; counterpartyId: number | undefined; contractId: number | undefined
  amount: number | undefined; dueDate: string; remark: string
}

export const fetchReceivableList = (keyword?: string, status?: string) => {
  const p: Record<string, string> = {}
  if (keyword) p.keyword = keyword; if (status) p.status = status
  return http.get<ApiResponse<ReceivableVO[]>>('/receivable', { params: p })
}
export const createReceivable = (d: ReceivableForm) => http.post<ApiResponse<ReceivableVO>>('/receivable', d)
export const recordReceivablePayment = (id: number, amount: number) =>
  http.post<ApiResponse<void>>(`/receivable/${id}/payment`, { amount })
export const markReceivableOverdue = (id: number) => http.post<ApiResponse<void>>(`/receivable/${id}/overdue`)
export const deleteReceivable = (id: number) => http.delete<ApiResponse<void>>(`/receivable/${id}`)
