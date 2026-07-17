<template>
  <view class="page">
    <!-- 未登录提示 -->
    <view class="login-prompt" v-if="!loggedIn">
      <view class="login-prompt-icon">🔒</view>
      <text class="login-prompt-title">登录后即可查看菜品</text>
      <text class="login-prompt-desc">登录后可管理家庭菜品库，点菜更方便</text>
      <view class="login-prompt-btn" @tap="goLogin">
        <text>去登录</text>
      </view>
    </view>

    <!-- 顶部家庭信息 -->
    <view class="header-card" @tap="goFamilyPage" v-if="loggedIn">
      <view class="header-avatar">
        <text>🍲</text>
      </view>
      <text class="header-title">{{ currentFamilyName }}的小厨房</text>
      <text class="header-arrow">›</text>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar" v-if="loggedIn">
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
    <view class="main-body" v-if="loggedIn">
      <!-- 左侧分类栏 -->
      <scroll-view class="side-nav" scroll-y>
        <view
          class="nav-item"
          :class="{ active: activeCategory === 'all' }"
          @tap="switchCategory('all')"
        >
          <text class="nav-icon">🍽️</text>
          <text class="nav-name">全部</text>
        </view>
        <view
          class="nav-item"
          :class="{ active: activeCategory === 'favorite' }"
          @tap="switchCategory('favorite')"
        >
          <text class="nav-icon">❤️</text>
          <text class="nav-name">最爱</text>
        </view>
        <view
          class="nav-item"
          :class="{ active: activeCategory === cat.code }"
          v-for="cat in categories"
          :key="cat.id"
          @tap="switchCategory(cat.code)"
        >
          <text class="nav-icon">{{ cat.icon || '🍴' }}</text>
          <text class="nav-name">{{ cat.name }}</text>
        </view>
        <view class="nav-add" @tap="addCategory">
          <text class="nav-add-text">+ 添加</text>
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
            class="dish-card"
            v-for="dish in filteredDishes"
            :key="dish.id"
          >
            <!-- 菜品图片 -->
            <view class="card-cover-wrap" @tap="goDishDetail(dish.id)">
              <image class="card-cover" :src="getImageUrl(dish.imageUrl) || defaultDishImage" mode="aspectFill" />
              <view class="card-fav" v-if="dish.favorite" @tap.stop="toggleFavorite(dish)">
                <text>❤️</text>
              </view>
            </view>

            <!-- 菜品信息 -->
            <view class="card-body" @tap="goDishDetail(dish.id)">
              <text class="card-name">{{ dish.name }}</text>
              <!-- 食材摘要 -->
              <text class="card-ingredients" v-if="dish.ingredients && dish.ingredients.length > 0">
                {{ dish.ingredients.slice(0, 3).join(' · ') }}{{ dish.ingredients.length > 3 ? ' ...' : '' }}
              </text>
              <!-- 五角星评分 -->
              <view class="card-rating">
                <view
                  class="star-wrap"
                  v-for="star in 5"
                  :key="star"
                  @tap.stop="onRate(dish, star)"
                >
                  <text class="star" :class="{ filled: star <= (dish.rating || 0) }">★</text>
                </view>
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
    <view class="fab" @tap="goAddDish" v-if="loggedIn">
      <text class="fab-icon">+</text>
    </view>

    <tab-bar-view :current="1" />
  </view>
</template>

<script>
import { dishApi, categoryApi, familyApi, getImageUrl, isLoggedIn } from '../../utils/api.js'
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
      getImageUrl,
      currentFamilyName: '我的家庭',
      loading: false,
      loggedIn: false
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
    this.loggedIn = isLoggedIn()
    if (this.loggedIn) {
      this.loadData()
    } else {
      this.allDishes = []
      this.filteredDishes = []
      this.categories = []
      this.currentFamilyName = '我的家庭'
    }
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
        this.favoriteCount = this.allDishes.filter(d => d.favorite).length

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

    async onRate(dish, star) {
      // 再次点击相同分数则取消评分
      const newRating = (dish.rating === star) ? 0 : star
      try {
        await dishApi.rate(dish.id, newRating)
        dish.rating = newRating
        uni.showToast({
          title: newRating > 0 ? `已评${newRating}星` : '已取消评分',
          icon: 'none',
          duration: 1000
        })
      } catch (e) {
        console.error('评分失败', e)
      }
    },

    toggleFavorite(dish) {
      // 预留收藏功能
    },

    goDishDetail(id) {
      uni.navigateTo({ url: `/pages/dish-detail/index?id=${id}` })
    },

    goAddDish() {
      uni.navigateTo({ url: '/pages/add-dish/index' })
    },

    goFamilyPage() {
      uni.navigateTo({ url: '/pages/family/index' })
    },

    goLogin() {
      uni.navigateTo({ url: '/pages/login/index' })
    }
  }
}
</script>

