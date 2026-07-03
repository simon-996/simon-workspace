export type HomeScrollStyle = Record<
  | '--home-progress'
  | '--terminal-progress'
  | '--intro-focus'
  | '--intro-scale'
  | '--intro-x'
  | '--intro-y'
  | '--terminal-x'
  | '--terminal-y'
  | '--terminal-scale'
  | '--terminal-opacity'
  | '--details-opacity'
  | '--details-y'
  | '--details-scale'
  | '--brief-opacity',
  string
>

export interface HomeScrollViewport {
  width: number
  height: number
}

export function getHomeScrollProgress(scrollY: number, distance: number) {
  if (distance <= 0) {
    return 1
  }

  return Math.min(1, Math.max(0, scrollY / distance))
}

export function buildHomeScrollStyle(
  progress: number,
  viewport: HomeScrollViewport = { width: 1200, height: 800 },
): HomeScrollStyle {
  const clamped = Math.min(1, Math.max(0, progress))
  const isCompact = viewport.width < 900
  const terminalProgress = smoothStep(segment(clamped, 0.05, isCompact ? 0.56 : 0.65))
  const introFocus = smoothStep(segment(clamped, 0.16, isCompact ? 0.7 : 0.74))
  const detailsProgress = smoothStep(segment(clamped, 0.58, 1))
  const introShiftX = isCompact ? 0 : Math.min(260, Math.max(80, viewport.width * 0.18)) * introFocus
  const introShiftY = viewport.height * (isCompact ? 0.04 : -0.08) * introFocus
  const terminalShiftX = (isCompact ? 0 : viewport.width * 0.025) * terminalProgress
  const terminalShiftY = viewport.height * (isCompact ? 0.32 : 0.38) * terminalProgress
  const introScale = 1 + introFocus * 0.22
  const terminalScale = 1 - terminalProgress * (isCompact ? 0.08 : 0.06)
  const terminalOpacity = Math.max(0, 1 - terminalProgress * 1.05)
  const briefOpacity = Math.max(0, 1 - detailsProgress * 1.12)
  const detailsY = (1 - detailsProgress) * 20
  const detailsScale = 1 / introScale

  return {
    '--home-progress': clamped.toFixed(3),
    '--terminal-progress': terminalProgress.toFixed(3),
    '--intro-focus': introFocus.toFixed(3),
    '--intro-scale': introScale.toFixed(3),
    '--intro-x': `${introShiftX.toFixed(1)}px`,
    '--intro-y': `${introShiftY.toFixed(1)}px`,
    '--terminal-x': `${terminalShiftX.toFixed(1)}px`,
    '--terminal-y': `${terminalShiftY.toFixed(1)}px`,
    '--terminal-scale': terminalScale.toFixed(3),
    '--terminal-opacity': terminalOpacity.toFixed(3),
    '--details-opacity': detailsProgress.toFixed(3),
    '--details-y': `${detailsY.toFixed(1)}px`,
    '--details-scale': detailsScale.toFixed(3),
    '--brief-opacity': briefOpacity.toFixed(3),
  }
}

function segment(progress: number, start: number, end: number) {
  return Math.min(1, Math.max(0, (progress - start) / (end - start)))
}

function smoothStep(progress: number) {
  return progress * progress * (3 - 2 * progress)
}
