/**
 * アプリ内ナビゲーションタブ
 * - tabs: タブの value / label 配列
 * - activeValue: 現在選択中の value
 * - onChange: タブクリック時に親へ value を通知
 */
export type NavTabItem = {
  value: string
  label: string
}

type NavTabsProps = {
  tabs: readonly NavTabItem[]
  activeValue: string
  onChange: (value: string) => void
}

export function NavTabs({ tabs, activeValue, onChange }: NavTabsProps) {
  return (
    <nav className="border-b border-gray-200 bg-white px-4 py-3">
      <div className="flex gap-4">
        {tabs.map((tab) => (
          <button
            key={tab.value}
            type="button" 
            onClick={() => onChange(tab.value)}
            className={`font-medium ${activeValue === tab.value ? 'text-gray-900 underline' : 'text-gray-600 hover:text-gray-900'}`}
          >
            {tab.label}
          </button>
        ))}
      </div>
    </nav>
  )
}
