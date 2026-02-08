/**
 * 共通 select（layout-form-input 付与）
 * options: { value, label }[] で選択肢を渡す
 */
type Option = { value: string; label: string }

type FormSelectProps = Omit<React.ComponentPropsWithoutRef<'select'>, 'className'> & {
  options: Option[]
  className?: string
}

export function FormSelect({ options, className = '', ...props }: FormSelectProps) {
  return (
    <select className={`layout-form-input ${className}`.trim()} {...props}>
      {options.map((opt) => (
        <option key={opt.value || '__empty__'} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>
  )
}
