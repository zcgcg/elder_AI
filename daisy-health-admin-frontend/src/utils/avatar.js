export const AVATAR_SOURCE_MAX_SIZE = 10 * 1024 * 1024
export const AVATAR_OUTPUT_SIZE = 512
export const AVATAR_OUTPUT_MAX_SIZE = 2 * 1024 * 1024

const SUPPORTED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp'])

export async function prepareAvatarImage(file) {
  if (!file) throw new Error('请选择头像图片')
  if (!SUPPORTED_TYPES.has(file.type)) throw new Error('仅支持 JPG、PNG、WebP 格式的头像')
  if (file.size > AVATAR_SOURCE_MAX_SIZE) throw new Error('原始头像不能超过 10MB')

  const image = await decodeImage(file)
  try {
    const canvas = document.createElement('canvas')
    canvas.width = AVATAR_OUTPUT_SIZE
    canvas.height = AVATAR_OUTPUT_SIZE
    const context = canvas.getContext('2d')
    if (!context) throw new Error('当前浏览器无法处理头像图片')

    const sourceWidth = image.width
    const sourceHeight = image.height
    const cropSize = Math.min(sourceWidth, sourceHeight)
    const sourceX = (sourceWidth - cropSize) / 2
    const sourceY = (sourceHeight - cropSize) / 2
    context.drawImage(image, sourceX, sourceY, cropSize, cropSize, 0, 0, AVATAR_OUTPUT_SIZE, AVATAR_OUTPUT_SIZE)

    const blob = await canvasToBlob(canvas, 'image/webp', 0.85)
    if (blob.size > AVATAR_OUTPUT_MAX_SIZE) throw new Error('头像压缩后仍超过 2MB，请更换图片')
    return new File([blob], avatarFileName(file.name), { type: 'image/webp', lastModified: Date.now() })
  } finally {
    image.close?.()
  }
}

async function decodeImage(file) {
  if (typeof createImageBitmap === 'function') {
    try {
      return await createImageBitmap(file, { imageOrientation: 'from-image' })
    } catch (error) {
      // Fall back to HTMLImageElement for browsers that reject imageOrientation.
    }
  }

  const objectUrl = URL.createObjectURL(file)
  try {
    return await new Promise((resolve, reject) => {
      const image = new Image()
      image.onload = () => resolve(image)
      image.onerror = () => reject(new Error('图片无法读取，请更换文件'))
      image.src = objectUrl
    })
  } finally {
    URL.revokeObjectURL(objectUrl)
  }
}

function canvasToBlob(canvas, type, quality) {
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) resolve(blob)
      else reject(new Error('头像压缩失败，请更换图片'))
    }, type, quality)
  })
}

function avatarFileName(originalName) {
  const baseName = String(originalName || 'avatar').replace(/\.[^.]+$/, '').replace(/[^\p{Script=Han}a-zA-Z0-9_-]+/gu, '_')
  return `${baseName || 'avatar'}.webp`
}
