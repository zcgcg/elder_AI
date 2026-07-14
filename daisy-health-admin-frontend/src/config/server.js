const SERVER_ORIGIN_KEY = 'daisy_server_origin'

export function normalizeServerOrigin(value, { native = false } = {}) {
  const input = String(value || '').trim()
  if (!input) throw new Error('请输入服务器地址')

  let url
  try {
    url = new URL(input)
  } catch {
    throw new Error('服务器地址格式不正确，请填写完整的 http:// 或 https:// 地址')
  }

  if (!['http:', 'https:'].includes(url.protocol)) {
    throw new Error('服务器地址只支持 http:// 或 https://')
  }
  if (native && ['localhost', '127.0.0.1', '::1', '[::1]'].includes(url.hostname)) {
    throw new Error('手机 App 不能使用 localhost 或 127.0.0.1，请填写电脑的 WLAN IPv4 地址')
  }
  if (native && url.protocol === 'http:' && !isPrivateIpv4(url.hostname)) {
    throw new Error('公网服务器必须使用 HTTPS；HTTP 仅允许局域网 IPv4 地址')
  }
  if (url.username || url.password || url.search || url.hash || (url.pathname && url.pathname !== '/')) {
    throw new Error('服务器地址只能包含协议、主机和端口，不能包含路径、参数或账号信息')
  }

  return url.origin
}

function isPrivateIpv4(hostname) {
  const parts = hostname.split('.').map(Number)
  if (parts.length !== 4 || parts.some((part) => !Number.isInteger(part) || part < 0 || part > 255)) return false
  return parts[0] === 10
    || (parts[0] === 172 && parts[1] >= 16 && parts[1] <= 31)
    || (parts[0] === 192 && parts[1] === 168)
}

export function createServerConfig({ native, preferences }) {
  let origin = ''

  return {
    async initialize() {
      if (!native) return ''
      const stored = await preferences.get({ key: SERVER_ORIGIN_KEY })
      if (!stored?.value) return ''
      try {
        origin = normalizeServerOrigin(stored.value, { native: true })
      } catch {
        origin = ''
      }
      return origin
    },

    async save(value) {
      const nextOrigin = normalizeServerOrigin(value, { native })
      const changed = Boolean(origin && origin !== nextOrigin)
      if (native) await preferences.set({ key: SERVER_ORIGIN_KEY, value: nextOrigin })
      origin = nextOrigin
      return { origin, changed }
    },

    currentOrigin() {
      return origin
    },

    isConfigured() {
      return !native || Boolean(origin)
    },

    apiBaseUrl() {
      return native && origin ? `${origin}/api/v1` : '/api/v1'
    },

    assetUrl(value) {
      if (!value || !String(value).startsWith('/uploads/')) return value
      return native && origin ? `${origin}${value}` : value
    }
  }
}

export async function saveServerForSession(config, auth, value) {
  const result = await config.save(value)
  if (result.changed) auth.clearSession()
  return result
}
