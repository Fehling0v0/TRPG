<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <h1>PC档案夹</h1>
        <div class="brand-rule"></div>
        <p>注册新账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="邀请码" prop="invite_code">
          <el-input v-model="form.invite_code" placeholder="请输入邀请码" :prefix-icon="Key" />
        </el-form-item>
        <el-button
          type="primary"
          class="submit-btn"
          :loading="loading"
          @click="handleRegister"
        >注 册</el-button>
      </el-form>

      <div class="auth-foot">
        已有账号？<router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { authApi } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  invite_code: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  invite_code: [{ required: true, message: '请输入邀请码', trigger: 'blur' }]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
    loading.value = true
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(1000px 500px at 50% -8%, #ffffff 0%, rgba(255, 255, 255, 0) 70%),
    var(--bg-page);
  padding: 16px;
}

.auth-card {
  width: 400px;
  max-width: 100%;
  background: var(--bg-card);
  border: 1px solid var(--hairline);
  border-radius: 16px;
  padding: 48px 40px 36px;
  box-shadow: 0 16px 48px rgba(26, 25, 24, 0.06);
}

.auth-brand {
  text-align: center;
  margin-bottom: 36px;
}

.auth-brand h1 {
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 8px;
  margin-right: -8px;
  color: var(--ink);
}

.brand-rule {
  width: 32px;
  height: 2px;
  background: var(--primary);
  margin: 18px auto;
  border-radius: 1px;
}

.auth-brand p {
  font-size: 13px;
  letter-spacing: 3px;
  margin-right: -3px;
  color: var(--text-secondary);
}

.auth-card :deep(.el-form-item) {
  margin-bottom: 22px;
}

.auth-card :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-regular);
  letter-spacing: 0.5px;
  padding-bottom: 8px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  letter-spacing: 4px;
  text-indent: 4px;
  margin-top: 4px;
}

.auth-foot {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 28px;
}

.auth-foot a {
  color: var(--primary);
  transition: color 0.2s;
}

.auth-foot a:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

@media (max-width: 480px) {
  .auth-card {
    padding: 36px 24px 28px;
  }
}
</style>
