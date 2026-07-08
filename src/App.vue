<script>
export default {
  onLaunch() {
    console.log('家庭菜单小程序启动')
  }
}
</script>

<style>
page {
  --color-primary: #ff6b35;
  --color-primary-soft: #fff3ee;
  --color-success: #22c55e;
  --color-danger: #ef4444;
  --color-text: #1f2937;
  --color-text-secondary: #6b7280;
  --color-text-muted: #9ca3af;
  --color-bg: #f5f7fa;
  --color-card: #ffffff;
  --color-border: #e5e7eb;
  --radius-md: 16rpx;
  --radius-lg: 20rpx;
  background-color: var(--color-bg);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: var(--color-text);
  font-size: 28rpx;
}

.container {
  padding: 24rpx;
}

/* 通用按钮样式 */
.btn-primary {
  background-color: var(--color-primary);
  color: #ffffff;
  border: none;
  border-radius: var(--radius-md);
  padding: 20rpx 40rpx;
  font-size: 30rpx;
  text-align: center;
}

.btn-primary:active {
  opacity: 0.85;
}

.btn-outline {
  background-color: var(--color-card);
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
  border-radius: var(--radius-md);
  padding: 20rpx 40rpx;
  font-size: 30rpx;
  text-align: center;
}

/* 卡片样式 */
.card {
  background-color: var(--color-card);
  border-radius: var(--radius-lg);
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.ui-card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--color-text);
}

.ui-card-subtitle {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}
</style>
