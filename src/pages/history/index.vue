<template>
  <view class="page">
    <!-- 月份切换 -->
    <view class="month-nav">
      <view class="month-btn" @tap="prevMonth">
        <text class="arrow-text">❮</text>
      </view>
      <text class="month-title">{{ currentYear }}年{{ currentMonth }}月</text>
      <view class="month-btn" @tap="nextMonth">
        <text class="arrow-text">❯</text>
      </view>
    </view>

    <!-- 日历 -->
    <view class="calendar">
      <view class="weekday-row">
        <text class="weekday" v-for="w in weekdays" :key="w">{{ w }}</text>
      </view>
      <view class="date-grid">
        <view
          class="date-cell"
          v-for="(day, idx) in calendarDays"
          :key="idx"
          :class="{
            empty: !day.date,
            'has-menu': day.hasMenu,
            today: day.isToday,
            selected: day.dateStr === selectedDate
          }"
          @tap="selectDate(day)"
        >
          <text class="date-num" v-if="day.date">{{ day.date }}</text>
          <view class="date-dot" v-if="day.hasMenu"></view>
        </view>
      </view>
    </view>

    <!-- 选中日期的菜单 -->
    <view class="day-menu" v-if="selectedDate">
      <view class="day-menu-header">
        <text class="day-menu-title">{{ formatSelectedDate }}</text>
        <text class="day-menu-count" v-if="selectedDishes.length > 0">{{ selectedDishes.length }}道菜</text>
      </view>
      <view class="day-menu-list" v-if="selectedDishes.length > 0">
        <view class="day-menu-item" v-for="dish in selectedDishes" :key="dish.id">
          <image class="item-cover" :src="dish.imageUrl || defaultDishImage" mode="aspectFill" />
          <text class="item-name">{{ dish.name }}</text>
          <text class="item-cat">{{ dish.categoryName }}</text>
        </view>
      </view>
      <view class="day-menu-empty" v-else>
        <text>这天没有记录</text>
      </view>
    </view>

    <!-- 统计摘要 -->
    <view class="stats-section">
      <text class="stats-title">本月统计</text>
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-num">{{ monthStats.totalDays }}</text>
          <text class="stat-label">记录天数</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">{{ monthStats.totalDishes }}</text>
          <text class="stat-label">总菜品次</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">{{ monthStats.uniqueDishes }}</text>
          <text class="stat-label">不重复菜</text>
        </view>
      </view>

      <!-- 高频菜品 -->
      <view class="freq-section" v-if="monthTopDishes.length > 0">
        <text class="freq-title">本月最爱</text>
        <view class="freq-list">
          <view class="freq-item" v-for="(item, idx) in monthTopDishes" :key="idx">
            <text class="freq-rank">{{ idx + 1 }}</text>
            <text class="freq-name">{{ item.name }}</text>
            <view class="freq-bar-wrap">
              <view class="freq-bar" :style="{ width: item.percent + '%' }"></view>
            </view>
            <text class="freq-count">{{ item.count }}次</text>
          </view>
        </view>
      </view>
    </view>

    <tab-bar-view :current="2" />
  </view>
</template>

