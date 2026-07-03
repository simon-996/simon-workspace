import { describe, expect, it } from 'vitest'

import { buildHomeScrollStyle, getHomeScrollProgress } from './homeScroll'

describe('home scroll animation helpers', () => {
  it('clamps scroll progress between 0 and 1', () => {
    expect(getHomeScrollProgress(-20, 400)).toBe(0)
    expect(getHomeScrollProgress(200, 400)).toBe(0.5)
    expect(getHomeScrollProgress(900, 400)).toBe(1)
  })

  it('returns CSS variables consumed by the homepage animation', () => {
    expect(buildHomeScrollStyle(0.5)).toEqual({
      '--home-progress': '0.500',
      '--intro-scale': '1.060',
      '--terminal-offset': '120.0px',
      '--terminal-opacity': '0.400',
    })
  })
})
