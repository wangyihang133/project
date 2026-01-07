<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import "../../assets/recruit.css";

const seatsPerRoom = ref<number>(30);
const roomPrefix = ref("A");
const startRoom = ref<number>(101);
const examDate = ref("");
const examTime = ref("09:00-11:00");
const address = ref("教学楼A区");

const filterYear = ref<number | null>(new Date().getFullYear());
const filterType = ref("");
const filterMajor = ref("");
const exams = ref<any[]>([]);
const selectedExam = ref<any | null>(null);

const examTypes = ref<string[]>([]);
const examYears = ref<number[]>([]);
const examMajors = ref<string[]>([]);

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
}

async function assign() {
  msg.value = "";
  err.value = "";
  try {
    const res = await http.post("/recruit/exam-rooms/assign", {
      seatsPerRoom: seatsPerRoom.value,
      roomPrefix: roomPrefix.value,
      startRoom: startRoom.value,
      examDate: examDate.value,
      examTime: examTime.value,
      address: address.value,
      examId: selectedExam.value ? selectedExam.value.id : null,
    });
    msg.value = `分配完成，已分配 ${res.data?.assigned ?? 0} 条`;
  } catch (e: any) {
    err.value = e?.response?.data || "分配失败";
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
  <div>
    <h3 class="recruit-title">考场分配</h3>
    <div class="recruit-card" style="margin-bottom: 20px; background-color: #f8f9fa;">
      <div style="color: #6c757d; font-size: 14px; line-height: 1.5;">
        说明：此功能将为已确认且未分配的报名记录分配考场座位
      </div>
    </div>

    <div class="recruit-card">
      <h4 class="recruit-subtitle">1. 筛选并选择考试</h4>
      
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>考试年份</label>
          <select v-model.number="filterYear" class="recruit-select">
            <option :value="null">全部</option>
            <option v-for="y in examYears" :key="y" :value="y">{{ y }}</option>
          </select>
        </div>
        <div class="recruit-form-group">
          <label>考试类型</label>
          <select v-model="filterType" class="recruit-select">
            <option value="">全部</option>
            <option v-for="t in examTypes" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>
        <div class="recruit-form-group">
          <label>考试专业</label>
          <select v-model="filterMajor" class="recruit-select">
            <option value="">全部</option>
            <option v-for="m in examMajors" :key="m" :value="m">{{ m }}</option>
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
                <button @click="chooseExam(e)" class="recruit-btn recruit-btn-success">
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
      <h4 class="recruit-subtitle">2. 设置考场参数</h4>
      
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>每考场座位数</label>
          <input v-model.number="seatsPerRoom" type="number" class="recruit-input" />
        </div>
        <div class="recruit-form-group">
          <label>考场前缀</label>
          <input v-model="roomPrefix" placeholder="如：A" class="recruit-input" />
        </div>
        <div class="recruit-form-group">
          <label>起始考场号</label>
          <input v-model.number="startRoom" type="number" class="recruit-input" />
        </div>
      </div>
      
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>考试日期</label>
          <input v-model="examDate" type="date" class="recruit-input" />
        </div>
        <div class="recruit-form-group">
          <label>考试时间</label>
          <input v-model="examTime" placeholder="如：09:00-11:00" class="recruit-input" />
        </div>
        <div class="recruit-form-group">
          <label>考场地址</label>
          <input v-model="address" class="recruit-input" />
        </div>
      </div>
      
      <div style="margin-top: 24px;">
        <button @click="assign" class="recruit-btn recruit-btn-lg">
          开始分配考场
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