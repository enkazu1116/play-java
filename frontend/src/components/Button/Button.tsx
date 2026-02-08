/**
 * 共通ボタンコンポーネント
 * - variant で見た目を切り替え（default / primary / danger）
 * - 呼び出す API は親が onClick で渡す
 */
const VARIANT_CLASS = {
  default: 'layout-btn-default',
  primary: 'layout-btn-primary',
  danger: 'layout-btn-danger',
} as const

type ButtonProps = {
  variant?: keyof typeof VARIANT_CLASS
  type?: 'button' | 'submit'
  onClick?: () => void
  disabled?: boolean
  children: React.ReactNode
  className?: string
}

export function Button({
  variant = 'default',
  type = 'button',
  onClick,
  disabled = false,
  children,
  className = '',
}: ButtonProps) {
  const classNames = [VARIANT_CLASS[variant], className].filter(Boolean).join(' ')
  return (
    <button type={type} className={classNames} onClick={onClick} disabled={disabled}>
      {children}
    </button>
  )
}
