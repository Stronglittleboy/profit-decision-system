import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface FactEventVO {
  id: number
  type: string
  typeName: string
  amount: number
  businessDate: string
  accountingDate: string
  subjectId: number
  subjectCode: string
  subjectName: string
  counterpartyId: number
  counterpartyName: string
  costCategory: string | null
  costCategoryName: string | null
  amortizeStart: string | null
  amortizeEnd: string | null
  amortizeMethod: string | null
  amortizeMonths: number
  invoiceNo: string | null
  status: string
  statusName: string
  remark: string | null
  createdAt: string
  updatedAt: string
}

export interface AmortizationEntryVO {
  id: number
  period: string
  amount: number
}

export interface FactEventForm {
  type: string
  amount: number | undefined
  businessDate: string
  accountingDate: string
  subjectId: number | undefined
  counterpartyId: number | undefined
  costCategory: string
  amortizeStart: string
  amortizeEnd: string
  amortizeMethod: string
  invoiceNo: string
  remark: string
}

export interface FactEventQuery {
  type?: string
  status?: string
  startDate?: string
  endDate?: string
}

export function fetchFactEventList(query: FactEventQuery = {}) {
  const params: Record<string, string> = {}
  if (query.type) params.type = query.type
  if (query.status) params.status = query.status
  if (query.startDate) params.startDate = query.startDate
  if (query.endDate) params.endDate = query.endDate
  return http.get<ApiResponse<FactEventVO[]>>('/fact-event', { params })
}

export function fetchFactEventDetail(id: number) {
  return http.get<ApiResponse<FactEventVO>>(`/fact-event/${id}`)
}

export function createFactEvent(data: FactEventForm) {
  return http.post<ApiResponse<FactEventVO>>('/fact-event', data)
}

export function reverseFactEvent(id: number) {
  return http.post<ApiResponse<void>>(`/fact-event/${id}/reverse`)
}

export function fetchAmortizationEntries(id: number) {
  return http.get<ApiResponse<AmortizationEntryVO[]>>(`/fact-event/${id}/amortization`)
}
