<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { NButton, NIcon, NInput, useMessage } from 'naive-ui'
import { ArrowLeft, ArrowRight, Command, Mail, Terminal2, User } from '@vicons/tabler'

import AppHeader from '../components/AppHeader.vue'
import { notifyAppError } from '../api/errorMessage'
import { registerAccount } from '../stores/auth'

const { t } = useI18n()
const router = useRouter()
const message = useMessage()

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
})

const submitting = ref(false)
const submitted = ref(false)

async function submitRegister() {
  if (!form.username.trim() || !form.nickname.trim() || !form.password) {
    message.warning(t('register.missingFields'))
    return
  }
  if (form.password.length < 8) {
    message.warning(t('register.passwordTooShort'))
    return
  }

  submitting.value = true
  try {
    await registerAccount({
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      email: form.email.trim() || null,
      password: form.password,
    })
    submitted.value = true
    message.success(t('register.success'))
  } catch (error) {
    notifyAppError(error, message, t)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="register-page">
    <AppHeader />

    <section class="register-shell">
      <form v-if="!submitted" class="register-panel" @submit.prevent="submitRegister">
        <label>
          <span>{{ t('register.username') }}</span>
          <n-input v-model:value="form.username" size="large" :placeholder="t('register.usernamePlaceholder')" clearable>
            <template #prefix>
              <n-icon :component="Command" />
            </template>
          </n-input>
        </label>

        <label>
          <span>{{ t('register.nickname') }}</span>
          <n-input v-model:value="form.nickname" size="large" :placeholder="t('register.nicknamePlaceholder')" clearable>
            <template #prefix>
              <n-icon :component="User" />
            </template>
          </n-input>
        </label>

        <label>
          <span>{{ t('register.email') }}</span>
          <n-input v-model:value="form.email" size="large" :placeholder="t('register.emailPlaceholder')" clearable>
            <template #prefix>
              <n-icon :component="Mail" />
            </template>
          </n-input>
        </label>

        <label>
          <span>{{ t('register.password') }}</span>
          <n-input
            v-model:value="form.password"
            size="large"
            :placeholder="t('register.passwordPlaceholder')"
            type="password"
            show-password-on="click"
            @keyup.enter="submitRegister"
          >
            <template #prefix>
              <n-icon :component="Terminal2" />
            </template>
          </n-input>
        </label>

        <n-button class="register-button" type="primary" size="large" attr-type="submit" :loading="submitting">
          <n-icon :component="ArrowRight" /> {{ t('register.submit') }}
        </n-button>
        <n-button text class="secondary-link" @click="router.push('/login')">
          <n-icon :component="ArrowLeft" /> {{ t('register.backToLogin') }}
        </n-button>
      </form>

      <section v-else class="register-panel success-panel">
        <strong>{{ t('register.pendingTitle') }}</strong>
        <p>{{ t('register.pendingText') }}</p>
        <n-button type="primary" size="large" @click="router.push('/login')">
          {{ t('register.backToLogin') }}
        </n-button>
      </section>
    </section>
  </main>
</template>

<style scoped>
.register-page {
  position: relative;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-height: 100dvh;
  background: var(--sw-page-bg);
  color: var(--sw-text);
  overflow: hidden;
}

.register-page::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: "";
  background:
    linear-gradient(90deg, var(--sw-grid-line) 1px, transparent 1px),
    linear-gradient(180deg, var(--sw-grid-line-soft) 1px, transparent 1px);
  background-size: 96px 96px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.72), transparent 70%);
}

.register-shell {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  width: min(100% - 48px, 520px);
  min-height: calc(100dvh - 74px);
  margin: 0 auto;
  padding: 32px 0;
}

.register-panel {
  display: grid;
  gap: 16px;
  width: min(100%, 430px);
  border: 1px solid var(--sw-border);
  border-radius: 8px;
  background: var(--sw-panel-bg);
  box-shadow: var(--sw-shadow);
  padding: 24px;
  backdrop-filter: blur(20px);
  animation: register-panel-in 560ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

label {
  display: grid;
  gap: 9px;
}

label span {
  color: var(--sw-muted);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.register-button {
  --n-border-radius: 6px !important;
  height: 52px;
  margin-top: 4px;
  font-weight: 700;
}

.register-button :deep(.n-button__content),
.secondary-link :deep(.n-button__content) {
  gap: 9px;
}

.secondary-link {
  justify-self: center;
}

.success-panel {
  text-align: center;
}

.success-panel strong {
  color: var(--sw-text);
  font-size: 22px;
}

.success-panel p {
  margin: 0;
  color: var(--sw-muted);
  line-height: 1.7;
}

@keyframes register-panel-in {
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
  .register-shell {
    width: min(100% - 32px, 520px);
  }

  .register-panel {
    padding: 20px;
  }
}
</style>
