import { describe, expect, it } from 'vitest'

import siteApiSource from '../api/site.ts?raw'
import enSource from '../i18n/locales/en.ts?raw'
import homeSource from './HomeView.vue?raw'
import siteConfigSource from './workspace/SiteConfigView.vue?raw'

describe('editable site profile', () => {
  it('exposes profile bio and structured technology stack through the site API contract', () => {
    expect(siteApiSource).toContain('profileBio?: string | null')
    expect(siteApiSource).toContain('techStack: SiteTechStackItem[]')
    expect(siteApiSource).toContain('export interface SiteTechStackItem')
  })

  it('renders public homepage profile content from backend site config', () => {
    expect(homeSource).toContain('site.value?.profileBio')
    expect(homeSource).toContain('site.value?.techStack?.length')
    expect(homeSource).toContain('v-for="item in techStackItems"')
    expect(homeSource).toContain('class="profile-bio"')
  })

  it('lets workspace users edit profile bio and technology stack items', () => {
    expect(siteConfigSource).toContain('v-model:value="form.profileBio"')
    expect(siteConfigSource).toContain('addTechStackItem')
    expect(siteConfigSource).toContain('removeTechStackItem')
    expect(siteConfigSource).toContain('v-for="(item, index) in form.techStack"')
  })

  it('adds English i18n keys for the new profile fields', () => {
    expect(enSource).toContain('profileBio')
    expect(enSource).toContain('techStack')
    expect(enSource).toContain('techLabel')
    expect(enSource).toContain('techValue')
  })
})
