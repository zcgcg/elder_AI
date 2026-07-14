<template>
  <div>
    <slot :items="pagedItems" />
    <div v-if="items.length > pageSize" class="list-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        background
        layout="prev, pager, next, total"
        :page-size="pageSize"
        :total="items.length"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { PAGE_SIZE, normalizePage, paginate } from '../utils/pagination'

const props = defineProps({
  items: { type: Array, default: () => [] },
  pageSize: { type: Number, default: PAGE_SIZE }
})

const currentPage = ref(1)
const pagedItems = computed(() => paginate(props.items, currentPage.value, props.pageSize))

watch(() => props.items, () => { currentPage.value = 1 })
watch(() => props.items.length, (total) => {
  currentPage.value = normalizePage(currentPage.value, total, props.pageSize)
})
</script>

<style scoped>
.list-pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 18px;
}
</style>
