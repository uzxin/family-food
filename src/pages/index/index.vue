<template>
  <view class="page">
    <!-- 顶部日期横幅 -->
    <view class="hero-banner">
      <view class="hero-bg"></view>
      <view class="hero-content">
        <view class="hero-top">
          <text class="hero-date-pill">{{ todayStr }}</text>
          <text class="hero-family-tag">{{ currentFamilyName }}</text>
        </view>
        <text class="hero-title">今天吃什么</text>
        <text class="hero-subtitle">3分钟搞定今日菜单，告别选择困难</text>
      </view>
    </view>

    <!-- 未登录提示 -->
    <view class="empty-state" v-if="!loggedIn">
      <view class="empty-illustration">
        <text class="empty-icon">🔒</text>
      </view>
      <text class="empty-title">登录后即可查看今日菜单</text>
      <text class="empty-desc">登录后可管理家庭菜单，点菜更方便</text>
      <view class="empty-action" @tap="goLogin">
        <text>去登录</text>
      </view>
    </view>

    <!-- 今日菜单概览 -->
    <view class="menu-overview" v-if="loggedIn && todayDishes.length > 0">
      <view class="overview-header">
        <view class="overview-left">
          <text class="overview-label">今日已点</text>
          <view class="overview-count">
            <text class="count-num">{{ todayDishes.length }}</text>
            <text class="count-unit">道菜</text>
          </view>
        </view>
        <view class="overview-right" @tap="goOrder">
          <text class="overview-edit">编辑</text>
        </view>
      </view>
      <!-- 分类统计标签 -->
    <view class="category-stats" v-if="categoryStats.length > 0">
      <view class="cat-stat-item" v-for="cat in categoryStats" :key="cat.name">
        <view class="cat-stat-dot" :style="{ background: cat.color }"></view>
        <text class="cat-stat-name">{{ cat.name }}</text>
        <text class="cat-stat-num">{{ cat.count }}</text>
      </view>
    </view>
    </view>

    <!-- 今日菜单卡片网格 -->
    <view class="dish-grid" v-if="loggedIn && todayDishes.length > 0">
      <view
        class="dish-card"
        v-for="dish in todayDishes"
        :key="dish.id"
      >
        <view class="dish-card-img-wrap">
          <image class="dish-card-img" :src="getImageUrl(dish.imageUrl) || defaultDishImage" mode="aspectFill" />
          <view class="dish-card-cat-tag" :style="{ background: getCategoryColor(dish.categoryName) }">
            <text>{{ dish.categoryName }}</text>
          </view>
          <view class="dish-card-remove" @tap.stop="removeDish(dish.id)">
            <text>✕</text>
          </view>
        </view>
        <view class="dish-card-body">
          <text class="dish-card-name">{{ dish.name }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="loggedIn && todayDishes.length === 0">
      <view class="empty-illustration">
        <text class="empty-icon">🍽️</text>
      </view>
      <text class="empty-title">今天还没点菜</text>
      <text class="empty-desc">去点几个菜吧，告别选择困难症！</text>
      <view class="empty-action" @tap="goOrder">
        <text>去点菜</text>
      </view>
    </view>

    <!-- 智能推荐 -->
    <view class="section" v-if="loggedIn && recommendations.length > 0">
      <view class="section-header">
        <text class="section-title">为你推荐</text>
        <view class="refresh-btn" @tap="refreshRecommend">
          <text>🔄 换一批</text>
        </view>
      </view>
      <view class="recommend-scroll" scroll-x="true">
        <view class="recommend-list">
          <view
            class="recommend-card"
            v-for="dish in recommendations"
            :key="dish.id"
            @tap="quickAdd(dish)"
          >
            <image class="recommend-cover" :src="getImageUrl(dish.imageUrl) || defaultDishImage" mode="aspectFill" />
            <view class="recommend-info">
              <text class="recommend-name">{{ dish.name }}</text>
              <text class="recommend-cat">{{ dish.categoryName }}</text>
            </view>
            <view class="recommend-add" v-if="!isDishInMenu(dish.id)">
              <text>＋</text>
            </view>
            <view class="recommend-added" v-else>
              <text>✓</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar" v-if="loggedIn">
      <view class="action-btn primary" @tap="goOrder">
        <text class="action-btn-icon">📋</text>
        <text>去点菜台</text>
      </view>
      <view class="action-btn secondary" @tap="randomMenu">
        <text class="action-btn-icon">🎲</text>
        <text>不知道吃啥</text>
      </view>
    </view>

    <!-- 菜品库入口提示 -->
    <view class="tip-card" v-if="loggedIn && dishCount === 0">
      <text class="tip-icon">💡</text>
      <view class="tip-content">
        <text class="tip-title">还没有添加菜品</text>
        <text class="tip-desc">先去菜品库添加你们家会做的菜吧</text>
      </view>
      <view class="tip-btn" @tap="goAddDish">
        <text>去添加</text>
      </view>
    </view>

    <tab-bar-view :current="0" />
  </view>
</template>

<script>
import { isLoggedIn, menuApi, dishApi, familyApi, getImageUrl } from '../../utils/api.js'
import tabBarView from '../../components/tab-bar-view/tab-bar-view.vue'
import defaultDishImage from '../../static/default-dish.svg'

function formatDate(date) {
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function getTodayStr() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export default {
  components: { tabBarView },
  data() {
    return {
      todayDishes: [],
      todayMenu: null,
      recommendations: [],
      dishCount: 0,
      todayStr: '',
      defaultDishImage,
      getImageUrl,
      currentFamilyName: '我的家庭',
      catColorMap: {
        '荤菜': '#e74c3c',
        '素菜': '#27ae60',
        '汤': '#f39c12',
        '主食': '#8e44ad',
        '凉菜': '#3498db',
        '海鲜': '#1abc9c',
        '甜品': '#e91e63',
        '其他': '#95a5a6'
      },
      loggedIn: false
    }
  },
  computed: {
    categoryStats() {
      const map = {}
      this.todayDishes.forEach(d => {
        const name = d.categoryName || '其他'
        if (!map[name]) map[name] = { name, count: 0, color: this.getCatColor(name) }
        map[name].count++
      })
      return Object.values(map)
    }
  },
  onShow() {
    this.loggedIn = isLoggedIn()
    if (!this.loggedIn) {
      this.todayDishes = []
      this.recommendations = []
      this.dishCount = 0
      this.todayStr = formatDate(new Date())
      this.currentFamilyName = '未登录'
      return
    }
    this.loadData()
  },
  methods: {
    async loadData() {
      this.todayStr = formatDate(new Date())
      try {
        const [menu, dishes, family] = await Promise.all([
          menuApi.today(),
          dishApi.list(),
          familyApi.current().catch(() => null)
        ])

        this.currentFamilyName = family ? family.name : '未加入家庭'
        this.dishCount = dishes ? dishes.length : 0

        // 今日菜单
        this.todayMenu = menu
        this.todayDishes = (menu && menu.dishes) ? menu.dishes : []

        this.refreshRecommend(dishes)
      } catch (e) {
        console.error('加载首页失败', e)
      }
    },

    refreshRecommend(dishes) {
      const all = dishes || []
      if (all.length === 0) {
        this.recommendations = []
        return
      }
      // 随机取6个
      const shuffled = [...all].sort(() => Math.random() - 0.5)
      this.recommendations = shuffled.slice(0, 6)
    },

    isDishInMenu(dishId) {
      return this.todayDishes.some(d => d.id === dishId)
    },

    async quickAdd(dish) {
      if (this.isDishInMenu(dish.id)) return
      const ids = this.todayDishes.map(d => d.id)
      ids.push(dish.id)
      try {
        await menuApi.save({ date: getTodayStr(), dishIds: ids })
        await this.loadData()
        uni.showToast({ title: `已添加「${dish.name}」`, icon: 'none' })
      } catch (e) {
        console.error('添加菜品失败', e)
      }
    },

    async removeDish(dishId) {
      uni.showModal({
        title: '移除菜品',
        content: '确定从今日菜单移除这道菜吗？',
        confirmColor: '#ff6b35',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await menuApi.removeDish(getTodayStr(), dishId)
            await this.loadData()
            uni.showToast({ title: '已移除', icon: 'none' })
          } catch (e) {
            console.error('移除菜品失败', e)
          }
        }
      })
    },

    getCatColor(name) {
      return this.catColorMap[name] || '#95a5a6'
    },

    getCategoryColor(name) {
      return this.catColorMap[name] || '#95a5a6'
    },

    goOrder() {
      uni.navigateTo({ url: '/pages/daily-menu/index' })
    },

    goAddDish() {
      uni.navigateTo({ url: '/pages/add-dish/index' })
    },

    goLogin() {
      uni.navigateTo({ url: '/pages/login/index' })
    },

    async randomMenu() {
      if (this.dishCount === 0) {
        uni.showToast({ title: '请先添加菜品', icon: 'none' })
        return
      }
      try {
        const dishes = await dishApi.list()
        if (!dishes || dishes.length === 0) {
          uni.showToast({ title: '菜品太少，无法搭配', icon: 'none' })
          return
        }
        const shuffled = dishes.sort(() => Math.random() - 0.5)
        const count = Math.min(4, shuffled.length)
        const ids = shuffled.slice(0, count).map(d => d.id)
        await menuApi.save({ date: getTodayStr(), dishIds: ids })
        await this.loadData()
        uni.showToast({ title: '已随机搭配菜单！', icon: 'none' })
      } catch (e) {
        console.error('随机菜单失败', e)
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 0 0 200rpx;
  background-color: var(--color-bg);
}

/* ===== 顶部横幅 ===== */
.hero-banner {
  position: relative;
  padding: 40rpx 32rpx 48rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}
.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #ff8a4c 0%, #ff6b35 50%, #e8542a 100%);
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.hero-date-pill {
  font-size: 24rpx;
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  font-weight: 500;
}
.hero-family-tag {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.15);
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
}
.hero-title {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2rpx;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}
.hero-subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

/* ===== 今日菜单概览 ===== */
.menu-overview {
  margin: 0 24rpx 20rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx 28rpx 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}
.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.overview-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.overview-label {
  font-size: 28rpx;
  color: var(--color-text-secondary);
  font-weight: 500;
}
.overview-count {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}
.count-num {
  font-size: 52rpx;
  font-weight: 800;
  color: var(--color-primary);
  line-height: 1;
}
.count-unit {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}
.overview-right {
  padding: 10rpx 28rpx;
  background: var(--color-primary-soft);
  border-radius: 999rpx;
}
.overview-edit {
  font-size: 26rpx;
  color: var(--color-primary);
  font-weight: 500;
}

/* 分类统计标签 */
.category-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 2rpx dashed #f0f0f0;
}
.cat-stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #f7f8fa;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
}
.cat-stat-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
}
.cat-stat-name {
  font-size: 24rpx;
  color: var(--color-text);
}
.cat-stat-num {
  font-size: 26rpx;
  font-weight: 700;
  color: var(--color-primary);
}

