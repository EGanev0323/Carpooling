import { createI18n } from 'vue-i18n'
import bg from './bg.json'
import en from './en.json'

export const i18n = createI18n({
  legacy: false,
  locale: 'bg',
  fallbackLocale: 'en',
  messages: { bg, en }
})
