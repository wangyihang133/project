<template>
  <div style="padding: 24px; max-width: 360px; margin: 0 auto">
    <h2>登录</h2>
    <input v-model="username" placeholder="用户名" style="width: 100%; margin: 8px 0" />
    <input v-model="password" type="password" placeholder="密码" style="width: 100%; margin: 8px 0" />
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

type LoginResp = {
  token: string;
  username: string;
  role: UserRole;
};

const login = async () => {
  // 你后端如果暂时只返回 true/false，可以先按 boolean 处理
  const res = await http.post<LoginResp | boolean>("/user/login", {
    username: username.value,
    password: password.value,
  });

  // 兼容两种返回
  if (typeof res.data === "boolean") {
    if (!res.data) {
      alert("登录失败");
      return;
    }
    alert("登录成功（后端未返回角色/token，后续再补）");
    router.push("/student");
    return;
  }

  userStore.setLogin(res.data);
  if (res.data.role === "STUDENT") router.push("/student");
  if (res.data.role === "ADMIN") router.push("/admin");
  if (res.data.role === "RECRUIT") router.push("/recruit");
};
</script>
