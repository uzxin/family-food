<template>
  <view class="page">
    <view class="card header">
      <text class="title">家庭空间</text>
      <text class="subtitle">和家人一起管理你们的菜品库</text>
    </view>

    <view class="card section" v-if="currentFamily">
      <text class="section-title">当前家庭</text>
      <view class="family-current">
        <view class="family-main">
          <text class="family-name">{{ currentFamily.name }}</text>
          <text class="family-meta">邀请码：{{ currentFamily.inviteCode }}</text>
        </view>
        <text class="family-badge">已启用</text>
      </view>
      <view class="actions">
        <view class="action-btn secondary" @tap="copyInviteCode">
          <text>复制邀请码</text>
        </view>
      </view>
    </view>

    <view class="card section">
      <text class="section-title">我的家庭列表</text>
      <view class="family-list" v-if="families.length > 0">
        <view class="family-item" v-for="family in families" :key="family.id">
          <view class="family-left">
            <text class="family-item-name">{{ family.name }}</text>
            <text class="family-item-meta">{{ family.members.length }}位成员 · {{ family.inviteCode }}</text>
          </view>
          <view
            class="switch-btn"
            :class="{ active: currentFamily && currentFamily.id === family.id }"
            @tap="switchFamily(family.id)"
          >
            <text>{{ currentFamily && currentFamily.id === family.id ? '当前' : '切换' }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="card section">
      <text class="section-title">成员预览</text>
      <view v-if="currentFamily && currentFamily.members.length > 0">
        <view class="member-item" v-for="member in currentFamily.members" :key="member.userId">
          <text class="member-name">{{ member.nickname }}</text>
          <text class="member-role">{{ member.role === 1 ? '管理员' : '成员' }}</text>
        </view>
      </view>
      <text class="hint" v-if="!currentFamily || !currentFamily.members || currentFamily.members.length === 0">暂无其他成员，快去邀请家人加入吧！</text>
    </view>

    <view class="bottom-actions">
      <view class="bottom-btn primary" @tap="createFamily">创建家庭</view>
      <view class="bottom-btn secondary" @tap="joinFamily">加入家庭</view>
    </view>
  </view>
</template>

<script>
import { familyApi, setCurrentFamilyId, getCurrentFamilyId } from '../../utils/api.js'

export default {
  data() {
    return {
      families: [],
      currentFamily: null
    }
  },
  onShow() {
    this.loadFamilies()
  },
  methods: {
    async loadFamilies() {
      try {
        const [list, current] = await Promise.all([
          familyApi.list(),
          familyApi.current()
        ])
        this.families = list || []
        this.currentFamily = current || (this.families.length > 0 ? this.families[0] : null)

        // 同步本地存储的 currentFamilyId
        if (this.currentFamily) {
          setCurrentFamilyId(this.currentFamily.id)
        }
      } catch (e) {
        console.error('加载家庭失败', e)
      }
    },

    async switchFamily(familyId) {
      if (this.currentFamily && this.currentFamily.id === familyId) return
      try {
        const detail = await familyApi.switch({ familyId })
        setCurrentFamilyId(familyId)
        this.currentFamily = detail
        uni.showToast({ title: '已切换家庭', icon: 'none' })
      } catch (e) {
        console.error('切换家庭失败', e)
      }
    },

    createFamily() {
      uni.showModal({
        title: '创建家庭',
        editable: true,
        placeholderText: '例如：四口之家',
        success: async (res) => {
          if (!res.confirm) return
          const name = (res.content || '').trim()
          if (!name) return uni.showToast({ title: '请输入家庭名', icon: 'none' })
          try {
            await familyApi.create({ name })
            uni.showToast({ title: '创建成功', icon: 'none' })
            await this.loadFamilies()
          } catch (e) {
            console.error('创建家庭失败', e)
          }
        }
      })
    },

    joinFamily() {
      uni.showModal({
        title: '加入家庭',
        editable: true,
        placeholderText: '请输入邀请码',
        success: async (res) => {
          if (!res.confirm) return
          const code = (res.content || '').trim()
          if (!code) return uni.showToast({ title: '请输入邀请码', icon: 'none' })
          try {
            await familyApi.join({ inviteCode: code })
            await this.loadFamilies()
            uni.showToast({ title: '加入成功', icon: 'none' })
          } catch (e) {
            console.error('加入家庭失败', e)
          }
        }
      })
    },

    copyInviteCode() {
      if (!this.currentFamily) return
      uni.setClipboardData({
        data: this.currentFamily.inviteCode,
        success: () => uni.showToast({ title: '邀请码已复制', icon: 'none' })
      })
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-bg);
  padding: 24rpx 24rpx 180rpx;
}
.header { margin-bottom: 20rpx; }
.title { display: block; font-size: 38rpx; font-weight: 700; color: var(--color-text); }
.subtitle { display: block; margin-top: 8rpx; font-size: 24rpx; color: var(--color-text-secondary); }
.section { margin-bottom: 20rpx; }
.section-title { display: block; font-size: 30rpx; font-weight: 600; color: var(--color-text); margin-bottom: 16rpx; }
.family-current { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.family-main { display: flex; flex-direction: column; }
.family-name { font-size: 30rpx; font-weight: 600; color: var(--color-text); }
.family-meta { margin-top: 6rpx; font-size: 22rpx; color: var(--color-text-secondary); }
.family-badge { font-size: 22rpx; color: var(--color-primary); background: var(--color-primary-soft); padding: 6rpx 14rpx; border-radius: 999rpx; }
.actions { margin-top: 10rpx; }
.action-btn { display: inline-flex; padding: 12rpx 18rpx; border-radius: 12rpx; font-size: 24rpx; }
.action-btn.secondary { color: var(--color-primary); border: 2rpx solid var(--color-primary); }
.family-item { display: flex; justify-content: space-between; align-items: center; padding: 18rpx 0; border-bottom: 1rpx solid var(--color-border); }
.family-item:last-child { border-bottom: none; }
.family-left { display: flex; flex-direction: column; }
.family-item-name { font-size: 28rpx; color: var(--color-text); }
.family-item-meta { margin-top: 6rpx; font-size: 22rpx; color: var(--color-text-muted); }
.switch-btn { font-size: 22rpx; color: var(--color-primary); padding: 8rpx 14rpx; border-radius: 999rpx; border: 2rpx solid var(--color-primary); }
.switch-btn.active { color: #fff; background: var(--color-primary); }
.member-item { display: flex; justify-content: space-between; align-items: center; padding: 14rpx 0; border-bottom: 1rpx solid var(--color-border); }
.member-item:last-child { border-bottom: none; }
.member-name { font-size: 26rpx; color: var(--color-text); }
.member-role { font-size: 22rpx; color: var(--color-text-secondary); }
.hint { display: block; margin-top: 12rpx; font-size: 22rpx; color: var(--color-text-muted); }
.bottom-actions { position: fixed; left: 24rpx; right: 24rpx; bottom: 24rpx; display: flex; gap: 16rpx; }
.bottom-btn { flex: 1; text-align: center; padding: 22rpx 0; border-radius: 16rpx; font-size: 28rpx; font-weight: 500; }
.bottom-btn.primary { background: var(--color-primary); color: #fff; }
.bottom-btn.secondary { background: #fff; color: var(--color-primary); border: 2rpx solid var(--color-primary); }
</style>
