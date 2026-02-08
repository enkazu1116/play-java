/**
 * 一覧用ページネーション
 * - 前へ / 次へ で親がページ番号を更新し API を再取得する想定
 */
import { Button } from '../Button'

type PaginationProps = {
  page: number
  totalPages: number
  onPrev: () => void
  onNext: () => void
}

export function Pagination({ page, totalPages, onPrev, onNext }: PaginationProps) {
  return (
    <nav className="layout-pagination">
      <Button variant="default" onClick={onPrev}>前へ</Button>
      <span>{page} / {totalPages} ページ</span>
      <Button variant="default" onClick={onNext}>次へ</Button>
    </nav>
  )
}
