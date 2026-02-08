/**
 * 検索フォーム用ラップ（layout-search-form）
 * 検索・一覧画面の検索条件エリアで利用
 */
type SearchFormProps = {
  onSubmit: (e: React.FormEvent) => void
  children: React.ReactNode
  className?: string
}

export function SearchForm({ onSubmit, children, className = '' }: SearchFormProps) {
  return (
    <form className={`layout-search-form ${className}`.trim()} onSubmit={onSubmit}>
      {children}
    </form>
  )
}
