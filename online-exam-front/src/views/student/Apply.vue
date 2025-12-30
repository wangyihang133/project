<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const examYear = ref<number>(new Date().getFullYear());
const examType = ref("自主招生");
const major = ref("");
const phone = ref("");
const email = ref("");

const myApps = ref<any[]>([]);
const msg = ref("");
const err = ref("");

async function loadMine() {
  try {
    const res = await http.get("/student/applications/me");
    myApps.value = res.data || [];
  } catch (e) {
    // ignore
  }
}

async function submit() {
  msg.value = "";
  err.value = "";
  try {
    await http.post("/student/applications", {
      examYear: examYear.value,
      examType: examType.value,
      major: major.value,
      phone: phone.value,
      email: email.value,
    });
    msg.value = "报名提交成功（状态：待确认）";
    await loadMine();
  } catch (e: any) {
    err.value = e?.response?.data || "提交失败";
  }
}

onMounted(loadMine);
</script>

<template>
  <div>
    <h3>在线报名（最小集：专业/年份/类型/联系方式）</h3>

    <div style="display:grid; grid-template-columns: 120px 1fr; gap:8px; max-width:520px">
      <div>年份</div>
      <input v-model.number="examYear" type="number" />

      <div>考试类型</div>
      <input v-model="examType" placeholder="如：自主招生" />

      <div>报考专业</div>
      <input v-model="major" placeholder="如：计算机科学与技术" />

      <div>联系电话</div>
      <input v-model="phone" placeholder="用于联系确认" />

      <div>邮箱</div>
      <input v-model="email" placeholder="用于通知" />
    </div>

    <button @click="submit" style="margin-top:12px">提交报名</button>
    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>

    <h4 style="margin-top:18px">我的报名记录</h4>
    <div v-if="myApps.length===0" style="color:#666">暂无记录</div>
    <div v-for="a in myApps" :key="a.id" style="border:1px solid #eee; border-radius:8px; padding:10px; margin:8px 0">
      <div>报名ID：{{ a.id }} ｜ 年份：{{ a.exam_year }} ｜ 类型：{{ a.exam_type }}</div>
      <div>状态：<b>{{ a.status }}</b></div>
      <div style="color:#666; font-size:13px">报名时间：{{ a.application_time }}</div>
      <div style="color:#666; font-size:13px">确认时间：{{ a.confirmation_time }}</div>
    </div>
  </div>
</template>
