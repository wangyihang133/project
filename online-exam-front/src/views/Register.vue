<template>
  <div style="padding: 24px; max-width: 360px; margin: 0 auto">
    <h2>注册</h2>
    <input v-model="username" placeholder="用户名" style="width: 100%; margin: 8px 0" />
    <input v-model="password" @input="clearMsg" type="password" placeholder="密码" style="width: 100%; margin: 8px 0" />
    <input v-model="confirmPassword" @input="clearMsg" type="password" placeholder="确认密码" style="width: 100%; margin: 8px 0" />
    <input v-model="phone" @input="clearMsg" placeholder="电话" style="width: 100%; margin: 8px 0" />
    <input v-model="email" @input="clearMsg" placeholder="邮箱" style="width: 100%; margin: 8px 0" />

    <!-- 注册默认为考生，移除角色选择 -->

    <div v-if="errorMessage" style="color:#b00020;margin:8px 0">{{ errorMessage }}</div>
    <div v-if="successMessage" style="color:#0b8043;margin:8px 0">{{ successMessage }}</div>
    <button @click="register" style="width: 100%; margin-top: 8px">注册</button>

    <p style="margin-top: 12px">
      已有账号？<RouterLink to="/login">去登录</RouterLink>
    </p>
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
