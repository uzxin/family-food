<template>
  <view class="page">
    <!-- 装饰背景 -->
    <view class="bg-decor">
      <view class="blob blob-1"></view>
      <view class="blob blob-2"></view>
      <view class="blob blob-3"></view>
    </view>

    <!-- 顶部品牌区 -->
    <view class="brand-area">
      <view class="logo-wrap">
        <text class="logo-emoji">🍲</text>
      </view>
      <text class="brand-title">家庭点菜</text>
      <text class="brand-slogan">今天吃什么，一起决定</text>
    </view>

    <!-- 主卡片 -->
    <view class="glass-card">
      <!-- Tab 切换 -->
      <view class="tab-switcher">
        <view class="tab-indicator" :class="{ 'tab-indicator-right': mode === 'register' }"></view>
        <view class="tab-item" :class="{ active: mode === 'login' }" @tap="mode = 'login'">
          <text>登录</text>
        </view>
        <view class="tab-item" :class="{ active: mode === 'register' }" @tap="mode = 'register'">
          <text>注册</text>
        </view>
      </view>

      <!-- 登录表单 -->
      <view v-if="mode === 'login'" class="form-area">
        <view class="input-group">
          <text class="input-icon">👤</text>
          <input
            class="form-input"
            placeholder="请输入用户名"
            placeholder-class="placeholder-style"
            v-model="loginForm.username"
            maxlength="20"
          />
        </view>
        <view class="input-group">
          <text class="input-icon">🔒</text>
          <input
            class="form-input"
            :type="showLoginPwd ? 'text' : 'password'"
            placeholder="请输入密码"
            placeholder-class="placeholder-style"
            v-model="loginForm.password"
            maxlength="20"
          />
          <text class="pwd-toggle" @tap="showLoginPwd = !showLoginPwd">{{ showLoginPwd ? '🙈' : '👁️' }}</text>
        </view>
        <view class="submit-btn" :class="{ disabled: loading }" @tap="handleLogin">
          <text class="btn-text">{{ loading ? '登录中...' : '登 录' }}</text>
        </view>
      </view>

      <!-- 注册表单 -->
      <view v-else class="form-area">
        <view class="input-group">
          <text class="input-icon">😊</text>
          <input
            class="form-input"
            placeholder="你的昵称，如：妈妈"
            placeholder-class="placeholder-style"
            v-model="regForm.nickname"
            maxlength="10"
          />
        </view>
        <view class="input-group">
          <text class="input-icon">👤</text>
          <input
            class="form-input"
            placeholder="用户名，4-20 个字符"
            placeholder-class="placeholder-style"
            v-model="regForm.username"
            maxlength="20"
          />
        </view>
        <view class="input-group">
          <text class="input-icon">🔒</text>
          <input
            class="form-input"
            :type="showRegPwd ? 'text' : 'password'"
            placeholder="设置密码，6-20 位"
            placeholder-class="placeholder-style"
            v-model="regForm.password"
            maxlength="20"
          />
          <text class="pwd-toggle" @tap="showRegPwd = !showRegPwd">{{ showRegPwd ? '🙈' : '👁️' }}</text>
        </view>
        <view class="submit-btn" :class="{ disabled: loading }" @tap="handleRegister">
          <text class="btn-text">{{ loading ? '注册中...' : '注 册' }}</text>
        </view>
      </view>
    </view>

    <!-- 微信登录区 -->
    <view class="wx-section">
      <view class="divider">
        <view class="divider-line"></view>
        <text class="divider-text">其他登录方式</text>
        <view class="divider-line"></view>
      </view>
      <view class="wx-btn" :class="{ disabled: loading }" @tap="handleWxLogin">
        <view class="wx-icon-circle">
          <text class="wx-icon-text">💬</text>
        </view>
        <text class="wx-btn-text">{{ loading ? '登录中...' : '微信一键登录' }}</text>
      </view>
    </view>

    <!-- 底部协议 -->
    <view class="footer">
      <view class="agree-row" @tap="agreed = !agreed">
        <view class="checkbox" :class="{ checked: agreed }">
          <text v-if="agreed" class="checkbox-icon">✓</text>
        </view>
        <text class="agree-text">我已阅读并同意</text>
        <text class="footer-link" @tap.stop="openAgreement('user')">《用户协议》</text>
        <text>和</text>
        <text class="footer-link" @tap.stop="openAgreement('privacy')">《隐私政策》</text>
      </view>
    </view>

    <!-- 协议弹窗 -->
    <view v-if="showAgreement" class="agreement-mask" @tap="closeAgreement">
      <view class="agreement-popup" @tap.stop>
        <view class="agreement-header">
          <text class="agreement-title">{{ agreementType === 'user' ? '用户协议' : '隐私政策' }}</text>
          <text class="agreement-close" @tap="closeAgreement">✕</text>
        </view>
        <scroll-view scroll-y class="agreement-body">
          <!-- 用户协议 -->
          <view v-if="agreementType === 'user'">
            <text class="agreement-section-title">一、服务说明</text>
            <text class="agreement-text">家庭点菜是一款为家庭用户提供菜单管理、菜品记录及点菜协作的小程序。使用本服务即表示您同意本协议各项条款。</text>

            <text class="agreement-section-title">二、用户行为规范</text>
            <text class="agreement-text">1. 您应注册真实信息，并对账号及密码安全负责。
