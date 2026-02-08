/**
 * 顧客 API（/api/v1/customers）
 * 注文フォームの顧客ドロップダウンなどで利用
 */
import { apiGet } from './client'
import type { Customer } from '../types/entity'
import type { PageResponse } from './types'

const BASE = '/api/v1/customers'

/** 顧客検索クエリパラメータ */
export type CustomerSearchParams = {
  customerNumber?: string
  customerName?: string
  pageNum?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

/** 顧客一覧検索（ページング対応） */
export function searchCustomers(params: CustomerSearchParams = {}): Promise<PageResponse<Customer>> {
  return apiGet<PageResponse<Customer>>(`${BASE}/search`, {
    customerNumber: params.customerNumber,
    customerName: params.customerName,
    deleteFlag: false,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 100,
    sortBy: params.sortBy ?? 'updateDate',
    sortOrder: params.sortOrder ?? 'desc',
  })
}
