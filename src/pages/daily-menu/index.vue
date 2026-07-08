<template>
  <view class="page">
    <view class="page-head">
      <text class="head-title">今晚点什么</text>
      <text class="head-subtitle">像在餐厅点菜一样，选好就开做</text>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          placeholder="搜索菜品"
          :value="keyword"
          @input="onSearch"
        />
        <text class="search-clear" v-if="keyword" @tap="clearSearch">✕</text>
      </view>
    </view>

    <!-- 主体：左分类 + 右网格 -->
    <view class="main-body">
      <!-- 左侧分类栏 -->
      <scroll-view class="side-nav" scroll-y>
        <view
          class="nav-item"
          :class="{ active: activeCategory === 'all' }"
          @tap="switchCat('all')"
        >
          <text class="nav-name">全部</text>
          <text class="nav-count">{{ allDishes.length }}</text>
        </view>
        <view
          class="nav-item"
          :class="{ active: activeCategory === cat.code }"
          v-for="cat in categories"
          :key="cat.id"
          @tap="switchCat(cat.code)"
        >
          <text class="nav-name">{{ cat.name }}</text>
          <text class="nav-count">{{ getCategoryCount(cat.code) }}</text>
        </view>
      </scroll-view>

      <!-- 右侧菜品网格 -->
      <scroll-view class="dish-area" scroll-y>
        <view class="dish-grid" v-if="filteredDishes.length > 0">
          <view
            class="dish-cell"
            :class="{ selected: isSelected(dish.id) }"
            v-for="dish in filteredDishes"
            :key="dish.id"
            @tap="toggleSelect(dish)"
          >
            <view class="cell-cover-wrap">
              <image class="cell-cover" :src="dish.imageUrl || defaultDishImage" mode="aspectFill" />
              <view class="cell-check" v-if="isSelected(dish.id)">
                <text>✓</text>
              </view>
            </view>
            <view class="cell-content">
              <text class="cell-name" :class="{ selected: isSelected(dish.id) }">{{ dish.name }}</text>
              <view class="cell-meta">
                <text class="cell-tag">{{ dish.categoryName }}</text>
                <text class="cell-tag diff">{{ getDifficultyName(dish.difficulty) }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="empty" v-else>
          <text class="empty-text">没有找到菜品</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部确认栏 -->
    <view class="bottom-bar" v-if="selectedIds.length > 0">
      <view class="selected-info">
        <text class="selected-count">已选 {{ selectedIds.length }} 道菜</text>
        <scroll-view class="selected-preview" scroll-x>
          <text class="selected-tag" v-for="name in selectedNames" :key="name">{{ name }}</text>
        </scroll-view>
      </view>
      <view class="confirm-btn" @tap="confirmMenu">
        <text>确认</text>
      </view>
    </view>
  </view>
</template>

<script>
import { dishApi, categoryApi, menuApi } from '../../utils/api.js'
import defaultDishImage from '../../static/default-dish.svg'

function getTodayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export default {
  data() {
    return {
      keyword: '',
      activeCategory: 'all',
      categories: [],
      allDishes: [],
      filteredDishes: [],
      selectedIds: [],
      selectedNames: [],
      defaultDishImage
    }
  },
  async onLoad() {
    try {
      const [dishList, catList, todayMenu] = await Promise.all([
        dishApi.list(),
        categoryApi.list(),
        menuApi.today()
      ])
      this.allDishes = dishList || []
      this.categories = catList || []

      // 加载今日已选
      if (todayMenu && todayMenu.dishes) {
        this.selectedIds = todayMenu.dishes.map(d => d.id)
        this.selectedNames = todayMenu.dishes.map(d => d.name)
      }

      this.filterDishes()
    } catch (e) {
      console.error('加载点菜台失败', e)
    }
  },
  methods: {
    filterDishes() {
      let dishes = this.allDishes
      if (this.activeCategory !== 'all') {
        dishes = dishes.filter(d => d.categoryCode === this.activeCategory)
      }
      if (this.keyword) {
        const kw = this.keyword.toLowerCase()
        dishes = dishes.filter(d => d.name.toLowerCase().includes(kw))
      }
      this.filteredDishes = dishes
    },

    onSearch(e) {
      this.keyword = e.detail.value
      this.filterDishes()
    },

    clearSearch() {
      this.keyword = ''
      this.filterDishes()
    },

    switchCat(cat) {
      this.activeCategory = cat
      this.filterDishes()
    },

    getCategoryCount(catCode) {
      return this.allDishes.filter(d => d.categoryCode === catCode).length
    },

    getDifficultyName(diff) {
      const map = { quick: '快手', normal: '普通', hard: '费时' }
      return map[diff] || '普通'
    },

    isSelected(id) {
      return this.selectedIds.includes(id)
    },

    toggleSelect(dish) {
      const idx = this.selectedIds.indexOf(dish.id)
      if (idx > -1) {
        this.selectedIds.splice(idx, 1)
        this.selectedNames.splice(idx, 1)
      } else {
        this.selectedIds.push(dish.id)
        this.selectedNames.push(dish.name)
      }
    },

    async confirmMenu() {
      try {
        await menuApi.save({ date: getTodayStr(), dishIds: this.selectedIds })
        uni.showToast({ title: '菜单已确认！', icon: 'success' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/index/index' })
        }, 800)
      } catch (e) {
        console.error('保存菜单失败', e)
      }
    }
  }
}
</script>

<style scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

.page-head {
  padding: 20rpx 24rpx 8rpx;
}

.head-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: var(--color-text);
}

