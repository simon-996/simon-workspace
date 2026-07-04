<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NIcon, NInput, useMessage } from 'naive-ui'
import { ArrowRight, Command, Terminal2 } from '@vicons/tabler'

import AppHeader from '../components/AppHeader.vue'
import { useAuthStore } from '../stores/auth'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

const submitting = ref(false)

async function submitLogin() {
  if (!form.username.trim() || !form.password) {
    message.warning(t('login.missingCredentials'))
    return
  }

  submitting.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/workspace'
    await router.replace(redirect)
  } catch (error) {
    message.error(error instanceof Error ? error.message : t('login.failed'))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <AppHeader />

    <section class="login-shell">
      <div class="login-copy">
        <p class="eyebrow">{{ t('login.eyebrow') }}</p>
        <h1>{{ t('login.title') }}</h1>
        <p>{{ t('login.subtitle') }}</p>
      </div>

      <form class="login-panel" @submit.prevent="submitLogin">
        <div class="panel-header">
          <p>{{ t('login.panelTitle') }}</p>
          <span>{{ t('login.session') }}</span>
        </div>

        <label>
          <span>{{ t('login.username') }}</span>
          <n-input v-model:value="form.username" size="large" :placeholder="t('login.usernamePlaceholder')" clearable>
            <template #prefix>
              <n-icon :component="Command" />
            </template>
          </n-input>
        </label>

        <label>
          <span>{{ t('login.password') }}</span>
          <n-input
            v-model:value="form.password"
            size="large"
            :placeholder="t('login.passwordPlaceholder')"
            type="password"
            show-password-on="click"
            @keyup.enter="submitLogin"
          >
            <template #prefix>
              <n-icon :component="Terminal2" />
            </template>
          </n-input>
        </label>

        <n-button class="login-button" type="primary" size="large" attr-type="submit" :loading="submitting">
          <n-icon :component="ArrowRight" /> {{ t('login.submit') }}
        </n-button>
      </form>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  position: relative;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-height: 100dvh;
  background:
    radial-gradient(circle at 80% 22%, rgba(22, 112, 143, 0.08), transparent 28%),
    linear-gradient(180deg, #fbfcfc 0%, #f7f8f8 52%, #eef3f5 100%);
  color: #17212b;
  overflow: hidden;
}

.login-page::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: "";
  background:
    linear-gradient(90deg, rgba(23, 33, 43, 0.04) 1px, transparent 1px),
    linear-gradient(180deg, rgba(23, 33, 43, 0.03) 1px, transparent 1px);
  background-size: 96px 96px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.72), transparent 70%);
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(340px, 420px);
  align-items: center;
  gap: clamp(36px, 7vw, 92px);
  width: min(1080px, calc(100% - 48px));
  min-height: calc(100dvh - 74px);
  margin: 0 auto;
  padding: 48px 0;
  position: relative;
  z-index: 1;
}

.eyebrow {
  margin: 0 0 18px;
  color: #16708f;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
}

h1 {
  max-width: 560px;
  margin: 0;
  color: #17212b;
  font-size: clamp(40px, 5.6vw, 70px);
  font-weight: 600;
  letter-spacing: 0;
  line-height: 1.05;
}

.login-copy p:last-child {
  max-width: 440px;
  margin: 24px 0 0;
  color: #657783;
  font-size: 16px;
  line-height: 1.62;
}

.login-panel {
  display: grid;
  gap: 20px;
  border: 1px solid rgba(223, 231, 235, 0.95);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 55px rgba(32, 53, 66, 0.08);
  padding: 24px;
  backdrop-filter: blur(20px);
  animation: login-panel-in 580ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #dfe7eb;
  padding-bottom: 18px;
}

.panel-header p,
label span {
  margin: 0;
  color: #657783;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.panel-header span {
  color: #28734d;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
}

label {
  display: grid;
  gap: 10px;
}

.login-button {
  --n-border-radius: 6px !important;
  height: 52px;
  margin-top: 6px;
  font-size: 14px;
  font-weight: 700;
}

.login-button :deep(.n-button__content) {
  gap: 10px;
}

@keyframes login-panel-in {
  from {
    opacity: 0;
    transform: translate3d(0, 14px, 0) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
  }
}

@media (max-width: 820px) {
  .login-shell {
    grid-template-columns: 1fr;
    gap: 34px;
    width: min(100% - 32px, 520px);
  }

  .eyebrow {
    margin-top: 18px;
  }

  .login-panel {
    padding: 20px;
  }
}
</style>
