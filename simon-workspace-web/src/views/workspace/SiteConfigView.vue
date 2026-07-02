<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NButton, NIcon, NInput, NSpin, NSwitch, useMessage } from 'naive-ui'
import { AlertTriangle, ExternalLink, Eye, Refresh, Settings } from '@vicons/tabler'

import {
  fetchAdminSiteConfig,
  updateAdminSiteConfig,
  type SiteConfig,
  type SiteConfigPayload,
} from '../../api/site'

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
    error.value = err instanceof Error ? err.message : '站点配置加载失败'
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

async function submitConfig() {
  if (!form.siteTitle.trim()) {
    message.warning('请输入站点标题')
    return
  }
  if (!form.ownerName.trim()) {
    message.warning('请输入所有者名称')
    return
  }
  if (!form.heroTitle.trim()) {
    message.warning('请输入首页标题')
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
    message.success('站点配置已保存')
  } catch (err) {
    message.error(err instanceof Error ? err.message : '站点配置保存失败')
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
        <span>站点标题</span>
        <strong>{{ form.siteTitle || '-' }}</strong>
      </article>
      <article>
        <n-icon :component="Eye" />
        <span>公开模块</span>
        <strong>{{ visibleCount }}</strong>
      </article>
      <article>
        <n-icon :component="Settings" />
        <span>最近更新</span>
        <strong>{{ config?.updatedTime ? config.updatedTime.slice(0, 10) : '-' }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <span>控制访客在首页能看到的内容</span>
      <div class="toolbar-actions">
        <n-button secondary class="icon-button" @click="loadConfig">
          <template #icon>
            <n-icon :component="Refresh" />
          </template>
          刷新
        </n-button>
        <n-button tag="a" href="/" target="_blank" class="icon-button">
          <template #icon>
            <n-icon :component="ExternalLink" />
          </template>
          打开首页
        </n-button>
      </div>
    </section>

    <section class="config-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadConfig">重试</n-button>
      </div>

      <n-spin v-else :show="loading">
        <form class="config-form" @submit.prevent="submitConfig">
          <label class="field">
            <span>站点标题</span>
            <n-input v-model:value="form.siteTitle" placeholder="例如：Simon Workspace" />
          </label>

          <label class="field">
            <span>所有者名称</span>
            <n-input v-model:value="form.ownerName" placeholder="例如：Simon" />
          </label>

          <label class="field span-2">
            <span>首页标题</span>
            <n-input v-model:value="form.heroTitle" placeholder="首页第一屏主标题" />
          </label>

          <label class="field span-2">
            <span>首页副标题</span>
            <n-input
              v-model:value="form.heroSubtitle"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
              placeholder="给访客看的简短介绍"
            />
          </label>

          <label class="field">
            <span>身份说明</span>
            <n-input v-model:value="form.ownerRole" placeholder="例如：软件教师 / 独立开发者" />
          </label>

          <label class="field">
            <span>公开邮箱</span>
            <n-input v-model:value="form.contactEmail" placeholder="可留空" />
          </label>

          <label class="field span-2">
            <span>GitHub 地址</span>
            <n-input v-model:value="form.githubUrl" placeholder="https://github.com/..." />
          </label>

          <div class="switch-grid span-2">
            <label class="switch-item">
              <span>
                <strong>个人简介</strong>
                <small>首页显示关于我模块</small>
              </span>
              <n-switch v-model:value="form.profileVisible" />
            </label>
            <label class="switch-item">
              <span>
                <strong>博客入口</strong>
                <small>首页显示博客占位和入口</small>
              </span>
              <n-switch v-model:value="form.blogVisible" />
            </label>
            <label class="switch-item">
              <span>
                <strong>项目展示</strong>
                <small>首页显示公开项目模块</small>
              </span>
              <n-switch v-model:value="form.projectsVisible" />
            </label>
            <label class="switch-item">
              <span>
                <strong>工作台入口</strong>
                <small>首页导航显示工作台入口</small>
              </span>
              <n-switch v-model:value="form.workspaceEntryVisible" />
            </label>
          </div>

          <div class="form-actions span-2">
            <n-button @click="loadConfig">重置</n-button>
            <n-button type="primary" attr-type="submit" :loading="saving">保存配置</n-button>
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
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
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
  color: #111a23;
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
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
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
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;
}

.config-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
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
  color: #111a23;
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
