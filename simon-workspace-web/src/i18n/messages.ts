import en from './locales/en'
import thTH from './locales/th-TH'
import zhCN from './locales/zh-CN'

export const messages = {
  en,
  'zh-CN': zhCN,
  'th-TH': thTH,
} as const

export type MessageSchema = typeof en
