<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";

const seatsPerRoom = ref<number>(30);
const roomPrefix = ref("A");
const startRoom = ref<number>(101);
const examDate = ref("");
const examTime = ref("09:00-11:00");
const address = ref("教学楼A区");

const msg = ref("");
const err = ref("");

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
    });
    msg.value = `分配完成，已分配 ${res.data?.assigned ?? 0} 条`;
  } catch (e: any) {
    err.value = e?.response?.data || "分配失败";
  }
}
</script>

<template>
  <div>
    <h3>考场分配（对已确认且未分配的报名记录）</h3>
    <div style="display:grid; grid-template-columns:140px 1fr; gap:8px; max-width:560px">
      <div>每考场座位数</div>
      <input v-model.number="seatsPerRoom" type="number" />
      <div>考场前缀</div>
      <input v-model="roomPrefix" placeholder="如 A" />
      <div>起始考场号</div>
      <input v-model.number="startRoom" type="number" />
      <div>考试日期</div>
      <input v-model="examDate" placeholder="yyyy-MM-dd（可空）" />
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
