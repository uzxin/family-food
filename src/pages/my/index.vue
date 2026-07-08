<template>
  <view class="page">
    <view class="profile card" @tap="editNickname">
      <view class="profile-main">
        <view class="avatar">
          <text>{{ avatarText }}</text>
        </view>
        <view class="profile-info">
          <text class="nickname">{{ userInfo.nickname || '未设置' }}</text>
          <text class="desc">{{ currentFamilyName }} · 点击修改昵称</text>
        </view>
      </view>
      <view class="profile-tag">家庭版</view>
    </view>

    <view class="quick-grid card">
      <view class="quick-item" v-for="item in quickActions" :key="item.key" @tap="handleQuickAction(item.key)">
        <view class="quick-icon">
          <text>{{ item.icon }}</text>
        </view>
        <text class="quick-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="menu-list card">
      <view class="menu-item" @tap="goFamilyPage">
        <view class="menu-left">
          <view class="menu-icon-wrap">
            <image class="menu-icon" src="/src/static/default-dish.svg" mode="aspectFill" />
          </view>
          <text class="menu-title">家庭管理</text>
        </view>
        <view class="menu-right"></view>
      </view>

      <view class="menu-item" @tap="selectSpicyLevel">
        <view class="menu-left">
          <view class="menu-icon-wrap">
            <image class="menu-icon" src="/src/static/default-dish.svg" mode="aspectFill" />
          </view>
          <text class="menu-title">吃辣偏好（{{ spicyLabel }}）</text>
        </view>
        <view class="menu-right"></view>
      </view>

      <view class="menu-item" @tap="selectTemplate">
        <view class="menu-left">
          <view class="menu-icon-wrap">
            <image class="menu-icon" src="/src/static/default-dish.svg" mode="aspectFill" />
          </view>
          <text class="menu-title">默认菜单模板</text>
        </view>
        <view class="menu-right"></view>
      </view>

      <view class="menu-item" @tap="selectReminderTime">
        <view class="menu-left">
          <view class="menu-icon-wrap">
            <image class="menu-icon" src="/src/static/default-dish.svg" mode="aspectFill" />
          </view>
          <text class="menu-title">提醒时间（{{ settings.enableReminder ? settings.reminderTime : '未开启' }}）</text>
        </view>
        <view class="menu-right"></view>
      </view>

      <view class="menu-item danger" @tap="clearHistory">
        <view class="menu-left">
          <view class="menu-icon-wrap">
            <image class="menu-icon" src="/src/static/default-dish.svg" mode="aspectFill" />
          </view>
          <text class="menu-title">清空历史菜单</text>
        </view>
        <view class="menu-right"></view>
      </view>
    </view>

    <view class="logout-btn" @tap="logout">
      <text>退出登录</text>
    </view>

    <tab-bar-view :current="3" />
  </view>
</template>

<script>
import tabBarView from '../../components/tab-bar-view/tab-bar-view.vue'
import { userApi, familyApi, setUser, getUser, clearAuth } from '../../utils/api.js'

const DEFAULT_SETTINGS = {
  spicyLevel: 'medium',
  preferQuickDish: true,
  defaultTemplate: 'balanced',
  enableReminder: false,
  reminderTime: '17:30'
}

