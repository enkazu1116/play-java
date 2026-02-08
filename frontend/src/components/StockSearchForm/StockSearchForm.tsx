/**
 * 在庫検索フォーム
 * - 商品はドロップダウン選択（ID 入力なし）
 * - 在庫数範囲・ステータスで検索
 * - 親が values / onChange / onSubmit で制御（controlled）
 */
import { Button } from '../Button'
import { FormGroup, FormInput, FormSelect, SearchForm } from '../form'
import type { StockSearchValues, Product } from '../../types/entity'

const STOCK_STATUS_OPTIONS = [
  { value: '', label: 'すべて' },
  { value: '0', label: '在庫あり' },
  { value: '1', label: '在庫なし' },
  { value: '2', label: '発注済み' },
]

type StockSearchFormProps = {
  values: StockSearchValues
  onChange: (values: StockSearchValues) => void
  onSubmit: (e: React.FormEvent) => void
  /** 商品一覧（API取得想定）。IDは入力させず選択のみ */
  products: Product[]
}

export function StockSearchForm({ values, onChange, onSubmit, products }: StockSearchFormProps) {
  const set = (key: keyof StockSearchValues) => (value: string) =>
    onChange({ ...values, [key]: value })

  const productOptions = [
    { value: '', label: 'すべて' },
    ...products.map((p) => ({
      value: p.productId,
      label: `${p.productNumber} ${p.productName}`.trim() || p.productId,
    })),
  ]

  return (
    <SearchForm onSubmit={onSubmit}>
      <FormGroup id="search-product" label="商品">
        <FormSelect
          id="search-product"
          options={productOptions}
          value={values.productId}
          onChange={(e) => set('productId')(e.target.value)}
        />
      </FormGroup>
      <FormGroup id="search-quantity-min" label="在庫数（以上）">
        <FormInput
          id="search-quantity-min"
          type="number"
          min={0}
          placeholder="0"
          value={values.quantityMin}
          onChange={(e) => set('quantityMin')(e.target.value)}
        />
      </FormGroup>
      <FormGroup id="search-quantity-max" label="在庫数（以下）">
        <FormInput
          id="search-quantity-max"
          type="number"
          min={0}
          placeholder=""
          value={values.quantityMax}
          onChange={(e) => set('quantityMax')(e.target.value)}
        />
      </FormGroup>
      <FormGroup id="search-status" label="ステータス">
        <FormSelect
          id="search-status"
          options={STOCK_STATUS_OPTIONS}
          value={values.status}
          onChange={(e) => set('status')(e.target.value)}
        />
      </FormGroup>
      <Button type="submit" variant="primary">検索</Button>
    </SearchForm>
  )
}
