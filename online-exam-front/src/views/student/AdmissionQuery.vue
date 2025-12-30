<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const loading = ref(false);
const info = ref<any>(null);
const error = ref("");

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const res = await http.get("/student/admission/me");
    info.value = res.data;
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div>
    <h3>录取查询</h3>
    <button @click="load">刷新</button>

    <div v-if="error" style="color:#b00020; margin-top:8px">{{ error }}</div>
    <div v-if="loading" style="margin-top:8px">加载中...</div>

    <div v-if="!loading && info" style="margin-top:10px">
      <div>状态：<b>{{ info.status }}</b></div>
      <div>年份：{{ info.examYear }}</div>
      <div>专业：{{ info.major }}</div>
      <div>总分：{{ info.total }}</div>
      <div>分数线：{{ info.minScore ?? "未设置" }}</div>
    </div>
  </div>
</template>
