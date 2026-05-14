import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface ContractVO {
  id: number
  code: string
  name: string
  counterpartyId: number
  counterpartyName: string
  projectId: number | null
  projectName: string | null
  type: string
  typeName: string
  amount: number
  signDate: string | null
  startDate: string | null
  endDate: string | null
  status: string
  statusName: string
  remark: string | null
  createdAt: string
  updatedAt: string
}

export interface ContractForm {
  code: string
  name: string
  counterpartyId: number | undefined
  projectId: number | undefined
  type: string
  amount: number | undefined
  signDate: string
  startDate: string
  endDate: string
  remark: string
}

export function fetchContractList(keyword?: string, type?: string, status?: string) {
  const params: Record<string, string> = {}
  if (keyword) params.keyword = keyword
  if (type) params.type = type
  if (status) params.status = status
  return http.get<ApiResponse<ContractVO[]>>('/contract', { params })
}

export function fetchContractDetail(id: number) {
  return http.get<ApiResponse<ContractVO>>(`/contract/${id}`)
}

export function createContract(data: ContractForm) {
  return http.post<ApiResponse<ContractVO>>('/contract', data)
}

export function updateContract(id: number, data: ContractForm) {
  return http.put<ApiResponse<ContractVO>>(`/contract/${id}`, data)
}

export function deleteContract(id: number) {
  return http.delete<ApiResponse<void>>(`/contract/${id}`)
}

export function transitionContract(id: number, action: string) {
  return http.post<ApiResponse<void>>(`/contract/${id}/transition`, { action })
}
