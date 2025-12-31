<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const loading = ref(false);
const items = ref<any[]>([]);
const error = ref("");

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const res = await http.get("/student/exam-info");
    items.value = res.data || [];
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
    <h3>报考须知 / 公告</h3>
    <div v-if="error" style="color:#b00020">{{ error }}</div>
    <div v-if="loading">加载中...</div>

    <div v-if="!loading && items.length === 0" style="color:#666">暂无公告</div>

    <div v-for="it in items" :key="it.id" style="border:1px solid #eee; border-radius:8px; padding:12px; margin:10px 0">
      <div style="font-weight:600">{{ it.title }}</div>
      <div style="color:#666; font-size:13px; margin:6px 0">
        发布人：{{ it.publish_by }} ｜ 发布时间：{{ it.publish_time }}
      </div>
      <pre style="white-space:pre-wrap; margin:0">{{ it.content }}</pre>
    </div>
  </div>
</template>
