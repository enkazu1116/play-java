/**
 * 在庫 API（/api/v1/stocks）
 */
import { apiGet, apiPost, apiPut, apiDelete } from './client'
import type { Stock } from '../types/entity'
import type { PageResponse } from './types'

const BASE = '/api/v1/stocks'

/** 在庫検索クエリパラメータ */
export type StockSearchParams = {
  productId?: string
  quantityMin?: number
  quantityMax?: number
  status?: number
  deleteFlag?: boolean
  pageNum?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

/** 在庫一覧検索（ページング・ソート対応） */
export function searchStocks(params: StockSearchParams = {}): Promise<PageResponse<Stock>> {
  return apiGet<PageResponse<Stock>>(`${BASE}/search`, {
    productId: params.productId || undefined,
    quantityMin: params.quantityMin,
    quantityMax: params.quantityMax,
    status: params.status,
    deleteFlag: params.deleteFlag ?? false,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? 10,
    sortBy: params.sortBy ?? 'updateDate',
    sortOrder: params.sortOrder ?? 'desc',
  })
}

/** 在庫登録（stockId はバックエンドで自動採番） */
export function createStock(body: { productId: string; quantity: number; status: number }): Promise<void> {
  return apiPost<void>(BASE, body)
}

/** 在庫更新 */
export function updateStock(body: Stock): Promise<void> {
  return apiPut<void>(`${BASE}/updateStock`, body)
}

/** 在庫論理削除 */
export function deleteStock(stockId: string): Promise<void> {
  return apiDelete<void>(`${BASE}/deleteStock/${stockId}`)
}
