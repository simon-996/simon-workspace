// @vitest-environment jsdom

import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { defineComponent, h, type DefineComponent } from 'vue'
import { createI18n } from 'vue-i18n'
import type { LocationQuery } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  fetchFiles,
  type FileResource,
} from '../../api/workspace'
import FileUploadDialog from '../../components/FileUploadDialog.vue'
import { messages } from '../../i18n/messages'
import FileCenterView from './FileCenterView.vue'
import source from './FileCenterView.vue?raw'

const routerMocks = vi.hoisted(() => ({
  query: {} as LocationQuery,
  replace: vi.fn(),
}))

const messageMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: routerMocks.query }),
  useRouter: () => ({ replace: routerMocks.replace }),
}))

vi.mock('../../api/workspace', () => ({
  deleteFileResource: vi.fn(),
  downloadFileResource: vi.fn(),
  fetchFiles: vi.fn(),
  uploadFileResource: vi.fn(),
}))

vi.mock('naive-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('naive-ui')>()

  return {
    ...actual,
    useMessage: () => messageMocks,
  }
})

const ButtonStub = defineComponent({
  name: 'NButton',
  inheritAttrs: false,
  props: {
    attrType: { type: String, default: 'button' },
    disabled: { type: Boolean, default: false },
    loading: { type: Boolean, default: false },
  },
  setup(props, { attrs, slots }) {
    return () => h(
      'button',
      {
        ...attrs,
        type: props.attrType,
        disabled: props.disabled,
        'data-loading': String(props.loading),
      },
      [slots.icon?.(), slots.default?.()],
    )
  },
})

const ModalStub = defineComponent({
  name: 'NModal',
  inheritAttrs: false,
  props: {
    show: { type: Boolean, default: false },
  },
  emits: {
    'update:show': (value: boolean) => typeof value === 'boolean',
  },
  setup(props, { attrs, slots }) {
    return () => props.show
      ? h('section', { ...attrs, 'data-testid': 'upload-modal', role: 'dialog' }, slots.default?.())
      : null
  },
})

const SlotStub = defineComponent({
  inheritAttrs: false,
  setup(_props, { attrs, slots }) {
    return () => h('div', attrs, [slots.trigger?.(), slots.default?.()])
  },
})

const FileCenterUnderTest = FileCenterView as DefineComponent
const TestableFileUploadDialog = FileUploadDialog as DefineComponent<{ show: boolean }>

function createResource(filename = 'lesson-plan.pdf'): FileResource {
  return {
    id: 'file-1',
    ownerUserId: 'user-1',
    sourceType: 'UPLOAD',
    originalFilename: filename,
    storageProvider: 'LOCAL',
    visibility: 'PRIVATE',
    fileSize: 12,
    status: 'ACTIVE',
  }
}

function mountFileCenter(): VueWrapper {
  const i18n = createI18n({
    legacy: false,
    locale: 'en',
    messages,
  })

  return mount(FileCenterUnderTest, {
    global: {
      plugins: [i18n],
      stubs: {
        Button: ButtonStub,
        Icon: SlotStub,
        Input: SlotStub,
        Modal: ModalStub,
        Popconfirm: SlotStub,
        Progress: SlotStub,
        Radio: SlotStub,
        RadioGroup: SlotStub,
        Spin: SlotStub,
      },
    },
  })
}

describe('FileCenterView upload entry', () => {
  beforeEach(() => {
    routerMocks.query = {}
    routerMocks.replace.mockReset()
    messageMocks.success.mockReset()
    messageMocks.error.mockReset()
    vi.mocked(fetchFiles).mockReset()
    vi.mocked(fetchFiles).mockResolvedValue([])
  })

  it('opens from a deep link and consumes only the upload action', async () => {
    routerMocks.query = {
      action: 'upload',
      keyword: 'lesson',
      tag: ['pdf', 'public'],
    }
    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(true)
    expect(wrapper.find('[data-testid="upload-modal"]').exists()).toBe(true)
    expect(routerMocks.replace).toHaveBeenCalledOnce()
    expect(routerMocks.replace).toHaveBeenCalledWith({
      query: {
        keyword: 'lesson',
        tag: ['pdf', 'public'],
      },
    })
  })

  it('does not open or rewrite the route for a non-upload action', async () => {
    routerMocks.query = { action: 'download', keyword: 'lesson' }
    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(false)
    expect(wrapper.find('[data-testid="upload-modal"]').exists()).toBe(false)
    expect(routerMocks.replace).not.toHaveBeenCalled()
  })

  it('opens from both the toolbar and the empty state', async () => {
    const wrapper = mountFileCenter()
    await flushPromises()

    await wrapper.get('[data-testid="toolbar-upload"]').trigger('click')
    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(true)

    wrapper.getComponent(TestableFileUploadDialog).vm.$emit('update:show', false)
    await flushPromises()
    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(false)

    await wrapper.get('[data-testid="empty-upload"]').trigger('click')
    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(true)
  })

  it('shows the uploaded filename and refreshes the list after upload', async () => {
    const resource = createResource()
    vi.mocked(fetchFiles)
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([resource])
    const wrapper = mountFileCenter()
    await flushPromises()

    wrapper.getComponent(TestableFileUploadDialog).vm.$emit('uploaded', resource)
    await flushPromises()

    expect(messageMocks.success).toHaveBeenCalledWith('Uploaded lesson-plan.pdf')
    expect(fetchFiles).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('lesson-plan.pdf')
  })

  it('stacks constrained toolbar actions at 390px without horizontal overflow', () => {
    expect(source).toContain('@media (max-width: 390px)')
    expect(source).toContain('flex-basis: 100%')
    expect(source).toContain('max-width: 100%')
  })
})
