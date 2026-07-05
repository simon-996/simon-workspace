<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NIcon, NSpin, useMessage } from 'naive-ui'
import { AlertTriangle, CircleCheck, Cloud, Refresh } from '@vicons/tabler'

import {
  activateStorageProvider,
  fetchStorageProviders,
  testStorageProvider,
  type StorageProviderState,
} from '../../api/workspace'

const { t } = useI18n()
const message = useMessage()

const providers = ref<StorageProviderState[]>([])
const loading = ref(false)
const testingCode = ref<string | null>(null)
const activatingCode = ref<string | null>(null)
const error = ref('')

const activeProvider = computed(() => providers.value.find((provider) => provider.active))
const configuredCount = computed(() => providers.value.filter((provider) => provider.configured).length)
const enabledCount = computed(() => providers.value.filter((provider) => provider.enabled).length)
const successCount = computed(() => providers.value.filter((provider) => provider.lastTestStatus === 'SUCCESS').length)

onMounted(() => {
  void loadProviders()
})

async function loadProviders() {
  loading.value = true
  error.value = ''
  try {
    providers.value = await fetchStorageProviders()
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('workspace.storage.messages.loadFailed')
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

async function testProvider(provider: StorageProviderState) {
  testingCode.value = provider.providerCode
  try {
    const updated = await testStorageProvider(provider.providerCode)
    mergeProvider(updated)
    message.success(
      updated.lastTestStatus === 'SUCCESS'
        ? t('workspace.storage.messages.testSuccess')
        : t('workspace.storage.messages.testFailed'),
    )
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.storage.messages.testFailed'))
  } finally {
    testingCode.value = null
  }
}

async function activateProvider(provider: StorageProviderState) {
  activatingCode.value = provider.providerCode
  try {
    const updated = await activateStorageProvider(provider.providerCode)
    providers.value = providers.value.map((item) => ({
      ...item,
      active: item.providerCode === updated.providerCode,
    }))
    mergeProvider(updated)
    message.success(t('workspace.storage.messages.activated'))
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.storage.messages.activateFailed'))
  } finally {
    activatingCode.value = null
  }
}

function mergeProvider(updated: StorageProviderState) {
  providers.value = providers.value.map((item) => (
    item.providerCode === updated.providerCode ? updated : item
  ))
}

function stateText(provider: StorageProviderState) {
  if (!provider.configured) return t('workspace.storage.state.unconfigured')
  if (!provider.enabled) return t('workspace.storage.state.disabled')
  if (provider.active) return t('workspace.storage.state.active')
  return t('workspace.storage.state.ready')
}

function testText(provider: StorageProviderState) {
  if (provider.lastTestStatus === 'SUCCESS') return t('workspace.storage.test.success')
  if (provider.lastTestStatus === 'FAILED') return t('workspace.storage.test.failed')
  return t('workspace.storage.test.untested')
}

function valueOrUnset(value?: string | null) {
  return value || t('workspace.storage.unset')
}
</script>

<template>
  <section class="storage-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Cloud" />
        <span>{{ t('workspace.storage.summary.active') }}</span>
        <strong>{{ activeProvider?.displayName || '-' }}</strong>
      </article>
      <article>
        <n-icon :component="CircleCheck" />
        <span>{{ t('workspace.storage.summary.configured') }}</span>
        <strong>{{ configuredCount }} / {{ providers.length }}</strong>
      </article>
      <article>
        <n-icon :component="CircleCheck" />
        <span>{{ t('workspace.storage.summary.enabled') }}</span>
        <strong>{{ enabledCount }}</strong>
      </article>
      <article>
        <n-icon :component="Cloud" />
        <span>{{ t('workspace.storage.summary.tested') }}</span>
        <strong>{{ successCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <span>{{ t('workspace.storage.toolbarText') }}</span>
      <n-button secondary class="icon-button" @click="loadProviders">
        <template #icon>
          <n-icon :component="Refresh" />
        </template>
        {{ t('common.actions.refresh') }}
      </n-button>
    </section>

    <section class="provider-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadProviders">{{ t('common.actions.retry') }}</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-grid">
          <span v-for="index in 4" :key="index" />
        </div>
      </n-spin>

      <div v-else class="provider-grid">
        <article
          v-for="provider in providers"
          :key="provider.providerCode"
          class="provider-card"
          :class="{ active: provider.active }"
        >
          <header>
            <div>
              <strong>{{ provider.displayName }}</strong>
              <span>{{ provider.providerCode }}</span>
            </div>
            <em :class="{ success: provider.active || provider.configured }">{{ stateText(provider) }}</em>
          </header>

          <dl>
            <div>
              <dt>{{ t('workspace.storage.fields.endpoint') }}</dt>
              <dd>{{ valueOrUnset(provider.endpoint) }}</dd>
            </div>
            <div>
              <dt>{{ t('workspace.storage.fields.bucket') }}</dt>
              <dd>{{ valueOrUnset(provider.bucket) }}</dd>
            </div>
            <div>
              <dt>{{ t('workspace.storage.fields.publicBaseUrl') }}</dt>
              <dd>{{ valueOrUnset(provider.publicBaseUrl) }}</dd>
            </div>
            <div>
              <dt>{{ t('workspace.storage.fields.lastTest') }}</dt>
              <dd>
                {{ testText(provider) }}
                <span v-if="provider.lastTestTime">{{ provider.lastTestTime.slice(0, 16) }}</span>
              </dd>
            </div>
          </dl>

          <p v-if="provider.lastTestMessage">{{ provider.lastTestMessage }}</p>

          <footer>
            <n-button
              secondary
              size="small"
              class="icon-button"
              :loading="testingCode === provider.providerCode"
              @click="testProvider(provider)"
            >
              {{ t('workspace.storage.actions.test') }}
            </n-button>
            <n-button
              type="primary"
              size="small"
              class="icon-button"
              :disabled="provider.active || !provider.enabled || !provider.configured"
              :loading="activatingCode === provider.providerCode"
              @click="activateProvider(provider)"
            >
              {{ provider.active ? t('workspace.storage.actions.active') : t('workspace.storage.actions.activate') }}
            </n-button>
          </footer>
        </article>
      </div>
    </section>
  </section>
</template>

<style scoped>
.storage-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.summary-grid article {
  display: grid;
  grid-template-columns: 42px 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 12px;
  min-height: 86px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  padding: 16px;
}

.summary-grid .n-icon {
  grid-row: 1 / 3;
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: #e7f5fb;
  color: #1688b9;
  font-size: 22px;
}

.summary-grid span,
.toolbar span {
  color: #647586;
  font-size: 12px;
  font-weight: 800;
}

.summary-grid strong {
  overflow: hidden;
  color: var(--sw-text);
  font-size: 20px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  padding: 12px;
}

.icon-button {
  --n-border-radius: 8px !important;
  font-weight: 700;
}

.provider-panel {
  min-height: 360px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  overflow: hidden;
}

.provider-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 12px;
}

.provider-card {
  display: grid;
  gap: 14px;
  min-height: 310px;
  border: 1px solid #dde5eb;
  border-radius: 8px;
  background: #fbfcfd;
  padding: 16px;
  transition:
    border-color 220ms cubic-bezier(0.16, 1, 0.3, 1),
    box-shadow 220ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 220ms cubic-bezier(0.16, 1, 0.3, 1);
}

.provider-card.active {
  border-color: #91d2e4;
  box-shadow: 0 14px 36px rgba(32, 123, 154, 0.12);
}

.provider-card:hover {
  transform: translate3d(0, -2px, 0);
}

.provider-card header,
.provider-card footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.provider-card header div {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.provider-card strong {
  color: var(--sw-text);
  font-size: 17px;
  font-weight: 800;
}

.provider-card header span,
.provider-card p {
  color: #6a7a86;
  font-size: 12px;
  line-height: 1.6;
}

.provider-card em {
  flex: none;
  min-height: 24px;
  border-radius: 999px;
  background: #eef1f4;
  color: #647586;
  padding: 4px 10px;
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.provider-card em.success {
  background: #e8f6ef;
  color: #15784c;
}

dl {
  display: grid;
  gap: 8px;
  margin: 0;
}

dl div {
  display: grid;
  grid-template-columns: 114px minmax(0, 1fr);
  gap: 10px;
  min-height: 34px;
  border-bottom: 1px solid #edf1f4;
  padding-bottom: 8px;
}

dt {
  color: #7a8994;
  font-size: 12px;
  font-weight: 800;
}

dd {
  overflow-wrap: anywhere;
  margin: 0;
  color: #263540;
  font-size: 13px;
  font-weight: 700;
}

dd span {
  display: block;
  margin-top: 3px;
  color: #7a8994;
  font-size: 12px;
  font-weight: 600;
}

.error-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 360px;
  color: #607283;
  text-align: center;
}

.error-state .n-icon {
  color: #c77724;
  font-size: 28px;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 12px;
}

.skeleton-grid span {
  min-height: 310px;
  border-radius: 8px;
  background: linear-gradient(90deg, #eef3f6, #f8fafb, #eef3f6);
  background-size: 220% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
  0% {
    background-position: 120% 0;
  }
  100% {
    background-position: -120% 0;
  }
}

@media (max-width: 920px) {
  .summary-grid,
  .provider-grid,
  .skeleton-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar,
  .provider-card header,
  .provider-card footer {
    align-items: stretch;
    flex-direction: column;
  }

  .provider-card footer .n-button {
    width: 100%;
  }

  dl div {
    grid-template-columns: 1fr;
  }
}
</style>
