import { createApp } from 'vue'
import ElementPlus, { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { initializeRuntimeConfig } from './config/runtime.js'
import { initializeNativeApp } from './native/app.js'
import './styles.css'

async function bootstrap() {
  let configInitializationFailed = false
  try {
    await initializeRuntimeConfig()
  } catch {
    configInitializationFailed = true
  }

  const app = createApp(App)

  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  app.use(createPinia())
  app.use(router)
  app.use(ElementPlus)
  app.mount('#app')

  if (configInitializationFailed) {
    ElMessage.warning('无法读取已保存的服务器地址，请在服务器设置中重新配置')
  }
  await initializeNativeApp(router)
}

bootstrap().catch(() => {
  ElMessage.error('应用启动失败，请完全退出后重试')
})
