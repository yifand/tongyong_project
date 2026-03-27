import { ref, computed } from 'vue'

interface UseTableOptions<T, Q> {
  fetchFn: (params: Q) => Promise<{ list: T[]; total: number }>
  initialQuery: Q
  immediate?: boolean
}

export function useTable<T, Q extends { page: number; size: number }>(
  options: UseTableOptions<T, Q>
) {
  const { fetchFn, initialQuery, immediate = true } = options

  // State
  const loading = ref(false)
  const list = ref<T[]>([])
  const total = ref(0)
  const query = ref<Q>({ ...initialQuery })
  const selectedItems = ref<T[]>([])

  // Getters
  const pagination = computed(() => ({
    currentPage: query.value.page,
    pageSize: query.value.size,
    total: total.value
  }))

  // Actions
  async function fetchData() {
    loading.value = true
    try {
      const { list: data, total: count } = await fetchFn(query.value)
      list.value = data
      total.value = count
    } finally {
      loading.value = false
    }
  }

  function handlePageChange(page: number) {
    query.value.page = page
    fetchData()
  }

  function handleSizeChange(size: number) {
    query.value.size = size
    query.value.page = 1
    fetchData()
  }

  function handleSelectionChange(selection: T[]) {
    selectedItems.value = selection
  }

  function resetQuery() {
    query.value = { ...initialQuery }
    fetchData()
  }

  function refresh() {
    fetchData()
  }

  // 立即执行
  if (immediate) {
    fetchData()
  }

  return {
    loading,
    list,
    total,
    query,
    selectedItems,
    pagination,
    fetchData,
    handlePageChange,
    handleSizeChange,
    handleSelectionChange,
    resetQuery,
    refresh
  }
}
