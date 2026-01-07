<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>

      <div class="input-group">
        <label class="input-label" for="username">用户名</label>
        <input
          id="username"
          v-model="username"
          @input="clearMsg"
          placeholder="请输入用户名"
          class="input-field"
        />
      </div>

      <div class="input-group">
        <label class="input-label" for="password">密码</label>
        <input
          id="password"
          v-model="password"
          @input="clearMsg"
          type="password"
          placeholder="请输入密码（至少6位）"
          class="input-field"
        />
      </div>

      <div class="input-group">
        <label class="input-label" for="confirmPassword">确认密码</label>
        <input
          id="confirmPassword"
          v-model="confirmPassword"
          @input="clearMsg"
          type="password"
          placeholder="请再次输入密码"
          class="input-field"
        />
      </div>

      <div class="input-group">
        <label class="input-label" for="phone">电话</label>
        <input
          id="phone"
          v-model="phone"
          @input="clearMsg"
          placeholder="选填：11位手机号"
          class="input-field"
        />
      </div>

      <div class="input-group">
        <label class="input-label" for="email">邮箱</label>
        <input
          id="email"
          v-model="email"
          @input="clearMsg"
          placeholder="选填：常用邮箱"
          class="input-field"
        />
      </div>

      <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

      <button @click="register" class="register-btn">注册</button>

      <div class="login-link">
        已有账号？<RouterLink to="/login" class="link-text">去登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import http from "../api/http";
import type { UserRole } from "../stores/user";

const router = useRouter();

const username = ref("");
const password = ref("");
const confirmPassword = ref("");
const role = ref<UserRole>("STUDENT");
const errorMessage = ref("");
const successMessage = ref("");
const phone = ref("");
const email = ref("");

function clearMsg() {
  errorMessage.value = ''
  successMessage.value = ''
}

const register = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!username.value) {
    errorMessage.value = '用户名不能为空'
    return
  }
  if (!password.value || password.value.length < 6) {
    errorMessage.value = '密码长度至少6位'
    return
  }
  // phone: optional but if provided must be 11 digits (trim whitespace first)
  if (phone.value && !/^\d{11}$/.test(phone.value.trim())) {
    errorMessage.value = '电话格式错误，应为11位数字'
    return
  }
  // email: optional but if provided must be simple valid
  if (email.value && !/^\S+@\S+\.\S+$/.test(email.value)) {
    errorMessage.value = '邮箱格式错误'
    return
  }
  if (password.value !== confirmPassword.value) {
    errorMessage.value = '两次密码不一致'
    return
  }

  try {
    const res = await http.post<string>('/user/register', {
      username: username.value,
      password: password.value,
      role: role.value,
      phone: phone.value,
      email: email.value,
    })
    // success
    successMessage.value = '注册成功，跳转到登录...'
    setTimeout(() => router.push('/login'), 1000)
  } catch (err: any) {
    const resp = err?.response
    if (resp) {
      if (resp.status === 409) {
        // backend may return specific conflict codes
        if (resp.data === 'PHONE_EXISTS') {
          errorMessage.value = '该电话已被注册'
          return
        }
        if (resp.data === 'EMAIL_EXISTS') {
          errorMessage.value = '该邮箱已被注册'
          return
        }
        // fallback: username exists or generic conflict
        errorMessage.value = '用户名已存在'
        return
      }
      if (resp.status === 400) {
        if (resp.data === 'PASSWORD_TOO_SHORT') {
          errorMessage.value = '密码长度至少6位'
          return
        }
        if (resp.data === 'INVALID_PHONE') {
          errorMessage.value = '电话格式错误，应为11位数字'
          return
        }
        if (resp.data === 'INVALID_EMAIL') {
          errorMessage.value = '邮箱格式错误'
          return
        }
      }
    }
    errorMessage.value = '注册失败'
  }
}
</script>

<style scoped>
.register-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  z-index: 10;
  padding: 20px;
  box-sizing: border-box;
}

.register-card {
  width: 100%;
  max-width: 380px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 28px 24px 24px;
  box-sizing: border-box;
}

.register-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  text-align: center;
}

.input-group {
  margin-bottom: 14px;
}

.input-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #4a5568;
}

.input-field {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #2d3748;
  background-color: rgba(255, 255, 255, 0.85);
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.input-field:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 2px rgba(66, 153, 225, 0.12);
  background-color: #ffffff;
}

.input-field::placeholder {
  color: #a0aec0;
}

.error-message {
  color: #e53e3e;
  margin: 4px 0 8px 0;
  font-size: 13px;
  padding: 8px 10px;
  background-color: rgba(229, 62, 62, 0.08);
  border-radius: 6px;
  line-height: 1.4;
}

.success-message {
  color: #38a169;
  margin: 4px 0 8px 0;
  font-size: 13px;
  padding: 8px 10px;
  background-color: rgba(56, 161, 105, 0.08);
  border-radius: 6px;
  line-height: 1.4;
}

.register-btn {
  width: 100%;
  margin-top: 6px;
  padding: 11px;
  background-color: #4299e1;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

.register-btn:hover {
  background-color: #3182ce;
  box-shadow: 0 4px 14px rgba(49, 130, 206, 0.35);
}

.login-link {
  margin-top: 16px;
  font-size: 14px;
  color: #718096;
  text-align: center;
}

.link-text {
  color: #4299e1;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.link-text:hover {
  color: #3182ce;
  text-decoration: underline;
}

@media (max-width: 480px) {
  .register-card {
    padding: 24px 20px 20px;
  }

  .register-title {
    font-size: 18px;
  }
}
</style>
