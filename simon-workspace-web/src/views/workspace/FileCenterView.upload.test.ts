// @vitest-environment jsdom

import { enableAutoUnmount, flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { defineComponent, h, nextTick, type DefineComponent } from 'vue'
import { createI18n } from 'vue-i18n'
import type { LocationQuery } from 'vue-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import {
  fetchFiles,
  type FileResource,
} from '../../api/workspace'
import FileUploadDialog from '../../components/FileUploadDialog.vue'
import { messages } from '../../i18n/messages'
import FileCenterView from './FileCenterView.vue'
import source from './FileCenterView.vue?raw'

const routerMocks = vi.hoisted(() => ({
  route: {
    query: {} as LocationQuery,
    hash: '',
  },
  replace: vi.fn(),
}))

const messageMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
}))

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive(routerMocks.route)
  routerMocks.route = route

  return {
    useRoute: () => route,
    useRouter: () => ({ replace: routerMocks.replace }),
  }
})

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

enableAutoUnmount(afterEach)

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

function createDeferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })

  return { promise, reject, resolve }
}

function findButton(wrapper: VueWrapper, label: string) {
  const button = wrapper.findAll('button').find((candidate) => candidate.text().includes(label))
  if (!button) throw new Error(`Button not found: ${label}`)
  return button
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
    routerMocks.route.query = {}
    routerMocks.route.hash = ''
    routerMocks.replace.mockReset()
    routerMocks.replace.mockResolvedValue(undefined)
    messageMocks.success.mockReset()
    messageMocks.error.mockReset()
    vi.mocked(fetchFiles).mockReset()
    vi.mocked(fetchFiles).mockResolvedValue([])
  })

  it('opens from a deep link and consumes only the upload action', async () => {
    routerMocks.route.query = {
      action: 'upload',
      keyword: 'lesson',
      tag: ['pdf', 'public'],
    }
    routerMocks.route.hash = '#files'
    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(true)
    expect(wrapper.find('[data-testid="upload-modal"]').exists()).toBe(true)
    expect(routerMocks.replace).toHaveBeenCalledOnce()
    expect(routerMocks.replace).toHaveBeenCalledWith({
      hash: '#files',
      query: {
        keyword: 'lesson',
        tag: ['pdf', 'public'],
      },
    })
  })

  it('does not open or rewrite the route for a non-upload action', async () => {
    routerMocks.route.query = { action: 'download', keyword: 'lesson' }
    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(false)
    expect(wrapper.find('[data-testid="upload-modal"]').exists()).toBe(false)
    expect(routerMocks.replace).not.toHaveBeenCalled()
  })

  it('opens when a reused view later receives the upload action', async () => {
    const wrapper = mountFileCenter()
    await flushPromises()

    routerMocks.route.query = { action: 'upload', keyword: 'lesson' }
    await nextTick()
    await flushPromises()

    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(true)
    expect(routerMocks.replace).toHaveBeenCalledOnce()
    expect(routerMocks.replace).toHaveBeenCalledWith({
      hash: '',
      query: { keyword: 'lesson' },
    })
  })

  it('keeps upload usable when consuming the route action fails', async () => {
    routerMocks.route.query = { action: 'upload' }
    const rejectedReplace = Promise.reject(new Error('navigation cancelled'))
    await rejectedReplace.catch(() => undefined)
    const catchSpy = vi.spyOn(rejectedReplace, 'catch')
    routerMocks.replace.mockReturnValueOnce(rejectedReplace)

    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.getComponent(TestableFileUploadDialog).props('show')).toBe(true)
    expect(wrapper.find('[data-testid="upload-modal"]').exists()).toBe(true)
    expect(routerMocks.replace).toHaveBeenCalledOnce()
    expect(catchSpy).toHaveBeenCalledOnce()
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

  it('keeps the latest list when overlapping refreshes resolve out of order', async () => {
    const firstRequest = createDeferred<FileResource[]>()
    const secondRequest = createDeferred<FileResource[]>()
    vi.mocked(fetchFiles)
      .mockReturnValueOnce(firstRequest.promise)
      .mockReturnValueOnce(secondRequest.promise)
    const wrapper = mountFileCenter()
    await nextTick()

    await findButton(wrapper, 'Refresh').trigger('click')
    secondRequest.resolve([createResource('latest.pdf')])
    await flushPromises()
    expect(wrapper.text()).toContain('latest.pdf')

    firstRequest.resolve([createResource('stale.pdf')])
    await flushPromises()
    expect(wrapper.text()).toContain('latest.pdf')
    expect(wrapper.text()).not.toContain('stale.pdf')
  })

  it('keeps loading visible when an older refresh finishes first', async () => {
    const firstRequest = createDeferred<FileResource[]>()
    const secondRequest = createDeferred<FileResource[]>()
    vi.mocked(fetchFiles)
      .mockReturnValueOnce(firstRequest.promise)
      .mockReturnValueOnce(secondRequest.promise)
    const wrapper = mountFileCenter()
    await nextTick()

    await findButton(wrapper, 'Refresh').trigger('click')
    firstRequest.resolve([])
    await flushPromises()
    expect(wrapper.find('.skeleton-table').exists()).toBe(true)

    secondRequest.resolve([createResource('latest.pdf')])
    await flushPromises()
    expect(wrapper.find('.skeleton-table').exists()).toBe(false)
    expect(wrapper.text()).toContain('latest.pdf')
  })

  it('uses the localized fallback when a request error message is blank', async () => {
    vi.mocked(fetchFiles).mockRejectedValueOnce(new Error('   '))

    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load files')
    expect(messageMocks.error).toHaveBeenCalledWith('Failed to load files')
  })

  it('labels icon-only file actions for assistive technology', async () => {
    vi.mocked(fetchFiles).mockResolvedValueOnce([createResource()])

    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.find('button[aria-label="Download"]').exists()).toBe(true)
    expect(wrapper.find('button[aria-label="Delete"]').exists()).toBe(true)
  })

  it('uses the shared binary file-size format', async () => {
    vi.mocked(fetchFiles).mockResolvedValueOnce([
      { ...createResource(), fileSize: 1536 },
    ])

    const wrapper = mountFileCenter()
    await flushPromises()

    expect(wrapper.text()).toContain('1.5 KiB')
  })

  it('stacks constrained toolbar actions at 390px without horizontal overflow', () => {
    expect(source).toContain('@media (max-width: 390px)')
    expect(source).toContain('flex-basis: 100%')
    expect(source).toContain('max-width: 100%')
  })
})
