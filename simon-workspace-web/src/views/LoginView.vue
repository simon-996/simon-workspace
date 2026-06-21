<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NIcon, NInput, useMessage } from 'naive-ui'
import { ArrowRight, Command, Terminal2 } from '@vicons/tabler'

import { useAuthStore } from '../stores/auth'

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
    message.warning('请输入用户名和密码')
    return
  }

  submitting.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/workspace'
    await router.replace(redirect)
  } catch (error) {
    message.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-shell">
      <div class="login-copy">
        <router-link class="brand" to="/"><span>&gt;_</span> Simon Workspace</router-link>
        <p class="eyebrow">// Secure workspace</p>
        <h1>Sign in to continue.</h1>
        <p>Courses, templates, generated files, and teaching records stay behind your workspace token.</p>
      </div>

      <form class="login-panel" @submit.prevent="submitLogin">
        <div class="panel-header">
          <p>Authentication</p>
          <span>Token session</span>
        </div>

        <label>
          <span>Username</span>
          <n-input v-model:value="form.username" size="large" placeholder="simon" clearable>
            <template #prefix>
              <n-icon :component="Command" />
            </template>
          </n-input>
        </label>

        <label>
          <span>Password</span>
          <n-input
            v-model:value="form.password"
            size="large"
            placeholder="Password"
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
          <n-icon :component="ArrowRight" /> Enter Workspace
        </n-button>
      </form>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  min-height: 100dvh;
  background:
    linear-gradient(90deg, rgba(62, 198, 255, 0.08) 1px, transparent 1px),
    linear-gradient(180deg, rgba(62, 198, 255, 0.04) 1px, transparent 1px),
    #061018;
  background-size: 120px 120px;
  color: #f6fbff;
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(360px, 440px);
  align-items: center;
  gap: 84px;
  width: min(1180px, calc(100% - 48px));
  min-height: 100dvh;
  margin: 0 auto;
  padding: 48px 0;
}

.brand {
  align-items: center;
  display: inline-flex;
  gap: 12px;
  color: #f5fbff;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand span,
.eyebrow {
  color: #3fc6ff;
}

.eyebrow {
  margin: 76px 0 20px;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
}

h1 {
  max-width: 620px;
  margin: 0;
  color: #f9fcff;
  font-size: clamp(42px, 6vw, 76px);
  font-weight: 500;
  letter-spacing: 0;
  line-height: 1.05;
}

.login-copy p:last-child {
  max-width: 520px;
  margin: 26px 0 0;
  color: #aab5c2;
  font-size: 18px;
  line-height: 1.55;
}

.login-panel {
  display: grid;
  gap: 22px;
  border: 1px solid rgba(88, 167, 217, 0.32);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(10, 23, 35, 0.94), rgba(5, 13, 22, 0.96)),
    #07111b;
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.38);
  padding: 28px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(88, 167, 217, 0.22);
  padding-bottom: 20px;
}

.panel-header p,
label span {
  margin: 0;
  color: #39bdf2;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.panel-header span {
  color: #80d778;
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
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-button :deep(.n-button__content) {
  gap: 10px;
}

@media (max-width: 820px) {
  .login-shell {
    grid-template-columns: 1fr;
    gap: 34px;
    width: min(100% - 32px, 520px);
  }

  .eyebrow {
    margin-top: 48px;
  }

  .login-panel {
    padding: 20px;
  }
}
</style>
