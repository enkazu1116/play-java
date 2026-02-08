/**
 * API 共通型
 */

/**
 * バックエンドの MyBatis-Plus IPage に相当するページングレスポンス
 * - records: 当該ページのデータ配列
 * - total: 全体件数
 * - current: 現在ページ番号（1始まり）
 * - pages: 総ページ数
 */
export type PageResponse<T> = {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
