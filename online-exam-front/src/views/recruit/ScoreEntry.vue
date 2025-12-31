<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";

const studentId = ref<number | null>(null);
const subject = ref("综合");
const score = ref<number>(0);

const msg = ref("");
const err = ref("");

async function submit() {
  msg.value = "";
  err.value = "";
  try {
    await http.post("/recruit/scores/entry", {
      studentId: studentId.value,
      subject: subject.value,
      score: score.value,
    });
    msg.value = "录入成功";
  } catch (e: any) {
    err.value = e?.response?.data || "录入失败";
  }
}
</script>

<template>
  <div>
    <h3>成绩录入</h3>
    <div style="display:grid; grid-template-columns:140px 1fr; gap:8px; max-width:520px">
      <div>报名ID</div>
      <input v-model.number="studentId" type="number" />
      <div>科目</div>
      <input v-model="subject" />
      <div>分数</div>
      <input v-model.number="score" type="number" />
    </div>
    <button @click="submit" style="margin-top:12px">提交</button>
    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>
  </div>
</template>
