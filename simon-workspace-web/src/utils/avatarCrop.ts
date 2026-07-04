export interface AvatarCropState {
  x: number
  y: number
  size: number
  imageWidth: number
  imageHeight: number
}

export function createCenteredAvatarCrop(imageWidth: number, imageHeight: number): AvatarCropState {
  const width = Math.max(1, Math.round(imageWidth))
  const height = Math.max(1, Math.round(imageHeight))
  const size = Math.min(width, height)

  return {
    x: Math.round((width - size) / 2),
    y: Math.round((height - size) / 2),
    size,
    imageWidth: width,
    imageHeight: height,
  }
}

export function clampAvatarCrop(crop: AvatarCropState): AvatarCropState {
  const imageWidth = Math.max(1, Math.round(crop.imageWidth))
  const imageHeight = Math.max(1, Math.round(crop.imageHeight))
  const size = clamp(Math.round(crop.size), 1, Math.min(imageWidth, imageHeight))

  return {
    x: clamp(Math.round(crop.x), 0, imageWidth - size),
    y: clamp(Math.round(crop.y), 0, imageHeight - size),
    size,
    imageWidth,
    imageHeight,
  }
}

export async function cropImageFileToAvatar(
  file: File,
  crop: AvatarCropState,
  outputSize = 512,
): Promise<File> {
  const image = await loadImage(file)
  const safeCrop = clampAvatarCrop(crop)
  const canvas = document.createElement('canvas')
  canvas.width = outputSize
  canvas.height = outputSize

  const context = canvas.getContext('2d')
  if (!context) {
    throw new Error('Canvas is not available')
  }

  context.drawImage(
    image,
    safeCrop.x,
    safeCrop.y,
    safeCrop.size,
    safeCrop.size,
    0,
    0,
    outputSize,
    outputSize,
  )

  const blob = await new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((value) => {
      if (value) {
        resolve(value)
      } else {
        reject(new Error('Avatar crop failed'))
      }
    }, 'image/webp', 0.9)
  })

  return new File([blob], 'avatar.webp', { type: 'image/webp' })
}

function loadImage(file: File) {
  return new Promise<HTMLImageElement>((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const image = new Image()
    image.onload = () => {
      URL.revokeObjectURL(url)
      resolve(image)
    }
    image.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('Avatar image could not be loaded'))
    }
    image.src = url
  })
}

function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value))
}
