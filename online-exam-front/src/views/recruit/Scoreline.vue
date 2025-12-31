<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

// 考试筛选
const filterYear = ref<number | null>(new Date().getFullYear());
const filterType = ref("");
const filterMajor = ref("");
const exams = ref<any[]>([]);
const selectedExam = ref<any | null>(null);

// 下拉选项：从后端 exams 推导
const examTypes = ref<string[]>([]);
const examYears = ref<number[]>([]);
const examMajors = ref<string[]>([]);

const examYear = ref<number>(new Date().getFullYear());
const major = ref("");
const minScore = ref<number>(0);

const msg = ref("");
const err = ref("");

async function searchExams() {
  err.value = "";
  try {
    const res = await http.get("/recruit/exams", {
      params: {
        year: filterYear.value || undefined,
        type: filterType.value || undefined,
        major: filterMajor.value || undefined,
      },
    });
    exams.value = res.data || [];
  } catch (e: any) {
    err.value = e?.response?.data || "加载考试失败";
  }
}

function chooseExam(e: any) {
  selectedExam.value = e;
  // 从考试信息预填年份和专业
  if (e.exam_time) {
    const y = String(e.exam_time).slice(0, 4);
    const n = parseInt(y, 10);
    if (!Number.isNaN(n)) examYear.value = n;
  }
  if (e.exam_major) {
    major.value = e.exam_major;
  }
}

async function submit() {
  msg.value = "";
  err.value = "";
  try {
    if (!selectedExam.value) {
      err.value = "请先在上方选中要设置分数线的考试";
      return;
    }
    await http.post("/recruit/scoreline", {
      examId: selectedExam.value.id,
      major: major.value,
      minScore: minScore.value,
    });
    msg.value = "设置成功";
  } catch (e: any) {
    err.value = e?.response?.data || "设置失败";
  }
}

// 初始化时加载一次全部考试，用于生成下拉选项（类型/年份/专业）
onMounted(async () => {
  try {
    const res = await http.get("/recruit/exams");
    const list: any[] = res.data || [];
    const typeSet = new Set<string>();
    const yearSet = new Set<number>();
    const majorSet = new Set<string>();

    list.forEach((e) => {
      if (e.exam_type) typeSet.add(e.exam_type);
      if (e.exam_time) {
        const y = String(e.exam_time).slice(0, 4);
        const n = parseInt(y, 10);
        if (!Number.isNaN(n)) yearSet.add(n);
      }
      if (e.exam_major) majorSet.add(e.exam_major);
    });

    examTypes.value = Array.from(typeSet);
    examYears.value = Array.from(yearSet).sort((a, b) => a - b);
    examMajors.value = Array.from(majorSet);
  } catch {
    // 忽略初始化失败，不影响页面基本功能
  }
});
</script>

<template>
  <div>
    <h3>设置录取分数线</h3>

    <h4 style="margin-top:12px">1. 按条件筛选考试（可选）</h4>
    <div style="display:grid; grid-template-columns:120px 1fr; gap:8px; max-width:520px">
      <div>考试年份</div>
      <select v-model.number="filterYear">
        <option :value="null">全部</option>
        <option v-for="y in examYears" :key="y" :value="y">{{ y }}</option>
      </select>
      <div>考试类型</div>
      <select v-model="filterType">
        <option value="">全部</option>
        <option v-for="t in examTypes" :key="t" :value="t">{{ t }}</option>
      </select>
      <div>考试专业</div>
      <select v-model="filterMajor">
        <option value="">全部</option>
        <option v-for="m in examMajors" :key="m" :value="m">{{ m }}</option>
      </select>
    </div>
    <button @click="searchExams" style="margin-top:8px">筛选考试</button>

    <div v-if="exams.length" style="margin-top:8px; max-width:760px; overflow:auto">
      <table border="1" cellspacing="0" cellpadding="4" style="border-collapse:collapse; width:100%">
        <thead>
          <tr>
            <th>考试ID</th>
            <th>考试名称</th>
            <th>类型</th>
            <th>时间</th>
            <th>专业</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in exams" :key="e.id">
            <td>{{ e.id }}</td>
            <td>{{ e.exam_name }}</td>
            <td>{{ e.exam_type }}</td>
            <td>{{ e.exam_time }}</td>
            <td>{{ e.exam_major }}</td>
            <td>
              <button @click="chooseExam(e)">选中</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="selectedExam" style="margin-top:8px; color:#555">
      当前选中考试：#{{ selectedExam.id }} - {{ selectedExam.exam_name }}（{{ selectedExam.exam_major || "专业未填" }}）
    </div>

    <h4 style="margin-top:16px">2. 设置分数线</h4>
    <div style="display:grid; grid-template-columns:120px 1fr; gap:8px; max-width:520px">
      <div>年份</div>
      <select v-model.number="examYear">
        <option v-for="y in examYears" :key="y" :value="y">{{ y }}</option>
      </select>
      <div>专业</div>
      <select v-model="major">
        <option v-for="m in examMajors" :key="m" :value="m">{{ m }}</option>
      </select>
      <div>最低分数线</div>
      <input v-model.number="minScore" type="number" />
    </div>
    <button @click="submit" style="margin-top:12px">提交</button>
    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>
  </div>
</template>
