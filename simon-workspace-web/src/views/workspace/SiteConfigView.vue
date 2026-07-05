<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NIcon, NInput, NSpin, NSwitch, useMessage } from 'naive-ui'
import { AlertTriangle, ExternalLink, Eye, Refresh, Settings } from '@vicons/tabler'

import {
  fetchAdminSiteConfig,
  updateAdminSiteConfig,
  type SiteConfig,
  type SiteConfigPayload,
} from '../../api/site'

const { t } = useI18n()
const message = useMessage()

const loading = ref(false)
const saving = ref(false)
const error = ref('')
const config = ref<SiteConfig | null>(null)

const form = reactive<SiteConfigPayload>({
  siteTitle: '',
  ownerName: '',
  heroTitle: '',
  heroSubtitle: '',
  ownerRole: '',
  contactEmail: '',
  githubUrl: '',
  profileVisible: true,
  blogVisible: true,
  projectsVisible: true,
  workspaceEntryVisible: false,
})

const visibleCount = computed(() => [
  form.profileVisible,
  form.blogVisible,
  form.projectsVisible,
  form.workspaceEntryVisible,
].filter(Boolean).length)

onMounted(() => {
  void loadConfig()
})

async function loadConfig() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchAdminSiteConfig()
    config.value = data
    syncForm(data)
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('workspace.site.messages.loadFailed')
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

async function submitConfig() {
  if (!form.siteTitle.trim()) {
    message.warning(t('workspace.site.messages.siteTitleRequired'))
    return
  }
  if (!form.ownerName.trim()) {
    message.warning(t('workspace.site.messages.ownerNameRequired'))
    return
  }
  if (!form.heroTitle.trim()) {
    message.warning(t('workspace.site.messages.heroTitleRequired'))
    return
  }

  saving.value = true
  try {
    const updated = await updateAdminSiteConfig({
      siteTitle: form.siteTitle.trim(),
      ownerName: form.ownerName.trim(),
      heroTitle: form.heroTitle.trim(),
      heroSubtitle: textOrNull(form.heroSubtitle),
      ownerRole: textOrNull(form.ownerRole),
      contactEmail: textOrNull(form.contactEmail),
      githubUrl: textOrNull(form.githubUrl),
      profileVisible: form.profileVisible,
      blogVisible: form.blogVisible,
      projectsVisible: form.projectsVisible,
      workspaceEntryVisible: form.workspaceEntryVisible,
    })
    config.value = updated
    syncForm(updated)
    message.success(t('workspace.site.messages.saved'))
  } catch (err) {
    message.error(err instanceof Error ? err.message : t('workspace.site.messages.saveFailed'))
  } finally {
    saving.value = false
  }
}

function syncForm(data: SiteConfig) {
  form.siteTitle = data.siteTitle ?? ''
  form.ownerName = data.ownerName ?? ''
  form.heroTitle = data.heroTitle ?? ''
  form.heroSubtitle = data.heroSubtitle ?? ''
  form.ownerRole = data.ownerRole ?? ''
  form.contactEmail = data.contactEmail ?? ''
  form.githubUrl = data.githubUrl ?? ''
  form.profileVisible = data.profileVisible
  form.blogVisible = data.blogVisible
  form.projectsVisible = data.projectsVisible
  form.workspaceEntryVisible = data.workspaceEntryVisible
}

function textOrNull(value?: string | null) {
  const trimmed = value?.trim() ?? ''
  return trimmed ? trimmed : null
}
</script>

