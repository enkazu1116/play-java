/**
 * バックエンド Entity に合わせた型定義
 * - バックエンドの TOrder / MStock / MCustomer / MProduct などに対応
 * - ID は API で自動採番されるため、フォームでは入力させず選択または表示のみ
 */

/** TOrder: 注文（orderId は自動採番） */
export type Order = {
  orderId: string
  customerId: string
  orderDate: string
  status: number
}

/** TOrderItem: 注文明細 */
export type OrderItem = {
  orderItemId: string
  orderId: string
  productId: string
  quantity: number
  unitPrice: number
}

/** MCustomer: 顧客（customerId は自動採番、選択時は value に使用） */
export type Customer = {
  customerId: string
  customerNumber: string
  customerName: string
  address?: string
  mobileNumber?: string
  email?: string
}

/** MProduct: 商品（productId は自動採番、選択時は value に使用） */
export type Product = {
  productId: string
  productNumber: string
  productName: string
  description?: string
  price: number
  category?: number
}

/** MStock: 在庫（stockId は自動採番） */
export type Stock = {
  stockId: string
  productId: string
  quantity: number
  status: number
  updateDate?: string
}

/** 注文検索条件（ID 入力なし: 顧客は選択、注文日・ステータス） */
export type OrderSearchValues = {
  customerId: string
  orderDateFrom: string
  orderDateTo: string
  status: string
}

/** 在庫検索条件（ID 入力なし: 商品は選択、在庫数・ステータス） */
export type StockSearchValues = {
  productId: string
  quantityMin: string
  quantityMax: string
  status: string
}
