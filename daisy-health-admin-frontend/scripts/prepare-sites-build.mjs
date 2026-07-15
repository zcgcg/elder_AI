import { cp, mkdir, rm } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const serverDir = resolve(root, 'dist/server')
const metadataDir = resolve(root, 'dist/.openai')

await rm(resolve(root, 'dist/assets'), { recursive: true, force: true })
await rm(resolve(root, 'dist/index.html'), { force: true })
await mkdir(serverDir, { recursive: true })
await mkdir(metadataDir, { recursive: true })
await cp(resolve(root, 'worker/index.js'), resolve(serverDir, 'index.js'))
await cp(resolve(root, '.openai/hosting.json'), resolve(metadataDir, 'hosting.json'))
