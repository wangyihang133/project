<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import "../../assets/recruit.css";

const list = ref<any[]>([]);
const error = ref("");

const examName = ref("");
const examType = ref("");
const examTime = ref("");
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
    const payload = {
      exam_name: examName.value,
      exam_type: examType.value,
      exam_time: examTime.value + ":00",
      exam_major: examMajor.value,
      candidate_count: candidateCount.value ?? 0,
      remarks: remarks.value,
    };

    if (editingId.value == null) {
      await http.post("/recruit/exams", payload);
    } else {
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
    <h3 class="recruit-title">考试设置</h3>
    
    <div v-if="error" class="recruit-msg recruit-msg-error">{{ error }}</div>

    <div class="recruit-card">
      <h4 class="recruit-subtitle">
        {{ editingId == null ? "新增考试" : `编辑考试（ID：${editingId}）` }}
      </h4>
      
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>考试名称</label>
          <input v-model="examName" placeholder="请输入考试名称" class="recruit-input" />
        </div>
        <div class="recruit-form-group">
          <label>考试类型</label>
          <input v-model="examType" placeholder="如：笔试/面试" class="recruit-input" />
        </div>
        <div class="recruit-form-group">
          <label>考试专业</label>
          <input v-model="examMajor" placeholder="如：计算机科学与技术" class="recruit-input" />
        </div>
      </div>
      
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>考试时间</label>
          <input v-model="examTime" type="datetime-local" class="recruit-input" />
        </div>
        <div class="recruit-form-group">
          <label>预计考生人数</label>
          <input v-model.number="candidateCount" type="number" min="0" placeholder="请输入人数" class="recruit-input" />
        </div>
      </div>
      
      <div class="recruit-form-group">
        <label>备注</label>
        <textarea v-model="remarks" placeholder="请输入备注信息（可选）" style="width:100%; height:80px; padding:12px; border:1px solid #ddd; border-radius:4px; font-size:14px; font-family: inherit;" />
      </div>
      
      <div style="display: flex; gap: 12px; margin-top: 20px;">
        <button @click="createExam" :disabled="saving" class="recruit-btn">
          {{ saving ? "保存中..." : editingId == null ? "保存考试" : "更新考试" }}
        </button>
        <button v-if="editingId != null" @click="cancelEdit" class="recruit-btn" style="background-color: #6c757d;">
          取消编辑
        </button>
      </div>
    </div>

    <div class="recruit-card" style="margin-top: 24px;">
      <h4 class="recruit-subtitle">已配置考试</h4>
      
      <div v-if="!list.length" class="recruit-empty-state">
        暂无考试记录
      </div>
      
      <table v-else class="recruit-table">
        <thead>
          <tr>
            <th style="width:60px">ID</th>
            <th>名称</th>
            <th>类型</th>
            <th>专业</th>
            <th>时间</th>
            <th>考生人数</th>
            <th>备注</th>
            <th style="width:140px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="it in list" :key="it.id">
            <td>{{ it.id }}</td>
            <td style="font-weight: 500;">{{ it.exam_name }}</td>
            <td>{{ it.exam_type }}</td>
            <td>{{ it.exam_major }}</td>
            <td>{{ it.exam_time }}</td>
            <td>{{ it.candidate_count }}</td>
            <td>{{ it.remarks }}</td>
            <td style="display: flex; gap: 6px;">
              <button @click="startEdit(it)" class="recruit-btn" style="padding: 6px 12px; font-size: 13px;">
                编辑
              </button>
              <button @click="removeExam(it.id)" class="recruit-btn recruit-btn-danger" style="padding: 6px 12px; font-size: 13px;">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>