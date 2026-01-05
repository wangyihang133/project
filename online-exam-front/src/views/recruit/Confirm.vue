<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import "../../assets/recruit.css";

const status = ref("待确认");
const list = ref<any[]>([]);
const error = ref("");

async function load() {
  error.value = "";
  try {
    const res = await http.get("/recruit/applications", { params: { status: status.value } });
    list.value = res.data || [];
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  }
}

async function confirm(id: number, approve: boolean) {
  await http.post(`/recruit/applications/${id}/confirm`, { approve });
  await load();
}

onMounted(load);
</script>

<template>
  <div class="recruit-container">
    <h3 class="recruit-title">现场确认</h3>
    
    <div class="recruit-card">
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>状态筛选：</label>
          <select v-model="status" @change="load" class="recruit-select">
            <option>待确认</option>
            <option>已确认</option>
            <option>已驳回</option>
          </select>
        </div>
        <div class="recruit-form-group" style="align-self: flex-end;">
          <button @click="load" class="recruit-btn">刷新列表</button>
        </div>
      </div>
    </div>

    <div v-if="error" class="recruit-msg recruit-msg-error">{{ error }}</div>

    <div class="recruit-card" v-if="list.length">
      <table class="recruit-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>考试ID</th>
            <th>考试名称</th>
            <th>考生</th>
            <th>专业</th>
            <th>年份</th>
            <th>类型</th>
            <th>联系方式</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in list" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.exam_id ?? "-" }}</td>
            <td>{{ a.exam_name ?? "-" }}</td>
            <td>{{ a.username }}</td>
            <td>{{ a.major }}</td>
            <td>{{ a.exam_year }}</td>
            <td>{{ a.exam_type }}</td>
            <td>{{ a.phone }} / {{ a.email }}</td>
            <td>
              <span :style="{ 
                color: a.status === '已确认' ? '#28a745' : 
                       a.status === '已驳回' ? '#dc3545' : '#ffc107',
                fontWeight: '500'
              }">
                {{ a.status }}
              </span>
            </td>
            <td>
              <button v-if="a.status==='待确认'" @click="confirm(a.id,true)" class="recruit-btn recruit-btn-success">确认</button>
              <button v-if="a.status==='待确认'" @click="confirm(a.id,false)" class="recruit-btn recruit-btn-danger" style="margin-left:6px">驳回</button>
              <span v-else style="color:#999; font-size:12px">无需操作</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="recruit-empty-state">
      暂无申请数据
    </div>
  </div>
</template>