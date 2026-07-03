import { describe, expect, it } from 'vitest'

import { buildHomeScrollStyle, getHomeScrollProgress } from './homeScroll'

describe('home scroll animation helpers', () => {
  it('clamps scroll progress between 0 and 1', () => {
    expect(getHomeScrollProgress(-20, 400)).toBe(0)
    expect(getHomeScrollProgress(200, 400)).toBe(0.5)
    expect(getHomeScrollProgress(900, 400)).toBe(1)
  })

  it('returns CSS variables consumed by the homepage animation', () => {
    expect(buildHomeScrollStyle(0.5, { width: 1200, height: 800 })).toEqual({
      '--home-progress': '0.500',
      '--terminal-progress': '0.844',
      '--intro-focus': '0.628',
      '--intro-scale': '1.138',
      '--intro-x': '135.7px',
      '--intro-y': '-40.2px',
      '--terminal-x': '25.3px',
      '--terminal-y': '256.5px',
      '--terminal-scale': '0.949',
      '--terminal-opacity': '0.114',
      '--details-opacity': '0.000',
      '--details-y': '20.0px',
      '--details-scale': '0.879',
      '--brief-opacity': '1.000',
    })
  })

  it('reveals expanded profile details near the end of the scroll scene', () => {
    expect(buildHomeScrollStyle(0.9, { width: 1200, height: 800 })).toMatchObject({
      '--intro-focus': '1.000',
      '--intro-scale': '1.220',
      '--terminal-opacity': '0.000',
      '--details-opacity': '0.857',
      '--details-y': '2.9px',
      '--details-scale': '0.820',
      '--brief-opacity': '0.040',
    })
  })

  it('keeps the compact scene below the mobile navigation while focused', () => {
    expect(buildHomeScrollStyle(1, { width: 390, height: 844 })).toMatchObject({
      '--intro-focus': '1.000',
      '--intro-y': '33.8px',
      '--terminal-opacity': '0.000',
      '--details-opacity': '1.000',
    })
  })
})
