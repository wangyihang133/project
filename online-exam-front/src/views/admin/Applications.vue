<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const status = ref("");
const examYear = ref<number | null>(null);
const major = ref("");

const list = ref<any[]>([]);
const error = ref("");

async function load() {
  error.value = "";
  try {
    const res = await http.get("/admin/applications", {
      params: {
        status: status.value || undefined,
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
    <h3>报名信息</h3>

    <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:center; margin:8px 0">
      <select v-model="status">
        <option value="">全部状态</option>
        <option>待确认</option>
        <option>已确认</option>
        <option>已驳回</option>
      </select>
      <input v-model.number="examYear" type="number" placeholder="年份(可空)" style="width:140px" />
      <input v-model="major" placeholder="专业(可空)" style="width:200px" />
      <button @click="load">查询</button>
    </div>

    <div v-if="error" style="color:#b00020">{{ error }}</div>

    <table v-if="list.length" border="1" cellspacing="0" cellpadding="6" style="border-collapse:collapse; width:100%; max-width:1100px">
      <thead>
        <tr>
          <th>ID</th><th>考生</th><th>专业</th><th>年份</th><th>状态</th><th>报名时间</th><th>确认时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="a in list" :key="a.id">
          <td>{{ a.id }}</td>
          <td>{{ a.username }}</td>
          <td>{{ a.major }}</td>
          <td>{{ a.exam_year }}</td>
          <td>{{ a.status }}</td>
          <td>{{ a.application_time }}</td>
          <td>{{ a.confirmation_time }}</td>
        </tr>
      </tbody>
    </table>
    <div v-else style="color:#666">暂无数据</div>
  </div>
</template>
