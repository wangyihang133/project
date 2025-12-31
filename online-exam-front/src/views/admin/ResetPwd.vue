<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";

const userId = ref<number | null>(null);
const password = ref("");

const msg = ref("");
const err = ref("");

async function submit() {
  msg.value = "";
  err.value = "";
  try {
    await http.post(`/recruit/students/${userId.value}/reset-password`, { password: password.value });
    msg.value = "重置成功";
    password.value = "";
  } catch (e: any) {
    err.value = e?.response?.data || "重置失败";
  }
}
</script>

<template>
  <div>
    <h3>学生密码重置</h3>
    <div style="display:grid; grid-template-columns:140px 1fr; gap:8px; max-width:520px">
      <div>学生用户ID（users.id）</div>
      <input v-model.number="userId" type="number" />
      <div>新密码</div>
      <input v-model="password" type="password" placeholder="至少 6 位" />
    </div>
    <button @click="submit" style="margin-top:12px">提交</button>
    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>
  </div>
</template>
