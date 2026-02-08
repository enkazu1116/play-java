/**
 * API クライアント
 * - ベースURL: VITE_API_BASE_URL（未設定時は '' = 相対パス。Vite プロキシで /api をバックエンドへ転送する想定）
 * - エラー時は ApiError をスロー（status / body を保持）
 */

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

/** API エラー（HTTP status とレスポンス body を保持） */
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public body?: unknown
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

/** 共通 fetch：JSON 送受信・エラー時 ApiError スロー */
async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE}${path}`
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })
  const text = await res.text()
  let body: unknown
  try {
    body = text ? JSON.parse(text) : undefined
  } catch {
    body = text
  }
  if (!res.ok) {
    throw new ApiError(
      (body && typeof body === 'object' && 'message' in body ? String((body as { message: unknown }).message) : null) ?? `HTTP ${res.status}`,
      res.status,
      body
    )
  }
  return body as T
}

/** GET（クエリパラメータは params で渡す。undefined / '' は付与しない） */
export async function apiGet<T>(path: string, params?: Record<string, string | number | boolean | undefined>): Promise<T> {
  const search = params
    ? '?' +
      Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== '')
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
        .join('&')
    : ''
  return request<T>(`${path}${search}`)
}

/** POST（body を JSON で送信） */
export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  return request<T>(path, { method: 'POST', body: JSON.stringify(body) })
}

/** PUT（body 省略可） */
export async function apiPut<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'PUT', body: body != null ? JSON.stringify(body) : undefined })
}

/** DELETE */
export async function apiDelete<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'DELETE' })
}
