<template>
  <view class="page">
    <view class="form">
      <!-- 菜名 -->
      <view class="form-item">
        <text class="form-label">菜名 *</text>
        <input
          class="form-input"
          placeholder="例如：红烧排骨"
          v-model="form.name"
          maxlength="20"
        />
      </view>

      <!-- 分类 -->
      <view class="form-item">
        <text class="form-label">分类 *</text>
        <view class="category-options">
          <view
            class="cat-option"
            :class="{ active: form.categoryCode === cat.code }"
            v-for="cat in categories"
            :key="cat.id"
            @tap="form.categoryCode = cat.code"
          >
            <text class="cat-option-name">{{ cat.name }}</text>
          </view>
        </view>
      </view>

      <!-- 难度 -->
      <view class="form-item">
        <text class="form-label">难度</text>
        <view class="diff-options">
          <view
            class="diff-option"
            :class="{ active: form.difficulty === d.value }"
            v-for="d in difficulties"
            :key="d.value"
            @tap="form.difficulty = d.value"
          >
            <text>{{ d.label }}</text>
          </view>
        </view>
      </view>

      <!-- 食材 -->
      <view class="form-item">
        <text class="form-label">食材</text>
        <view class="ingredient-list">
          <view class="ingredient-tag" v-for="(ing, idx) in form.ingredients" :key="idx">
            <text>{{ ing }}</text>
            <text class="ingredient-remove" @tap="removeIngredient(idx)">✕</text>
          </view>
          <view class="ingredient-add" v-if="!showIngredientInput" @tap="showIngredientInput = true">
            <text>+ 添加食材</text>
          </view>
        </view>
        <view class="ingredient-input-wrap" v-if="showIngredientInput">
          <input
            class="form-input"
            placeholder="输入食材名称"
            v-model="ingredientInput"
            @confirm="addIngredient"
            focus
          />
          <view class="ingredient-confirm" @tap="addIngredient">
            <text>添加</text>
          </view>
        </view>
      </view>

      <!-- 菜品图片 -->
      <view class="form-item">
        <text class="form-label">菜品图片</text>
        <!-- 上传中 -->
        <view class="image-uploading" v-if="uploading">
          <text class="image-uploading-text">图片上传中...</text>
        </view>
        <!-- 未选择图片 -->
        <view class="image-upload" v-else-if="!form.image" @tap="chooseDishImage">
          <text class="image-upload-icon">📷</text>
          <text class="image-upload-text">上传菜品图片</text>
          <text class="image-upload-tip">支持拍照或从相册选择</text>
        </view>
        <!-- 已选择图片 -->
        <view class="image-preview-wrap" v-else>
          <image class="dish-image-preview" :src="form.image" mode="aspectFill" @tap="previewDishImage" />
          <view class="image-actions">
            <view class="image-action-btn" @tap="chooseDishImage">
              <text>重新选择</text>
            </view>
            <view class="image-action-btn danger" @tap="removeDishImage">
              <text>删除图片</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-item">
        <text class="form-label">备注</text>
        <textarea
          class="form-textarea"
          placeholder="简单记录做法或注意事项（选填）"
          v-model="form.remark"
          maxlength="200"
          :auto-height="true"
        />
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-bar">
      <view class="submit-btn" @tap="submit">
        <text>{{ isEdit ? '保存修改' : '添加菜品' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { categoryApi, dishApi, fileApi, getImageUrl } from '../../utils/api.js'

export default {
  data() {
    return {
      isEdit: false,
      editId: '',
      loading: false,
      uploading: false,
      form: {
        name: '',
        categoryCode: 'stir-fry',
        difficulty: 'normal',
        ingredients: [],
        image: '',      // 预览用（本地路径或完整 URL）
        imageUrl: '',   // 服务器返回的相对路径
        remark: ''
      },
      categories: [],
      difficulties: [
        { label: '⚡ 快手菜', value: 'quick' },
        { label: '👨‍🍳 普通', value: 'normal' },
        { label: '⏰ 费时', value: 'hard' }
      ],
      ingredientInput: '',
      showIngredientInput: false
    }
  },
  async onLoad(options) {
    try {
      this.categories = (await categoryApi.list()) || []
    } catch (e) {
      this.categories = []
    }

    if (options.id) {
      this.isEdit = true
      this.editId = options.id
      try {
        const dish = await dishApi.detail(options.id)
        if (dish) {
          this.form = {
            name: dish.name,
            categoryCode: dish.categoryCode,
            difficulty: dish.difficulty || 'normal',
            ingredients: dish.ingredients ? [...dish.ingredients] : [],
            image: getImageUrl(dish.imageUrl),
            imageUrl: dish.imageUrl || '',
            remark: dish.remark || ''
          }
        }
      } catch (e) {
        console.error('加载菜品失败', e)
      }
      uni.setNavigationBarTitle({ title: '编辑菜品' })
    }
  },
  methods: {
    addIngredient() {
      const val = this.ingredientInput.trim()
      if (!val) return
      if (this.form.ingredients.includes(val)) {
        uni.showToast({ title: '已添加该食材', icon: 'none' })
        return
      }
      this.form.ingredients.push(val)
      this.ingredientInput = ''
    },

    removeIngredient(idx) {
      this.form.ingredients.splice(idx, 1)
    },

    chooseDishImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const [filePath] = res.tempFilePaths || []
          if (!filePath) return
          this.uploading = true
          try {
            const relativePath = await fileApi.upload(filePath)
            this.form.imageUrl = relativePath
            this.form.image = getImageUrl(relativePath)
          } catch (e) {
            console.error('图片上传失败', e)
          } finally {
            this.uploading = false
          }
        }
      })
    },

    previewDishImage() {
      if (!this.form.image) return
      uni.previewImage({
        current: this.form.image,
        urls: [this.form.image]
      })
    },

    removeDishImage() {
      this.form.image = ''
      this.form.imageUrl = ''
    },

    async submit() {
      if (!this.form.name.trim()) {
        uni.showToast({ title: '请输入菜名', icon: 'none' })
        return
      }

      const payload = {
        name: this.form.name.trim(),
        categoryCode: this.form.categoryCode,
        difficulty: this.form.difficulty,
        ingredients: this.form.ingredients,
        imageUrl: this.form.imageUrl || undefined,
        remark: this.form.remark || undefined
      }

      this.loading = true
      try {
        if (this.isEdit) {
          await dishApi.update(this.editId, payload)
          uni.showToast({ title: '修改成功', icon: 'success' })
        } else {
          await dishApi.add(payload)
          uni.showToast({ title: '添加成功', icon: 'success' })
        }
        setTimeout(() => uni.navigateBack(), 800)
      } catch (e) {
        console.error('提交菜品失败', e)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 160rpx;
}

.form {
  padding: 24rpx;
}

.form-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}

