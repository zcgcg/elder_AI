<template>
  <main class="login-page">
    <section class="login-hero">
      <h1>黛西健康</h1>
      <p>智慧养老后台管理系统</p>
      <div class="login-metrics">
        <span>用户健康档案</span>
        <span>服务调度</span>
        <span>交易运营</span>
      </div>
    </section>
    <section class="login-panel">
      <h2>管理员登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @keyup.enter="submit">
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-checkbox v-model="form.agreed">我已阅读并同意《用户隐私政策》</el-checkbox>
        <el-button class="login-button" type="primary" :loading="loading" @click="submit">登录</el-button>
        <p class="muted">忘记密码请联系管理员</p>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ phone: '13800000000', password: 'admin123', agreed: true })
const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  if (!form.agreed) {
    ElMessage.warning('请先同意用户隐私政策')
    return
  }
  loading.value = true
  try {
    await auth.signIn(form)
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error('登录失败，请确认后端服务已启动')
  } finally {
    loading.value = false
  }
}
</script>
