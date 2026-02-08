/**
 * 共通 input（layout-form-input 付与）
 * type / value / onChange などはそのまま渡す
 */
type FormInputProps = Omit<React.ComponentPropsWithoutRef<'input'>, 'className'> & {
  className?: string
}

export function FormInput({ className = '', ...props }: FormInputProps) {
  return <input className={`layout-form-input ${className}`.trim()} {...props} />
}
