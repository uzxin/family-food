/**
 * HTTP 请求封装
 * - 自动携带 Authorization 头
 * - 401/403 时自动清除登录态并引导用户登录
 */

const BASE_URL = 'https://api.uzxin.asia'
const IMAGE_BASE_URL = 'https://api.uzxin.asia'

// 防止多个请求同时触发多次跳转
let _authRedirecting = false

/** 清除登录态并跳转登录页 */
function handleAuthExpired() {
  clearAuth()
  if (_authRedirecting) return
  _authRedirecting = true
  uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.navigateTo({ url: '/pages/login/index' })
    _authRedirecting = false
  }, 500)
}

// ========== Token 管理 ==========

export function getToken() {
  return uni.getStorageSync('token') || ''
}

export function setToken(token) {
  uni.setStorageSync('token', token)
}

export function getUser() {
  const raw = uni.getStorageSync('user')
  return raw ? JSON.parse(raw) : null
}

export function setUser(user) {
  uni.setStorageSync('user', JSON.stringify(user))
}

export function clearAuth() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('user')
}

export function isLoggedIn() {
  return !!getToken()
}

export function getCurrentFamilyId() {
  const id = uni.getStorageSync('currentFamilyId')
  return id || null
}

export function setCurrentFamilyId(id) {
  uni.setStorageSync('currentFamilyId', id)
}

// ========== 通用请求 ==========

function request(options) {
  const { url, method = 'GET', data, header = {} } = options
  const token = getToken()
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }
  const familyId = getCurrentFamilyId()
  if (familyId) {
    header['X-Family-Id'] = familyId
  }
  header['Content-Type'] = header['Content-Type'] || 'application/json'

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header,
      success: (res) => {
        const body = res.data
        if (res.statusCode === 401 || res.statusCode === 403) {
          handleAuthExpired()
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode >= 400) {
          const msg = body.message || body.msg || `请求失败(${res.statusCode})`
          uni.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
          return
        }
        // 后端统一返回 { code, data, message }
        if (body.code === 200 || body.code === 0) {
          resolve(body.data)
        } else {
          const msg = body.message || body.msg || '操作失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, params) => {
  let queryString = ''
  if (params) {
    const pairs = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    if (pairs.length > 0) queryString = '?' + pairs.join('&')
  }
  return request({ url: url + queryString, method: 'GET' })
}

export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url) => request({ url, method: 'DELETE' })

// ========== 文件上传 ==========

export function uploadFile(filePath) {
  const token = getToken()
  const familyId = getCurrentFamilyId()
  const header = {}
  if (token) header['Authorization'] = `Bearer ${token}`
  if (familyId) header['X-Family-Id'] = familyId

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}/api/file/upload`,
      filePath,
      name: 'file',
      header,
      success: (res) => {
        if (res.statusCode === 401 || res.statusCode === 403) {
          handleAuthExpired()
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode >= 400) {
          uni.showToast({ title: `上传失败(${res.statusCode})`, icon: 'none' })
          reject(new Error('上传失败'))
          return
        }
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200 || body.code === 0) {
            resolve(body.data)
          } else {
            const msg = body.message || body.msg || '上传失败'
            uni.showToast({ title: msg, icon: 'none' })
            reject(new Error(msg))
          }
        } catch (e) {
          uni.showToast({ title: '上传响应解析失败', icon: 'none' })
          reject(e)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

/** 拼接图片完整 URL，若已是完整 URL 则直接返回 */
export function getImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return `${IMAGE_BASE_URL}${path}`
}

// ========== 业务接口 ==========

// 认证
export const authApi = {
  register: (data) => post('/api/auth/register', data),
  login:    (data) => post('/api/auth/login', data),
  wxLogin:  (data) => post('/api/auth/wx-login', data),
}

// 用户信息
export const userApi = {
  profile:       ()       => get('/api/user/profile'),
  updateProfile: (data)   => put('/api/user/profile', data),
}

// 家庭
export const familyApi = {
  list:     ()         => get('/api/family/list'),
  current:  ()         => get('/api/family/current'),
  detail:   (id)       => get(`/api/family/${id}`),
  create:   (data)     => post('/api/family/create', data),
  join:     (data)     => post('/api/family/join', data),
  switch:   (data)     => post('/api/family/switch', data),
}

// 分类
export const categoryApi = {
  list:     ()         => get('/api/category/list'),
  add:      (data)     => post('/api/category/add', data),
}

// 文件
export const fileApi = {
  upload: (filePath) => uploadFile(filePath),
}

// 菜品
export const dishApi = {
  list:     (params)   => get('/api/dish/list', params),
  detail:   (id)       => get(`/api/dish/${id}`),
  add:      (data)     => post('/api/dish/add', data),
  update:   (id, data) => put(`/api/dish/update/${id}`, data),
  delete:   (id)       => del(`/api/dish/${id}`),
  rate:     (id, rating) => put(`/api/dish/${id}/rate?rating=${rating}`),
}

// 菜单
export const menuApi = {
  today:      ()              => get('/api/menu/today'),
  getByDate:  (date)          => get(`/api/menu/date/${date}`),
  save:       (data)          => post('/api/menu/save', data),
  removeDish: (date, dishId)  => del(`/api/menu/dish?date=${date}&dishId=${dishId}`),
}

// 历史统计
export const historyApi = {
  monthStats: (year, month)   => get(`/api/history/stats/${year}/${month}`),
}

export default {
  getToken, setToken, getUser, setUser, clearAuth, isLoggedIn,
  getCurrentFamilyId, setCurrentFamilyId,
  uploadFile, getImageUrl,
  authApi, userApi, familyApi, categoryApi, fileApi, dishApi, menuApi, historyApi
}
