export type HomeScrollStyle = Record<
  '--home-progress' | '--intro-scale' | '--terminal-offset' | '--terminal-opacity',
  string
>

export function getHomeScrollProgress(scrollY: number, distance: number) {
  if (distance <= 0) {
    return 1
  }

  return Math.min(1, Math.max(0, scrollY / distance))
}

export function buildHomeScrollStyle(progress: number): HomeScrollStyle {
  const clamped = Math.min(1, Math.max(0, progress))
  const introScale = 1 + clamped * 0.12
  const terminalOffset = clamped * 240
  const terminalOpacity = 1 - clamped * 1.2

  return {
    '--home-progress': clamped.toFixed(3),
    '--intro-scale': introScale.toFixed(3),
    '--terminal-offset': `${terminalOffset.toFixed(1)}px`,
    '--terminal-opacity': Math.max(0, terminalOpacity).toFixed(3),
  }
}
