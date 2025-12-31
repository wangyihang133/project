<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const seatsPerRoom = ref<number>(30);
const roomPrefix = ref("A");
const startRoom = ref<number>(101);
const examDate = ref("");
const examTime = ref("09:00-11:00");
const address = ref("教学楼A区");

// 考试筛选与选择
const filterYear = ref<number | null>(new Date().getFullYear());
const filterType = ref("");
const filterMajor = ref("");
const exams = ref<any[]>([]);
const selectedExam = ref<any | null>(null);

// 下拉选项：从后端 exams 推导
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
    // 忽略初始化失败
  }
});
</script>

<template>
  <div>
    <h3>考场分配（对已确认且未分配的报名记录）</h3>

    <h4 style="margin-top:12px">1. 按条件筛选考试并选中</h4>
    <div style="display:grid; grid-template-columns:140px 1fr; gap:8px; max-width:560px">
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

    <h4 style="margin-top:16px">2. 设置考场参数</h4>
    <div style="display:grid; grid-template-columns:140px 1fr; gap:8px; max-width:560px">
      <div>每考场座位数</div>
      <input v-model.number="seatsPerRoom" type="number" />
      <div>考场前缀</div>
      <input v-model="roomPrefix" placeholder="如 A" />
      <div>起始考场号</div>
      <input v-model.number="startRoom" type="number" />
      <div>考试日期</div>
      <input v-model="examDate" type="date" placeholder="yyyy-MM-dd（可空）" />
      <div>考试时间</div>
      <input v-model="examTime" placeholder="如 09:00-11:00" />
      <div>地址</div>
      <input v-model="address" />
    </div>
    <button @click="assign" style="margin-top:12px">开始分配</button>
    <div v-if="msg" style="color:#0a7a0a; margin-top:8px">{{ msg }}</div>
    <div v-if="err" style="color:#b00020; margin-top:8px">{{ err }}</div>
  </div>
</template>
