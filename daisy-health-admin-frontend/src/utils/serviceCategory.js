export const SERVICE_CATEGORIES = Object.freeze(['家政护理', '康复理疗', '上门体检', '其他'])

export function catalogItemsByCategory(items = [], category = '') {
  return items.filter((item) => item.category === category)
}

export function productCategory(items = [], productId = '') {
  return items.find((item) => String(item.id) === String(productId))?.category || ''
}

export function personnelByProduct(personnel = [], products = [], productId = '') {
  const category = productCategory(products, productId)
  if (!category) return []
  return personnel.filter((item) => item.serviceType === category)
}
