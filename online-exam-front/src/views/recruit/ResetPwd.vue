<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";
import "../../assets/recruit.css";

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
    <h3 class="recruit-title">学生密码重置</h3>
    
    <div class="recruit-card">
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>学生用户ID（users.id）</label>
          <input v-model.number="userId" type="number" class="recruit-input" placeholder="请输入用户ID" />
        </div>
        <div class="recruit-form-group">
          <label>新密码</label>
          <input v-model="password" type="password" class="recruit-input" placeholder="至少6位字符" />
        </div>
      </div>
      
      <div style="margin-top: 24px;">
        <button @click="submit" class="recruit-btn">提交重置</button>
      </div>
      
      <div v-if="msg" class="recruit-msg recruit-msg-success" style="margin-top: 20px;">
        {{ msg }}
      </div>
      <div v-if="err" class="recruit-msg recruit-msg-error" style="margin-top: 20px;">
        {{ err }}
      </div>
    </div>
  </div>
</template>