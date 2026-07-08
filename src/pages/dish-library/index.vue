<template>
  <view class="page">
    <view class="kitchen-head" @tap="goFamilyPage">
      <view class="kitchen-avatar">
        <text>🍲</text>
      </view>
      <text class="kitchen-title">{{ currentFamilyName }}的小厨房</text>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          placeholder="搜索菜品或食材"
          :value="keyword"
          @input="onSearch"
          confirm-type="search"
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
          @tap="switchCategory('all')"
        >
          <text class="nav-name">全部</text>
          <text class="nav-count">{{ dishCount }}</text>
        </view>
        <view
          class="nav-item"
          :class="{ active: activeCategory === 'favorite' }"
          @tap="switchCategory('favorite')"
        >
          <text class="nav-name">最爱</text>
          <text class="nav-count">{{ favoriteCount }}</text>
        </view>
        <view
          class="nav-item"
          :class="{ active: activeCategory === cat.code }"
          v-for="cat in categories"
          :key="cat.id"
          @tap="switchCategory(cat.code)"
        >
          <text class="nav-name">{{ cat.name }}</text>
          <text class="nav-count">{{ getCategoryCount(cat.code) }}</text>
        </view>
        <view class="nav-add" @tap="addCategory">
          <text class="nav-add-text">+ 添加分类</text>
        </view>
      </scroll-view>

      <!-- 右侧菜品网格 -->
      <scroll-view class="dish-area" scroll-y>
        <!-- 分类标题 -->
        <view class="area-header">
          <text class="area-title">{{ currentCategoryName }}</text>
          <text class="area-count">{{ filteredDishes.length }}道菜</text>
        </view>

        <view class="dish-grid" v-if="filteredDishes.length > 0">
          <view
            class="dish-cell"
            v-for="dish in filteredDishes"
            :key="dish.id"
            @tap="goDishDetail(dish.id)"
          >
            <view class="cell-cover-wrap">
              <image class="cell-cover" :src="dish.imageUrl || defaultDishImage" mode="aspectFill" />
              <view class="cell-fav" v-if="dish.favorite">
                <text>❤️</text>
              </view>
            </view>
            <view class="cell-content">
              <text class="cell-name">{{ dish.name }}</text>
              <view class="cell-meta">
                <text class="cell-tag">{{ dish.categoryName || getCategoryName(dish.categoryCode) }}</text>
                <text class="cell-tag diff">{{ getDifficultyName(dish.difficulty) }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty" v-else>
          <text class="empty-icon">📭</text>
          <text class="empty-text" v-if="keyword">没有找到「{{ keyword }}」相关菜品</text>
          <text class="empty-text" v-else>还没有菜品，点右下角添加吧</text>
        </view>
      </scroll-view>
    </view>

    <!-- 添加按钮 -->
    <view class="fab" @tap="goAddDish">
      <text class="fab-icon">+</text>
    </view>

    <tab-bar-view :current="1" />
  </view>
</template>

<script>
import { dishApi, categoryApi, familyApi } from '../../utils/api.js'
import tabBarView from '../../components/tab-bar-view/tab-bar-view.vue'
import defaultDishImage from '../../static/default-dish.svg'

export default {
  components: { tabBarView },
  data() {
    return {
      keyword: '',
      activeCategory: 'all',
      categories: [],
      allDishes: [],
      filteredDishes: [],
      dishCount: 0,
      favoriteCount: 0,
      defaultDishImage,
      currentFamilyName: '我的家庭',
      loading: false
    }
  },
  computed: {
    currentCategoryName() {
      if (this.keyword) return '搜索结果'
      if (this.activeCategory === 'all') return '全部菜品'
      if (this.activeCategory === 'favorite') return '我的最爱'
      const cat = this.categories.find(c => c.id === this.activeCategory || c.code === this.activeCategory)
      return cat ? cat.name : '全部菜品'
    }
  },
  onShow() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const [dishList, catList] = await Promise.all([
          dishApi.list({ keyword: this.keyword || undefined }),
          categoryApi.list()
        ])
        this.allDishes = dishList || []
        this.categories = catList || []
        this.dishCount = this.allDishes.length
        this.favoriteCount = 0

        // 加载家庭名
        try {
          const family = await familyApi.current()
          this.currentFamilyName = family ? family.name : '我的家庭'
        } catch (e) {
          this.currentFamilyName = '我的家庭'
        }

        this.filterDishes()
      } catch (e) {
        console.error('加载菜品失败', e)
      } finally {
        this.loading = false
      }
    },

    filterDishes() {
      let dishes = this.allDishes
      if (this.activeCategory === 'favorite') {
        dishes = dishes.filter(d => d.favorite)
      } else if (this.activeCategory !== 'all') {
        dishes = dishes.filter(d => d.categoryCode === this.activeCategory || d.id === this.activeCategory)
      }
      if (this.keyword) {
        const kw = this.keyword.toLowerCase()
        dishes = dishes.filter(d =>
          d.name.toLowerCase().includes(kw) ||
          (d.ingredients && d.ingredients.some(i => i.toLowerCase().includes(kw)))
        )
      }
      this.filteredDishes = dishes
    },

    async onSearch(e) {
      this.keyword = e.detail.value
      // 重新从后端搜索
      if (this.keyword) {
        try {
          this.allDishes = await dishApi.list({ keyword: this.keyword })
        } catch (e) { /* ignore */ }
      } else {
        await this.loadData()
        return
      }
      this.filterDishes()
    },

    clearSearch() {
      this.keyword = ''
      this.loadData()
    },

    switchCategory(catId) {
      this.activeCategory = catId
      this.filterDishes()
    },

    getCategoryCount(catId) {
      return this.allDishes.filter(d => d.categoryCode === catId).length
    },

    addCategory() {
      uni.showModal({
        title: '新增分类',
        editable: true,
        placeholderText: '例如：家常菜',
        success: async (res) => {
          if (!res.confirm) return
          const name = (res.content || '').trim()
          if (!name) return uni.showToast({ title: '请输入分类名称', icon: 'none' })
          try {
            await categoryApi.add({ name })
            uni.showToast({ title: '分类已添加', icon: 'none' })
            await this.loadData()
          } catch (e) {
            console.error('添加分类失败', e)
          }
        }
      })
    },

    getCategoryName(code) {
      const cat = this.categories.find(c => c.code === code)
      return cat ? cat.name : code || '未分类'
    },

    getDifficultyName(diff) {
      const map = { quick: '快手', normal: '普通', hard: '费时' }
      return map[diff] || '普通'
    },

    goDishDetail(id) {
      uni.navigateTo({ url: `/pages/dish-detail/index?id=${id}` })
    },

    goAddDish() {
      uni.navigateTo({ url: '/pages/add-dish/index' })
    },

    goFamilyPage() {
      uni.navigateTo({ url: '/pages/family/index' })
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

.kitchen-head {
  margin: 16rpx 24rpx 12rpx;
  border-radius: 22rpx;
  padding: 18rpx 16rpx;
  background: #fff8f3;
  border: 2rpx solid #ffe4d5;
  display: flex;
  align-items: center;
}

.kitchen-avatar {
  width: 66rpx;
  height: 66rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #ff8f5d, #ff6b35);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 38rpx;
  margin-right: 12rpx;
}

.kitchen-title {
  font-size: 29rpx;
  color: #2f2f2f;
  font-weight: 700;
  max-width: 480rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 搜索栏 */
.search-bar {
  padding: 16rpx 24rpx;
  background: var(--color-card);
  flex-shrink: 0;
}

.search-input-wrap {
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
  color: var(--color-text);
}

.search-clear {
  font-size: 24rpx;
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
  width: 170rpx;
  height: 100%;
  background: #f8fafc;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8rpx;
  padding: 24rpx 12rpx;
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
  font-size: 24rpx;
  color: var(--color-text-secondary);
  font-weight: 400;
  flex: 0 1 auto;
  max-width: 96rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.nav-item.active .nav-name {
  color: var(--color-primary);
  font-weight: 600;
}

.nav-count {
  font-size: 20rpx;
  color: var(--color-text-muted);
  min-width: 24rpx;
  text-align: left;
}

.nav-item.active .nav-count {
  color: var(--color-primary);
}

.nav-add {
  margin: 14rpx 12rpx 20rpx;
  padding: 14rpx 10rpx;
  border-radius: 12rpx;
  border: 2rpx dashed var(--color-primary);
  text-align: center;
}

.nav-add-text {
  font-size: 22rpx;
  color: var(--color-primary);
}

/* 右侧菜品区 */
.dish-area {
  flex: 1;
  height: 100%;
  background: var(--color-card);
  padding-bottom: 160rpx;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx 12rpx;
}

.area-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-text);
}

.area-count {
  font-size: 22rpx;
  color: var(--color-text-secondary);
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

.dish-cell:active {
  background: #fffdfb;
  border-color: var(--color-primary);
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.2);
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

.cell-fav {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 42rpx;
  height: 42rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
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

/* 空状态 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 24rpx;
}

.empty-icon {
  font-size: 72rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  text-align: center;
}

/* 浮动添加按钮 */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 200rpx;
  width: 96rpx;
  height: 96rpx;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.4);
  z-index: 100;
}

.fab-icon {
  font-size: 48rpx;
  color: #fff;
  line-height: 1;
}
</style>
