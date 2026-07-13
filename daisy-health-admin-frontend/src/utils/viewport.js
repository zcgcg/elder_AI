import { onBeforeUnmount, onMounted, ref } from 'vue'

export const MOBILE_DESCRIPTION_MAX_WIDTH = 720
export const TABLET_DESCRIPTION_MAX_WIDTH = 1000

export function columnsForWidth(width, desktopColumns = 2) {
  if (Number(width) <= MOBILE_DESCRIPTION_MAX_WIDTH) return 1
  if (Number(width) <= TABLET_DESCRIPTION_MAX_WIDTH) return Math.min(2, desktopColumns)
  return desktopColumns
}

export function useResponsiveColumns(desktopColumns = 2) {
  const columns = ref(columnsForWidth(typeof window === 'undefined' ? 1280 : window.innerWidth, desktopColumns))

  const update = () => {
    columns.value = columnsForWidth(window.innerWidth, desktopColumns)
  }

  onMounted(() => {
    update()
    window.addEventListener('resize', update, { passive: true })
  })
  onBeforeUnmount(() => window.removeEventListener('resize', update))

  return columns
}
