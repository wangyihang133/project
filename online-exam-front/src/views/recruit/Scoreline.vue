<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";

const examYear = ref<number>(new Date().getFullYear());
const major = ref("");
const minScore = ref<number>(0);

const msg = ref("");
const err = ref("");

async function submit() {
  msg.value = "";
  err.value = "";
  try {
    await http.post("/recruit/scoreline", { examYear: examYear.value, major: major.value, minScore: minScore.value });
    msg.value = "设置成功";
  } catch (e: any) {
    err.value = e?.response?.data || "设置失败";
  }
}
</script>

<template>
  <div>
    <h3>设置录取分数线</h3>
    <div style="display:grid; grid-template-columns:120px 1fr; gap:8px; max-width:520px">
      <div>年份</div>
      <input v-model.number="examYear" type="number" />
      <div>专业</div>
      <input v-model="major" />
      <div>最低分数线</div>
      <input v-model.number="minScore" type="number" />
    </div>
    <button @click="submit" style="margin-top:12px">提交</button>
    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>
  </div>
</template>
