/**
 * 汎用一覧テーブル（layout-table）
 * - columns: 列定義（key / header / cellClassName）
 * - data: 行データ配列
 * - getRowKey: 行の一意キー
 * - renderCell: セル内容（テキスト・バッジ・ボタンなどは親で分岐）
 */
export type TableColumn<T> = {
  key: string
  header: string
  /** セル（td）に付与するクラス名 */
  cellClassName?: string
}

type TableProps<T> = {
  columns: readonly TableColumn<T>[]
  data: readonly T[]
  getRowKey: (row: T) => string
  renderCell: (columnKey: string, row: T) => React.ReactNode
}

export function Table<T>({ columns, data, getRowKey, renderCell }: TableProps<T>) {
  return (
    <table className="layout-table">
      <thead>
        <tr>
          {columns.map((col) => (
            <th key={col.key}>{col.header}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {data.map((row) => (
          <tr key={getRowKey(row)}>
            {columns.map((col) => (
              <td key={col.key} className={col.cellClassName}>
                {renderCell(col.key, row)}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  )
}
