import { http } from './http'

export interface MonthTrend {
  month: string
  income: number
  cost: number
  profit: number
}

export interface TopCustomer {
  counterpartyId: number
  counterpartyName: string
  income: number
  cost: number
  profit: number
}

export interface DashboardSummary {
  totalIncome: number
  totalCost: number
  totalProfit: number
  profitRate: number
  monthIncome: number
  monthCost: number
  monthProfit: number
  projectCount: number
  activeProjectCount: number
  contractCount: number
  activeContractCount: number
  receivableRemaining: number
  payableRemaining: number
  overdueReceivableCount: number
  overduePayableCount: number
  monthTrends: MonthTrend[]
  topCustomers: TopCustomer[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function fetchDashboardSummary() {
  return http.get<ApiResponse<DashboardSummary>>('/dashboard/summary')
}
