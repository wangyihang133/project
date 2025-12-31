<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const list = ref<any[]>([]);
const error = ref("");

const examName = ref("");
const examType = ref("");
const examTime = ref(""); // 使用 datetime-local 字符串
const examMajor = ref("");
const candidateCount = ref<number | null>(null);
const remarks = ref("");

const saving = ref(false);
const editingId = ref<number | null>(null);

async function load() {
  error.value = "";
  try {
    const res = await http.get("/recruit/exams");
    list.value = res.data || [];
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  }
}

async function createExam() {
  if (!examName.value || !examType.value || !examTime.value) {
    alert("考试名称、类型和时间必填");
    return;
  }
  saving.value = true;
  error.value = "";
  try {
    // datetime-local 默认格式为 yyyy-MM-ddTHH:mm，后端会把 T 替换成空格再解析
    const payload = {
      exam_name: examName.value,
      exam_type: examType.value,
      exam_time: examTime.value + ":00", // 补秒成 yyyy-MM-ddTHH:mm:ss
      exam_major: examMajor.value,
      candidate_count: candidateCount.value ?? 0,
      remarks: remarks.value,
    };

    if (editingId.value == null) {
      // 新增
      await http.post("/recruit/exams", payload);
    } else {
      // 编辑
      await http.put(`/recruit/exams/${editingId.value}`, payload);
    }
    examName.value = "";
    examType.value = "";
    examTime.value = "";
    examMajor.value = "";
    candidateCount.value = null;
    remarks.value = "";
    editingId.value = null;
    await load();
  } catch (e: any) {
    error.value = e?.response?.data || "保存失败";
  } finally {
    saving.value = false;
  }
}

function startEdit(it: any) {
  editingId.value = it.id;
  examName.value = it.exam_name || "";
  examType.value = it.exam_type || "";
  examMajor.value = it.exam_major || "";
  candidateCount.value = typeof it.candidate_count === "number" ? it.candidate_count : null;
  // 兼容 "yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-ddTHH:mm:ss"，转为 datetime-local 需要到分钟
  if (it.exam_time) {
    let t = String(it.exam_time);
    t = t.replace(" ", "T");
    examTime.value = t.slice(0, 16);
  } else {
    examTime.value = "";
  }
  remarks.value = it.remarks || "";
}

function cancelEdit() {
  editingId.value = null;
  examName.value = "";
  examType.value = "";
  examTime.value = "";
  examMajor.value = "";
  candidateCount.value = null;
  remarks.value = "";
}

async function removeExam(id: number) {
  if (!confirm("确定删除该考试？")) return;
  await http.delete(`/recruit/exams/${id}`);
  await load();
}

onMounted(load);
</script>

<template>
  <div>
    <h3>考试设置</h3>
    <div v-if="error" style="color:#b00020">{{ error }}</div>

    <div
      style="border:1px solid #eee; border-radius:8px; padding:12px; max-width:820px; margin:10px 0"
    >
      <div style="font-weight:600; margin-bottom:8px">
        {{ editingId == null ? "新增考试" : `编辑考试（ID：${editingId}）` }}
      </div>
      <div style="display:flex; flex-wrap:wrap; gap:8px; margin-bottom:8px">
        <input v-model="examName" placeholder="考试名称" style="flex:1 1 160px" />
        <input v-model="examType" placeholder="考试类型（如 笔试/面试）" style="flex:1 1 160px" />
        <input
          v-model="examMajor"
          placeholder="考试专业（如 计算机科学与技术）"
          style="flex:1 1 200px"
        />
      </div>
      <div style="display:flex; flex-wrap:wrap; gap:8px; margin-bottom:8px">
        <label style="display:flex; align-items:center; gap:4px">
          <span>考试时间</span>
          <input v-model="examTime" type="datetime-local" />
        </label>
        <input
          v-model.number="candidateCount"
          type="number"
          min="0"
          placeholder="预计考生人数"
          style="flex:0 0 160px"
        />
      </div>
      <textarea
        v-model="remarks"
        placeholder="备注（可选）"
        style="width:100%; height:80px; margin:6px 0"
      ></textarea>
      <button @click="createExam" :disabled="saving">
        {{ saving ? "保存中..." : editingId == null ? "保存考试" : "更新考试" }}
      </button>
      <button v-if="editingId != null" @click="cancelEdit" style="margin-left:8px">取消编辑</button>
    </div>

    <div style="margin-top:16px">
      <h4>已配置考试</h4>
      <div v-if="!list.length" style="color:#666; margin-top:6px">暂无考试记录</div>
      <table v-else border="1" cellspacing="0" cellpadding="4" style="width:100%; max-width:900px; margin-top:8px; border-collapse:collapse; font-size:13px">
        <thead style="background:#f5f5f5">
          <tr>
            <th style="width:60px">ID</th>
            <th>名称</th>
            <th>类型</th>
            <th>专业</th>
            <th>时间</th>
            <th>考生人数</th>
            <th>备注</th>
            <th style="width:110px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="it in list" :key="it.id">
            <td>{{ it.id }}</td>
            <td>{{ it.exam_name }}</td>
            <td>{{ it.exam_type }}</td>
            <td>{{ it.exam_major }}</td>
            <td>{{ it.exam_time }}</td>
            <td>{{ it.candidate_count }}</td>
            <td>{{ it.remarks }}</td>
            <td>
              <button @click="startEdit(it)">编辑</button>
              
              <button @click="removeExam(it.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
