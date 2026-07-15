export function eligiblePersonnel(rows = [], serviceType = '') {
  return rows.filter((item) => item.status === '启用'
    && item.auditStatus === '已通过'
    && (!serviceType || item.serviceType === serviceType))
}