export default {
  components: { tabBarView },
  data() {
    return {
      settings: { ...DEFAULT_SETTINGS },
      spicyOptions: [
        { label: '不辣', value: 'none' },
        { label: '微辣', value: 'low' },
        { label: '中辣', value: 'medium' },
        { label: '重辣', value: 'high' }
      ],
      templateOptions: [
        { label: '2荤1素1汤（均衡）', value: 'balanced' },
        { label: '1荤2素1汤（清爽）', value: 'light' },
        { label: '3荤1素（下饭）', value: 'hearty' }
      ],
      userInfo: {},
      currentFamilyName: '',
      quickActions: [
        { key: 'family', label: '家庭', icon: '🏠' },
        { key: 'dishes', label: '菜品库', icon: '📚' },
        { key: 'add', label: '加菜', icon: '➕' },
        { key: 'history', label: '历史', icon: '📅' },
        { key: 'export', label: '导出', icon: '📤' },
        { key: 'reminder', label: '提醒', icon: '⏰' },
        { key: 'quick', label: '快手菜', icon: '⚡' },
        { key: 'template', label: '模板', icon: '🍱' }
      ]
    }
  },
  computed: {
    avatarText() {
      const name = this.userInfo.nickname || this.userInfo.username || 'U'
      return name.charAt(0).toUpperCase()
    },
    spicyIndex() {
      const idx = this.spicyOptions.findIndex(i => i.value === this.settings.spicyLevel)
      return idx === -1 ? 2 : idx
    },
    spicyLabel() {
      return this.spicyOptions[this.spicyIndex].label
    },
    templateIndex() {
      const idx = this.templateOptions.findIndex(i => i.value === this.settings.defaultTemplate)
      return idx === -1 ? 0 : idx
    },
    templateLabel() {
      return this.templateOptions[this.templateIndex].label
    }
  },
  onShow() {
    const cached = uni.getStorageSync('userSettings')
    this.settings = cached ? { ...DEFAULT_SETTINGS, ...cached } : { ...DEFAULT_SETTINGS }
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      try {
        const [profile, familyDetail] = await Promise.all([
          userApi.profile(),
          familyApi.current()
        ])
        this.userInfo = profile || {}
        // 同步本地存储
        const cached = getUser() || {}
        setUser({ ...cached, userId: profile.id, nickname: profile.nickname })
        this.currentFamilyName = familyDetail ? familyDetail.name : '未加入家庭'
      } catch (e) {
        console.error('加载用户信息失败', e)
        this.currentFamilyName = '未加入家庭'
      }
    },
    editNickname() {
      uni.showModal({
        title: '修改昵称',
        editable: true,
        placeholderText: '请输入新昵称',
        content: this.userInfo.nickname || '',
        success: async (res) => {
          if (!res.confirm) return
          const nickname = (res.content || '').trim()
          if (!nickname) return uni.showToast({ title: '昵称不能为空', icon: 'none' })
          try {
            await userApi.updateProfile({ nickname })
            this.userInfo.nickname = nickname
            const cached = getUser() || {}
            setUser({ ...cached, nickname })
            uni.showToast({ title: '修改成功', icon: 'none' })
          } catch (e) {
            console.error('修改昵称失败', e)
          }
        }
      })
    },
    saveSettings() {
      uni.setStorageSync('userSettings', this.settings)
    },
    onSpicyChange(e) {
      const item = this.spicyOptions[e.detail.value]
      this.settings.spicyLevel = item.value
      this.saveSettings()
    },
    onTemplateChange(e) {
      const item = this.templateOptions[e.detail.value]
      this.settings.defaultTemplate = item.value
      this.saveSettings()
    },
    onQuickDishChange(e) {
      this.settings.preferQuickDish = e.detail.value
      this.saveSettings()
    },
    onReminderChange(e) {
      this.settings.enableReminder = e.detail.value
      this.saveSettings()
    },
    onTimeChange(e) {
      this.settings.reminderTime = e.detail.value
      this.saveSettings()
    },
    handleQuickAction(key) {
      if (key === 'family') return this.goFamilyPage()
      if (key === 'dishes') return uni.switchTab({ url: '/pages/dish-library/index' })
      if (key === 'add') return uni.navigateTo({ url: '/pages/add-dish/index' })
      if (key === 'history') return uni.switchTab({ url: '/pages/history/index' })
      if (key === 'export') return this.exportData()
      if (key === 'reminder') {
        this.settings.enableReminder = !this.settings.enableReminder
        this.saveSettings()
        return uni.showToast({ title: this.settings.enableReminder ? '已开启提醒' : '已关闭提醒', icon: 'none' })
      }
      if (key === 'quick') {
        this.settings.preferQuickDish = !this.settings.preferQuickDish
        this.saveSettings()
        return uni.showToast({
          title: this.settings.preferQuickDish ? '已偏好快手菜' : '已关闭快手偏好',
          icon: 'none'
        })
      }
      if (key === 'template') return this.selectTemplate()
    },
    selectSpicyLevel() {
      uni.showActionSheet({
        itemList: this.spicyOptions.map(i => i.label),
        success: (res) => {
          const item = this.spicyOptions[res.tapIndex]
          this.settings.spicyLevel = item.value
          this.saveSettings()
        }
      })
    },
    selectTemplate() {
      uni.showActionSheet({
        itemList: this.templateOptions.map(i => i.label),
        success: (res) => {
          const item = this.templateOptions[res.tapIndex]
          this.settings.defaultTemplate = item.value
          this.saveSettings()
        }
      })
    },
    selectReminderTime() {
      if (!this.settings.enableReminder) {
        this.settings.enableReminder = true
        this.saveSettings()
        uni.showToast({ title: '已开启提醒，可设置时间', icon: 'none' })
        return
      }
      uni.showActionSheet({
        itemList: ['17:00', '17:30', '18:00', '18:30', '19:00'],
        success: (res) => {
          const map = ['17:00', '17:30', '18:00', '18:30', '19:00']
          this.settings.reminderTime = map[res.tapIndex]
          this.saveSettings()
        }
      })
    },
    exportData() {
      const payload = {
        exportAt: Date.now(),
        dishes: uni.getStorageSync('dishes') || [],
        dailyMenus: uni.getStorageSync('dailyMenus') || {},
        categories: uni.getStorageSync('categories') || [],
        userSettings: this.settings
      }
      uni.setClipboardData({
        data: JSON.stringify(payload),
        success: () => {
          uni.showToast({ title: '已复制到剪贴板', icon: 'none' })
        }
      })
    },
    clearHistory() {
      uni.showModal({
        title: '确认清空历史',
        content: '仅清空历史菜单记录，不删除菜品库，确定继续吗？',
        confirmColor: '#ef4444',
        success: (res) => {
          if (!res.confirm) return
          uni.setStorageSync('dailyMenus', {})
          uni.showToast({ title: '历史已清空', icon: 'none' })
        }
      })
    },
    goFamilyPage() {
      uni.navigateTo({ url: '/pages/family/index' })
    },
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (!res.confirm) return
          clearAuth()
          uni.reLaunch({ url: '/pages/login/index' })
        }
      })
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-bg);
  padding: 24rpx 24rpx 220rpx;
}

