import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface PayableVO {
  id: number; code: string; counterpartyId: number; counterpartyName: string
  contractId: number | null; contractName: string | null
  amount: number; paidAmount: number; remaining: number
  dueDate: string; status: string; statusName: string
  remark: string | null; agingDays: number; createdAt: string
}

export interface PayableForm {
  code: string; counterpartyId: number | undefined; contractId: number | undefined
  amount: number | undefined; dueDate: string; remark: string
}

export interface PaymentRecordForm {
  amount: number | undefined; payDate: string; remark: string
}

export interface PaymentRecordVO {
  id: number; amount: number; payDate: string; remark: string | null; createdAt: string
}

export const fetchPayableList = (keyword?: string, status?: string) => {
  const p: Record<string, string> = {}
  if (keyword) p.keyword = keyword; if (status) p.status = status
  return http.get<ApiResponse<PayableVO[]>>('/payable', { params: p })
}
export const createPayable = (d: PayableForm) => http.post<ApiResponse<PayableVO>>('/payable', d)
export const recordPayablePayment = (id: number, data: PaymentRecordForm) =>
  http.post<ApiResponse<void>>(`/payable/${id}/payment`, data)
export const fetchPayablePayments = (id: number) =>
  http.get<ApiResponse<PaymentRecordVO[]>>(`/payable/${id}/payments`)
export const markPayableOverdue = (id: number) => http.post<ApiResponse<void>>(`/payable/${id}/overdue`)
export const batchPayableOverdue = () => http.post<ApiResponse<{ affected: number }>>('/payable/batch-overdue')
export const deletePayable = (id: number) => http.delete<ApiResponse<void>>(`/payable/${id}`)