<style scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fef6f0;
}

/* 未登录提示 */
.login-prompt {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
}
.login-prompt-icon {
  font-size: 100rpx;
  margin-bottom: 32rpx;
}
.login-prompt-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #5a3e2b;
  margin-bottom: 16rpx;
}
.login-prompt-desc {
  font-size: 26rpx;
  color: #b89a7d;
  margin-bottom: 48rpx;
  text-align: center;
}
.login-prompt-btn {
  background: linear-gradient(135deg, #ff8f5d, #ff6b35);
  border-radius: 999rpx;
  padding: 22rpx 80rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.4);
}
.login-prompt-btn text {
  font-size: 30rpx;
  color: #fff;
  font-weight: 600;
}

/* 顶部家庭卡片 */
.header-card {
  margin: 16rpx 24rpx 8rpx;
  border-radius: 24rpx;
  padding: 22rpx 24rpx;
  background: linear-gradient(135deg, #fff5ee, #ffe8d6);
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 100, 0.12);
}

.header-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff8f5d, #ff6b35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.header-title {
  flex: 1;
  font-size: 30rpx;
  color: #5a3e2b;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-arrow {
  font-size: 36rpx;
  color: #c9a78a;
  margin-left: 8rpx;
}

/* 搜索栏 */
.search-bar {
  padding: 12rpx 24rpx 8rpx;
  flex-shrink: 0;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 40rpx;
  padding: 14rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.search-icon {
  font-size: 26rpx;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 26rpx;
  color: #5a3e2b;
}

.search-clear {
  font-size: 24rpx;
  color: #c9a78a;
  padding: 8rpx;
}

/* 主体布局 */
.main-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧分类导航 */
.side-nav {
  width: 160rpx;
  height: 100%;
  background: #fff5ee;
  flex-shrink: 0;
  padding-top: 8rpx;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 22rpx 8rpx;
  position: relative;
  transition: all 0.2s ease;
}

.nav-item.active {
  background: #fff;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 20%;
  bottom: 20%;
  width: 6rpx;
  background: #ff6b35;
  border-radius: 0 6rpx 6rpx 0;
}

.nav-icon {
  font-size: 36rpx;
  margin-bottom: 6rpx;
}

.nav-name {
  font-size: 22rpx;
  color: #8b7355;
  font-weight: 400;
  max-width: 120rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  text-align: center;
}

.nav-item.active .nav-name {
  color: #ff6b35;
  font-weight: 600;
}

.nav-add {
  margin: 16rpx 16rpx 24rpx;
  padding: 16rpx 8rpx;
  border-radius: 16rpx;
  border: 2rpx dashed #ffb088;
  text-align: center;
}

.nav-add-text {
  font-size: 22rpx;
  color: #ff6b35;
}

/* 右侧菜品区 */
.dish-area {
  flex: 1;
  height: 100%;
  padding-bottom: 160rpx;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 20rpx 12rpx;
}

.area-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #5a3e2b;
}

.area-count {
  font-size: 22rpx;
  color: #b89a7d;
}

/* 菜品卡片网格 */
.dish-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  padding: 8rpx 14rpx 24rpx;
}

.dish-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(139, 115, 85, 0.08);
  transition: transform 0.15s ease;
}

.dish-card:active {
  transform: scale(0.97);
}

/* 菜品图片 */
.card-cover-wrap {
  position: relative;
  width: 100%;
  height: 200rpx;
  overflow: hidden;
}

.card-cover {
  width: 100%;
  height: 100%;
}

.card-fav {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}

/* 菜品信息 */
.card-body {
  padding: 14rpx 16rpx 16rpx;
}

.card-name {
  font-size: 28rpx;
  color: #3d2c1e;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  margin-bottom: 6rpx;
}

.card-ingredients {
  font-size: 22rpx;
  color: #b89a7d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  margin-bottom: 10rpx;
  line-height: 1.4;
}

/* 五角星评分 */
.card-rating {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.star-wrap {
  padding: 2rpx 2rpx;
}

.star {
  font-size: 28rpx;
  color: #e0d5c8;
  transition: color 0.15s ease;
}

.star.filled {
  color: #ffb020;
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
  color: #b89a7d;
  text-align: center;
}

/* 浮动添加按钮 */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 200rpx;
  width: 96rpx;
  height: 96rpx;
  background: linear-gradient(135deg, #ff8f5d, #ff6b35);
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
