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
  <div class="admin-container">
    <div class="page-header">
      <h3>学生密码重置</h3>
    </div>

    <div class="form-container">
      <div class="form-grid">
        <label class="form-label">学生用户ID（users.id）</label>
        <input 
          v-model.number="userId" 
          type="number" 
          class="form-input"
          required
        />
        
        <label class="form-label">新密码</label>
        <input 
          v-model="password" 
          type="password" 
          placeholder="至少 6 位" 
          class="form-input"
          required
        />
      </div>
      
      <button @click="submit" class="btn primary-btn submit-btn">提交</button>
      
      <div v-if="msg" class="alert success-alert">{{ msg }}</div>
      <div v-if="err" class="alert error-alert">{{ err }}</div>
    </div>
  </div>
</template>

<style scoped>
.admin-container {
  width: 1200px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  color: #1f2937;
  font-size: 1.5rem;
  font-weight: 600;
}

.form-container {
  max-width: 520px;
  background-color: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.form-grid {
  display:grid; 
  grid-template-columns:140px 1fr; 
  gap:12px; 
  margin-bottom: 20px;
  align-items: center;
}

.form-label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
  text-align: right;
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.primary-btn {
  background-color: #165dff;
  color: white;
}

.primary-btn:hover {
  background-color: #0d47d6;
}

.submit-btn {
  width: 100%;
  padding: 10px;
  font-weight: 500;
}

.alert {
  padding: 12px 16px;
  border-radius: 6px;
  margin-top: 16px;
  font-size: 14px;
}

.success-alert {
  background-color: #ecfdf5;
  color: #065f46;
  border: 1px solid #d1fae5;
}

.error-alert {
  background-color: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}
</style>