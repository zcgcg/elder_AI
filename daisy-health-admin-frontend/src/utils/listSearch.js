const STATUS_OPTIONS = {
  workOrders: ['待服务', '服务中', '已完成', '已取消'],
  products: ['上架', '下架'],
  orders: ['待服务', '已完成', '售后中']
}

export function isListSearchEnabled(resource) {
  return Object.prototype.hasOwnProperty.call(STATUS_OPTIONS, resource)
}

export function listSearchStatuses(resource) {
  return STATUS_OPTIONS[resource] || []
}
