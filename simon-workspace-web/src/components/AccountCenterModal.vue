<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  useMessage,
  NAlert,
  NAvatar,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSlider,
  NTabPane,
  NTabs,
} from 'naive-ui'

import { useAuthStore } from '../stores/auth'
import {
  clampAvatarCrop,
  createCenteredAvatarCrop,
  cropImageFileToAvatar,
  type AvatarCropState,
} from '../utils/avatarCrop'

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
const selectedAvatarFile = ref<File | null>(null)
const selectedAvatarUrl = ref('')
const avatarCrop = reactive<AvatarCropState>({
  x: 0,
  y: 0,
  size: 1,
  imageWidth: 1,
  imageHeight: 1,
})

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
const avatarCropSizeMax = computed(() => Math.min(avatarCrop.imageWidth, avatarCrop.imageHeight))
const avatarCropXMax = computed(() => Math.max(0, avatarCrop.imageWidth - avatarCrop.size))
const avatarCropYMax = computed(() => Math.max(0, avatarCrop.imageHeight - avatarCrop.size))
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

onBeforeUnmount(() => {
  revokeSelectedAvatarUrl()
})

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

  revokeSelectedAvatarUrl()
  selectedAvatarFile.value = file
  selectedAvatarUrl.value = URL.createObjectURL(file)
  const imageSize = await loadImageSize(selectedAvatarUrl.value)
  setAvatarCrop(createCenteredAvatarCrop(imageSize.width, imageSize.height))
  input.value = ''
}

async function uploadCroppedAvatar() {
  if (!selectedAvatarFile.value) {
    return
  }

  uploadingAvatar.value = true
  try {
    const croppedFile = await cropImageFileToAvatar(selectedAvatarFile.value, avatarCrop)
    const resource = await auth.uploadAvatar(croppedFile)
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
  }
}

function clampCurrentCrop() {
  setAvatarCrop(clampAvatarCrop(avatarCrop))
}

function setAvatarCrop(next: AvatarCropState) {
  Object.assign(avatarCrop, next)
}

function loadImageSize(url: string) {
  return new Promise<{ width: number; height: number }>((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve({ width: image.naturalWidth, height: image.naturalHeight })
    image.onerror = () => reject(new Error('Avatar image could not be loaded'))
    image.src = url
  })
}

function revokeSelectedAvatarUrl() {
  if (selectedAvatarUrl.value) {
    URL.revokeObjectURL(selectedAvatarUrl.value)
    selectedAvatarUrl.value = ''
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
          <div v-if="selectedAvatarUrl" class="avatar-crop-panel">
            <div class="avatar-crop-preview">
              <img :src="selectedAvatarUrl" alt="">
            </div>
            <div class="avatar-crop-controls">
              <label>
                <span>{{ t('account.avatar.positionX') }}</span>
                <n-slider v-model:value="avatarCrop.x" :min="0" :max="avatarCropXMax" @update:value="clampCurrentCrop" />
              </label>
              <label>
                <span>{{ t('account.avatar.positionY') }}</span>
                <n-slider v-model:value="avatarCrop.y" :min="0" :max="avatarCropYMax" @update:value="clampCurrentCrop" />
              </label>
              <label>
                <span>{{ t('account.avatar.size') }}</span>
                <n-slider v-model:value="avatarCrop.size" :min="1" :max="avatarCropSizeMax" @update:value="clampCurrentCrop" />
              </label>
            </div>
          </div>
          <input
            ref="avatarFileInput"
            class="avatar-file-input"
            type="file"
            accept="image/*"
            @change="selectAvatarFile"
          >
          <div class="avatar-actions">
            <n-button secondary @click="openAvatarPicker">
              {{ t('account.avatar.choose') }}
            </n-button>
            <n-button type="primary" :disabled="!selectedAvatarFile" :loading="uploadingAvatar" @click="uploadCroppedAvatar">
              {{ t('account.avatar.upload') }}
            </n-button>
          </div>
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

.avatar-crop-panel {
  display: grid;
  gap: 14px;
  width: 100%;
}

.avatar-crop-preview {
  overflow: hidden;
  width: min(260px, 70vw);
  aspect-ratio: 1;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #f7f8f8;
}

.avatar-crop-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.avatar-crop-controls {
  display: grid;
  gap: 10px;
  width: 100%;
}

.avatar-crop-controls label {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  color: #667583;
  font-size: 12px;
  font-weight: 800;
}

.avatar-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
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
