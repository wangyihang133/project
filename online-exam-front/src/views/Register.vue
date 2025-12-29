<template>
  <div style="padding: 24px; max-width: 360px; margin: 0 auto">
    <h2>注册</h2>
    <input v-model="username" placeholder="用户名" style="width: 100%; margin: 8px 0" />
    <input v-model="password" type="password" placeholder="密码" style="width: 100%; margin: 8px 0" />

    <select v-model="role" style="width: 100%; margin: 8px 0">
      <option value="STUDENT">考生</option>
      <option value="ADMIN">系统管理员</option>
      <option value="RECRUIT">招生管理员</option>
    </select>

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
const role = ref<UserRole>("STUDENT");

const register = async () => {
  const res = await http.post<string>("/api/user/register", {
    username: username.value,
    password: password.value,
    role: role.value,
  });
  alert(res.data || "注册完成");
  router.push("/login");
};
</script>
