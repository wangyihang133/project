<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const examYear = ref<number | null>(null);
const major = ref("");

const list = ref<any[]>([]);
const error = ref("");

async function load() {
  error.value = "";
  try {
    const res = await http.get("/admin/scores", {
      params: {
        examYear: examYear.value ?? undefined,
        major: major.value || undefined,
      },
    });
    list.value = res.data || [];
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  }
}

onMounted(load);
</script>

<template>
  <div>
    <h3>成绩信息</h3>

    <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:center; margin:8px 0">
      <input v-model.number="examYear" type="number" placeholder="年份(可空)" style="width:140px" />
      <input v-model="major" placeholder="专业(可空)" style="width:200px" />
      <button @click="load">查询</button>
    </div>

    <div v-if="error" style="color:#b00020">{{ error }}</div>

    <table v-if="list.length" border="1" cellspacing="0" cellpadding="6" style="border-collapse:collapse; width:100%; max-width:1100px">
      <thead>
        <tr>
          <th>成绩ID</th><th>报名ID</th><th>考生</th><th>专业</th><th>年份</th><th>科目</th><th>分数</th><th>录入时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in list" :key="s.id">
          <td>{{ s.id }}</td>
          <td>{{ s.application_id }}</td>
          <td>{{ s.username }}</td>
          <td>{{ s.major }}</td>
          <td>{{ s.exam_year }}</td>
          <td>{{ s.subject }}</td>
          <td>{{ s.score }}</td>
          <td>{{ s.entry_time }}</td>
        </tr>
      </tbody>
    </table>
    <div v-else style="color:#666">暂无数据</div>
  </div>
</template>
