<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";

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
    <h3>招考信息维护</h3>
    <div v-if="error" style="color:#b00020">{{ error }}</div>

    <div style="border:1px solid #eee; border-radius:8px; padding:12px; max-width:760px; margin:10px 0">
      <div style="font-weight:600; margin-bottom:8px">新增公告</div>
      <input v-model="title" placeholder="标题" style="width:100%; margin:6px 0" />
      <textarea v-model="content" placeholder="内容" style="width:100%; height:120px; margin:6px 0"></textarea>
      <label style="margin-right:12px">
        <input type="checkbox" v-model="isActive" true-value="1" false-value="0" />
        立即启用
      </label>
      <button @click="add">发布</button>
    </div>

    <div v-for="it in items" :key="it.id" style="border:1px solid #eee; border-radius:8px; padding:12px; margin:10px 0">
      <div style="display:flex; justify-content:space-between; gap:12px">
        <div style="font-weight:600">{{ it.title }}</div>
        <div style="white-space:nowrap; color:#666">状态：{{ it.is_active ? "启用" : "停用" }}</div>
      </div>
      <div style="color:#666; font-size:13px; margin:6px 0">
        发布人：{{ it.publish_by }} ｜ 发布时间：{{ it.publish_time }}
      </div>
      <pre style="white-space:pre-wrap; margin:0">{{ it.content }}</pre>
      <div style="margin-top:8px">
        <button @click="toggle(it)">{{ it.is_active ? "停用" : "启用" }}</button>
        <button @click="remove(it.id)" style="margin-left:8px">删除</button>
      </div>
    </div>
  </div>
</template>