.profile {
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-main {
  display: flex;
  align-items: center;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff8e5d, #ff6b35);
  color: #fff;
  font-size: 40rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18rpx;
}

.profile-info {
  display: flex;
  flex-direction: column;
}

.nickname {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--color-text);
}

.desc {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.profile-tag {
  font-size: 22rpx;
  color: var(--color-primary);
  background: var(--color-primary-soft);
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
}

.quick-grid {
  margin-bottom: 20rpx;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18rpx 10rpx;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quick-icon {
  width: 78rpx;
  height: 78rpx;
  border-radius: 20rpx;
  background: #fff3ee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}

.quick-label {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: var(--color-text);
}

.menu-list {
  padding: 6rpx 24rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid var(--color-border);
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.menu-title {
  font-size: 30rpx;
  color: var(--color-text);
  font-weight: 500;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.menu-icon-wrap {
  width: 44rpx;
  height: 44rpx;
  border-radius: 10rpx;
  overflow: hidden;
  margin-right: 12rpx;
  flex-shrink: 0;
  background: #fff3ee;
}

.menu-icon {
  width: 100%;
  height: 100%;
}

.menu-right {
  width: 14rpx;
  height: 14rpx;
  border-top: 3rpx solid #c4c9d1;
  border-right: 3rpx solid #c4c9d1;
  transform: rotate(45deg);
  margin-right: 4rpx;
}

.menu-item.danger .menu-title {
  color: var(--color-danger);
}

.logout-btn {
  margin-top: 20rpx;
  text-align: center;
  padding: 24rpx 0;
  background: #fff;
  border-radius: 16rpx;
  font-size: 30rpx;
  color: var(--color-danger);
  font-weight: 500;
}
</style>
