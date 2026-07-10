<template>
  <div class="avatar-editor">
    <el-avatar :size="72" :src="assetUrl(previewUrl)">{{ fallback || '用' }}</el-avatar>
    <div class="avatar-choice-grid">
      <button
        v-for="avatar in defaultAvatars"
        :key="avatar"
        type="button"
        class="avatar-choice"
        :class="{ active: !pendingFile && selectedUrl === avatar }"
        @click="selectPreset(avatar)"
      >
        <img :src="avatar" alt="预设头像" />
      </button>
    </div>
    <div class="avatar-upload-actions">
      <input ref="fileInput" class="avatar-file-input" type="file" accept=".jpg,.jpeg,.png,.webp" @change="handleFileChange" />
      <el-button :loading="processing" @click="fileInput?.click()">选择自定义头像</el-button>
      <span>支持 JPG、PNG、WebP，原图最大 10MB</span>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { assetUrl, uploadFile } from '../api/http'
import { prepareAvatarImage } from '../utils/avatar'

const props = defineProps({
  modelValue: { type: String, default: '' },
  fallback: { type: String, default: '用' }
})

const defaultAvatars = [
  '/default-avatars/avatar-01.svg',
  '/default-avatars/avatar-02.svg',
  '/default-avatars/avatar-03.svg',
  '/default-avatars/avatar-04.svg',
  '/default-avatars/avatar-05.svg',
  '/default-avatars/avatar-06.svg'
]
const fileInput = ref(null)
const selectedUrl = ref(props.modelValue)
const pendingFile = ref(null)
const localPreviewUrl = ref('')
const processing = ref(false)
const previewUrl = computed(() => localPreviewUrl.value || selectedUrl.value)

watch(() => props.modelValue, (value) => reset(value))

function selectPreset(url) {
  releasePreview()
  pendingFile.value = null
  selectedUrl.value = url
}

async function handleFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  processing.value = true
  try {
    const prepared = await prepareAvatarImage(file)
    releasePreview()
    pendingFile.value = prepared
    selectedUrl.value = ''
    localPreviewUrl.value = URL.createObjectURL(prepared)
  } catch (error) {
    ElMessage.error(error.message || '头像处理失败')
  } finally {
    processing.value = false
  }
}

async function commitSelection() {
  if (processing.value) throw new Error('头像正在处理中，请稍候')
  if (!pendingFile.value) return selectedUrl.value
  const data = await uploadFile(pendingFile.value, 'avatar')
  selectedUrl.value = data.url
  pendingFile.value = null
  releasePreview()
  return data.url
}

function reset(value = props.modelValue) {
  releasePreview()
  pendingFile.value = null
  selectedUrl.value = value || ''
  if (fileInput.value) fileInput.value.value = ''
}

function releasePreview() {
  if (localPreviewUrl.value) URL.revokeObjectURL(localPreviewUrl.value)
  localPreviewUrl.value = ''
}

onBeforeUnmount(releasePreview)
defineExpose({ reset, commitSelection })
</script>

<style scoped>
.avatar-editor {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.avatar-choice-grid {
  display: grid;
  grid-template-columns: repeat(6, 44px);
  gap: 8px;
}

.avatar-choice {
  width: 44px;
  height: 44px;
  padding: 2px;
  overflow: hidden;
  border: 2px solid transparent;
  border-radius: 50%;
  background: transparent;
  cursor: pointer;
}

.avatar-choice.active {
  border-color: var(--el-color-primary);
}

.avatar-choice img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-upload-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-upload-actions span {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.avatar-file-input {
  display: none;
}

@media (max-width: 720px) {
  .avatar-choice-grid {
    grid-template-columns: repeat(3, 44px);
  }

  .avatar-upload-actions {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
