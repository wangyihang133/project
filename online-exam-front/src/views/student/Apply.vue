<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

// 筛选条件
const filterYear = ref<number>(new Date().getFullYear());
const filterType = ref("自主招生");
const filterMajor = ref("");


// 列表
const myApps = ref<any[]>([]);
const exams = ref<any[]>([]);

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

async function searchExams() {
  err.value = "";
  try {
    const res = await http.get("/student/exams", {
      params: {
        year: filterYear.value,
        type: filterType.value || undefined,
        major: filterMajor.value || undefined,
      },
    });
    exams.value = res.data || [];
  } catch (e: any) {
    err.value = e?.response?.data || "加载考试列表失败";
  }
}

async function applyExam(exam: any) {
  msg.value = "";
  err.value = "";
  try {
    await http.post("/student/applications", {
      examId: exam.id,
    });
    msg.value = `已提交对“${exam.exam_name}”的报名（状态：待确认）`;
    await Promise.all([loadMine(), searchExams()]);
  } catch (e: any) {
    err.value = e?.response?.data || "提交失败";
  }
}

onMounted(async () => {
  await loadMine();
  await searchExams();
});
</script>

<template>
  <div>
    <h3>在线报名：</h3>

    <div
      style="border:1px solid #eee; border-radius:8px; padding:12px; max-width:720px; margin-bottom:12px"
    >
      <div style="font-weight:600; margin-bottom:8px">1. 选择考试（筛选条件）</div>
      <div style="display:grid; grid-template-columns: 120px 1fr; gap:8px; max-width:520px">
        <div>年份</div>
        <input v-model.number="filterYear" type="number" />

        <div>考试类型</div>
        <input v-model="filterType" placeholder="如：自主招生" />

        <div>考试专业</div>
        <input v-model="filterMajor" placeholder="如：计算机科学与技术" />
      </div>
      <button @click="searchExams" style="margin-top:8px">筛选考试</button>
    </div>

    <div style="margin-bottom:12px">
      <div style="font-weight:600; margin-bottom:4px">2. 筛选结果：可报名的考试</div>
      <div v-if="exams.length === 0" style="color:#666">暂无匹配的考试</div>
      <table
        v-else
        border="1"
        cellspacing="0"
        cellpadding="4"
        style="width:100%; max-width:900px; margin-top:4px; border-collapse:collapse; font-size:13px"
      >
        <thead style="background:#f5f5f5">
          <tr>
            <th style="width:60px">ID</th>
            <th>名称</th>
            <th>类型</th>
            <th>专业</th>
            <th>时间</th>
            <th>预计人数</th>
            <th style="width:80px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in exams" :key="e.id">
            <td>{{ e.id }}</td>
            <td>{{ e.exam_name }}</td>
            <td>{{ e.exam_type }}</td>
            <td>{{ e.exam_major }}</td>
            <td>{{ e.exam_time }}</td>
            <td>{{ e.candidate_count }}</td>
            <td>
              <button @click="applyExam(e)">报名</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

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
