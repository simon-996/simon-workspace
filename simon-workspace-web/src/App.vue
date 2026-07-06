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
    primaryColor: '#176f84',
    primaryColorHover: '#1f8298',
    primaryColorPressed: '#115a69',
    primaryColorSuppl: '#1f8298',
    borderRadius: '8px',
    borderColor: theme.isDark ? '#22303c' : '#d8e4e6',
    textColorBase: theme.isDark ? '#f2f7fa' : '#16232a',
  },
  Button: {
    borderRadiusSmall: '8px',
    borderRadiusMedium: '8px',
    borderRadiusLarge: '8px',
    fontWeight: '700',
    textColorHover: '#115a69',
    textColorPressed: '#115a69',
  },
  Card: {
    borderRadius: '8px',
    paddingMedium: '22px',
  },
  Input: {
    borderRadius: '8px',
    borderHover: '1px solid #9ab8bd',
    borderFocus: '1px solid #176f84',
    boxShadowFocus: '0 0 0 3px rgba(23, 111, 132, 0.14)',
  },
  InputNumber: {
    borderRadius: '8px',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '8px',
        borderHover: '1px solid #9ab8bd',
        borderFocus: '1px solid #176f84',
        boxShadowFocus: '0 0 0 3px rgba(23, 111, 132, 0.14)',
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
