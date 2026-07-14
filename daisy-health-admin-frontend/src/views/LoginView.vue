<template>
  <main class="login-page">
    <section class="login-visual" aria-label="系统介绍">
      <div class="login-copy">
        <h1>智慧养老后台管理系统</h1>
        <p>
          <span>智能健康信息及服务管理；</span>
          <span>实现资源的优化配置和管理，降低运营成本。</span>
        </p>
      </div>
      <img class="login-picture" :src="loginPicture" alt="" />
    </section>

    <section class="login-card-area">
      <section class="login-panel">
        <div class="login-logo" aria-hidden="true">
          <svg viewBox="0 0 40 40" focusable="false">
            <path
              d="M20 28.5c-.6 0-1.1-.2-1.6-.7l-7.2-7.1a6.3 6.3 0 0 1 0-8.9 6.1 6.1 0 0 1 8.8 0l.1.1.1-.1a6.1 6.1 0 0 1 8.8 0 6.3 6.3 0 0 1 0 8.9l-2.2 2.2-3.4-3.4a3.7 3.7 0 0 0-5.2 0l-2.2 2.2 4.5 4.4 3.8-3.8 2.1 2.1-4.8 4.7c-.4.2-.9.4-1.5.4z"
            />
            <path
              d="M20.5 22.6 18 20.1l1.2-1.1a2.1 2.1 0 0 1 3 0l4.4 4.4-2.4 2.4-3.7-3.2z"
            />
          </svg>
        </div>
        <h2>欢迎登录</h2>
        <server-settings ref="serverSettingsRef" />

        <el-form class="login-form" :model="form" :rules="rules" ref="formRef" @keyup.enter="submit">
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              class="login-input user-input"
              autocomplete="username"
              autofocus
              placeholder=""
              @input="loginError = ''"
            >
              <template #suffix>
                <el-icon class="input-clear" @click.stop="form.phone = ''; loginError = ''"><CircleCloseFilled /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              class="login-input password-input"
              type="password"
              autocomplete="current-password"
              placeholder="请输入密码"
              @input="loginError = ''"
            />
            <p v-if="loginError" class="login-error" role="alert">{{ loginError }}</p>
          </el-form-item>
          <div class="agreement-row">
            <el-checkbox v-model="form.agreed">
              我已阅读并同意 <a href="javascript:void(0)">《用户隐私政策》</a>
            </el-checkbox>
          </div>
          <el-button class="login-button" type="primary" :loading="loading" @click="submit">登录</el-button>
          <p class="forgot-text">忘记密码请联系管理员</p>
        </el-form>
      </section>
    </section>

    <footer class="login-footer">© DaisyAxure 版权所有</footer>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCloseFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import ServerSettings from '../components/ServerSettings.vue'
