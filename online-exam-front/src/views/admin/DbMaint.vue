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
  <div>
    <h3>数据库维护（简化版）</h3>
    <div style="display:flex; gap:12px; flex-wrap:wrap; margin:8px 0">
      <button @click="backup">导出备份(JSON)</button>

      <div>
        <input v-model.number="cleanupDays" type="number" style="width:120px" />
        <button @click="cleanup" style="margin-left:6px">清理日志(早于N天)</button>
      </div>
    </div>

    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>

    <textarea v-model="backupJson" style="width:100%; height:260px; margin-top:10px" placeholder="点击导出后，这里显示 JSON 备份"></textarea>
  </div>
</template>
