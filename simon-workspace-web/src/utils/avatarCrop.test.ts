import { describe, expect, it } from 'vitest'

import { clampAvatarCrop, createCenteredAvatarCrop } from './avatarCrop'

describe('avatar crop helpers', () => {
  it('creates a centered square crop from rectangular images', () => {
    expect(createCenteredAvatarCrop(800, 600)).toEqual({
      x: 100,
      y: 0,
      size: 600,
      imageWidth: 800,
      imageHeight: 600,
    })
    expect(createCenteredAvatarCrop(480, 720)).toEqual({
      x: 0,
      y: 120,
      size: 480,
      imageWidth: 480,
      imageHeight: 720,
    })
  })

  it('clamps crop position and size inside image bounds', () => {
    expect(clampAvatarCrop({
      x: -30,
      y: 680,
      size: 900,
      imageWidth: 800,
      imageHeight: 600,
    })).toEqual({
      x: 0,
      y: 0,
      size: 600,
      imageWidth: 800,
      imageHeight: 600,
    })

    expect(clampAvatarCrop({
      x: 700,
      y: 500,
      size: 200,
      imageWidth: 800,
      imageHeight: 600,
    })).toEqual({
      x: 600,
      y: 400,
      size: 200,
      imageWidth: 800,
      imageHeight: 600,
    })
  })
})