2. 不得发布违法、骚扰、侮辱等内容。
3. 不得以任何方式破坏或干扰本服务的正常运行。</text>

            <text class="agreement-section-title">三、账号管理</text>
            <text class="agreement-text">1. 您可随时注销账号，注销后数据将被删除。
2. 如发现账号被盗用，请立即联系我们。
3. 我们有权对违规账号进行封禁处理。</text>

            <text class="agreement-section-title">四、免责声明</text>
            <text class="agreement-text">1. 因网络故障、系统维护等原因导致服务中断，我们不承担责任。
2. 您应自行备份重要数据，我们对数据丢失不承担责任。</text>

            <text class="agreement-section-title">五、协议修改</text>
            <text class="agreement-text">我们有权根据法律法规及业务发展需要修改本协议，修改后的协议自公布之日起生效。</text>
          </view>

          <!-- 隐私政策 -->
          <view v-else>
            <text class="agreement-section-title">一、信息收集</text>
            <text class="agreement-text">我们在您使用本小程序时可能收集以下信息：
1. 您注册时填写的用户名、昵称和密码。
2. 您主动创建的菜品、菜单等家庭数据。
注：本小程序不会获取您的微信昵称和头像。</text>

            <text class="agreement-section-title">二、信息使用</text>
            <text class="agreement-text">1. 收集的信息仅用于提供本小程序的功能服务。
2. 我们不会将您的信息出售或提供给第三方。
3. 除法律法规要求外，不会向任何第三方披露您的个人信息。</text>

            <text class="agreement-section-title">三、信息存储与安全</text>
            <text class="agreement-text">1. 您的数据存储在安全的服务器中，我们采取合理措施保护数据安全。
2. 我们将在实现服务目的所必需的期限内保留您的信息。</text>

            <text class="agreement-section-title">四、用户权利</text>
            <text class="agreement-text">1. 您有权访问、更正和删除您的个人信息。
2. 您可通过注销账号删除所有个人数据。
3. 如需行使上述权利，请联系我们。</text>

            <text class="agreement-section-title">五、未成年人保护</text>
            <text class="agreement-text">本小程序主要面向家庭用户。未成年人应在监护人指导下使用，我们不会主动收集未成年人个人信息。</text>

            <text class="agreement-section-title">六、联系我们</text>
            <text class="agreement-text">如对本隐私政策有任何疑问，可通过小程序内的反馈功能与我们联系。</text>
          </view>
        </scroll-view>
        <view class="agreement-footer">
          <view class="agreement-ok-btn" @tap="closeAgreement">
            <text class="agreement-ok-text">我知道了</text>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import { authApi, setToken, setUser } from '../../utils/api.js'

