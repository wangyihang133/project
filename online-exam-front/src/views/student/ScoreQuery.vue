<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const loading = ref(false);
const data = ref<any>(null);
const error = ref("");

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const res = await http.get("/student/scores/me");
    data.value = res.data;
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
    <h3>成绩查询</h3>
    <button @click="load">刷新</button>

    <div v-if="error" style="color:#b00020; margin-top:8px">{{ error }}</div>
    <div v-if="loading" style="margin-top:8px">加载中...</div>

    <div v-if="!loading && data">
      <div style="margin-top:10px">报名ID：{{ data.applicationId ?? "暂无" }}</div>
      <div style="margin:6px 0">总分：<b>{{ data.total }}</b></div>

      <table v-if="data.scores && data.scores.length" border="1" cellspacing="0" cellpadding="6" style="border-collapse:collapse; width:100%; max-width:720px">
        <thead>
          <tr>
            <th>科目</th>
            <th>分数</th>
            <th>录入时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in data.scores" :key="s.id">
            <td>{{ s.subject }}</td>
            <td>{{ s.score }}</td>
            <td>{{ s.entry_time }}</td>
          </tr>
        </tbody>
      </table>

      <div v-else style="color:#666; margin-top:8px">暂无成绩（管理员尚未录入）</div>
    </div>
  </div>
</template>
