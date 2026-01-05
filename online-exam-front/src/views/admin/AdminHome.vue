<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div class="admin-container">
    <div class="page-header">
      <h2>系统管理员</h2>
      <div class="user-info">当前用户：{{ username }}</div>
    </div>

    <div class="nav-panel">
      <router-link to="/admin/applications" class="nav-link">报名信息</router-link>
      <router-link to="/admin/scores" class="nav-link">成绩信息</router-link>
      <router-link to="/admin/admin-users" class="nav-link">管理员维护</router-link>
      <router-link to="/admin/reset-pwd" class="nav-link">学生密码重置</router-link>
      <router-link to="/admin/db-maint" class="nav-link">数据库维护</router-link>
    </div>

    <div class="content-container">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.admin-container {
  width: 1200px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-size: 1.75rem;
  font-weight: 600;
}

.user-info {
  color: #4b5563;
  font-size: 14px;
}

.nav-panel {
  display: flex; 
  gap: 16px; 
  flex-wrap: wrap; 
  margin-bottom: 24px;
  padding: 20px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.nav-link {
  text-decoration: none;
  color: #165dff;
  padding: 10px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  background-color: white;
}

.nav-link:hover {
  background-color: #f0f7ff;
  border-color: #165dff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(22, 93, 255, 0.1);
}

.content-container {
  border-top: 1px solid #eee;
  padding-top: 20px;
}
</style>