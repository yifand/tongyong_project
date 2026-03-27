<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { useUserStore } from '@/stores/modules/user'

  const router = useRouter()
  const userStore = useUserStore()
  const loading = ref(false)

  const loginForm = reactive({
    username: '',
    password: '',
    remember: false
  })

  const loginRules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }

  const loginFormRef = ref()

  async function handleLogin() {
    await loginFormRef.value.validate()
    loading.value = true
    try {
      await userStore.loginAction({
        username: loginForm.username,
        password: loginForm.password
      })
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error) {
      ElMessage.error('登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  }
</script>

<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <h1 class="title">PDI智能监测平台</h1>
        <p class="subtitle">欢迎回来，请登录您的账号</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <div class="login-options">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .login-page {
    width: 100%;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);

    .login-box {
      width: 400px;
      padding: 40px;
      background: rgba(255, 255, 255, 0.05);
      backdrop-filter: blur(10px);
      border-radius: 16px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);

      .login-header {
        text-align: center;
        margin-bottom: 32px;

        .title {
          font-size: 28px;
          font-weight: 600;
          color: #fff;
          margin-bottom: 8px;
        }

        .subtitle {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.6);
        }
      }

      .login-form {
        .login-options {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 24px;

          .forgot-password {
            color: var(--pdi-primary);
            font-size: 14px;
            text-decoration: none;

            &:hover {
              color: var(--pdi-primary-light);
            }
          }
        }

        .login-btn {
          width: 100%;
          font-size: 16px;
        }
      }
    }
  }
</style>
