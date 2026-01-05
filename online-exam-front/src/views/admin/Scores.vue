<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const examYear = ref<number | null>(null);
const major = ref("");

const list = ref<any[]>([]);
const error = ref("");

async function load() {
  error.value = "";
  try {
    const res = await http.get("/admin/scores", {
      params: {
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
  <div class="admin-container">
    <div class="page-header">
      <h3>成绩信息</h3>
    </div>

    <div class="search-panel">
      <input 
        v-model.number="examYear" 
        type="number" 
        placeholder="年份(可空)" 
        class="form-input"
      />
      <input 
        v-model="major" 
        placeholder="专业(可空)" 
        class="form-input"
      />
      <button @click="load" class="btn primary-btn">查询</button>
    </div>

    <div v-if="error" class="alert error-alert">{{ error }}</div>

    <div v-if="list.length" class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>成绩ID</th><th>报名ID</th><th>考生</th><th>专业</th><th>年份</th>
            <th>考试类型</th><th>科目</th><th>分数</th><th>录入时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in list" :key="s.id" class="table-row">
            <td>{{ s.id }}</td>
            <td>{{ s.application_id }}</td>
            <td>{{ s.username }}</td>
            <td>{{ s.major }}</td>
            <td>{{ s.exam_year }}</td>
            <td>{{ s.exam_type }}</td>
            <td>{{ s.subject }}</td>
            <td>{{ s.score }}</td>
            <td>{{ s.entry_time }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else class="empty-state">暂无数据</div>
  </div>
</template>

<style scoped>
.admin-container {
  width: 1200px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  color: #1f2937;
  font-size: 1.5rem;
  font-weight: 600;
}

.search-panel {
  display:flex; 
  gap:12px; 
  flex-wrap:wrap; 
  align-items:center; 
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  width: 180px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.primary-btn {
  background-color: #165dff;
  color: white;
}

.primary-btn:hover {
  background-color: #0d47d6;
}

.alert {
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 14px;
}

.error-alert {
  background-color: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
}

.data-table th {
  background-color: #f3f4f6;
  color: #374151;
  font-weight: 600;
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
  border-bottom: 1px solid #e5e7eb;
}

.table-row {
  border-bottom: 1px solid #f3f4f6;
}

.table-row td {
  padding: 12px 16px;
  font-size: 14px;
  color: #1f2937;
}

.table-row:hover {
  background-color: #f9fafb;
}

/* 斑马纹效果 */
.table-row:nth-child(even) {
  background-color: #fafafa;
}

.empty-state {
  padding: 48px 24px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}
</style>