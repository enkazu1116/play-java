/**
 * 注文検索フォーム
 * - 顧客はドロップダウン選択（ID 入力なし）
 * - 注文日範囲・ステータスで検索
 * - 親が values / onChange / onSubmit で制御（controlled）
 */
import { Button } from '../Button'
import { FormGroup, FormInput, FormSelect, SearchForm } from '../form'
import type { OrderSearchValues, Customer } from '../../types/entity'

const ORDER_STATUS_OPTIONS = [
  { value: '', label: 'すべて' },
  { value: '0', label: '仮注文' },
  { value: '1', label: '注文確定' },
  { value: '2', label: '取り寄せ中' },
  { value: '3', label: 'カスタマイズ中' },
  { value: '4', label: '配送中' },
  { value: '5', label: '配送完了' },
  { value: '6', label: 'キャンセル済み' },
]

type OrderSearchFormProps = {
  values: OrderSearchValues
  onChange: (values: OrderSearchValues) => void
  onSubmit: (e: React.FormEvent) => void
  /** 顧客一覧（API取得想定）。IDは入力させず選択のみ */
  customers: Customer[]
}

export function OrderSearchForm({ values, onChange, onSubmit, customers }: OrderSearchFormProps) {
  const set = (key: keyof OrderSearchValues) => (value: string) =>
    onChange({ ...values, [key]: value })

  const customerOptions = [
    { value: '', label: 'すべて' },
    ...customers.map((c) => ({
      value: c.customerId,
      label: `${c.customerNumber} ${c.customerName}`.trim() || c.customerId,
    })),
  ]

  return (
    <SearchForm onSubmit={onSubmit}>
      <FormGroup id="search-customer" label="顧客">
        <FormSelect
          id="search-customer"
          options={customerOptions}
          value={values.customerId}
          onChange={(e) => set('customerId')(e.target.value)}
        />
      </FormGroup>
      <FormGroup id="search-order-date-from" label="注文日（開始）">
        <FormInput
          id="search-order-date-from"
          type="datetime-local"
          value={values.orderDateFrom}
          onChange={(e) => set('orderDateFrom')(e.target.value)}
        />
      </FormGroup>
      <FormGroup id="search-order-date-to" label="注文日（終了）">
        <FormInput
          id="search-order-date-to"
          type="datetime-local"
          value={values.orderDateTo}
          onChange={(e) => set('orderDateTo')(e.target.value)}
        />
      </FormGroup>
      <FormGroup id="search-status" label="ステータス">
        <FormSelect
          id="search-status"
          options={ORDER_STATUS_OPTIONS}
          value={values.status}
          onChange={(e) => set('status')(e.target.value)}
        />
      </FormGroup>
      <Button type="submit" variant="primary">検索</Button>
    </SearchForm>
  )
}
