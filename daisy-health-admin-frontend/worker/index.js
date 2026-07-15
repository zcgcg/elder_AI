function json(message, status) {
  return new Response(JSON.stringify({ code: status, message }), {
    status,
    headers: { 'content-type': 'application/json; charset=utf-8' }
  })
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url)

    if (url.pathname.startsWith('/api/') || url.pathname.startsWith('/uploads/')) {
      return json('在线数据服务尚未配置，请联系站点管理员。', 503)
    }

    const response = await env.ASSETS.fetch(request)
    if (response.status !== 404 || request.method !== 'GET') return response

    const acceptsHtml = request.headers.get('accept')?.includes('text/html')
    if (!acceptsHtml) return response

    return env.ASSETS.fetch(new Request(new URL('/index.html', request.url), request))
  }
}