export default {
  data() {
    return {
      mode: 'login',
      loading: false,
      showLoginPwd: false,
      showRegPwd: false,
      loginForm: { username: '', password: '' },
      regForm: { nickname: '', username: '', password: '' },
      showAgreement: false,
      agreementType: 'user',
      agreed: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.agreed) return uni.showToast({ title: '请先勾选同意用户协议和隐私政策', icon: 'none' })
      const { username, password } = this.loginForm
      if (!username.trim()) return uni.showToast({ title: '请输入用户名', icon: 'none' })
      if (!password) return uni.showToast({ title: '请输入密码', icon: 'none' })
      this.loading = true
      try {
        const res = await authApi.login({ username, password })
        this.onLoginSuccess(res)
      } catch (e) {
        console.error('登录失败', e)
      } finally {
        this.loading = false
      }
    },

    async handleRegister() {
      if (!this.agreed) return uni.showToast({ title: '请先勾选同意用户协议和隐私政策', icon: 'none' })
      const { nickname, username, password } = this.regForm
      if (!nickname.trim()) return uni.showToast({ title: '请输入昵称', icon: 'none' })
      if (username.length < 4) return uni.showToast({ title: '用户名至少 4 位', icon: 'none' })
      if (password.length < 6) return uni.showToast({ title: '密码至少 6 位', icon: 'none' })
      this.loading = true
      try {
        await authApi.register({ nickname, username, password })
        uni.showToast({ title: '注册成功，请登录', icon: 'success' })
        this.mode = 'login'
        this.loginForm.username = username
        this.regForm = { nickname: '', username: '', password: '' }
      } catch (e) {
        console.error('注册失败', e)
      } finally {
        this.loading = false
      }
    },

    async handleWxLogin() {
      if (!this.agreed) return uni.showToast({ title: '请先勾选同意用户协议和隐私政策', icon: 'none' })
      this.loading = true
      try {
        const loginRes = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: resolve,
            fail: reject
          })
        })
        if (!loginRes || !loginRes.code) {
          uni.showToast({ title: '获取微信授权失败', icon: 'none' })
          return
        }
        const res = await authApi.wxLogin({ code: loginRes.code })
        this.onLoginSuccess(res)
      } catch (e) {
        console.error('微信登录失败', e)
        uni.showToast({ title: '获取微信授权失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    openAgreement(type) {
      this.agreementType = type
      this.showAgreement = true
    },

    closeAgreement() {
      this.showAgreement = false
    },

    onLoginSuccess(res) {
      setToken(res.token)
      setUser({ userId: res.userId, nickname: res.nickname })
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          uni.navigateBack()
        } else {
          uni.switchTab({ url: '/pages/index/index' })
        }
      }, 600)
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(160deg, #ff8a4c 0%, #ff6b35 30%, #e8542a 60%, #d63f6e 100%);
  padding: 0 48rpx;
  padding-top: 0;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

/* ===== 装饰背景 ===== */
.bg-decor {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(40rpx);
  opacity: 0.5;
}
.blob-1 {
  width: 360rpx;
  height: 360rpx;
  background: rgba(255, 200, 100, 0.6);
  top: -80rpx;
  right: -60rpx;
}
.blob-2 {
  width: 300rpx;
  height: 300rpx;
  background: rgba(255, 100, 150, 0.4);
  top: 200rpx;
  left: -100rpx;
}
.blob-3 {
  width: 400rpx;
  height: 400rpx;
  background: rgba(255, 140, 60, 0.3);
  bottom: -120rpx;
  right: -80rpx;
}

/* ===== 品牌区 ===== */
.brand-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 140rpx;
  padding-bottom: 60rpx;
  position: relative;
  z-index: 1;
}
.logo-wrap {
  width: 140rpx;
  height: 140rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.12);
}
.logo-emoji {
  font-size: 72rpx;
}
.brand-title {
  font-size: 52rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 4rpx;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}
.brand-slogan {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 2rpx;
}

/* ===== 玻璃卡片 ===== */
.glass-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(30rpx);
  -webkit-backdrop-filter: blur(30rpx);
  border-radius: 32rpx;
  padding: 40rpx 36rpx 44rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.12);
  border: 2rpx solid rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 1;
}

