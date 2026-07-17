<template>
  <view class="page" v-if="dish">
    <!-- 菜品头部 -->
    <view class="dish-header">
      <view class="dish-header-top">
        <image
          class="dish-cover"
          :src="getImageUrl(dish.imageUrl) || defaultDishImage"
          mode="aspectFill"
          @tap="previewDishImage"
        />
        <view class="dish-header-info">
          <text class="dish-name">{{ dish.name }}</text>
          <view class="dish-meta">
            <text class="dish-cat">{{ dish.categoryName }}</text>
            <text class="dish-diff">{{ getDifficultyName(dish.difficulty) }}</text>
          </view>
          <!-- 评分展示 -->
          <view class="dish-rating" v-if="dish.rating">
            <text class="rating-star" v-for="star in 5" :key="star" :class="{ filled: star <= dish.rating }">★</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 食材 -->
    <view class="section" v-if="dish.ingredients && dish.ingredients.length > 0">
      <text class="section-title">食材</text>
      <view class="ingredient-list">
        <view class="ingredient-tag" v-for="(ing, idx) in dish.ingredients" :key="idx">
          <text>{{ ing }}</text>
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="section" v-if="dish.remark">
      <text class="section-title">备注</text>
      <text class="remark-text">{{ dish.remark }}</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-row">
      <view class="action-btn edit" @tap="editDish">
        <text>编辑</text>
      </view>
      <view class="action-btn delete" @tap="deleteDish">
        <text>删除</text>
      </view>
    </view>
  </view>
</template>

<script>
import { dishApi, getImageUrl } from '../../utils/api.js'
import defaultDishImage from '../../static/default-dish.svg'

export default {
  data() {
    return {
      dishId: '',
      dish: null,
      defaultDishImage,
      getImageUrl
    }
  },
  onLoad(options) {
    this.dishId = options.id
    this.loadDish()
  },
  onShow() {
    if (this.dishId) this.loadDish()
  },
  methods: {
    async loadDish() {
      try {
        const dish = await dishApi.detail(this.dishId)
        if (!dish) {
          uni.showToast({ title: '菜品不存在', icon: 'none' })
          setTimeout(() => uni.navigateBack(), 1000)
          return
        }
        this.dish = dish
        uni.setNavigationBarTitle({ title: dish.name })
      } catch (e) {
        console.error('加载菜品失败', e)
      }
    },

    getDifficultyName(diff) {
      const map = { quick: '⚡ 快手菜', normal: '👨‍🍳 普通', hard: '⏰ 费时' }
      return map[diff] || '普通'
    },

    previewDishImage() {
      if (!this.dish || !this.dish.imageUrl) return
      const url = getImageUrl(this.dish.imageUrl)
      uni.previewImage({
        current: url,
        urls: [url]
      })
    },

    editDish() {
      uni.navigateTo({ url: `/pages/add-dish/index?id=${this.dishId}` })
    },

    deleteDish() {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除「${this.dish.name}」吗？`,
        confirmColor: '#f44336',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await dishApi.delete(this.dishId)
            uni.showToast({ title: '已删除', icon: 'none' })
            setTimeout(() => uni.navigateBack(), 500)
          } catch (e) {
            console.error('删除失败', e)
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 24rpx;
}

.dish-header {
  background: #fff;
  border-radius: 20rpx;
  padding: 36rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.dish-header-top {
  display: flex;
  align-items: center;
}

.dish-cover {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  background: #f3f4f6;
  flex-shrink: 0;
}

.dish-header-info {
  flex: 1;
}

.dish-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.dish-meta {
  display: flex;
  gap: 16rpx;
}

.dish-cat, .dish-diff {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 10rpx;
}

.dish-cat {
  background: #fff3ee;
  color: #ff6b35;
}

.dish-diff {
  background: #f0f7ff;
  color: #4a90d9;
}

/* 评分 */
.dish-rating {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 12rpx;
}

.rating-star {
  font-size: 30rpx;
  color: #e0d5c8;
}

.rating-star.filled {
  color: #ffb020;
}

.fav-btn {
  padding: 16rpx;
}

.fav-icon {
  font-size: 48rpx;
}

/* 通用区块 */
.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

/* 食材 */
.ingredient-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.ingredient-tag {
  background: #f0f7ff;
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #4a90d9;
}

/* 备注 */
.remark-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

/* 统计 */
.stat-row {
  display: flex;
  gap: 24rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
  background: #f5f5f5;
  padding: 24rpx;
  border-radius: 16rpx;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #ff6b35;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 操作按钮 */
.action-row {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.action-btn {
  flex: 1;
  text-align: center;
  padding: 26rpx;
  border-radius: 20rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.action-btn.edit {
  background: #fff;
  color: #ff6b35;
  border: 2rpx solid #ff6b35;
}

.action-btn.delete {
  background: #fff;
  color: #f44336;
  border: 2rpx solid #f44336;
}
</style>
