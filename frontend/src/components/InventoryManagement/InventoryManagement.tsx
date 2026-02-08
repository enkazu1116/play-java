/**
 * 在庫管理画面
 * - 在庫一覧は searchStocks で取得（検索・ページ送りで再取得）
 * - 削除: deleteStock を呼び出し（確認ダイアログあり）
 * - 商品ドロップダウンは商品 API 未実装のためモックを使用
 */
import { useState, useEffect, useCallback } from 'react'
import { Button } from '../Button'
import { Pagination } from '../Pagination'
import { StockSearchForm } from '../StockSearchForm'
import { Table, type TableColumn } from '../Table'
import { Title } from '../Title'
import { searchStocks, deleteStock } from '../../api/stocks'
import type { Stock, StockSearchValues, Product } from '../../types/entity'

/** 在庫ステータス表示用ラベル */
const STOCK_STATUS_LABELS: Record<number, string> = {
  0: '在庫あり',
  1: '在庫なし',
  2: '発注済み',
}

/** 一覧表示用（商品名は API に含まれない場合は productId を表示） */
type StockRow = Stock & { productNumber?: string; productName?: string }

function StockStatusBadge({ status }: { status: number }) {
  const label = STOCK_STATUS_LABELS[status] ?? '不明'
  return <span className={`layout-badge-stock-${status}`}>{label}</span>
}

const initialSearchValues: StockSearchValues = {
  productId: '',
  quantityMin: '',
  quantityMax: '',
  status: '',
}

/** 商品一覧は別APIがないためモック。必要に応じて /api/v1/products を追加してください */
const MOCK_PRODUCTS: Product[] = [
  { productId: 'p111', productNumber: 'P001', productName: '商品A', price: 1000 },
  { productId: 'p222', productNumber: 'P002', productName: '商品B', price: 2000 },
]

const STOCK_COLUMNS: readonly TableColumn<StockRow>[] = [
  { key: 'stockId', header: '在庫ID' },
  { key: 'product', header: '商品' },
  { key: 'quantity', header: '数量' },
  { key: 'status', header: 'ステータス' },
  { key: 'updateDate', header: '更新日時' },
  { key: 'actions', header: '操作', cellClassName: 'layout-detail-actions' },
]

export function InventoryManagement() {
  const [stocks, setStocks] = useState<StockRow[]>([])
  const [products] = useState<Product[]>(MOCK_PRODUCTS)
  const [searchValues, setSearchValues] = useState<StockSearchValues>(initialSearchValues)
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchStocks = useCallback(async (pageNum: number, values: StockSearchValues) => {
    setLoading(true)
    setError(null)
    try {
      const res = await searchStocks({
        productId: values.productId || undefined,
        quantityMin: values.quantityMin ? Number(values.quantityMin) : undefined,
        quantityMax: values.quantityMax ? Number(values.quantityMax) : undefined,
        status: values.status ? Number(values.status) : undefined,
        pageNum: pageNum,
        pageSize: 10,
        sortBy: 'updateDate',
        sortOrder: 'desc',
      })
      setStocks((res.records ?? []) as StockRow[])
      setTotalPages(Math.max(1, res.pages ?? 1))
      setPage(pageNum)
    } catch (e) {
      setError(e instanceof Error ? e.message : '在庫一覧の取得に失敗しました')
    } finally {
      setLoading(false)
    }
  }, [])

  // マウント時に在庫一覧を取得
  useEffect(() => {
    fetchStocks(1, initialSearchValues)
  }, []) // eslint-disable-line react-hooks/exhaustive-deps -- 初回のみ

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    fetchStocks(1, searchValues)
  }

  const handlePrev = () => {
    if (page <= 1) return
    fetchStocks(page - 1, searchValues)
  }

  const handleNext = () => {
    if (page >= totalPages) return
    fetchStocks(page + 1, searchValues)
  }

  const handleDeleteStock = async (stockId: string) => {
    if (!window.confirm('この在庫を削除しますか？')) return
    try {
      await deleteStock(stockId)
      fetchStocks(page, searchValues)
    } catch (e) {
      setError(e instanceof Error ? e.message : '在庫の削除に失敗しました')
    }
  }

  return (
    <div className="layout-container">
      <Title>在庫管理</Title>

      <section className="layout-section">
        <h2 className="layout-section-title">在庫一覧</h2>

        {error != null && <p className="text-red-600 text-sm mb-2">{error}</p>}

        <StockSearchForm
          values={searchValues}
          onChange={setSearchValues}
          onSubmit={handleSearch}
          products={products}
        />

        <div className="layout-form-actions">
          <Button variant="primary" onClick={() => { /* TODO: 在庫登録モーダル */ }}>新規登録</Button>
        </div>

        {loading ? (
          <p className="py-4">読み込み中...</p>
        ) : (
          <>
            <Table<StockRow>
              columns={STOCK_COLUMNS}
              data={stocks}
              getRowKey={(row) => row.stockId}
              renderCell={(key, stock) => {
                switch (key) {
                  case 'stockId':
                    return stock.stockId
                  case 'product':
                    return stock.productName != null ? `${stock.productNumber} ${stock.productName}`.trim() : stock.productId
                  case 'quantity':
                    return stock.quantity
                  case 'status':
                    return <StockStatusBadge status={stock.status} />
                  case 'updateDate':
                    return stock.updateDate != null ? String(stock.updateDate) : ''
                  case 'actions':
                    return (
                      <>
                        <Button variant="default" onClick={() => { /* TODO: 在庫編集モーダル */ }}>編集</Button>
                        <Button variant="danger" onClick={() => handleDeleteStock(stock.stockId)}>削除</Button>
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
