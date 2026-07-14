const DISPLAY_TEXT = {
  weight: '体重',
  heart_rate: '心率',
  blood_pressure: '血压',
  blood_sugar: '血糖',
  band: '智能手环',
  watch: '智能手表',
  scale: '智能体重秤',
  bpm: '次/分钟',
  kg: '千克',
  device: '设备采集',
  manual: '手工录入',
  pending: '待服务',
  service_in: '服务中',
  completed: '已完成',
  cancelled: '已取消',
  pending_accept: '待接单',
  pending_service: '待服务',
  closed: '已关闭',
  after_sale: '售后中',
  enabled: '启用',
  disabled: '禁用',
  draft: '草稿',
  published: '已发布',
  processing: '处理中',
  resolved: '已解决'
}

export function localizeValue(value) {
  return DISPLAY_TEXT[String(value ?? '')] ?? value
}
