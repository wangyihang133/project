<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";

const backupJson = ref("");
const cleanupDays = ref<number>(90);
const msg = ref("");
const err = ref("");

async function backup() {
  msg.value = "";
  err.value = "";
  try {
    const res = await http.post("/admin/db/backup");
    backupJson.value = JSON.stringify(res.data, null, 2);
    msg.value = "备份导出成功（已在页面展示 JSON）";
  } catch (e: any) {
    err.value = e?.response?.data || "备份失败";
  }
}

async function cleanup() {
  msg.value = "";
  err.value = "";
  try {
    const res = await http.post("/admin/db/cleanup", { days: cleanupDays.value });
    msg.value = `清理完成，删除 ${res.data?.deleted ?? 0} 条日志`;
  } catch (e: any) {
    err.value = e?.response?.data || "清理失败";
  }
}
</script>

<template>
  <div class="admin-container">
    <div class="page-header">
      <h3>数据库维护（简化版）</h3>
    </div>

    <div class="action-panel">
      <button @click="backup" class="btn primary-btn">导出备份(JSON)</button>

      <div class="cleanup-group">
        <input 
          v-model.number="cleanupDays" 
          type="number" 
          class="form-input"
          min="1"
        />
        <button @click="cleanup" class="btn primary-btn">清理日志(早于N天)</button>
      </div>
    </div>

    <div v-if="msg" class="alert success-alert">{{ msg }}</div>
    <div v-if="err" class="alert error-alert">{{ err }}</div>

    <div class="backup-container">
      <textarea 
        v-model="backupJson" 
        class="backup-textarea"
        placeholder="点击导出后，这里显示 JSON 备份"
      ></textarea>
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

.action-panel {
  display:flex; 
  gap:16px; 
  flex-wrap:wrap; 
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
  align-items: center;
}

.cleanup-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  width: 120px;
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
  font-weight: 500;
}

.primary-btn {
  background-color: #165dff;
  color: white;
}

.primary-btn:hover {
  background-color: #0d47d6;
}

.alert {
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
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

.backup-container {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.backup-textarea {
  width: 100%;
  height: 260px;
  padding: 16px;
  border: none;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  background-color: #f9fafb;
  resize: vertical;
  box-sizing: border-box;
}

.backup-textarea:focus {
  outline: none;
  background-color: white;
}
</style>