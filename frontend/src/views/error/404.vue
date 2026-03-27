<script setup lang="ts">
  import { useRouter } from 'vue-router'

  const router = useRouter()

  function goBack() {
    router.back()
  }

  function goHome() {
    router.push('/')
  }
</script>

<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-title">页面不存在</div>
      <div class="error-desc">抱歉，您访问的页面不存在或已被移除</div>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">返回首页</el-button>
        <el-button size="large" @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .error-page {
    width: 100%;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--el-bg-color-page);

    .error-content {
      text-align: center;

      .error-code {
        font-size: 160px;
        font-weight: 700;
        color: var(--el-color-primary);
        line-height: 1;
        margin-bottom: 24px;
      }

      .error-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        margin-bottom: 16px;
      }

      .error-desc {
        font-size: 16px;
        color: var(--el-text-color-secondary);
        margin-bottom: 32px;
      }

      .error-actions {
        .el-button {
          margin: 0 8px;
        }
      }
    }
  }
</style>
