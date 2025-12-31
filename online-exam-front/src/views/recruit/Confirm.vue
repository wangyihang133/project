<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

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
  <div>
    <h3>现场确认</h3>
    <div style="margin:8px 0">
      状态筛选：
      <select v-model="status" @change="load">
        <option>待确认</option>
        <option>已确认</option>
        <option>已驳回</option>
      </select>
      <button @click="load" style="margin-left:8px">刷新</button>
    </div>

    <div v-if="error" style="color:#b00020">{{ error }}</div>

    <table v-if="list.length" border="1" cellspacing="0" cellpadding="6" style="border-collapse:collapse; width:100%; max-width:980px">
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
          <td>{{ a.status }}</td>
          <td>
            <button v-if="a.status==='待确认'" @click="confirm(a.id,true)">确认</button>
            <button v-if="a.status==='待确认'" @click="confirm(a.id,false)" style="margin-left:6px">驳回</button>
            <span v-else style="color:#666">-</span>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-else style="color:#666">暂无数据</div>
  </div>
</template>
