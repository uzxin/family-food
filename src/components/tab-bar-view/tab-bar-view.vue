<template>
  <view class="tab-bar">
    <view
      class="tab-item"
      v-for="(item, index) in tabs"
      :key="index"
      :class="{ active: current === index }"
      @tap="switchTab(index)"
    >
      <!-- 图标容器 -->
      <view class="tab-icon-wrap" :class="{ active: current === index }">
        <!-- 今日 - 日历图标 -->
        <view v-if="index === 0" class="tab-icon">
          <view class="icon-svg">
            <view v-if="current === 0" class="icon-today-active">
              <view class="cal-top"></view>
              <view class="cal-body">
                <text class="cal-date">{{ todayDate }}</text>
              </view>
            </view>
            <view v-else class="icon-today">
              <view class="cal-top"></view>
              <view class="cal-body">
                <text class="cal-date">{{ todayDate }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 菜品库 - 菜单/网格图标 -->
        <view v-if="index === 1" class="tab-icon">
          <view class="icon-svg">
            <view class="icon-library" :class="{ active: current === 1 }">
              <view class="grid-row">
                <view class="grid-dot"></view>
                <view class="grid-dot"></view>
              </view>
              <view class="grid-row">
                <view class="grid-dot"></view>
                <view class="grid-dot"></view>
              </view>
            </view>
          </view>
        </view>

        <!-- 历史 - 时钟图标 -->
        <view v-if="index === 2" class="tab-icon">
          <view class="icon-svg">
            <view class="icon-history" :class="{ active: current === 2 }">
              <view class="clock-face">
                <view class="clock-hand-h"></view>
                <view class="clock-hand-m"></view>
                <view class="clock-dot"></view>
              </view>
            </view>
          </view>
        </view>

        <!-- 我的 - 用户图标 -->
        <view v-if="index === 3" class="tab-icon">
          <view class="icon-svg">
            <view class="icon-user" :class="{ active: current === 3 }">
              <view class="user-head"></view>
              <view class="user-body"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 文字 -->
      <text class="tab-text" :class="{ active: current === index }">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'TabBarView',
  props: {
    current: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      tabs: [
        { text: '今日', pagePath: '/pages/index/index' },
        { text: '菜品库', pagePath: '/pages/dish-library/index' },
        { text: '历史', pagePath: '/pages/history/index' },
        { text: '我的', pagePath: '/pages/my/index' }
      ],
      todayDate: new Date().getDate()
    }
  },
  methods: {
    switchTab(index) {
      if (this.current === index) return
      uni.switchTab({
        url: this.tabs[index].pagePath
      })
    }
  }
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 110rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: #ffffff;
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.06);
  z-index: 999;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  position: relative;
  transition: all 0.25s ease;
}

/* 图标容器 */
.tab-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rpx;
  transition: transform 0.25s ease;
}

.tab-icon-wrap.active {
  transform: scale(1.1);
}

.tab-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== 今日 - 日历图标 ===== */
.icon-today, .icon-today-active {
  width: 44rpx;
  height: 44rpx;
  border-radius: 10rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.icon-today {
  border: 3rpx solid #b0b0b0;
}

.icon-today-active {
  border: 3rpx solid #ff6b35;
}

.cal-top {
  height: 12rpx;
  width: 100%;
}

.icon-today .cal-top {
  background: #b0b0b0;
}

.icon-today-active .cal-top {
  background: #ff6b35;
}

.cal-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cal-date {
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1;
}

.icon-today .cal-date {
  color: #b0b0b0;
}

.icon-today-active .cal-date {
  color: #ff6b35;
}

/* ===== 菜品库 - 网格图标 ===== */
.icon-library {
  width: 42rpx;
  height: 42rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 4rpx;
}

.grid-row {
  display: flex;
  justify-content: space-between;
}

.grid-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 4rpx;
  background: #b0b0b0;
  transition: all 0.25s ease;
}

.icon-library.active .grid-dot {
  background: #ff6b35;
}

/* ===== 历史 - 时钟图标 ===== */
.icon-history {
  width: 42rpx;
  height: 42rpx;
  border-radius: 50%;
  border: 3rpx solid #b0b0b0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: border-color 0.25s ease;
}

.icon-history.active {
  border-color: #ff6b35;
}

.clock-face {
  width: 100%;
  height: 100%;
  position: relative;
}

.clock-hand-h {
  position: absolute;
  width: 3rpx;
  height: 12rpx;
  background: #b0b0b0;
  left: 50%;
  top: 30%;
  transform: translateX(-50%);
  border-radius: 2rpx;
  transition: background 0.25s ease;
}

.clock-hand-m {
  position: absolute;
  width: 3rpx;
  height: 10rpx;
  background: #b0b0b0;
  left: 50%;
  top: 50%;
  transform-origin: top center;
  transform: translateX(-50%) rotate(90deg);
  border-radius: 2rpx;
  transition: background 0.25s ease;
}

.clock-dot {
  position: absolute;
  width: 6rpx;
  height: 6rpx;
  border-radius: 50%;
  background: #b0b0b0;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  transition: background 0.25s ease;
}

.icon-history.active .clock-hand-h,
.icon-history.active .clock-hand-m,
.icon-history.active .clock-dot {
  background: #ff6b35;
}

/* ===== 我的 - 用户图标 ===== */
.icon-user {
  width: 42rpx;
  height: 42rpx;
  position: relative;
}

.user-head {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #b0b0b0;
  position: absolute;
  left: 50%;
  top: 4rpx;
  transform: translateX(-50%);
  transition: background 0.25s ease;
}

.user-body {
  width: 28rpx;
  height: 16rpx;
  border-radius: 14rpx 14rpx 8rpx 8rpx;
  border: 3rpx solid #b0b0b0;
  border-top: none;
  position: absolute;
  left: 50%;
  bottom: 4rpx;
  transform: translateX(-50%);
  transition: border-color 0.25s ease;
}

.icon-user.active .user-head {
  background: #ff6b35;
}

.icon-user.active .user-body {
  border-color: #ff6b35;
  border-top: none;
}

/* ===== 文字 ===== */
.tab-text {
  font-size: 22rpx;
  color: #b0b0b0;
  transition: color 0.25s ease;
  font-weight: 400;
}

.tab-text.active {
  color: #ff6b35;
  font-weight: 600;
}
</style>
