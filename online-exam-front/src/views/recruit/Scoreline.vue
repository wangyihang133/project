<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import "../../assets/recruit.css";

const filterYear = ref<number | null>(new Date().getFullYear());
const filterType = ref("");
const filterMajor = ref("");
const exams = ref<any[]>([]);
const selectedExam = ref<any | null>(null);
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
    // 忽略初始化失败
  }
});
</script>

<template>
  <div class="recruit-container">
    <h3 class="recruit-title">设置录取分数线</h3>

    <div class="recruit-card">
      <h4 class="recruit-subtitle">1. 筛选考试（可选）</h4>
      
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>考试年份</label>
          <select v-model.number="filterYear" class="recruit-select">
            <option :value="null">全部</option>
            <option v-for="y in examYears" :key="`year-${y}`" :value="y">
              {{ y }}
            </option>
          </select>
        </div>
        <div class="recruit-form-group">
          <label>考试类型</label>
          <select v-model="filterType" class="recruit-select">
            <option value="">全部</option>
            <option v-for="t in examTypes" :key="`type-${t}`" :value="t">
              {{ t }}
            </option>
          </select>
        </div>
        <div class="recruit-form-group">
          <label>考试专业</label>
          <select v-model="filterMajor" class="recruit-select">
            <option value="">全部</option>
            <option v-for="m in examMajors" :key="`major-${m}`" :value="m">
              {{ m }}
            </option>
          </select>
        </div>
      </div>
      
      <button @click="searchExams" class="recruit-btn">筛选考试</button>

      <div v-if="exams.length" style="margin-top: 20px;">
        <table class="recruit-table">
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
                <button @click="chooseExam(e)" class="recruit-btn recruit-btn-success" style="padding: 6px 12px; font-size: 13px;">
                  选中
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="selectedExam" class="recruit-selected-info">
        <div style="font-weight: 600; margin-bottom: 4px;">已选中考试</div>
        <div>ID: #{{ selectedExam.id }}</div>
        <div>名称: {{ selectedExam.exam_name }}</div>
        <div>专业: {{ selectedExam.exam_major || "专业未填" }}</div>
      </div>
    </div>

    <div class="recruit-card" style="margin-top: 24px;">
      <h4 class="recruit-subtitle">2. 设置分数线</h4>
      
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>年份</label>
          <select v-model.number="examYear" class="recruit-select">
            <option v-for="y in examYears" :key="`set-year-${y}`" :value="y">
              {{ y }}
            </option>
          </select>
        </div>
        <div class="recruit-form-group">
          <label>专业</label>
          <select v-model="major" class="recruit-select">
            <option v-for="m in examMajors" :key="`set-major-${m}`" :value="m">
              {{ m }}
            </option>
          </select>
        </div>
        <div class="recruit-form-group">
          <label>最低分数线</label>
          <input v-model.number="minScore" type="number" class="recruit-input" placeholder="请输入最低分数" />
        </div>
      </div>
      
      <div style="margin-top: 24px;">
        <button @click="submit" class="recruit-btn" style="padding: 10px 24px; font-size: 15px;">
          设置分数线
        </button>
      </div>
      
      <div v-if="msg" class="recruit-msg recruit-msg-success" style="margin-top: 20px;">
        {{ msg }}
      </div>
      <div v-if="err" class="recruit-msg recruit-msg-error" style="margin-top: 20px;">
        {{ err }}
      </div>
    </div>
  </div>
</template>