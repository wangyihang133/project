<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";
import "../../assets/student-common.css"; // 引入通用样式

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div class="student-container" style="max-width: 1200px; padding: 24px">
    <h2 style="color:#2c3e50; margin:0 0 20px 0">考生端</h2>
    <div style="margin: 0 0 20px 0; color: #606266">当前用户：{{ username }}</div>

    <!-- 导航菜单 -->
    <div style="display: flex; gap: 16px; flex-wrap: wrap; margin: 0 0 24px 0; padding-bottom: 20px; border-bottom: 1px solid #e8e8e8">
      <router-link 
        to="/student/exam-info" 
        style="padding: 8px 16px; background-color: #f5f7fa; border-radius: 6px; color: #409eff; text-decoration: none; transition: all 0.2s"
        hover="background-color: #e8f4ff"
      >
        报考须知
      </router-link>
      <router-link 
        to="/student/apply" 
        style="padding: 8px 16px; background-color: #f5f7fa; border-radius: 6px; color: #409eff; text-decoration: none; transition: all 0.2s"
        hover="background-color: #e8f4ff"
      >
        在线报名
      </router-link>
      <router-link 
        to="/student/score" 
        style="padding: 8px 16px; background-color: #f5f7fa; border-radius: 6px; color: #409eff; text-decoration: none; transition: all 0.2s"
        hover="background-color: #e8f4ff"
      >
        成绩查询
      </router-link>
      <router-link 
        to="/student/admission" 
        style="padding: 8px 16px; background-color: #f5f7fa; border-radius: 6px; color: #409eff; text-decoration: none; transition: all 0.2s"
        hover="background-color: #e8f4ff"
      >
        录取查询
      </router-link>
    </div>

    <!-- 子页面容器 -->
    <div style="min-height: 500px">
      <router-view />
    </div>
  </div>
</template>