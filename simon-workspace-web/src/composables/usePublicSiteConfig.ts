import { ref } from 'vue'

import { fetchPublicSiteConfig, type SiteConfig } from '../api/site'

const site = ref<SiteConfig | null>(null)
const loading = ref(false)
const failed = ref(false)
let pendingRequest: Promise<void> | null = null

export function usePublicSiteConfig() {
  async function loadSite(force = false) {
    if (site.value && !force) {
      return site.value
    }

    if (pendingRequest && !force) {
      await pendingRequest
      return site.value
    }

    loading.value = true
    failed.value = false
    pendingRequest = fetchPublicSiteConfig()
      .then((data) => {
        site.value = data
      })
      .catch(() => {
        failed.value = true
      })
      .finally(() => {
        loading.value = false
        pendingRequest = null
      })

    await pendingRequest
    return site.value
  }

  return {
    site,
    loading,
    failed,
    loadSite,
  }
}
