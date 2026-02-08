/**
 * 注文 API（/api/v1/orders）
 */
import { apiGet, apiPut } from './client'
import type { Order } from '../types/entity'
import type { PageResponse } from './types'

const BASE = '/api/v1/orders'

/** 注文検索クエリパラメータ */
export type OrderSearchParams = {
  orderId?: string
  customerId?: string
  orderDateFrom?: string
  orderDateTo?: string
  status?: number
  deleteFlag?: boolean
  pageNum?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

/** 注文一覧検索（ページング・ソート対応） */
export function searchOrders(params: OrderSearchParams = {}): Promise<PageResponse<Order>> {
  return apiGet<PageResponse<Order>>(`${BASE}/search`, {
    orderId: params.orderId,
    customerId: params.customerId || undefined,
    orderDateFrom: params.orderDateFrom,
    orderDateTo: params.orderDateTo,
    status: params.status,
    deleteFlag: params.deleteFlag,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    sortBy: params.sortBy ?? 'orderDate',
    sortOrder: params.sortOrder ?? 'desc',
  })
}

/** 注文確定（仮注文・取り寄せ中・カスタマイズ中 → 確定） */
export function confirmOrder(orderId: string): Promise<void> {
  return apiPut<void>(`${BASE}/${orderId}/confirm`)
}

/** 注文キャンセル（既存注文をキャンセル済みに更新） */
export function cancelOrder(orderId: string): Promise<void> {
  return apiPut<void>(`${BASE}/${orderId}/cancel`)
}
