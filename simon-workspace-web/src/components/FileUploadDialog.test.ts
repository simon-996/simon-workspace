// @vitest-environment jsdom

import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { defineComponent, h, type DefineComponent } from 'vue'
import { createI18n } from 'vue-i18n'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { uploadFileResource, type FileResource } from '../api/workspace'
import { messages } from '../i18n/messages'
import FileUploadDialog from './FileUploadDialog.vue'
import source from './FileUploadDialog.vue?raw'

type FileUploadDialogTestProps = {
  show: boolean
  onUploaded?: (resource: FileResource) => void
  'onUpdate:show'?: (value: boolean) => void
}

const TestableFileUploadDialog = FileUploadDialog as DefineComponent<FileUploadDialogTestProps>

vi.mock('../api/workspace', () => ({
  uploadFileResource: vi.fn(),
}))

const ModalStub = defineComponent({
  name: 'NModal',
  inheritAttrs: false,
  props: {
    show: { type: Boolean, default: false },
    title: { type: String, default: '' },
    maskClosable: { type: Boolean, default: true },
    closeOnEsc: { type: Boolean, default: true },
    closable: { type: Boolean, default: true },
    closeFocusable: { type: Boolean, default: true },
  },
  emits: {
    'update:show': (value: boolean) => typeof value === 'boolean',
  },
  setup(props, { attrs, emit, slots }) {
    return () => props.show
      ? h('section', { ...attrs, 'data-testid': 'modal', role: 'dialog' }, [
          h('h2', props.title),
          h(
            'button',
            {
              type: 'button',
              'data-testid': 'modal-close',
              disabled: !props.closeFocusable,
              onClick: () => emit('update:show', false),
            },
            'Close',
          ),
          slots.default?.(),
        ])
      : null
  },
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
      slots.default?.(),
    )
  },
})

const RadioGroupStub = defineComponent({
  name: 'NRadioGroup',
  props: {
    value: { type: String, default: 'PRIVATE' },
    disabled: { type: Boolean, default: false },
  },
  emits: {
    'update:value': (value: string) => value === 'PRIVATE' || value === 'PUBLIC',
  },
  setup(_props, { slots }) {
    return () => h('div', { 'data-testid': 'radio-group' }, slots.default?.())
  },
})

const RadioStub = defineComponent({
  name: 'NRadio',
  props: {
    value: { type: String, required: true },
    disabled: { type: Boolean, default: false },
  },
  setup(props, { slots }) {
    return () => h('label', [
      h('input', { type: 'radio', value: props.value, disabled: props.disabled }),
      slots.default?.(),
    ])
  },
})

const ProgressStub = defineComponent({
  name: 'NProgress',
  props: {
    percentage: { type: Number, default: 0 },
  },
  setup(props) {
    return () => h('div', {
      'data-testid': 'progress',
      'data-percentage': String(props.percentage),
    })
  },
})

function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })

  return { promise, resolve, reject }
}

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

function mountDialog(
  eventOrder: string[] = [],
) {
  const i18n = createI18n({
    legacy: false,
    locale: 'en',
    messages,
  })

  return mount(TestableFileUploadDialog, {
    props: {
      show: true,
      onUploaded: () => eventOrder.push('uploaded'),
      'onUpdate:show': (value: boolean) => eventOrder.push(`show:${value}`),
    },
    global: {
      plugins: [i18n],
      stubs: {
        Modal: ModalStub,
        Button: ButtonStub,
        Progress: ProgressStub,
        Radio: RadioStub,
        RadioGroup: RadioGroupStub,
      },
    },
  })
}

async function selectFile(wrapper: VueWrapper, file: File) {
  const input = wrapper.get<HTMLInputElement>('input[type="file"]')
  Object.defineProperty(input.element, 'files', {
    configurable: true,
    value: [file],
  })
  await input.trigger('change')
}

