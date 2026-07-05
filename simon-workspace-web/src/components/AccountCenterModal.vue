<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch, type CSSProperties } from 'vue'
import { useI18n } from 'vue-i18n'
import { CircleStencil, Cropper, Preview, type CropperResult } from 'vue-advanced-cropper'
import 'vue-advanced-cropper/dist/style.css'
import {
  useMessage,
  NAlert,
  NAvatar,
  NButton,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NTabPane,
  NTabs,
} from 'naive-ui'

import { useAuthStore } from '../stores/auth'
import { resolveAvatarUrl } from '../utils/avatarUrl'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
}>()

type AvatarCropResult = Pick<CropperResult, 'coordinates' | 'image'>

const { t } = useI18n()
const message = useMessage()
const auth = useAuthStore()
const avatarFileInput = ref<HTMLInputElement | null>(null)
const savingProfile = ref(false)
const savingPassword = ref(false)
const uploadingAvatar = ref(false)
const selectedAvatarFile = ref<File | null>(null)
const selectedAvatarUrl = ref('')
const avatarCropResult = ref<AvatarCropResult | null>(null)
const avatarCropCanvas = ref<HTMLCanvasElement | null>(null)

const modalStyle: CSSProperties = {
  width: 'min(760px, calc(100vw - 32px))',
  borderRadius: '8px',
}

const modalContentStyle: CSSProperties = {
  padding: '0',
  maxHeight: 'calc(100dvh - 132px)',
  overflow: 'hidden',
}

const cropperStencilProps = {
  aspectRatio: 1,
}

const cropperCanvasOptions = {
  width: 512,
  height: 512,
  imageSmoothingEnabled: true,
  imageSmoothingQuality: 'high',
}

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

const avatarPreview = computed(() => resolveAvatarUrl(profileForm.avatarUrl || auth.user?.avatarUrl))
const displayName = computed(() =>
  auth.user?.nickname?.trim() || auth.user?.username?.trim() || t('common.account'),
)
const usernameLabel = computed(() => auth.user?.username ? `@${auth.user.username}` : '')
const roleSummary = computed(() => auth.user?.roles?.join(' / ') || '')
const accountInitial = computed(() =>
  (displayName.value || '?').slice(0, 1).toUpperCase(),
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
    } else {
      resetSelectedAvatar()
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  resetSelectedAvatar()
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

function selectAvatarFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }

  resetSelectedAvatar()
  selectedAvatarFile.value = file
  selectedAvatarUrl.value = URL.createObjectURL(file)
  input.value = ''
}

function onAvatarCropChange(result: CropperResult) {
  avatarCropResult.value = {
    coordinates: result.coordinates,
    image: result.image,
  }
  avatarCropCanvas.value = result.canvas || null
}

async function uploadCroppedAvatar() {
  if (!selectedAvatarFile.value || !avatarCropCanvas.value) {
    return
  }

  uploadingAvatar.value = true
  try {
    const croppedFile = await createAvatarFile(avatarCropCanvas.value)
    const resource = await auth.uploadAvatar(croppedFile)
    const avatarUrl = resource.publicUrl || (resource.id ? `/api/files/${resource.id}/download` : '')
    if (avatarUrl) {
      profileForm.avatarUrl = avatarUrl
      await auth.updateProfile({
        nickname: profileForm.nickname.trim(),
        email: profileForm.email.trim() || null,
        avatarUrl,
      })
      resetSelectedAvatar()
      message.success(t('account.avatar.uploaded'))
    }
  } finally {
    uploadingAvatar.value = false
  }
}

function createAvatarFile(canvas: HTMLCanvasElement) {
  return new Promise<File>((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error('Avatar crop failed'))
        return
      }
      resolve(new File([blob], 'avatar.webp', { type: 'image/webp' }))
    }, 'image/webp', 0.9)
  })
}

function resetSelectedAvatar() {
  selectedAvatarFile.value = null
  avatarCropResult.value = null
  avatarCropCanvas.value = null
  revokeSelectedAvatarUrl()
}

function revokeSelectedAvatarUrl() {
  if (selectedAvatarUrl.value) {
    URL.revokeObjectURL(selectedAvatarUrl.value)
    selectedAvatarUrl.value = ''
  }
}
</script>

