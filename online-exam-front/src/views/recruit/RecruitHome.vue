<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div style="padding: 16px">
    <h2>招生管理员</h2>
    <div style="margin: 8px 0; color: #444">当前用户：{{ username }}</div>

    <div style="display: flex; gap: 12px; flex-wrap: wrap; margin: 12px 0">
      <router-link to="/recruit/exam-info">招考信息维护</router-link>
      <router-link to="/recruit/exams">考试设置</router-link>
      <router-link to="/recruit/confirm">现场确认</router-link>
      <router-link to="/recruit/scoreline">设置分数线</router-link>
      <router-link to="/recruit/room-assign">考场分配</router-link>
      <router-link to="/recruit/score-entry">成绩录入</router-link>
    </div>

    <div style="border-top: 1px solid #eee; padding-top: 12px">
      <router-view />
    </div>
  </div>
</template>
