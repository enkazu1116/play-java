/**
 * 注文管理画面
 * - 顧客一覧は API で取得し検索フォームのドロップダウンに使用
 * - 注文一覧は searchOrders で取得（検索・ページ送りで再取得）
 * - 確定: confirmOrder / キャンセル: cancelOrder を呼び出し
 */
import { useState, useEffect, useCallback } from 'react'
import { Button } from '../Button'
import { OrderSearchForm } from '../OrderSearchForm'
import { Pagination } from '../Pagination'
import { Table, type TableColumn } from '../Table'
import { Title } from '../Title'
import { searchOrders, confirmOrder, cancelOrder } from '../../api/orders'
import { searchCustomers } from '../../api/customers'
import type { Order, OrderSearchValues, Customer } from '../../types/entity'

/** 注文ステータス表示用ラベル */
const ORDER_STATUS_LABELS: Record<number, string> = {
  0: '仮注文',
  1: '注文確定',
  2: '取り寄せ中',
  3: 'カスタマイズ中',
  4: '配送中',
  5: '配送完了',
  6: 'キャンセル済み',
}

/** 一覧表示用（顧客名は API に含まれない場合は customerId を表示） */
type OrderRow = Order & { customerNumber?: string; customerName?: string }

function OrderStatusBadge({ status }: { status: number }) {
  const label = ORDER_STATUS_LABELS[status] ?? '不明'
  return <span className={`layout-badge-order-${status}`}>{label}</span>
}

const initialSearchValues: OrderSearchValues = {
  customerId: '',
  orderDateFrom: '',
  orderDateTo: '',
  status: '',
}

const ORDER_COLUMNS: readonly TableColumn<OrderRow>[] = [
  { key: 'orderId', header: '注文ID' },
  { key: 'customer', header: '顧客' },
  { key: 'orderDate', header: '注文日時' },
  { key: 'status', header: 'ステータス' },
  { key: 'actions', header: '操作' },
]

export function OrderManagement() {
  const [orders, setOrders] = useState<OrderRow[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [searchValues, setSearchValues] = useState<OrderSearchValues>(initialSearchValues)
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchCustomers = useCallback(async () => {
    try {
      const res = await searchCustomers({ pageSize: 500 })
      setCustomers(res.records ?? [])
    } catch (e) {
      setError(e instanceof Error ? e.message : '顧客一覧の取得に失敗しました')
    }
  }, [])

  const fetchOrders = useCallback(async (pageNum: number, values: OrderSearchValues) => {
    setLoading(true)
    setError(null)
    try {
      const res = await searchOrders({
        customerId: values.customerId || undefined,
        orderDateFrom: values.orderDateFrom || undefined,
        orderDateTo: values.orderDateTo || undefined,
        status: values.status ? Number(values.status) : undefined,
        pageNum: pageNum,
        pageSize: 10,
        sortBy: 'orderDate',
        sortOrder: 'desc',
      })
      setOrders((res.records ?? []) as OrderRow[])
      setTotalPages(Math.max(1, res.pages ?? 1))
      setPage(pageNum)
    } catch (e) {
      setError(e instanceof Error ? e.message : '注文一覧の取得に失敗しました')
    } finally {
      setLoading(false)
    }
  }, [])

  // マウント時に顧客一覧を取得（検索フォームのドロップダウン用）
  useEffect(() => {
    fetchCustomers()
  }, [fetchCustomers])

  // マウント時に注文一覧を取得
  useEffect(() => {
    fetchOrders(1, initialSearchValues)
  }, []) // eslint-disable-line react-hooks/exhaustive-deps -- 初回のみ

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    fetchOrders(1, searchValues)
  }

  const handlePrev = () => {
    if (page <= 1) return
    fetchOrders(page - 1, searchValues)
  }

  const handleNext = () => {
    if (page >= totalPages) return
    fetchOrders(page + 1, searchValues)
  }

  const handleConfirmOrder = async (orderId: string) => {
    try {
      await confirmOrder(orderId)
      fetchOrders(page, searchValues)
    } catch (e) {
      setError(e instanceof Error ? e.message : '注文の確定に失敗しました')
    }
  }

  const handleCancelOrder = async (orderId: string) => {
    try {
      await cancelOrder(orderId)
      fetchOrders(page, searchValues)
    } catch (e) {
      setError(e instanceof Error ? e.message : '注文のキャンセルに失敗しました')
    }
  }

  return (
    <div className="layout-container">
      <Title>注文管理</Title>

      <section className="layout-section">
        <h2 className="layout-section-title">注文一覧</h2>

        {error != null && <p className="text-red-600 text-sm mb-2">{error}</p>}

        <OrderSearchForm
          values={searchValues}
          onChange={setSearchValues}
          onSubmit={handleSearch}
          customers={customers}
        />

        <div className="layout-form-actions">
          <Button variant="primary" onClick={() => { /* TODO: 新規注文画面へ */ }}>新規注文</Button>
        </div>

        {loading ? (
          <p className="py-4">読み込み中...</p>
        ) : (
          <>
            <Table<OrderRow>
              columns={ORDER_COLUMNS}
              data={orders}
              getRowKey={(row) => row.orderId}
              renderCell={(key, order) => {
                switch (key) {
                  case 'orderId':
                    return order.orderId
                  case 'customer':
                    return order.customerName != null ? `${order.customerNumber} ${order.customerName}`.trim() : order.customerId
                  case 'orderDate':
                    return typeof order.orderDate === 'string' ? order.orderDate : String(order.orderDate)
                  case 'status':
                    return <OrderStatusBadge status={order.status} />
                  case 'actions':
                    return (
                      <>
                        {order.status === 0 && (
                          <Button variant="default" onClick={() => handleConfirmOrder(order.orderId)}>確定</Button>
                        )}
                        {order.status !== 6 && order.status !== 5 && (
                          <Button variant="danger" onClick={() => handleCancelOrder(order.orderId)}>キャンセル</Button>
                        )}
                      </>
                    )
                  default:
                    return null
                }
              }}
            />

            <Pagination
              page={page}
              totalPages={totalPages}
              onPrev={handlePrev}
              onNext={handleNext}
            />
          </>
        )}
      </section>
    </div>
  )
}
