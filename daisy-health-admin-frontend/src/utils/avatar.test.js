import test from 'node:test'
import assert from 'node:assert/strict'
import { AVATAR_SOURCE_MAX_SIZE, prepareAvatarImage } from './avatar.js'

test('avatar source larger than 10MB is rejected before processing', async () => {
  await assert.rejects(
    prepareAvatarImage({ name: 'large.png', type: 'image/png', size: AVATAR_SOURCE_MAX_SIZE + 1 }),
    /原始头像不能超过 10MB/
  )
})

test('HEIC avatar source is rejected with supported formats', async () => {
  await assert.rejects(
    prepareAvatarImage({ name: 'phone.heic', type: 'image/heic', size: 1024 }),
    /仅支持 JPG、PNG、WebP 格式/
  )
})

test('GIF avatar source is rejected with supported formats', async () => {
  await assert.rejects(
    prepareAvatarImage({ name: 'animated.gif', type: 'image/gif', size: 1024 }),
    /仅支持 JPG、PNG、WebP 格式/
  )
})
