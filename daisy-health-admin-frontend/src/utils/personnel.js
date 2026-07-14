export function eligiblePersonnel(rows = []) {
  return rows.filter((item) => item.status === '启用' && item.auditStatus === '已通过')
}
