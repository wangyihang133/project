<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import "../../assets/student-common.css"; // 引入通用样式

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
  <div class="student-container">
    <h3 class="student-title">在线报名</h3>

    <!-- 筛选区域 -->
    <div class="student-filter-card">
      <div style="font-weight:600; margin-bottom:12px">1. 选择考试（筛选条件）</div>
      <div class="student-grid">
        <div>年份</div>
        <input v-model.number="filterYear" type="number" class="student-input" />

        <div>考试类型</div>
        <input v-model="filterType" placeholder="如：自主招生" class="student-input" />

        <div>考试专业</div>
        <input v-model="filterMajor" placeholder="如：计算机科学与技术" class="student-input" />
      </div>
      <button @click="searchExams" class="student-btn">筛选考试</button>
    </div>

    <!-- 筛选结果 -->
    <div style="margin-bottom:20px">
      <div style="font-weight:600; margin-bottom:8px; color:#2c3e50">2. 筛选结果：可报名的考试</div>
      <div v-if="exams.length === 0" class="student-text-empty">暂无匹配的考试</div>
      <table v-else class="student-table">
        <thead>
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
              <button @click="applyExam(e)" class="student-btn" style="padding:4px 8px; font-size:12px">报名</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 提示信息 -->
    <div v-if="msg" class="student-text-success">{{ msg }}</div>
    <div v-if="err" class="student-text-error">{{ err }}</div>

    <!-- 报名记录 -->
    <div style="margin-top:24px">
      <h4 style="color:#2c3e50; margin-bottom:12px">我的报名记录</h4>
      <div v-if="myApps.length===0" class="student-text-empty">暂无记录</div>
      <div v-for="a in myApps" :key="a.id" class="student-item-card">
        <div style="margin-bottom:4px">报名ID：{{ a.id }} ｜ 考试ID：{{ a.exam_id ?? '-' }}</div>
        <div style="margin-bottom:4px">状态：<b style="color:#409eff">{{ a.status }}</b></div>
        <div style="color:#909399; font-size:13px">报名时间：{{ a.application_time }}</div>
        <div style="color:#909399; font-size:13px">确认时间：{{ a.confirmation_time }}</div>
      </div>
    </div>
  </div>
</template>