import { isNativeApp, serverConfig } from '../config/runtime.js'
import { loginFailureMessage } from '../utils/loginFeedback'
import loginPicture from '../../assets/login_picture.png'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const loginError = ref('')
const serverSettingsRef = ref()
const form = reactive({ phone: '', password: '', agreed: true })
const rules = {
  phone: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  loginError.value = ''
  if (isNativeApp && !serverConfig.isConfigured()) {
    serverSettingsRef.value?.open()
    loginError.value = '请先配置并测试服务器连接'
    return
  }
  await formRef.value.validate()
  if (!form.agreed) {
    ElMessage.warning('请先同意用户隐私政策')
    return
  }
  loading.value = true
  try {
    await auth.signIn(form)
    router.push(auth.homePath)
  } catch (error) {
    loginError.value = loginFailureMessage(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(560px, 1fr) minmax(560px, 0.9fr);
  overflow: hidden;
  background: #effaf8;
  color: #1f2328;
}

.login-visual {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 270px 48px 72px;
  box-sizing: border-box;
}

.login-copy {
  text-align: center;
}

.login-copy h1 {
  margin: 0;
  color: #05080c;
  font-size: 29px;
  font-weight: 500;
  line-height: 1.35;
  letter-spacing: 0;
}

.login-copy p {
  margin: 29px 0 0;
  color: #b8c1c8;
  font-size: 18px;
  font-weight: 400;
  line-height: 1.85;
}

.login-copy span {
  display: block;
}

.login-picture {
  width: min(440px, 34vw);
  max-height: 42vh;
  object-fit: contain;
  margin-top: 24px;
  user-select: none;
  pointer-events: none;
}

.login-card-area {
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding: 88px 56px 64px 0;
  box-sizing: border-box;
}

.login-panel {
  width: min(600px, 100%);
  min-height: min(760px, calc(100vh - 120px));
  padding: 94px 78px 44px;
  box-sizing: border-box;
  border: 1px solid rgba(224, 233, 231, 0.8);
  border-radius: 22px;
  background: #ffffff;
  box-shadow: 0 2px 18px rgba(42, 64, 58, 0.06);
}

.login-logo {
  width: 62px;
  height: 62px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: linear-gradient(180deg, #50d4a6 0%, #62d7a5 52%, #e3df70 100%);
  box-shadow: 0 24px 58px rgba(68, 206, 164, 0.24);
}

.login-logo svg {
  width: 39px;
  height: 39px;
  fill: #ffffff;
}

.login-panel h2 {
  margin: 34px 0 0;
  color: #24262b;
  text-align: center;
  font-size: 38px;
  font-weight: 400;
  line-height: 1.2;
  letter-spacing: 0;
}

.login-form {
  width: 100%;
  margin-top: 64px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 32px;
}

.login-form :deep(.el-form-item__error) {
  padding-top: 8px;
  color: #ee6b6b;
}

.login-error {
  width: 100%;
  margin: 8px 0 0;
  color: #ee6b6b;
  font-size: 12px;
  line-height: 1;
}

.login-input :deep(.el-input__wrapper) {
  height: 62px;
  padding: 0 26px 0 33px;
  border-radius: 10px;
  box-shadow: none;
  transition: box-shadow 0.15s ease, background-color 0.15s ease;
}

.user-input :deep(.el-input__wrapper),
.user-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #47ddb0 inset;
}

.password-input :deep(.el-input__wrapper) {
  background: #f2fbfa;
}

.login-input :deep(.el-input__inner) {
  height: 62px;
  color: #273036;
  font-size: 18px;
  line-height: 62px;
}

.login-input :deep(.el-input__inner::placeholder) {
  color: #c3ccd1;
  font-size: 20px;
  font-weight: 600;
}

.input-clear {
  color: #c9c9c9;
  font-size: 25px;
  cursor: pointer;
}

.agreement-row {
  margin: 0 0 28px;
}

.agreement-row :deep(.el-checkbox) {
  height: 30px;
  align-items: center;
  color: #b7c0c5;
  font-size: 19px;
  font-weight: 600;
}

.agreement-row :deep(.el-checkbox__input) {
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.agreement-row :deep(.el-checkbox__inner) {
  width: 24px;
  height: 24px;
  display: inline-block;
  box-sizing: border-box;
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  background-color: #ffffff;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}

.agreement-row :deep(.el-checkbox__inner::after) {
  width: 7px;
  height: 13px;
  left: 7px;
  top: 2px;
  border-color: #ffffff;
  border-width: 0 3px 3px 0;
  transform: rotate(45deg) scaleY(0);
  transform-origin: center;
  transition: transform 0.15s ease;
}

.agreement-row :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  transform: rotate(45deg) scaleY(1);
}

.agreement-row :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  border-color: #54d4ad;
  background-color: #54d4ad;
}

.agreement-row :deep(.el-checkbox__label) {
  padding-left: 14px;
  color: #b7c0c5;
}

.agreement-row a {
  color: #29c88d;
  text-decoration: none;
}

.login-button {
  width: 100%;
  height: 62px;
  margin: 0;
  border: 0;
  border-radius: 10px;
  background: #55d1aa;
  box-shadow: 0 16px 42px rgba(85, 209, 170, 0.2);
  color: #ffffff;
  font-size: 24px;
  font-weight: 600;
}

.login-button:hover,
.login-button:focus {
  background: #4cc7a1;
}

.forgot-text {
  margin: 28px 0 0;
  color: #c1c7cc;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.login-footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 67px;
  color: #c5cbd0;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  pointer-events: none;
}

@media (max-width: 1180px) {
  .login-page {
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .login-visual {
    min-height: auto;
    padding: 64px 28px 24px;
  }

  .login-picture {
    width: min(440px, 86vw);
    max-height: 360px;
    margin-top: 28px;
  }

  .login-card-area {
    min-height: auto;
    justify-content: center;
    padding: 24px 28px 92px;
  }

  .login-panel {
    min-height: auto;
    padding: 64px 52px 48px;
  }

  .login-footer {
    bottom: 28px;
  }
}

@media (max-width: 640px) {
  .login-copy h1 {
    font-size: 24px;
  }

  .login-copy p,
  .forgot-text,
  .login-footer {
    font-size: 15px;
  }

  .login-panel {
    padding: 48px 24px 36px;
    border-radius: 18px;
  }

  .login-panel h2 {
    font-size: 32px;
  }

  .login-input :deep(.el-input__wrapper),
  .login-input :deep(.el-input__inner),
  .login-button {
    height: 56px;
    line-height: 56px;
  }

  .agreement-row :deep(.el-checkbox) {
    font-size: 15px;
  }
}
</style>
