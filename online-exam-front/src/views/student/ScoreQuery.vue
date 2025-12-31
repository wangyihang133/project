<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const loading = ref(false);
const data = ref<any>(null);
const error = ref("");

// 查询条件：报名ID + 考试/时间筛选
const applicationId = ref<string>("");
const examYear = ref<number | null>(null);
const examType = ref("");
const fromDate = ref("");
const toDate = ref("");

async function query() {
  loading.value = true;
  error.value = "";
  try {
    const res = await http.get("/student/scores/query", {
      params: {
        applicationId: applicationId.value || undefined,
        examYear: examYear.value || undefined,
        examType: examType.value || undefined,
        fromDate: fromDate.value || undefined,
        toDate: toDate.value || undefined,
      },
    });
    data.value = res.data;
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(query);
</script>

<template>
  <div>
    <h3>成绩查询（按报名ID，可筛选考试和时间）</h3>

    <div
      style="border:1px solid #eee; border-radius:8px; padding:12px; max-width:720px; margin-bottom:12px"
    >
      <div style="font-weight:600; margin-bottom:8px">查询条件</div>
      <div style="display:grid; grid-template-columns: 120px 1fr; gap:8px; max-width:520px">
        <div>报名ID</div>
        <input v-model="applicationId" placeholder="如：在报名记录中看到的ID" />

        <div>考试年份</div>
        <input v-model.number="examYear" type="number" placeholder="可选" />

        <div>考试类型</div>
        <input v-model="examType" placeholder="如：自主招生，留空为不限" />

        <div>成绩录入时间从</div>
        <input v-model="fromDate" type="date" />

        <div>成绩录入时间到</div>
        <input v-model="toDate" type="date" />
      </div>
      <button @click="query" style="margin-top:8px">查询成绩</button>
    </div>

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
