<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import "../../assets/recruit.css";

const items = ref<any[]>([]);
const error = ref("");

const title = ref("");
const content = ref("");
const isActive = ref(1);

async function load() {
  error.value = "";
  try {
    const res = await http.get("/recruit/exam-info");
    items.value = res.data || [];
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  }
}

async function add() {
  error.value = "";
  try {
    await http.post("/recruit/exam-info", { title: title.value, content: content.value, is_active: isActive.value });
    title.value = "";
    content.value = "";
    await load();
  } catch (e: any) {
    error.value = e?.response?.data || "新增失败";
  }
}

async function remove(id: number) {
  if (!confirm("确定删除？")) return;
  await http.delete(`/recruit/exam-info/${id}`);
  await load();
}

async function toggle(it: any) {
  await http.put(`/recruit/exam-info/${it.id}`, { is_active: it.is_active ? 0 : 1 });
  await load();
}

onMounted(load);
</script>

<template>
  <div>
    <h3 class="recruit-title">招考信息维护</h3>
    
    <div v-if="error" class="recruit-msg recruit-msg-error">{{ error }}</div>

    <div class="recruit-card">
      <h4 class="recruit-subtitle">新增公告</h4>
      <div class="recruit-form-group" style="margin-bottom: 16px;">
        <input v-model="title" placeholder="请输入公告标题" class="recruit-input" />
      </div>
      <div class="recruit-form-group" style="margin-bottom: 16px;">
        <textarea v-model="content" placeholder="请输入公告内容" style="width:100%; height:160px; padding:12px; border:1px solid #ddd; border-radius:4px; font-size:14px; font-family: inherit;" />
      </div>
      <div class="recruit-form-row">
        <label style="display: flex; align-items: center; gap: 8px;">
          <input type="checkbox" v-model="isActive" true-value="1" false-value="0" />
          立即启用
        </label>
        <button @click="add" class="recruit-btn">发布公告</button>
      </div>
    </div>

    <div v-if="items.length">
      <h4 class="recruit-subtitle">公告列表</h4>
      <div v-for="it in items" :key="it.id" class="recruit-card" style="margin-bottom: 16px;">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom: 12px;">
          <div>
            <div style="font-weight:600; font-size:16px; color:#333; margin-bottom:4px;">{{ it.title }}</div>
            <div style="color:#666; font-size:13px;">
              发布人：{{ it.publish_by }} ｜ 发布时间：{{ it.publish_time }}
            </div>
          </div>
          <div :style="{ 
            padding: '4px 12px', 
            borderRadius: '12px', 
            fontSize: '12px',
            fontWeight: '500',
            backgroundColor: it.is_active ? '#d4edda' : '#f8d7da',
            color: it.is_active ? '#155724' : '#721c24'
          }">
            {{ it.is_active ? "启用中" : "已停用" }}
          </div>
        </div>
        <div style="
          white-space: pre-wrap;
          margin: 0;
          padding: 16px;
          background: #f8f9fa;
          border-radius: 6px;
          font-size: 14px;
          line-height: 1.6;
          color: #333;
        ">{{ it.content }}</div>
        <div style="margin-top: 16px; display: flex; gap: 8px;">
          <button @click="toggle(it)" :class="['recruit-btn', it.is_active ? 'recruit-btn-danger' : 'recruit-btn-success']">
            {{ it.is_active ? "停用公告" : "启用公告" }}
          </button>
          <button @click="remove(it.id)" class="recruit-btn recruit-btn-danger">删除公告</button>
        </div>
      </div>
    </div>
    
    <div v-else class="recruit-empty-state">
      暂无招考信息
    </div>
  </div>
</template>