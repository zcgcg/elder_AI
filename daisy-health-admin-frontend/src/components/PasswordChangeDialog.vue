<template>
  <el-dialog
    :model-value="modelValue"
    title="修改密码"
    width="480px"
    @update:model-value="emit('update:modelValue', $event)"
    @closed="resetForm"
  >
    <el-form label-width="92px" @submit.prevent="submit">
      <el-form-item label="当前密码" required>
        <el-input v-model="form.currentPassword" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码" required>
        <el-input v-model="form.newPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
      <el-form-item label="确认新密码" required>
        <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
    </el-form>
    <el-alert title="新密码至少 6 位，修改后当前登录状态保持不变。" type="info" :closable="false" show-icon />
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePassword } from '../api/http'

defineProps({ modelValue: { type: Boolean, default: false } })
const emit = defineEmits(['update:modelValue', 'changed'])
const saving = ref(false)
const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

function close() {
  emit('update:modelValue', false)
}

function resetForm() {
  Object.assign(form, { currentPassword: '', newPassword: '', confirmPassword: '' })
}

async function submit() {
  if (!form.currentPassword || !form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请完整填写三项密码')
    return
  }
  if (form.newPassword.length < 6) {
    ElMessage.warning('新密码至少需要 6 位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  saving.value = true
  try {
    await updatePassword({ ...form })
    ElMessage.success('密码修改成功')
    emit('changed')
    close()
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    saving.value = false
  }
}
</script>
