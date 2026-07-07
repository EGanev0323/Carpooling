import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const theme = ref<'dark' | 'light'>('dark')

  /** Toggles between dark and light theme, persisting the choice to the DOM. */
  function toggleTheme(): void {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  return { theme, toggleTheme }
})
