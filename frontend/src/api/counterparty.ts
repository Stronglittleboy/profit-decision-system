import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface CounterpartyVO {
  id: number
  name: string
  type: string
  typeName: string
  contact: string
  phone: string
  address: string
  taxNo: string
  creditLevel: string | null
  creditLevelName: string | null
  enabled: boolean
  remark: string
  createdAt: string
  updatedAt: string
}

export interface CounterpartyForm {
  name: string
  type: string
  contact: string
  phone: string
  address: string
  taxNo: string
  creditLevel: string
  remark: string
}

export function fetchCounterpartyList(keyword?: string, type?: string) {
  const params: Record<string, string> = {}
  if (keyword) params.keyword = keyword
  if (type) params.type = type
  return http.get<ApiResponse<CounterpartyVO[]>>('/counterparty', { params })
}

export function fetchCounterpartyDetail(id: number) {
  return http.get<ApiResponse<CounterpartyVO>>(`/counterparty/${id}`)
}

export function createCounterparty(data: CounterpartyForm) {
  return http.post<ApiResponse<CounterpartyVO>>('/counterparty', data)
}

export function updateCounterparty(id: number, data: CounterpartyForm) {
  return http.put<ApiResponse<CounterpartyVO>>(`/counterparty/${id}`, data)
}

export function deleteCounterparty(id: number) {
  return http.delete<ApiResponse<void>>(`/counterparty/${id}`)
}

export function toggleCounterpartyStatus(id: number, enabled: boolean) {
  return http.patch<ApiResponse<void>>(`/counterparty/${id}/status`, { enabled })
}
