<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import "../../assets/student-common.css"; // 引入通用样式

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
  <div class="student-container">
    <h3 class="student-title">报考须知 / 公告</h3>
    
    <div v-if="error" class="student-text-error">{{ error }}</div>
    <div v-if="loading" class="student-loading">加载中...</div>

    <div v-if="!loading && items.length === 0" class="student-text-empty">暂无公告</div>

    <div v-for="it in items" :key="it.id" class="student-item-card">
      <div style="font-weight:600; color:#2c3e50; margin-bottom:8px">{{ it.title }}</div>
      <div style="color:#909399; font-size:13px; margin-bottom:8px">
        发布人：{{ it.publish_by }} ｜ 发布时间：{{ it.publish_time }}
      </div>
      <pre style="white-space:pre-wrap; margin:0; color:#606266; line-height:1.6">{{ it.content }}</pre>
    </div>
  </div>
</template>