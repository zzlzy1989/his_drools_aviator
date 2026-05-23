<template>
  <div class="his-login">
    <div class="his-login__left">
      <div class="his-login__brand">
        <span class="his-login__brand-icon">HIS</span>
        <h1 class="his-login__brand-title">规则中台</h1>
      </div>
      <p class="his-login__tagline">Dynamic Rule Engine for Healthcare Intelligence</p>
      <div class="his-login__features">
        <div class="his-login__feature">
          <span class="his-login__feature-dot" />
          <span>Drools 规则引擎 · 实时决策</span>
        </div>
        <div class="his-login__feature">
          <span class="his-login__feature-dot" />
          <span>Aviator 公式计算 · 精准结算</span>
        </div>
        <div class="his-login__feature">
          <span class="his-login__feature-dot" />
          <span>DRG 分组 · 质量控制 · 用药审核</span>
        </div>
      </div>
    </div>
    <div class="his-login__right">
      <div class="his-login__form-wrapper">
        <h2 class="his-login__form-title">登录</h2>
        <p class="his-login__form-subtitle">登录到 HIS 规则中台管理后台</p>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="his-login__form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="his-login__submit"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="his-login__hint">
          <span>默认账号: admin / admin</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { loginApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin',
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await loginApi({
          username: loginForm.username,
          password: loginForm.password,
        })
        if (result.token) {
          userStore.setLoginInfo({
            token: result.token,
            userId: result.userId,
            username: result.username,
            realName: result.realName,
            tenantId: result.tenantId,
            role: result.role,
          })
          ElMessage.success(`欢迎回来，${result.realName || result.username}`)
          router.push('/')
        }
      } catch (error: any) {
        const msg = error?.response?.data?.message || error?.message || '登录请求失败'
        ElMessage.error(msg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>

.his-login {
  min-height: 100vh;
  display: flex;
}

.his-login__left {
  flex: 1;
  background-color: $color-inverse-canvas;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: $spacing-xxl $spacing-section;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -20%;
    right: -10%;
    width: 400px;
    height: 400px;
    background-color: $color-primary;
    opacity: 0.08;
    border-radius: 50%;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -15%;
    right: 5%;
    width: 300px;
    height: 300px;
    background-color: $color-primary-deep;
    opacity: 0.06;
    border-radius: 50%;
  }
}

.his-login__brand {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.his-login__brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background-color: $color-primary;
  color: $color-on-primary;
  border-radius: $radius-md;
  font-size: $font-size-body-lg;
  font-weight: $font-weight-semibold;
  letter-spacing: 1px;
}

.his-login__brand-title {
  color: $color-inverse-ink;
  font-size: $font-size-display-lg;
  font-weight: $font-weight-light;
  margin: 0;
}

.his-login__tagline {
  color: $color-inverse-ink-muted;
  font-size: $font-size-body-lg;
  margin: 0 0 $spacing-xl 0;
  letter-spacing: $letter-spacing-body;
}

.his-login__features {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.his-login__feature {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  color: $color-inverse-ink-muted;
  font-size: $font-size-body;
  letter-spacing: $letter-spacing-body;
}

.his-login__feature-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: $color-primary;
  flex-shrink: 0;
}

.his-login__right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $color-canvas;
  padding: $spacing-xxl;
}

.his-login__form-wrapper {
  width: 100%;
  max-width: 360px;
}

.his-login__form-title {
  font-size: $font-size-headline;
  font-weight: $font-weight-regular;
  color: $color-ink;
  margin: 0 0 $spacing-xxs 0;
}

.his-login__form-subtitle {
  font-size: $font-size-body;
  color: $color-ink-muted;
  margin: 0 0 $spacing-xl 0;
}

.his-login__form {
  .el-form-item {
    margin-bottom: $spacing-md;
  }
}

.his-login__submit {
  width: 100%;
  height: 48px;
  font-size: $font-size-body;
  font-weight: $font-weight-regular;
  letter-spacing: $letter-spacing-body;
}

.his-login__hint {
  text-align: center;
  margin-top: $spacing-lg;
  color: $color-ink-muted;
  font-size: $font-size-caption;
  letter-spacing: $letter-spacing-caption;
}
</style>
