/**
 * アプリルート
 * - 上部に NavTabs（注文管理 / 在庫管理）で画面切り替え
 * - 選択中のタブに応じて OrderManagement / InventoryManagement を表示
 */
import { useState } from 'react'
import { InventoryManagement } from './components/InventoryManagement'
import { NavTabs, type NavTabItem } from './components/NavTabs'
import { OrderManagement } from './components/OrderManagement'

type Page = 'orders' | 'inventory'

/** ナビゲーションタブの定義 */
const APP_TABS: readonly NavTabItem[] = [
  { value: 'orders', label: '注文管理' },
  { value: 'inventory', label: '在庫管理' },
]

function App() {
  const [page, setPage] = useState<Page>('orders')

  return (
    <div className="min-h-screen bg-gray-50">
      <NavTabs
        tabs={APP_TABS}
        activeValue={page}
        onChange={(value) => setPage(value as Page)}
      />
      <main>
        {page === 'orders' && <OrderManagement />}
        {page === 'inventory' && <InventoryManagement />}
      </main>
    </div>
  )
}

export default App
