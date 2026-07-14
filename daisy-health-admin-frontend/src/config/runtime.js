import { Capacitor } from '@capacitor/core'
import { Preferences } from '@capacitor/preferences'
import { createServerConfig } from './server.js'

export const isNativeApp = Capacitor.isNativePlatform()
export const serverConfig = createServerConfig({ native: isNativeApp, preferences: Preferences })

export async function initializeRuntimeConfig() {
  await serverConfig.initialize()
}
