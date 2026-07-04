<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage, NAlert, NAvatar, NButton, NForm, NFormItem, NInput, NModal, NTabPane, NTabs } from 'naive-ui'

import { useAuthStore } from '../stores/auth'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
}>()

const { t } = useI18n()
const message = useMessage()
const auth = useAuthStore()
const avatarFileInput = ref<HTMLInputElement | null>(null)
const savingProfile = ref(false)
const savingPassword = ref(false)
const uploadingAvatar = ref(false)

const visible = computed({
  get: () => props.show,
  set: (value: boolean) => emit('update:show', value),
})

const profileForm = reactive({
  nickname: '',
  email: '',
  avatarUrl: '',
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const avatarPreview = computed(() => profileForm.avatarUrl || auth.user?.avatarUrl || '')
const accountInitial = computed(() =>
  (auth.user?.nickname?.trim() || auth.user?.username?.trim() || '?').slice(0, 1).toUpperCase(),
)
const passwordMismatch = computed(() =>
  Boolean(passwordForm.confirmPassword) && passwordForm.newPassword !== passwordForm.confirmPassword,
)
const passwordSaveDisabled = computed(() =>
  savingPassword.value
  || !passwordForm.currentPassword
  || !passwordForm.newPassword
  || !passwordForm.confirmPassword,
)

watch(
  () => [props.show, auth.user] as const,
  ([show]) => {
    if (show) {
      hydrateProfileForm()
    }
  },
  { immediate: true },
)

function hydrateProfileForm() {
  profileForm.nickname = auth.user?.nickname || ''
  profileForm.email = auth.user?.email || ''
  profileForm.avatarUrl = auth.user?.avatarUrl || ''
}

async function saveProfile() {
  if (!profileForm.nickname.trim()) {
    return
  }

  savingProfile.value = true
  try {
    await auth.updateProfile({
      nickname: profileForm.nickname.trim(),
      email: profileForm.email.trim() || null,
      avatarUrl: profileForm.avatarUrl.trim() || null,
    })
    message.success(t('account.profile.saved'))
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  if (passwordMismatch.value || passwordSaveDisabled.value) {
    return
  }

  savingPassword.value = true
  try {
    await auth.updatePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    message.success(t('account.password.saved'))
  } finally {
    savingPassword.value = false
  }
}

function openAvatarPicker() {
  avatarFileInput.value?.click()
}

async function selectAvatarFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }

  uploadingAvatar.value = true
  try {
    const resource = await auth.uploadAvatar(file)
    const avatarUrl = resource.publicUrl || (resource.id ? `/api/files/${resource.id}/download` : '')
    if (avatarUrl) {
      profileForm.avatarUrl = avatarUrl
      await auth.updateProfile({
        nickname: profileForm.nickname.trim(),
        email: profileForm.email.trim() || null,
        avatarUrl,
      })
      message.success(t('account.avatar.uploaded'))
    }
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}
</script>

<template>
  <n-modal v-model:show="visible" class="account-center-modal" preset="card" :title="t('account.title')">
    <n-tabs type="segment" animated>
      <n-tab-pane name="profile" :tab="t('account.tabs.profile')">
        <n-form class="account-form" label-placement="top">
          <n-form-item :label="t('account.profile.username')">
            <n-input :value="auth.user?.username || ''" readonly />
          </n-form-item>
          <n-form-item :label="t('account.profile.nickname')">
            <n-input v-model:value="profileForm.nickname" />
          </n-form-item>
          <n-form-item :label="t('account.profile.email')">
            <n-input v-model:value="profileForm.email" />
          </n-form-item>
          <n-form-item :label="t('account.profile.avatarUrl')">
            <n-input v-model:value="profileForm.avatarUrl" />
          </n-form-item>
          <div class="account-actions">
            <n-button type="primary" :loading="savingProfile" :disabled="!profileForm.nickname.trim()" @click="saveProfile">
              {{ t('account.profile.save') }}
            </n-button>
          </div>
        </n-form>
      </n-tab-pane>

      <n-tab-pane name="avatar" :tab="t('account.tabs.avatar')">
        <section class="avatar-panel">
          <n-avatar round :size="88" :src="avatarPreview || undefined">
            {{ accountInitial }}
          </n-avatar>
          <input
            ref="avatarFileInput"
            class="avatar-file-input"
            type="file"
            accept="image/*"
            @change="selectAvatarFile"
          >
          <n-button secondary :loading="uploadingAvatar" @click="openAvatarPicker">
            {{ t('account.avatar.upload') }}
          </n-button>
          <p>{{ t('account.avatar.hint') }}</p>
        </section>
      </n-tab-pane>

      <n-tab-pane name="password" :tab="t('account.tabs.password')">
        <n-form class="account-form" label-placement="top">
          <n-form-item :label="t('account.password.current')">
            <n-input v-model:value="passwordForm.currentPassword" type="password" show-password-on="click" />
          </n-form-item>
          <n-form-item :label="t('account.password.new')">
            <n-input v-model:value="passwordForm.newPassword" type="password" show-password-on="click" />
          </n-form-item>
          <n-form-item :label="t('account.password.confirm')">
            <n-input
              v-model:value="passwordForm.confirmPassword"
              type="password"
              show-password-on="click"
              :status="passwordMismatch ? 'error' : undefined"
            />
          </n-form-item>
          <n-alert v-if="passwordMismatch" type="error" :show-icon="false">
            {{ t('account.password.mismatch') }}
          </n-alert>
          <div class="account-actions">
            <n-button
              type="primary"
              :loading="savingPassword"
              :disabled="passwordMismatch || passwordSaveDisabled"
              @click="savePassword"
            >
              {{ t('account.password.save') }}
            </n-button>
          </div>
        </n-form>
      </n-tab-pane>
    </n-tabs>
  </n-modal>
</template>

<style scoped>
.account-center-modal {
  width: min(560px, calc(100vw - 32px));
  border-radius: 8px;
}

.account-form {
  display: grid;
  gap: 2px;
  padding-top: 12px;
}

.account-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.avatar-panel {
  display: grid;
  justify-items: center;
  gap: 14px;
  padding: 24px 0 8px;
  text-align: center;
}

.avatar-panel p {
  max-width: 360px;
  margin: 0;
  color: #667583;
  font-size: 13px;
  line-height: 1.6;
}

.avatar-file-input {
  display: none;
}
</style>
