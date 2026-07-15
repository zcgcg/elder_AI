export const PAGE_SIZE = 20

export function normalizePage(page, total, pageSize = PAGE_SIZE) {
  const size = Math.max(1, Number(pageSize) || PAGE_SIZE)
  const pages = Math.max(1, Math.ceil(Math.max(0, Number(total) || 0) / size))
  return Math.min(Math.max(1, Number(page) || 1), pages)
}

export function paginate(rows, page, pageSize = PAGE_SIZE) {
  const list = Array.isArray(rows) ? rows : []
  const current = normalizePage(page, list.length, pageSize)
  const start = (current - 1) * pageSize
  return list.slice(start, start + pageSize)
}

export function hasSameItemOrder(previous, next) {
  if (!Array.isArray(previous) || !Array.isArray(next) || previous.length !== next.length) return false
  return previous.every((item, index) => itemIdentity(item) === itemIdentity(next[index]))
}

function itemIdentity(item) {
  return item && typeof item === 'object' && item.id != null ? item.id : item
}
