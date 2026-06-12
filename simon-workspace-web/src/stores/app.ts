import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    terminalVisible: true,
  }),
  actions: {
    toggleTerminal() {
      this.terminalVisible = !this.terminalVisible
    },
  },
})
