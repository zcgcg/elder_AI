export function portalRouteDecision(roleType, path) {
  if (roleType === 'elderly') {
    return path === '/portal/user' || path.startsWith('/portal/user/') ? true : '/portal/user'
  }
  if (roleType === 'service') {
    return path === '/portal/service' ? true : '/portal/service'
  }
  if (path.startsWith('/portal/')) return '/dashboard'
  return null
}
