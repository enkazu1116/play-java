/**
 * ページタイトル（h1 + layout-page-title）
 */
type TitleProps = {
  children: React.ReactNode
}

export function Title({ children }: TitleProps) {
  return <h1 className="layout-page-title">{children}</h1>
}
