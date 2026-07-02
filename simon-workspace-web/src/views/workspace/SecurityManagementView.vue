<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  NButton,
  NCheckbox,
  NCheckboxGroup,
  NIcon,
  NInput,
  NModal,
  NSpin,
  NTag,
  useMessage,
} from 'naive-ui'
import { AlertTriangle, CircleCheck, Edit, Refresh, Search, Users } from '@vicons/tabler'

import {
  fetchSecurityRoles,
  fetchSecurityUsers,
  updateSecurityUserRoles,
  type ManagedUser,
  type Role,
} from '../../api/workspace'

const message = useMessage()

const users = ref<ManagedUser[]>([])
const roles = ref<Role[]>([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const modalVisible = ref(false)
const editingUser = ref<ManagedUser | null>(null)
const selectedRoles = ref<string[]>([])

const enabledCount = computed(() => users.value.filter((user) => user.status === 'ENABLED').length)
const ownerCount = computed(() => users.value.filter((user) => user.roles.includes('OWNER')).length)
const permissionCount = computed(() => {
  const codes = new Set<string>()
  roles.value.forEach((role) => role.permissions.forEach((permission) => codes.add(permission.permissionCode)))
  return codes.size
})
const modalTitle = computed(() => editingUser.value ? `设置 ${editingUser.value.nickname || editingUser.value.username} 的角色` : '设置角色')

onMounted(() => {
  void loadSecurity()
})

async function loadSecurity() {
  loading.value = true
  error.value = ''
  try {
    const [userList, roleList] = await Promise.all([
      fetchSecurityUsers(keyword.value.trim()),
      fetchSecurityRoles(),
    ])
    users.value = userList
    roles.value = roleList
  } catch (err) {
    error.value = err instanceof Error ? err.message : '权限数据加载失败'
    message.error(error.value)
  } finally {
    loading.value = false
  }
}

function openRoleModal(user: ManagedUser) {
  editingUser.value = user
  selectedRoles.value = [...user.roles]
  modalVisible.value = true
}

async function submitRoles() {
  if (!editingUser.value) {
    return
  }

  saving.value = true
  try {
    const updated = await updateSecurityUserRoles(editingUser.value.id, {
      roleCodes: selectedRoles.value,
    })
    users.value = users.value.map((user) => user.id === updated.id ? updated : user)
    message.success('角色已更新')
    modalVisible.value = false
  } catch (err) {
    message.error(err instanceof Error ? err.message : '角色更新失败')
  } finally {
    saving.value = false
  }
}

function roleName(roleCode: string) {
  return roles.value.find((role) => role.roleCode === roleCode)?.roleName || roleCode
}

function statusText(status: string) {
  if (status === 'ENABLED') return '启用'
  if (status === 'DISABLED') return '停用'
  return status
}
</script>

<template>
  <section class="security-page">
    <div class="summary-grid">
      <article>
        <n-icon :component="Users" />
        <span>账号总数</span>
        <strong>{{ users.length }}</strong>
      </article>
      <article>
        <n-icon :component="CircleCheck" />
        <span>启用账号</span>
        <strong>{{ enabledCount }}</strong>
      </article>
      <article>
        <n-icon :component="Users" />
        <span>OWNER</span>
        <strong>{{ ownerCount }}</strong>
      </article>
      <article>
        <n-icon :component="CircleCheck" />
        <span>权限点</span>
        <strong>{{ permissionCount }}</strong>
      </article>
    </div>

    <section class="toolbar">
      <n-input
        v-model:value="keyword"
        clearable
        placeholder="搜索账号、昵称或邮箱"
        @keyup.enter="loadSecurity"
      >
        <template #prefix>
          <n-icon :component="Search" />
        </template>
      </n-input>
      <n-button secondary class="icon-button" @click="loadSecurity">
        <template #icon>
          <n-icon :component="Refresh" />
        </template>
        刷新
      </n-button>
    </section>

    <section class="table-panel">
      <div v-if="error" class="error-state">
        <n-icon :component="AlertTriangle" />
        <span>{{ error }}</span>
        <n-button size="small" tertiary @click="loadSecurity">重试</n-button>
      </div>

      <n-spin v-else-if="loading" :show="loading">
        <div class="skeleton-table">
          <span v-for="index in 6" :key="index" />
        </div>
      </n-spin>

      <div v-else-if="users.length === 0" class="empty-state">
        <strong>暂无账号</strong>
      </div>

      <div v-else class="security-table-wrap">
        <table class="security-table">
          <thead>
            <tr>
              <th>账号</th>
              <th>状态</th>
              <th>角色</th>
              <th>最近登录</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>
                <strong>{{ user.nickname || user.username }}</strong>
                <span>{{ user.username }} · {{ user.email || '-' }}</span>
              </td>
              <td>
                <n-tag :type="user.status === 'ENABLED' ? 'success' : 'warning'" size="small" round>
                  {{ statusText(user.status) }}
                </n-tag>
              </td>
              <td>
                <div class="role-tags">
                  <n-tag v-for="role in user.roles" :key="role" size="small" round>
                    {{ roleName(role) }}
                  </n-tag>
                  <span v-if="user.roles.length === 0" class="muted">未分配</span>
                </div>
              </td>
              <td>{{ user.lastLoginTime ? user.lastLoginTime.slice(0, 16).replace('T', ' ') : '-' }}</td>
              <td>{{ user.updatedTime ? user.updatedTime.slice(0, 10) : '-' }}</td>
              <td>
                <n-button quaternary size="small" @click="openRoleModal(user)">
                  <template #icon>
                    <n-icon :component="Edit" />
                  </template>
                </n-button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="role-panel">
      <header>
        <strong>角色权限</strong>
        <span>{{ roles.length }} 个角色</span>
      </header>
      <div class="role-grid">
        <article v-for="role in roles" :key="role.id">
          <div class="role-head">
            <strong>{{ role.roleName }}</strong>
            <span>{{ role.roleCode }}</span>
          </div>
          <p>{{ role.description || '-' }}</p>
          <div class="permission-tags">
            <n-tag v-for="permission in role.permissions" :key="permission.id" size="small">
              {{ permission.permissionName }}
            </n-tag>
          </div>
        </article>
      </div>
    </section>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" class="security-modal">
      <form class="role-form" @submit.prevent="submitRoles">
        <n-checkbox-group v-model:value="selectedRoles">
          <div class="role-options">
            <label v-for="role in roles" :key="role.id" class="role-option">
              <n-checkbox :value="role.roleCode" />
              <span>
                <strong>{{ role.roleName }}</strong>
                <em>{{ role.roleCode }} · {{ role.permissions.length }} 项权限</em>
              </span>
            </label>
          </div>
        </n-checkbox-group>

        <div class="form-actions">
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" attr-type="submit" :loading="saving">保存</n-button>
        </div>
      </form>
    </n-modal>
  </section>
</template>

<style scoped>
.security-page {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.summary-grid article {
  display: grid;
  grid-template-columns: 42px 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 12px;
  min-height: 86px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
}

.summary-grid .n-icon {
  grid-row: 1 / 3;
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: #e7f5fb;
  color: #1688b9;
  font-size: 22px;
}

.summary-grid span,
.role-panel header span {
  color: #647586;
  font-size: 12px;
  font-weight: 800;
}

.summary-grid strong {
  color: #111a23;
  font-size: 28px;
  font-weight: 800;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(0, 440px) auto;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  padding: 12px;
}

.icon-button {
  --n-border-radius: 8px !important;
  font-weight: 700;
}

.table-panel,
.role-panel {
  border: 1px solid #d8e0e7;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
}

.table-panel {
  min-height: 360px;
}

.security-table-wrap {
  overflow-x: auto;
}

.security-table {
  width: 100%;
  min-width: 940px;
  border-collapse: collapse;
}

.security-table th {
  border-bottom: 1px solid #d8e0e7;
  background: #f7f9fb;
  color: #5e7181;
  font-size: 12px;
  font-weight: 800;
  text-align: left;
}

.security-table th,
.security-table td {
  padding: 14px 16px;
}

.security-table td {
  border-bottom: 1px solid #edf1f4;
  color: #2b3843;
  font-size: 14px;
  vertical-align: top;
}

.security-table tr:last-child td {
  border-bottom: 0;
}

.security-table td strong {
  display: block;
  color: #111a23;
  font-weight: 800;
}

.security-table td span {
  display: block;
  margin-top: 4px;
  color: #687a89;
  font-size: 12px;
}

.role-tags,
.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.muted {
  color: #7b8d9b !important;
  font-size: 13px !important;
}

.error-state,
.empty-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  min-height: 360px;
  color: #607283;
  text-align: center;
}

.error-state .n-icon {
  color: #b76b2b;
  font-size: 30px;
}

.empty-state strong {
  color: #111a23;
  font-size: 18px;
}

.skeleton-table {
  display: grid;
  gap: 10px;
  padding: 18px;
}

.skeleton-table span {
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(90deg, #eef3f6, #f8fafb, #eef3f6);
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

.role-panel {
  display: grid;
  gap: 12px;
  padding: 16px;
}

.role-panel header,
.role-head,
.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.role-panel header strong {
  color: #111a23;
  font-size: 16px;
  font-weight: 800;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.role-grid article {
  display: grid;
  gap: 10px;
  border: 1px solid #e2e8ee;
  border-radius: 8px;
  padding: 14px;
}

.role-head strong {
  color: #111a23;
  font-size: 15px;
  font-weight: 800;
}

.role-head span {
  color: #1688b9;
  font-size: 12px;
  font-weight: 800;
}

.role-grid p {
  margin: 0;
  color: #647586;
  font-size: 13px;
}

.role-form {
  display: grid;
  gap: 16px;
}

.role-options {
  display: grid;
  gap: 10px;
}

.role-option {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 10px;
  border: 1px solid #e2e8ee;
  border-radius: 8px;
  padding: 12px;
}

.role-option span {
  display: grid;
  gap: 3px;
}

.role-option strong {
  color: #111a23;
  font-size: 14px;
  font-weight: 800;
}

.role-option em {
  color: #647586;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.form-actions {
  justify-content: flex-end;
}

:global(.security-modal) {
  max-width: 640px;
  width: calc(100vw - 32px);
  border-radius: 8px;
}

@keyframes shimmer {
  from {
    background-position: 200% 0;
  }

  to {
    background-position: -200% 0;
  }
}

@media (max-width: 860px) {
  .summary-grid,
  .toolbar,
  .role-grid {
    grid-template-columns: 1fr;
  }

  .toolbar .n-button {
    width: 100%;
  }
}
</style>
