import { http } from './http'
import type { ApiResponse } from './dashboard'

export interface CustomerRankVO {
  counterpartyId: number
  counterpartyName: string
  income: number
  cost: number
  profit: number
  profitRate: number
}

export function fetchCustomerRank(startDate?: string, endDate?: string) {
  const params: Record<string, string> = {}
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  return http.get<ApiResponse<CustomerRankVO[]>>('/analysis/customer-rank', { params })
}