<template>
  <section class="site-config-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Settings" />
        <span>{{ t('workspace.site.summary.title') }}</span>
        <strong>{{ form.siteTitle || '-' }}</strong>
      </article>
      <article>
        <n-icon :component="Eye" />
        <span>{{ t('workspace.site.summary.publicModules') }}</span>
        <strong>{{ visibleCount }}</strong>
      </article>
      <article>
        <n-icon :component="Settings" />
        <span>{{ t('workspace.site.summary.updatedTime') }}</span>
        <strong>{{ config?.updatedTime ? config.updatedTime.slice(0, 10) : '-' }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <span>{{ t('workspace.site.toolbarText') }}</span>
      <div class="toolbar-actions">
        <n-button secondary class="icon-button" @click="loadConfig">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          {{ t('common.actions.refresh') }}
        </n-button>
        <n-button tag="a" href="/" target="_blank" class="icon-button">
          <template #icon>
            <n-icon :component="ExternalLink" />
          </template>
          {{ t('workspace.site.openHome') }}
        </n-button>
      </div>
    </section>

    <section class="config-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadConfig">{{ t('common.actions.retry') }}</n-button>
      </div>

      <div v-else-if="loading && !config" class="config-skeleton" aria-hidden="true">
        <span v-for="index in 8" :key="index" />
      </div>

      <n-spin v-else :show="loading">
        <form class="config-form" @submit.prevent="submitConfig">
          <label class="field">
            <span>{{ t('workspace.site.fields.siteTitle') }}</span>
            <n-input v-model:value="form.siteTitle" :placeholder="t('workspace.site.fields.siteTitlePlaceholder')" />
          </label>

          <label class="field">
            <span>{{ t('workspace.site.fields.ownerName') }}</span>
            <n-input v-model:value="form.ownerName" :placeholder="t('workspace.site.fields.ownerNamePlaceholder')" />
          </label>

          <label class="field span-2">
            <span>{{ t('workspace.site.fields.heroTitle') }}</span>
            <n-input v-model:value="form.heroTitle" :placeholder="t('workspace.site.fields.heroTitlePlaceholder')" />
          </label>

          <label class="field span-2">
            <span>{{ t('workspace.site.fields.heroSubtitle') }}</span>
            <n-input
              v-model:value="form.heroSubtitle"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
              :placeholder="t('workspace.site.fields.heroSubtitlePlaceholder')"
            />
          </label>

          <label class="field">
            <span>{{ t('workspace.site.fields.ownerRole') }}</span>
            <n-input v-model:value="form.ownerRole" :placeholder="t('workspace.site.fields.ownerRolePlaceholder')" />
          </label>

          <label class="field">
            <span>{{ t('workspace.site.fields.contactEmail') }}</span>
            <n-input v-model:value="form.contactEmail" :placeholder="t('workspace.site.fields.contactEmailPlaceholder')" />
          </label>

          <label class="field span-2">
            <span>{{ t('workspace.site.fields.githubUrl') }}</span>
            <n-input v-model:value="form.githubUrl" placeholder="https://github.com/..." />
          </label>

          <div class="switch-grid span-2">
            <label class="switch-item">
              <span>
                <strong>{{ t('workspace.site.switches.profile') }}</strong>
                <small>{{ t('workspace.site.switches.profileHelp') }}</small>
              </span>
              <n-switch v-model:value="form.profileVisible" />
            </label>
            <label class="switch-item">
              <span>
                <strong>{{ t('workspace.site.switches.blog') }}</strong>
                <small>{{ t('workspace.site.switches.blogHelp') }}</small>
              </span>
              <n-switch v-model:value="form.blogVisible" />
            </label>
            <label class="switch-item">
              <span>
                <strong>{{ t('workspace.site.switches.projects') }}</strong>
                <small>{{ t('workspace.site.switches.projectsHelp') }}</small>
              </span>
              <n-switch v-model:value="form.projectsVisible" />
            </label>
            <label class="switch-item">
              <span>
                <strong>{{ t('workspace.site.switches.workspace') }}</strong>
                <small>{{ t('workspace.site.switches.workspaceHelp') }}</small>
              </span>
              <n-switch v-model:value="form.workspaceEntryVisible" />
            </label>
          </div>

          <div class="form-actions span-2">
            <n-button @click="loadConfig">{{ t('common.actions.reset') }}</n-button>
            <n-button type="primary" attr-type="submit" :loading="saving">{{ t('workspace.site.saveConfig') }}</n-button>
          </div>
        </form>
      </n-spin>
    </section>
  </section>
</template>

<style scoped>
.site-config-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr 0.8fr;
  gap: 10px;
}

.summary-grid article {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
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
.toolbar span,
.field span {
  color: #647586;
  font-size: 12px;
  font-weight: 800;
}

.summary-grid strong {
  overflow: hidden;
  color: var(--sw-text);
  font-size: 24px;
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

.toolbar-actions,
.form-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.icon-button {
  --n-border-radius: 8px !important;
  font-weight: 700;
}

.config-panel {
  min-height: 420px;
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-surface-solid);
  padding: 18px;
}

.config-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.config-skeleton {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.config-skeleton span {
  min-height: 58px;
  border-radius: 8px;
  background: linear-gradient(90deg, #e9eef1 0%, #f8fafb 45%, #e9eef1 100%);
  background-size: 200% 100%;
  animation: sw-shimmer 1.45s ease-in-out infinite;
}

.field {
  display: grid;
  gap: 7px;
}

.span-2 {
  grid-column: 1 / -1;
}

.switch-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  border: 1px solid #e2e8ee;
  border-radius: 8px;
  padding: 14px;
}

.switch-item span {
  display: grid;
  gap: 4px;
}

.switch-item strong {
  color: var(--sw-text);
  font-size: 14px;
  font-weight: 800;
}

.switch-item small {
  color: #647586;
  font-size: 12px;
  font-weight: 700;
}

.form-actions {
  padding-top: 8px;
}

.error-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 380px;
  color: #607283;
  text-align: center;
}

.error-state .n-icon {
  color: #b76b2b;
  font-size: 30px;
}

@media (max-width: 860px) {
  .summary-grid,
  .config-skeleton,
  .config-form,
  .switch-grid {
    grid-template-columns: 1fr;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-actions {
    justify-content: stretch;
  }

  .toolbar-actions .n-button {
    flex: 1;
  }
}
</style>
