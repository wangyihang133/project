<template>
  <div style="padding: 24px; max-width: 360px; margin: 0 auto">
    <h2>登录</h2>
    <input v-model="username" @input="clearError" placeholder="用户名" style="width: 100%; margin: 8px 0" />
    <input v-model="password" @input="clearError" type="password" placeholder="密码" style="width: 100%; margin: 8px 0" />
    <div v-if="errorMessage" style="color: #b00020; margin: 8px 0; font-size: 14px">{{ errorMessage }}</div>
    <button @click="login" style="width: 100%; margin-top: 8px">登录</button>

    <p style="margin-top: 12px">
      没有账号？<RouterLink to="/register">去注册</RouterLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import http from "../api/http";
import { useUserStore, type UserRole } from "../stores/user";

const router = useRouter();
const userStore = useUserStore();

const username = ref("");
const password = ref("");
const errorMessage = ref("");

type LoginResp = {
  token: string;
  username: string;
  role: UserRole;
};

const login = async () => {
  try {
    errorMessage.value = ''
    const res = await http.post<LoginResp>('/user/login', {
      username: username.value,
      password: password.value,
    });

    // success
    errorMessage.value = ''
    userStore.setLogin(res.data);
    if (res.data.role === 'STUDENT') router.push('/student');
    if (res.data.role === 'ADMIN') router.push('/admin');
    if (res.data.role === 'RECRUIT') router.push('/recruit');
  } catch (err: any) {
    // axios error handling
    const resp = err?.response
    if (resp) {
      if (resp.status === 404) {
        errorMessage.value = '账号不存在'
        return
      }
      if (resp.status === 401) {
        errorMessage.value = '密码错误'
        return
      }
    }
    errorMessage.value = '登录失败'
  }
}

function clearError() {
  errorMessage.value = ''
}
</script>