.form-label {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

.form-input {
  width: 100%;
  font-size: 30rpx;
  color: #333;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #f0f0f0;
}

.form-textarea {
  width: 100%;
  font-size: 28rpx;
  color: #333;
  padding: 16rpx 0;
  min-height: 80rpx;
}

/* 分类选项 */
.category-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 12rpx;
}

.cat-option {
  display: flex;
  align-items: center;
  padding: 16rpx 28rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
}

.cat-option.active {
  background: #fff3ee;
  border-color: #ff6b35;
}

.cat-option-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
}

.cat-option-name {
  font-size: 26rpx;
  color: #333;
}

.cat-option.active .cat-option-name {
  color: #ff6b35;
}

/* 难度选项 */
.diff-options {
  display: flex;
  gap: 16rpx;
  margin-top: 12rpx;
}

.diff-option {
  flex: 1;
  text-align: center;
  padding: 18rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #666;
  border: 2rpx solid transparent;
}

.diff-option.active {
  background: #fff3ee;
  border-color: #ff6b35;
  color: #ff6b35;
}

/* 食材 */
.ingredient-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 12rpx;
}

.ingredient-tag {
  display: flex;
  align-items: center;
  background: #f0f7ff;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #4a90d9;
}

.ingredient-remove {
  margin-left: 8rpx;
  font-size: 22rpx;
  color: #999;
}

.ingredient-add {
  padding: 10rpx 20rpx;
  border: 2rpx dashed #ccc;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #999;
}

.ingredient-input-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
}

.ingredient-input-wrap .form-input {
  flex: 1;
}

.ingredient-confirm {
  padding: 16rpx 28rpx;
  background: #ff6b35;
  border-radius: 12rpx;
}

.ingredient-confirm text {
  font-size: 26rpx;
  color: #fff;
}

/* 图片上传 */
.image-uploading {
  border: 2rpx dashed #d1d5db;
  border-radius: 16rpx;
  padding: 36rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
}

.image-uploading-text {
  font-size: 28rpx;
  color: #999;
}

.image-upload {
  border: 2rpx dashed #d1d5db;
  border-radius: 16rpx;
  padding: 36rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-upload-icon {
  font-size: 52rpx;
}

.image-upload-text {
  margin-top: 10rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.image-upload-tip {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #999;
}

.image-preview-wrap {
  display: flex;
  flex-direction: column;
}

.dish-image-preview {
  width: 100%;
  height: 320rpx;
  border-radius: 16rpx;
  background: #f3f4f6;
}

.image-actions {
  margin-top: 16rpx;
  display: flex;
  gap: 16rpx;
}

.image-action-btn {
  flex: 1;
  text-align: center;
  padding: 16rpx;
  border-radius: 12rpx;
  background: #fff3ee;
  color: #ff6b35;
  font-size: 26rpx;
}

.image-action-btn.danger {
  background: #feeeee;
  color: #ef4444;
}

/* 提交 */
.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 40rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.submit-btn {
  background: #ff6b35;
  color: #fff;
  text-align: center;
  padding: 28rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 500;
}

.submit-btn:active {
  opacity: 0.85;
}
</style>
