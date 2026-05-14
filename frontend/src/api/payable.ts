import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface PayableVO {
  id: number; code: string; counterpartyId: number; counterpartyName: string
  contractId: number | null; contractName: string | null
  amount: number; paidAmount: number; remaining: number
  dueDate: string; status: string; statusName: string
  remark: string | null; createdAt: string
}

export interface PayableForm {
  code: string; counterpartyId: number | undefined; contractId: number | undefined
  amount: number | undefined; dueDate: string; remark: string
}

export const fetchPayableList = (keyword?: string, status?: string) => {
  const p: Record<string, string> = {}
  if (keyword) p.keyword = keyword; if (status) p.status = status
  return http.get<ApiResponse<PayableVO[]>>('/payable', { params: p })
}
export const createPayable = (d: PayableForm) => http.post<ApiResponse<PayableVO>>('/payable', d)
export const recordPayablePayment = (id: number, amount: number) =>
  http.post<ApiResponse<void>>(`/payable/${id}/payment`, { amount })
export const markPayableOverdue = (id: number) => http.post<ApiResponse<void>>(`/payable/${id}/overdue`)
export const deletePayable = (id: number) => http.delete<ApiResponse<void>>(`/payable/${id}`)
