<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

const status = ref("");
const examYear = ref<number | null>(null);
const major = ref("");

const list = ref<any[]>([]);
const error = ref("");

async function load() {
  error.value = "";
  try {
    const res = await http.get("/admin/applications", {
      params: {
        status: status.value || undefined,
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
  <!-- 新增统一容器 -->
  <div class="admin-container">
    <h3>报名信息</h3>

    <div class="search-bar">
      <select v-model="status" class="form-select">
        <option value="">全部状态</option>
        <option>待确认</option>
        <option>已确认</option>
        <option>已驳回</option>
      </select>
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
      <button @click="load" class="btn-query">查询</button>
    </div>

    <!-- 错误提示美化 -->
    <div v-if="error" class="alert-error">{{ error }}</div>

    <!-- 表格美化 -->
    <div v-if="list.length" class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>考生</th>
            <th>专业</th>
            <th>年份</th>
            <th>状态</th>
            <th>报名时间</th>
            <th>确认时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in list" :key="a.id" class="table-row">
            <td>{{ a.id }}</td>
            <td>{{ a.username }}</td>
            <td>{{ a.major }}</td>
            <td>{{ a.exam_year }}</td>
            <!-- 状态文字加样式区分 -->
            <td>
              <span :class="['status-tag', `status-${a.status.replace(/已|待/, '').toLowerCase()}`]">
                {{ a.status }}
              </span>
            </td>
            <td>{{ a.application_time }}</td>
            <td>{{ a.confirmation_time || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else class="empty-tip">暂无数据</div>
  </div>
</template>

<style scoped>
/* 统一容器样式 */
.admin-container {
  width: 1200px; /* 固定宽度，保证尺寸一致 */
  margin: 0 auto; /* 水平居中 */
  padding: 20px; /* 统一内边距 */
  box-sizing: border-box; /* 防止padding撑大宽度 */
  font-family: "Microsoft Yahei", Arial, sans-serif;
}

/* 搜索栏样式优化 */
.search-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
  margin: 16px 0;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.form-select {
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
  outline: none;
  transition: border-color 0.2s;
}

.form-select:focus {
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.form-input {
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-input:nth-of-type(1) {
  width: 140px;
}

.form-input:nth-of-type(2) {
  width: 200px;
}

.form-input:focus {
  border-color: #165dff;
  box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
}

.btn-query {
  padding: 8px 20px;
  background-color: #165dff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-query:hover {
  background-color: #0d47d6;
}

/* 错误提示样式 */
.alert-error {
  color: #dc3545;
  background-color: #f8d7da;
  padding: 10px 15px;
  border-radius: 4px;
  margin: 8px 0;
  border: 1px solid #f5c6cb;
}

/* 表格容器 */
.table-container {
  overflow-x: auto; /* 适配小屏幕横向滚动 */
  margin: 8px 0;
}

/* 表格样式 */
.data-table {
  width: 100%;
  max-width: 1100px;
  border-collapse: collapse;
  font-size: 14px;
  background-color: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.data-table thead {
  background-color: #e9ecef;
}

.data-table th {
  padding: 12px 8px;
  text-align: left;
  font-weight: 600;
  color: #212529;
  border-bottom: 2px solid #dee2e6;
}

.data-table td {
  padding: 10px 8px;
  border-bottom: 1px solid #e9ecef;
  color: #495057;
}

/* 表格行hover效果 */
.table-row:hover {
  background-color: #f8f9fa;
}

/* 状态标签样式 */
.status-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-确认 {
  background-color: #e6f7e6;
  color: #28a745;
}

.status-驳回 {
  background-color: #fee;
  color: #dc3545;
}

.status-待确认 {
  background-color: #fff8e6;
  color: #ffc107;
}

/* 空数据提示 */
.empty-tip {
  color: #6c757d;
  text-align: center;
  padding: 40px 0;
  background-color: #f8f9fa;
  border-radius: 6px;
  margin: 8px 0;
}
</style>