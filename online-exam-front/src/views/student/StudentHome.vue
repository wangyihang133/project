<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div style="padding: 16px">
    <h2>考生端</h2>
    <div style="margin: 8px 0; color: #444">当前用户：{{ username }}</div>

    <div style="display: flex; gap: 12px; flex-wrap: wrap; margin: 12px 0">
      <router-link to="/student/exam-info">报考须知</router-link>
      <router-link to="/student/apply">在线报名</router-link>
      <router-link to="/student/score">成绩查询</router-link>
      <router-link to="/student/admission">录取查询</router-link>
    </div>

    <div style="border-top: 1px solid #eee; padding-top: 12px">
      <router-view />
    </div>
  </div>
</template>
