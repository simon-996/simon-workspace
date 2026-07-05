<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  dateEnUS,
  dateThTH,
  dateZhCN,
  darkTheme,
  enUS,
  type GlobalThemeOverrides,
  NConfigProvider,
  NMessageProvider,
  thTH,
  zhCN,
} from 'naive-ui'

import { useThemeStore } from './stores/theme'

const { locale } = useI18n()
const theme = useThemeStore()

const naiveLocale = computed(() => {
  if (locale.value === 'zh-CN') return zhCN
  if (locale.value === 'th-TH') return thTH
  return enUS
})

const naiveDateLocale = computed(() => {
  if (locale.value === 'zh-CN') return dateZhCN
  if (locale.value === 'th-TH') return dateThTH
  return dateEnUS
})

const themeOverrides = computed<GlobalThemeOverrides>(() => ({
  common: {
    fontFamily:
      'Outfit, Geist, Satoshi, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", sans-serif',
    fontFamilyMono: '"JetBrains Mono", "SFMono-Regular", Consolas, monospace',
    primaryColor: '#16708f',
    primaryColorHover: '#1d86a9',
    primaryColorPressed: '#105c76',
    primaryColorSuppl: '#1d86a9',
    borderRadius: '8px',
    borderColor: theme.isDark ? '#22303c' : '#dfe7eb',
    textColorBase: theme.isDark ? '#f2f7fa' : '#17212b',
  },
  Button: {
    borderRadiusSmall: '8px',
    borderRadiusMedium: '8px',
    borderRadiusLarge: '8px',
    fontWeight: '700',
    textColorHover: '#105c76',
    textColorPressed: '#105c76',
  },
  Card: {
    borderRadius: '8px',
    paddingMedium: '22px',
  },
  Input: {
    borderRadius: '8px',
    borderHover: '1px solid #9bb8c2',
    borderFocus: '1px solid #16708f',
    boxShadowFocus: '0 0 0 2px rgba(22, 112, 143, 0.12)',
  },
  InputNumber: {
    borderRadius: '8px',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '8px',
        borderHover: '1px solid #9bb8c2',
        borderFocus: '1px solid #16708f',
        boxShadowFocus: '0 0 0 2px rgba(22, 112, 143, 0.12)',
      },
    },
  },
  Modal: {
    borderRadius: '8px',
  },
}))
</script>

<template>
  <NConfigProvider
    :locale="naiveLocale"
    :date-locale="naiveDateLocale"
    :theme="theme.isDark ? darkTheme : null"
    :theme-overrides="themeOverrides"
  >
    <NMessageProvider>
      <router-view />
    </NMessageProvider>
  </NConfigProvider>
</template>