.head-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

/* 搜索栏 */
.search-bar {
  padding: 16rpx 24rpx;
  background: var(--color-card);
  flex-shrink: 0;
}

.search-wrap {
  display: flex;
  align-items: center;
  background: #f8fafc;
  border: 2rpx solid var(--color-border);
  border-radius: 36rpx;
  padding: 14rpx 24rpx;
}

.search-icon {
  font-size: 26rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 26rpx;
}

.search-clear {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  padding: 8rpx;
}

/* 主体布局 */
.main-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧分类 */
.side-nav {
  width: 130rpx;
  height: 100%;
  background: #f8fafc;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 10rpx;
  position: relative;
  transition: all 0.2s ease;
}

.nav-item.active {
  background: var(--color-card);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 20%;
  bottom: 20%;
  width: 6rpx;
  background: var(--color-primary);
  border-radius: 0 6rpx 6rpx 0;
}


.nav-name {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-item.active .nav-name {
  color: var(--color-primary);
  font-weight: 600;
}

.nav-count {
  font-size: 20rpx;
  color: var(--color-text-muted);
  margin-left: 8rpx;
  min-width: 24rpx;
  text-align: right;
}

.nav-item.active .nav-count {
  color: var(--color-primary);
}

/* 右侧菜品区 */
.dish-area {
  flex: 1;
  height: 100%;
  background: var(--color-card);
  padding-bottom: 200rpx;
}

/* 菜品网格 */
.dish-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
  padding: 12rpx 16rpx 24rpx;
}

.dish-cell {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #ffffff;
  border-radius: 20rpx;
  border: 2rpx solid transparent;
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease, box-shadow 0.2s ease;
}

.dish-cell.selected {
  background: var(--color-primary-soft);
  border-color: var(--color-primary);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.2);
}

.dish-cell:active {
  transform: translateY(2rpx);
}

.cell-cover-wrap {
  position: relative;
  width: 100%;
  height: 180rpx;
  overflow: hidden;
}

.cell-cover {
  width: 100%;
  height: 100%;
}

.cell-check {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 38rpx;
  height: 38rpx;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cell-check text {
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
}

.cell-content {
  padding: 14rpx 14rpx 16rpx;
}

.cell-name {
  font-size: 26rpx;
  color: var(--color-text);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10rpx;
}

.cell-name.selected {
  color: var(--color-primary);
}

.cell-meta {
  display: flex;
  gap: 10rpx;
}

.cell-tag {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.cell-tag.diff {
  color: #4a90d9;
  background: #edf5ff;
}

.empty {
  text-align: center;
  padding: 80rpx;
}

.empty-text {
  font-size: 26rpx;
  color: var(--color-text-secondary);
}

/* 底部确认栏 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  background: var(--color-card);
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.selected-info {
  flex: 1;
  margin-right: 20rpx;
  overflow: hidden;
}

.selected-count {
  font-size: 26rpx;
  color: var(--color-text);
  font-weight: 500;
  display: block;
  margin-bottom: 8rpx;
}

.selected-preview {
  white-space: nowrap;
}

.selected-tag {
  display: inline-block;
  font-size: 22rpx;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  padding: 4rpx 14rpx;
  border-radius: 10rpx;
  margin-right: 10rpx;
}

.confirm-btn {
  background: var(--color-primary);
  color: #fff;
  padding: 20rpx 48rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.confirm-btn:active {
  opacity: 0.85;
}
</style>
