/**
 * フォームの1フィールド用ラップ（label + 子要素）
 * layout-form-group / layout-form-label を付与
 */
type FormGroupProps = {
  id?: string
  label?: string
  children: React.ReactNode
  className?: string
}

export function FormGroup({ id, label, children, className = '' }: FormGroupProps) {
  return (
    <div className={`layout-form-group ${className}`.trim()}>
      {label != null && (
        <label htmlFor={id} className="layout-form-label">
          {label}
        </label>
      )}
      {children}
    </div>
  )
}