/* ===== 菜品卡片网格 ===== */
.dish-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  padding: 0 24rpx;
  margin-bottom: 32rpx;
}
.dish-card {
  width: calc(50% - 10rpx);
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}
.dish-card-img-wrap {
  position: relative;
  width: 100%;
  height: 200rpx;
}
.dish-card-img {
  width: 100%;
  height: 100%;
  background: #f3f4f6;
}
.dish-card-cat-tag {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
}
.dish-card-cat-tag text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 600;
}
.dish-card-remove {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(8rpx);
}
.dish-card-remove text {
  font-size: 22rpx;
  color: #fff;
}
.dish-card-body {
  padding: 18rpx 20rpx;
}
.dish-card-name {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--color-text);
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ===== 通用区块 ===== */
.section {
  margin-bottom: 32rpx;
  padding: 0 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--color-text);
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 60rpx;
}
.empty-illustration {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: var(--color-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}
.empty-icon {
  font-size: 80rpx;
}
.empty-title {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12rpx;
}
.empty-desc {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  margin-bottom: 32rpx;
}
.empty-action {
  padding: 20rpx 60rpx;
  background: var(--color-primary);
  border-radius: 999rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}
.empty-action text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
}

/* ===== 推荐列表 ===== */
.refresh-btn {
  padding: 8rpx 20rpx;
  background: var(--color-primary-soft);
  border-radius: 999rpx;
}
.refresh-btn text {
  font-size: 24rpx;
  color: var(--color-primary);
}
.recommend-scroll {
  width: 100%;
  overflow: hidden;
}
.recommend-list {
  display: flex;
  gap: 16rpx;
  padding-bottom: 8rpx;
}
.recommend-card {
  flex-shrink: 0;
  width: 200rpx;
  background: var(--color-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  position: relative;
}
.recommend-cover {
  width: 100%;
  height: 140rpx;
  background: #f3f4f6;
}
.recommend-info {
  padding: 14rpx 16rpx 20rpx;
}
.recommend-name {
  font-size: 28rpx;
  color: var(--color-text);
  font-weight: 600;
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.recommend-cat {
  font-size: 22rpx;
  color: var(--color-text-secondary);
  margin-top: 4rpx;
}
.recommend-add {
  position: absolute;
  bottom: 16rpx;
  right: 16rpx;
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  border-radius: 50%;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.4);
}
.recommend-add text {
  font-size: 32rpx;
  color: #fff;
  font-weight: 700;
}
.recommend-added {
  position: absolute;
  bottom: 16rpx;
  right: 16rpx;
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-success);
  border-radius: 50%;
}
.recommend-added text {
  font-size: 26rpx;
  color: #fff;
}

/* ===== 操作按钮 ===== */
.action-bar {
  display: flex;
  gap: 20rpx;
  margin: 40rpx 24rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28rpx;
  border-radius: var(--radius-lg);
  font-size: 30rpx;
  font-weight: 600;
}

.action-btn-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.action-btn.primary {
  background: var(--color-primary);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}

.action-btn.secondary {
  background: var(--color-card);
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
}

/* ===== 提示卡片 ===== */
.tip-card {
  display: flex;
  align-items: center;
  background: var(--color-card);
  border-radius: var(--radius-lg);
  padding: 28rpx;
  margin: 0 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.tip-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.tip-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tip-title {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
}

.tip-desc {
  font-size: 24rpx;
  color: var(--color-text-secondary);
  margin-top: 4rpx;
}

.tip-btn {
  padding: 12rpx 28rpx;
  background: var(--color-primary);
  border-radius: 24rpx;
}

.tip-btn text {
  font-size: 26rpx;
  color: #fff;
}
</style>
