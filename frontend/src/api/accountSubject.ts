import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface AccountSubjectTreeNode {
  id: number
  code: string
  name: string
  parentId: number | null
  level: number
  type: string
  typeName: string
  debitCredit: string
  debitCreditName: string
  enabled: boolean
  sort: number
  remark: string
  children: AccountSubjectTreeNode[]
}

export interface AccountSubjectVO {
  id: number
  code: string
  name: string
  parentId: number | null
  level: number
  type: string
  typeName: string
  debitCredit: string
  debitCreditName: string
  enabled: boolean
  sort: number
  remark: string
  createdAt: string
  updatedAt: string
}

export interface AccountSubjectForm {
  code: string
  name: string
  parentId: number | null
  type: string
  debitCredit: string
  sort: number
  remark: string
}

export function fetchSubjectTree(keyword?: string) {
  return http.get<ApiResponse<AccountSubjectTreeNode[]>>('/account-subject/tree', {
    params: keyword ? { keyword } : {}
  })
}

export function fetchSubjectDetail(id: number) {
  return http.get<ApiResponse<AccountSubjectVO>>(`/account-subject/${id}`)
}

export function createSubject(data: AccountSubjectForm) {
  return http.post<ApiResponse<AccountSubjectVO>>('/account-subject', data)
}

export function updateSubject(id: number, data: AccountSubjectForm) {
  return http.put<ApiResponse<AccountSubjectVO>>(`/account-subject/${id}`, data)
}

export function deleteSubject(id: number) {
  return http.delete<ApiResponse<void>>(`/account-subject/${id}`)
}

export function toggleSubjectStatus(id: number, enabled: boolean) {
  return http.patch<ApiResponse<void>>(`/account-subject/${id}/status`, { enabled })
}
