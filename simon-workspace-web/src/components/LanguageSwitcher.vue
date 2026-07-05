<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NDropdown, NIcon } from 'naive-ui'
import { ChevronDown, Language } from '@vicons/tabler'

import {
  type AppLanguage,
  isAppLanguage,
  languageOptions,
  normalizeLanguage,
} from '../i18n/language'
import { setAppLanguage } from '../i18n'

const { locale, t } = useI18n()

const currentLanguage = computed(() => normalizeLanguage(locale.value))
const currentLabel = computed(() =>
  languageOptions.find((option) => option.value === currentLanguage.value)?.nativeLabel ?? 'English',
)
const dropdownOptions = computed(() =>
  languageOptions.map((option) => ({
    key: option.value,
    label: option.nativeLabel,
  })),
)

function selectLanguage(key: string | number) {
  const value = String(key)
  if (isAppLanguage(value)) {
    setAppLanguage(value as AppLanguage)
  }
}
</script>

<template>
  <n-dropdown
    trigger="click"
    :options="dropdownOptions"
    :value="currentLanguage"
    @select="selectLanguage"
  >
    <n-button class="language-switcher" secondary size="small" :aria-label="t('common.language')">
      <template #icon>
        <n-icon :component="Language" />
      </template>
      <span>{{ currentLabel }}</span>
      <n-icon class="chevron" :component="ChevronDown" />
    </n-button>
  </n-dropdown>
</template>

<style scoped>
.language-switcher {
  --n-border-radius: 8px !important;
  min-width: 0;
  border-color: var(--sw-border) !important;
  background: var(--sw-panel-bg) !important;
  color: var(--sw-muted) !important;
  font-weight: 700;
  backdrop-filter: blur(16px);
}

.language-switcher :deep(.n-button__content) {
  gap: 7px;
}

.chevron {
  font-size: 14px;
}
</style>
