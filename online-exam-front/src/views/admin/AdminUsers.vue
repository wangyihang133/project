<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";

const username = ref("");
const password = ref("");
// 注意：后端 users.role 是枚举：student / system_admin / recruitment_admin
const role = ref("recruitment_admin");

const msg = ref("");
const err = ref("");

async function submit() {
  msg.value = "";
  err.value = "";
  try {
    await http.post("/admin/users", { username: username.value, password: password.value, role: role.value });
    msg.value = "创建成功";
    username.value = "";
    password.value = "";
  } catch (e: any) {
    err.value = e?.response?.data || "创建失败";
  }
}
</script>

<template>
  <div>
    <h3>管理员维护（新增账号）</h3>
    <div style="display:grid; grid-template-columns:140px 1fr; gap:8px; max-width:520px">
      <div>用户名</div>
      <input v-model="username" />
      <div>密码</div>
      <input v-model="password" type="password" />
      <div>角色(role)</div>
      <input v-model="role" placeholder="如：recruitment_admin / system_admin" />
    </div>
    <button @click="submit" style="margin-top:12px">创建</button>
    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>
  </div>
</template>