describe('FileUploadDialog', () => {
  beforeEach(() => {
    vi.mocked(uploadFileResource).mockReset()
  })

  it('shows accessible validation without calling the upload API when no file is selected', async () => {
    const wrapper = mountDialog()
    const input = wrapper.get('input[type="file"]')

    expect(wrapper.get('[data-testid="modal"]').attributes('aria-label')).toBe('Upload material')
    expect(wrapper.get('button[type="submit"]').attributes('disabled')).toBeUndefined()

    await wrapper.get('form').trigger('submit')

    expect(uploadFileResource).not.toHaveBeenCalled()
    expect(input.attributes('aria-invalid')).toBe('true')
    expect(input.attributes('aria-describedby')).toBe('file-upload-error')
    expect(wrapper.get('#file-upload-error').text()).toBe('Choose a file to upload')
  })

  it('refuses parent closure while pending, preserves a failure, then retries and closes after success', async () => {
    const firstRequest = deferred<FileResource>()
    let reportProgress: ((progress: number) => void) | undefined
    vi.mocked(uploadFileResource).mockImplementationOnce((_file, _visibility, onProgress) => {
      reportProgress = onProgress
      return firstRequest.promise
    })
    const eventOrder: string[] = []
    const wrapper = mountDialog(eventOrder)
    const file = new File(['lesson'], 'lesson-plan.pdf')
    await selectFile(wrapper, file)

    await wrapper.get('form').trigger('submit')
    reportProgress?.(37)
    await flushPromises()

    expect(uploadFileResource).toHaveBeenCalledWith(file, 'PRIVATE', expect.any(Function))
    expect(wrapper.get('form').attributes('aria-busy')).toBe('true')
    expect(wrapper.get('input[type="file"]').attributes('disabled')).toBeDefined()
    const cancelButton = wrapper.findAll('button').find((button) => button.text() === 'Cancel')
    expect(cancelButton).toBeDefined()
    expect(cancelButton?.attributes('disabled')).toBeDefined()
    const modal = wrapper.getComponent(ModalStub)
    expect(modal.props('maskClosable')).toBe(false)
    expect(modal.props('closeOnEsc')).toBe(false)
    expect(modal.props('closable')).toBe(false)
    expect(modal.props('closeFocusable')).toBe(false)

    await wrapper.setProps({ show: false })

    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(true)
    expect(wrapper.emitted('update:show')).toContainEqual([true])

    firstRequest.reject(new Error('Storage unavailable'))
    await flushPromises()

    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Storage unavailable')
    expect(wrapper.text()).toContain('lesson-plan.pdf')
    expect(wrapper.get('input[type="file"]').attributes('disabled')).toBeUndefined()

    const secondRequest = deferred<FileResource>()
    vi.mocked(uploadFileResource).mockImplementationOnce(() => secondRequest.promise)
    await wrapper.get('form').trigger('submit')
    const resource = createResource()
    secondRequest.resolve(resource)
    await flushPromises()

    expect(wrapper.emitted('uploaded')).toEqual([[resource]])
    expect(wrapper.emitted('update:show')?.at(-1)).toEqual([false])
    expect(eventOrder.slice(-2)).toEqual(['uploaded', 'show:false'])
    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(false)

    await wrapper.setProps({ show: true })
    expect(wrapper.text()).not.toContain('lesson-plan.pdf')
    expect(wrapper.find('.upload-progress').exists()).toBe(false)
  })

  it('removes stale failed progress and error when a replacement file is selected', async () => {
    vi.mocked(uploadFileResource).mockImplementationOnce((_file, _visibility, onProgress) => {
      onProgress?.(37)
      return Promise.reject(new Error('Storage unavailable'))
    })
    const wrapper = mountDialog()
    await selectFile(wrapper, new File(['old'], 'old.pdf'))

    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(wrapper.get('[data-testid="progress"]').attributes('data-percentage')).toBe('37')
    expect(wrapper.text()).toContain('Storage unavailable')

    await selectFile(wrapper, new File(['new'], 'new.pdf'))

    expect(wrapper.find('.upload-progress').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Storage unavailable')
    expect(wrapper.text()).toContain('new.pdf')
  })

  it('uses responsive token-based styling without gradients', () => {
    expect(source).toContain("width: 'min(520px, calc(100vw - 32px))'")
    expect(source).toContain("borderRadius: '8px'")
    expect(source).toContain('min-height: 44px')
    expect(source).toContain('@media (prefers-reduced-motion: reduce)')
    expect(source).toContain('var(--sw-')
    expect(source).not.toMatch(/gradient/i)
  })
})
