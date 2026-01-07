<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";
import "../../assets/student-common.css"; // 引入通用样式

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div class="student-container student-container-wide">
    <h2 class="student-title">考生端</h2>
    <div class="student-subtitle">当前用户：{{ username }}</div>

    <!-- 导航菜单 -->
    <div class="student-nav">
      <router-link to="/student/exam-info" class="student-nav-link">
        报考须知
      </router-link>
      <router-link to="/student/apply" class="student-nav-link">
        在线报名
      </router-link>
      <router-link to="/student/score" class="student-nav-link">
        成绩查询
      </router-link>
      <router-link to="/student/admission" class="student-nav-link">
        录取查询
      </router-link>
    </div>

    <!-- 子页面容器 -->
    <div style="min-height: 500px">
      <router-view />
    </div>
  </div>
</template>