<script>
import { historyApi, menuApi } from '../../utils/api.js'
import tabBarView from '../../components/tab-bar-view/tab-bar-view.vue'
import defaultDishImage from '../../static/default-dish.svg'

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
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
      currentYear: 0,
      currentMonth: 0,
      weekdays: ['日', '一', '二', '三', '四', '五', '六'],
      calendarDays: [],
      selectedDate: '',
      selectedDishes: [],
      menuDates: [],
      monthStats: { totalDays: 0, totalDishes: 0, uniqueDishes: 0 },
      monthTopDishes: [],
      defaultDishImage
    }
  },
  computed: {
    formatSelectedDate() {
      return formatDate(this.selectedDate)
    }
  },
  onShow() {
    const now = new Date()
    this.currentYear = now.getFullYear()
    this.currentMonth = now.getMonth() + 1
    this.selectedDate = getTodayStr()
    this.loadMonthData()
    this.loadSelectedMenu()
  },
  methods: {
    async loadMonthData() {
      try {
        const stats = await historyApi.monthStats(this.currentYear, this.currentMonth)
        this.monthStats = {
          totalDays: stats.totalDays || 0,
          totalDishes: stats.totalDishes || 0,
          uniqueDishes: stats.uniqueDishes || 0
        }
        this.monthTopDishes = stats.topDishes || []
        this.menuDates = stats.menuDates || []
        this.buildCalendar()
      } catch (e) {
        console.error('加载月度统计失败', e)
      }
    },

    buildCalendar() {
      const year = this.currentYear
      const month = this.currentMonth
      const firstDay = new Date(year, month - 1, 1).getDay()
      const daysInMonth = new Date(year, month, 0).getDate()
      const today = getTodayStr()

      const days = []
      for (let i = 0; i < firstDay; i++) {
        days.push({ date: null, dateStr: '', hasMenu: false, isToday: false })
      }
      for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
        const hasMenu = this.menuDates.includes(dateStr)
        days.push({
          date: d,
          dateStr,
          hasMenu,
          isToday: dateStr === today
        })
      }
      this.calendarDays = days
    },

    prevMonth() {
      this.currentMonth--
      if (this.currentMonth < 1) {
        this.currentMonth = 12
        this.currentYear--
      }
      this.selectedDate = ''
      this.selectedDishes = []
      this.loadMonthData()
    },

    nextMonth() {
      this.currentMonth++
      if (this.currentMonth > 12) {
        this.currentMonth = 1
        this.currentYear++
      }
      this.selectedDate = ''
      this.selectedDishes = []
      this.loadMonthData()
    },

    selectDate(day) {
      if (!day.date) return
      this.selectedDate = day.dateStr
      this.loadSelectedMenu()
    },

    async loadSelectedMenu() {
      if (!this.selectedDate) {
        this.selectedDishes = []
        return
      }
      try {
        const menu = await menuApi.getByDate(this.selectedDate)
        this.selectedDishes = (menu && menu.dishes) ? menu.dishes : []
      } catch (e) {
        this.selectedDishes = []
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 24rpx;
  padding-bottom: 160rpx;
}

/* 月份导航 */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
  margin-bottom: 16rpx;
}

.month-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 50%;
  font-size: 32rpx;
  color: #666;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.month-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
  margin: 0 40rpx;
}

/* 日历 */
.calendar {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.weekday-row {
  display: flex;
}

.weekday {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: #999;
  padding: 12rpx 0;
}

.date-grid {
  display: flex;
  flex-wrap: wrap;
}

.date-cell {
  width: calc(100% / 7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx 0;
  position: relative;
}

.date-num {
  font-size: 28rpx;
  color: #333;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.date-cell.today .date-num {
  background: #ff6b35;
  color: #fff;
}

.date-cell.selected .date-num {
  border: 2rpx solid #ff6b35;
}

.date-cell.today.selected .date-num {
  border: none;
}

.date-dot {
  width: 10rpx;
  height: 10rpx;
  background: #ff6b35;
  border-radius: 50%;
  margin-top: 4rpx;
}

.date-cell.today .date-dot {
  background: #fff;
}

.date-cell.empty {
  visibility: hidden;
}

/* 选中日期菜单 */
.day-menu {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.day-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.day-menu-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.day-menu-count {
  font-size: 24rpx;
  color: #999;
}

.day-menu-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.day-menu-item {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
}

.item-cover {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  margin-right: 16rpx;
  background: #f3f4f6;
  flex-shrink: 0;
}

.item-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.item-cat {
  font-size: 22rpx;
  color: #999;
}

.day-menu-empty {
  text-align: center;
  padding: 24rpx;
  color: #999;
  font-size: 26rpx;
}

/* 统计 */
.stats-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.stats-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.stats-grid {
  display: flex;
  gap: 16rpx;
  margin-bottom: 28rpx;
}

.stat-card {
  flex: 1;
  text-align: center;
  background: #f9f9f9;
  padding: 24rpx 12rpx;
  border-radius: 16rpx;
}

.stat-num {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #ff6b35;
}

.stat-label {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 排行榜 */
.freq-section {
  border-top: 1rpx solid #f0f0f0;
  padding-top: 24rpx;
}

.freq-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

.freq-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.freq-item {
  display: flex;
  align-items: center;
}

.freq-rank {
  width: 40rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #ff6b35;
}

.freq-name {
  width: 160rpx;
  font-size: 26rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.freq-bar-wrap {
  flex: 1;
  height: 16rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin: 0 16rpx;
  overflow: hidden;
}

.freq-bar {
  height: 100%;
  background: linear-gradient(90deg, #ff6b35, #ff9a62);
  border-radius: 8rpx;
  transition: width 0.3s;
}

.freq-count {
  font-size: 24rpx;
  color: #999;
  width: 60rpx;
  text-align: right;
}
</style>
