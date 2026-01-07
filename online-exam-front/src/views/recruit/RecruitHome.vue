<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";
import "../../assets/recruit.css";

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div class="recruit-container">
    <h2 class="recruit-title">招生管理</h2>
    <div class="recruit-subtitle">当前用户：{{ username }}</div>

    <div class="recruit-nav">
      <router-link
        v-for="route in [
          { path: '/recruit/exam-info', name: '招考信息维护' },
          { path: '/recruit/exams', name: '考试设置' },
          { path: '/recruit/confirm', name: '报名审核' },
          { path: '/recruit/scoreline', name: '设置分数线' },
          { path: '/recruit/room-assign', name: '考场分配' },
          { path: '/recruit/score-entry', name: '成绩录入' }
        ]"
        :key="route.path"
        :to="route.path"
        class="recruit-nav-link"
      >
        {{ route.name }}
      </router-link>
    </div>

    <div class="recruit-card" style="min-height: 400px;">
      <router-view />
    </div>
  </div>
</template>