/* ===== Tab 切换 ===== */
.tab-switcher {
  display: flex;
  background: #f2f3f5;
  border-radius: 20rpx;
  padding: 6rpx;
  margin-bottom: 40rpx;
  position: relative;
}
.tab-indicator {
  position: absolute;
  top: 6rpx;
  left: 6rpx;
  width: calc(50% - 6rpx);
  height: calc(100% - 12rpx);
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.tab-indicator-right {
  transform: translateX(100%);
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 22rpx 0;
  font-size: 30rpx;
  font-weight: 600;
  color: #9ca3af;
  position: relative;
  z-index: 1;
  transition: color 0.3s;
}
.tab-item.active {
  color: #ff6b35;
}

/* ===== 表单输入 ===== */
.form-area {
  display: flex;
  flex-direction: column;
}
.input-group {
  display: flex;
  align-items: center;
  background: #f7f8fa;
  border-radius: 20rpx;
  padding: 0 28rpx;
  margin-bottom: 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;
}
.input-group:focus-within {
  border-color: #ff6b35;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(255, 107, 53, 0.12);
}
.input-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}
.form-input {
  flex: 1;
  padding: 28rpx 0;
  font-size: 30rpx;
  color: #1f2937;
  background: transparent;
}
.placeholder-style {
  color: #c4c9d1;
  font-size: 28rpx;
}
.pwd-toggle {
  font-size: 36rpx;
  padding: 10rpx;
  flex-shrink: 0;
}

/* ===== 提交按钮 ===== */
.submit-btn {
  margin-top: 16rpx;
  background: linear-gradient(135deg, #ff8a4c, #ff6b35);
  border-radius: 20rpx;
  padding: 30rpx;
  text-align: center;
  box-shadow: 0 12rpx 32rpx rgba(255, 107, 53, 0.4);
  transition: all 0.2s;
}
.submit-btn:active {
  transform: scale(0.98);
}
.submit-btn.disabled {
  opacity: 0.6;
}
.btn-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4rpx;
}

/* ===== 微信登录区 ===== */
.wx-section {
  margin-top: 48rpx;
  position: relative;
  z-index: 1;
}
.divider {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
}
.divider-line {
  flex: 1;
  height: 1rpx;
  background: rgba(255, 255, 255, 0.35);
}
.divider-text {
  padding: 0 24rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}
.wx-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}
.wx-btn:active {
  transform: scale(0.98);
}
.wx-btn.disabled {
  opacity: 0.6;
}
.wx-icon-circle {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #07c160;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}
.wx-icon-text {
  font-size: 28rpx;
}
.wx-btn-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

/* ===== 底部协议 ===== */
.footer {
  margin-top: auto;
  padding-top: 48rpx;
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
  position: relative;
  z-index: 1;
}
.agree-row {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}
.checkbox {
  width: 32rpx;
  height: 32rpx;
  border-radius: 8rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
  flex-shrink: 0;
  transition: all 0.2s;
}
.checkbox.checked {
  background: #ff6b35;
  border-color: #ff6b35;
}
.checkbox-icon {
  font-size: 24rpx;
  color: #fff;
  font-weight: bold;
}
.agree-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}
.footer-link {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.95);
  text-decoration: underline;
}

/* ===== 协议弹窗 ===== */
.agreement-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.agreement-popup {
  width: 86%;
  max-height: 76vh;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.agreement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 36rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.agreement-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2937;
}
.agreement-close {
  font-size: 36rpx;
  color: #9ca3af;
  padding: 4rpx 8rpx;
}
.agreement-body {
  flex: 1;
  padding: 28rpx 36rpx;
  display: flex;
  flex-direction: column;
}
.agreement-section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  margin-top: 24rpx;
  margin-bottom: 12rpx;
  display: block;
}
.agreement-section-title:first-child {
  margin-top: 0;
}
.agreement-text {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.8;
  display: block;
}
.agreement-footer {
  padding: 20rpx 36rpx 32rpx;
  border-top: 1rpx solid #f0f0f0;
}
.agreement-ok-btn {
  background: linear-gradient(135deg, #ff8a4c, #ff6b35);
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
}
.agreement-ok-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
}

</style>