<template>
  <n-modal
    v-model:show="visible"
    class="account-center-modal"
    preset="card"
    :title="t('account.title')"
    :style="modalStyle"
    :content-style="modalContentStyle"
  >
    <div class="account-layout">
      <aside class="account-summary">
        <n-avatar round :size="76" :src="avatarPreview || undefined">
          {{ accountInitial }}
        </n-avatar>
        <div class="account-summary-copy">
          <strong>{{ displayName }}</strong>
          <span v-if="usernameLabel">{{ usernameLabel }}</span>
          <small v-if="roleSummary">{{ roleSummary }}</small>
        </div>
      </aside>

      <section class="account-main">
        <n-tabs type="segment" animated>
          <n-tab-pane name="profile" :tab="t('account.tabs.profile')">
            <n-form class="account-form" label-placement="top">
              <n-form-item :label="t('account.profile.nickname')">
                <n-input v-model:value="profileForm.nickname" />
              </n-form-item>
              <n-form-item :label="t('account.profile.email')">
                <n-input v-model:value="profileForm.email" />
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
              <div class="avatar-head">
                <n-avatar round :size="64" :src="avatarPreview || undefined">
                  {{ accountInitial }}
                </n-avatar>
                <div>
                  <strong>{{ t('account.avatar.current') }}</strong>
                  <span>{{ displayName }}</span>
                </div>
              </div>

              <div v-if="selectedAvatarUrl" class="avatar-crop-panel">
                <Cropper
                  class="avatar-cropper"
                  :src="selectedAvatarUrl"
                  :stencil-component="CircleStencil"
                  :stencil-props="cropperStencilProps"
                  :canvas="cropperCanvasOptions"
                  :debounce="false"
                  image-restriction="stencil"
                  @change="onAvatarCropChange"
                />
                <div class="avatar-preview-panel">
                  <span>{{ t('account.avatar.preview') }}</span>
                  <Preview
                    v-if="avatarCropResult"
                    class="avatar-live-preview"
                    :width="104"
                    :height="104"
                    :image="avatarCropResult.image"
                    :coordinates="avatarCropResult.coordinates"
                  />
                </div>
              </div>

              <div v-else class="avatar-empty">
                <p>{{ t('account.avatar.hint') }}</p>
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
                <n-button type="primary" :disabled="!avatarCropCanvas" :loading="uploadingAvatar" @click="uploadCroppedAvatar">
                  {{ t('account.avatar.upload') }}
                </n-button>
              </div>
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
      </section>
    </div>
  </n-modal>
</template>

<style scoped>
.account-center-modal :deep(.n-card-header) {
  padding: 18px 20px 12px;
}

.account-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  min-height: 430px;
}

.account-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  border-right: 1px solid #dfe6eb;
  background: #f7f8f8;
}

.account-summary-copy {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.account-summary-copy strong {
  overflow: hidden;
  color: #17212b;
  font-size: 18px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-summary-copy span,
.account-summary-copy small {
  overflow: hidden;
  color: #667583;
  font-size: 13px;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-main {
  min-width: 0;
  max-height: calc(100dvh - 156px);
  overflow: auto;
  padding: 20px;
}

.account-form {
  display: grid;
  gap: 2px;
  padding-top: 14px;
}

.account-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
}

.avatar-panel {
  display: grid;
  gap: 16px;
  padding-top: 16px;
}

.avatar-head {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar-head div {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.avatar-head strong {
  color: #17212b;
  font-size: 14px;
  line-height: 1.2;
}

.avatar-head span {
  overflow: hidden;
  color: #667583;
  font-size: 13px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avatar-crop-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 128px;
  align-items: start;
  gap: 14px;
}

.avatar-cropper {
  overflow: hidden;
  height: 316px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #eef2f4;
}

.avatar-preview-panel {
  display: grid;
  justify-items: center;
  gap: 12px;
  padding-top: 8px;
}

.avatar-preview-panel span {
  color: #667583;
  font-size: 12px;
  font-weight: 700;
}

.avatar-live-preview {
  overflow: hidden;
  width: 104px;
  height: 104px;
  border: 1px solid #d8e0e7;
  border-radius: 50%;
  background: #f7f8f8;
}

.avatar-empty {
  display: grid;
  align-items: center;
  min-height: 120px;
  border: 1px dashed #c9d3dc;
  border-radius: 8px;
  background: #fbfcfc;
  text-align: center;
}

.avatar-empty p {
  max-width: 320px;
  margin: 0 auto;
  color: #667583;
  font-size: 13px;
  line-height: 1.6;
}

.avatar-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.avatar-file-input {
  display: none;
}

@media (max-width: 720px) {
  .account-layout {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .account-summary {
    flex-direction: row;
    align-items: center;
    padding: 16px;
    border-right: 0;
    border-bottom: 1px solid #dfe6eb;
  }

  .account-main {
    max-height: calc(100dvh - 252px);
    padding: 16px;
  }

  .avatar-crop-panel {
    grid-template-columns: 1fr;
  }

  .avatar-cropper {
    height: min(340px, calc(100vw - 64px));
  }

  .avatar-actions {
    justify-content: stretch;
  }

  .avatar-actions :deep(.n-button) {
    flex: 1 1 120px;
  }
}
</style>
