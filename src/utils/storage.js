/**
 * 本地存储工具模块
 * 封装菜品、每日菜单的 CRUD 操作
 */

// ========== 通用工具 ==========

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
}

function getToday() {
  const d = new Date()
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDate(dateStr) {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// ========== 家庭空间（本地模拟，后续可切服务端） ==========

function generateInviteCode() {
  return Math.random().toString(36).slice(2, 8).toUpperCase()
}

function getFamilies() {
  const families = uni.getStorageSync('families')
  if (families && families.length > 0) return families
  const defaultFamily = {
    id: `family-${generateId()}`,
    name: '我的家庭',
    inviteCode: generateInviteCode(),
    ownerName: '我',
    members: [
      { id: `member-${generateId()}`, name: '我', role: 'owner', joinedAt: Date.now() }
    ],
    createTime: Date.now()
  }
  uni.setStorageSync('families', [defaultFamily])
  uni.setStorageSync('currentFamilyId', defaultFamily.id)
  return [defaultFamily]
}

function saveFamilies(families) {
  uni.setStorageSync('families', families)
}

function getCurrentFamilyId() {
  const id = uni.getStorageSync('currentFamilyId')
  if (id) return id
  const families = getFamilies()
  const firstId = families[0].id
  uni.setStorageSync('currentFamilyId', firstId)
  return firstId
}

function getCurrentFamily() {
  const familyId = getCurrentFamilyId()
  return getFamilies().find(f => f.id === familyId) || null
}

function switchCurrentFamily(familyId) {
  const exists = getFamilies().some(f => f.id === familyId)
  if (!exists) return false
  uni.setStorageSync('currentFamilyId', familyId)
  return true
}

function createFamily(name, ownerName = '我') {
  const val = (name || '').trim()
  if (!val) return { ok: false, reason: 'empty' }
  const families = getFamilies()
  const exists = families.some(f => f.name === val)
  if (exists) return { ok: false, reason: 'duplicate' }
  const family = {
    id: `family-${generateId()}`,
    name: val,
    inviteCode: generateInviteCode(),
    ownerName,
    members: [
      { id: `member-${generateId()}`, name: ownerName, role: 'owner', joinedAt: Date.now() }
    ],
    createTime: Date.now()
  }
  const next = [family, ...families]
  saveFamilies(next)
  uni.setStorageSync('currentFamilyId', family.id)
  return { ok: true, family }
}

function joinFamilyByCode(code, memberName = '家庭成员') {
  const val = (code || '').trim().toUpperCase()
  if (!val) return { ok: false, reason: 'empty' }
  const families = getFamilies()
  const target = families.find(f => f.inviteCode === val)
  if (!target) return { ok: false, reason: 'not_found' }
  const existsMember = target.members.some(m => m.name === memberName)
  if (!existsMember) {
    target.members.push({
      id: `member-${generateId()}`,
      name: memberName,
      role: 'member',
      joinedAt: Date.now()
    })
  }
  saveFamilies(families)
  uni.setStorageSync('currentFamilyId', target.id)
  return { ok: true, family: target }
}

// ========== 菜品管理 ==========

function getDishes() {
  return uni.getStorageSync('dishes') || []
}

function getDishById(id) {
  const dishes = getDishes()
  return dishes.find(d => d.id === id) || null
}

function addDish(dish) {
  const dishes = getDishes()
  const newDish = {
    id: generateId(),
    name: dish.name,
    category: dish.category,        // meat/vegetable/soup/staple/cold
    ingredients: dish.ingredients || [],
    difficulty: dish.difficulty || 'normal', // quick/normal/hard
    favorite: dish.favorite || false,
    image: dish.image || '',
    remark: dish.remark || '',
    createTime: Date.now()
  }
  dishes.unshift(newDish)
  uni.setStorageSync('dishes', dishes)
  return newDish
}

function updateDish(id, data) {
  const dishes = getDishes()
  const index = dishes.findIndex(d => d.id === id)
  if (index === -1) return false
  dishes[index] = { ...dishes[index], ...data, updateTime: Date.now() }
  uni.setStorageSync('dishes', dishes)
  return dishes[index]
}

function deleteDish(id) {
  let dishes = getDishes()
  dishes = dishes.filter(d => d.id !== id)
  uni.setStorageSync('dishes', dishes)
  return true
}

function toggleFavorite(id) {
  const dishes = getDishes()
  const dish = dishes.find(d => d.id === id)
  if (dish) {
    dish.favorite = !dish.favorite
    uni.setStorageSync('dishes', dishes)
  }
  return dish
}

function getDishesByCategory(category) {
  const dishes = getDishes()
  if (!category || category === 'all') return dishes
  return dishes.filter(d => d.category === category)
}

function searchDishes(keyword) {
  if (!keyword) return getDishes()
  const kw = keyword.toLowerCase()
  return getDishes().filter(d =>
    d.name.toLowerCase().includes(kw) ||
    (d.ingredients && d.ingredients.some(i => i.toLowerCase().includes(kw)))
  )
}

// ========== 分类管理 ==========

function getCategories() {
  return uni.getStorageSync('categories') || [
    { id: 'cold', name: '凉菜' },
    { id: 'stir-fry', name: '小炒' },
    { id: 'braised', name: '烧菜' },
    { id: 'steamed', name: '蒸菜' },
    { id: 'soup', name: '汤/煲' },
    { id: 'staple', name: '主食' },
    { id: 'snack', name: '小吃' },
    { id: 'special', name: '特色菜' }
  ]
}

function addCategory(name) {
  const val = (name || '').trim()
  if (!val) return { ok: false, reason: 'empty' }
  const categories = getCategories()
  const existed = categories.some(c => c.name === val)
  if (existed) return { ok: false, reason: 'duplicate' }
  const id = `custom-${generateId()}`
  const next = [...categories, { id, name: val }]
  uni.setStorageSync('categories', next)
  return { ok: true, category: { id, name: val } }
}

function getCategoryName(id) {
  const cat = getCategories().find(c => c.id === id)
  return cat ? cat.name : '未分类'
}

function getCategoryIcon(id) {
  const iconMap = {
    'cold': '🥗', 'stir-fry': '🍳', 'braised': '🍖',
    'steamed': '🫕', 'soup': '🍲', 'staple': '🍚',
    'snack': '🥟', 'special': '⭐'
  }
  return iconMap[id] || '🍽️'
}

// ========== 每日菜单管理 ==========

function getDailyMenus() {
  return uni.getStorageSync('dailyMenus') || {}
}

function getTodayMenu() {
  const menus = getDailyMenus()
  return menus[getToday()] || null
}

function getMenuByDate(date) {
  const menus = getDailyMenus()
  return menus[date] || null
}

function saveDailyMenu(date, dishIds) {
  const menus = getDailyMenus()
  menus[date] = {
    date,
    dishIds,
    createTime: Date.now()
  }
  uni.setStorageSync('dailyMenus', menus)
  return menus[date]
}

function saveTodayMenu(dishIds) {
  return saveDailyMenu(getToday(), dishIds)
}

function removeDishFromMenu(date, dishId) {
  const menus = getDailyMenus()
  if (!menus[date]) return false
  menus[date].dishIds = menus[date].dishIds.filter(id => id !== dishId)
  if (menus[date].dishIds.length === 0) {
    delete menus[date]
  }
  uni.setStorageSync('dailyMenus', menus)
  return true
}

// ========== 智能推荐 ==========

function getRecentDishIds(days = 7) {
  const menus = getDailyMenus()
  const recentIds = new Set()
  const today = new Date()
  for (let i = 0; i < days; i++) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    const menu = menus[dateStr]
    if (menu && menu.dishIds) {
      menu.dishIds.forEach(id => recentIds.add(id))
    }
  }
  return recentIds
}

function getRecommendations(count = 4) {
  const dishes = getDishes()
  if (dishes.length === 0) return []

  const recentIds = getRecentDishIds(7)
  // 优先推荐最近没吃过的
  let candidates = dishes.filter(d => !recentIds.has(d.id))
  // 如果候选不够，也加入最近吃过的
  if (candidates.length < count) {
    candidates = [...candidates, ...dishes.filter(d => recentIds.has(d.id))]
  }
  // 随机打乱
  for (let i = candidates.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [candidates[i], candidates[j]] = [candidates[j], candidates[i]]
  }
  return candidates.slice(0, count)
}

function getRandomMenu(config = { 'stir-fry': 2, cold: 1, soup: 1 }) {
  const dishes = getDishes()
  const recentIds = getRecentDishIds(3)
  const result = []

  for (const [category, count] of Object.entries(config)) {
    let candidates = dishes.filter(d => d.category === category && !recentIds.has(d.id))
    if (candidates.length < count) {
      candidates = dishes.filter(d => d.category === category)
    }
    // 随机打乱
    for (let i = candidates.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [candidates[i], candidates[j]] = [candidates[j], candidates[i]]
    }
    result.push(...candidates.slice(0, count))
  }
  return result
}

// ========== 统计功能 ==========

function getDishFrequency() {
  const menus = getDailyMenus()
  const freq = {}
  for (const menu of Object.values(menus)) {
    if (menu.dishIds) {
      menu.dishIds.forEach(id => {
        freq[id] = (freq[id] || 0) + 1
      })
    }
  }
  return freq
}

function getMenuDates() {
  const menus = getDailyMenus()
  return Object.keys(menus).sort().reverse()
}

export default {
  generateId,
  getToday,
  formatDate,
  // 家庭空间
  getFamilies,
  getCurrentFamilyId,
  getCurrentFamily,
  switchCurrentFamily,
  createFamily,
  joinFamilyByCode,
  // 菜品
  getDishes,
  getDishById,
  addDish,
  updateDish,
  deleteDish,
  toggleFavorite,
  getDishesByCategory,
  searchDishes,
  // 分类
  getCategories,
  addCategory,
  getCategoryName,
  getCategoryIcon,
  // 每日菜单
  getDailyMenus,
  getTodayMenu,
  getMenuByDate,
  saveDailyMenu,
  saveTodayMenu,
  removeDishFromMenu,
  // 推荐
  getRecentDishIds,
  getRecommendations,
  getRandomMenu,
  // 统计
  getDishFrequency,
  getMenuDates
}
