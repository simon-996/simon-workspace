<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  dateEnUS,
  dateThTH,
  dateZhCN,
  enUS,
  NConfigProvider,
  NMessageProvider,
  thTH,
  zhCN,
} from 'naive-ui'

const { locale } = useI18n()

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
</script>

<template>
  <NConfigProvider :locale="naiveLocale" :date-locale="naiveDateLocale">
    <NMessageProvider>
      <router-view />
    </NMessageProvider>
  </NConfigProvider>
</template>
