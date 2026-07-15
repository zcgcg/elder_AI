import { cp, mkdir } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const serverDir = resolve(root, 'dist/server')
const metadataDir = resolve(root, 'dist/.openai')

await mkdir(serverDir, { recursive: true })
await mkdir(metadataDir, { recursive: true })
await cp(resolve(root, 'worker/index.js'), resolve(serverDir, 'index.js'))
await cp(resolve(root, '.openai/hosting.json'), resolve(metadataDir, 'hosting.json'))
