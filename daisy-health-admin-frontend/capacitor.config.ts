import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.daisy.health',
  appName: '黛西健康',
  webDir: 'dist',
  android: {
    allowMixedContent: true,
    backgroundColor: '#effaf8'
  },
  server: {
    androidScheme: 'https'
  },
  plugins: {
    SplashScreen: {
      launchAutoHide: false,
      launchShowDuration: 3000,
      backgroundColor: '#effaf8',
      showSpinner: false
    },
    StatusBar: {
      overlaysWebView: false,
      style: 'LIGHT',
      backgroundColor: '#159a84'
    }
  }
}

export default config
