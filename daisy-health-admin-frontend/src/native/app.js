import { App } from '@capacitor/app'
import { Browser } from '@capacitor/browser'
import { Network } from '@capacitor/network'
import { SplashScreen } from '@capacitor/splash-screen'
import { StatusBar, Style } from '@capacitor/status-bar'
import { isNativeApp } from '../config/runtime.js'

let networkConnected = true

export async function initializeNativeApp(router) {
  if (!isNativeApp) return

  try {
    const status = await Network.getStatus()
    networkConnected = status.connected
  } catch {
    networkConnected = true
  }

  try {
    await Network.addListener('networkStatusChange', (nextStatus) => {
      networkConnected = nextStatus.connected
    })
  } catch {
    // HTTP requests still provide the final connectivity result.
  }

  await Promise.allSettled([
    StatusBar.setStyle({ style: Style.Light }),
    StatusBar.setBackgroundColor({ color: '#159a84' }),
    StatusBar.setOverlaysWebView({ overlay: false })
  ])

  try {
    await App.addListener('backButton', () => {
      const action = nativeBackAction({
        hasOverlay: Boolean(document.querySelector('.el-overlay')),
        path: router.currentRoute.value.path,
        historyLength: window.history.length
      })
      if (action === 'close-overlay') {
        document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', bubbles: true }))
        return
      }
      if (action === 'go-back') {
        router.back()
        return
      }
      App.exitApp()
    })
  } catch {
    // Android's default back behavior remains available if listener setup fails.
  }

  try {
    await SplashScreen.hide()
  } catch {
    // A missing splash plugin must not prevent the web UI from running.
  }
}

export function isNetworkConnected() {
  return networkConnected
}

export function nativeBackAction({ hasOverlay, path, historyLength }) {
  if (hasOverlay) return 'close-overlay'
  if (path !== '/login' && historyLength > 1) return 'go-back'
  return 'exit-app'
}

export async function openExternalUrl(url) {
  if (!url) return
  if (isNativeApp) {
    await Browser.open({ url })
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}
