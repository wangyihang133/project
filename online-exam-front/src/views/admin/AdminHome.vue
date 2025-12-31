<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div style="padding: 16px">
    <h2>系统管理员</h2>
    <div style="margin: 8px 0; color: #444">当前用户：{{ username }}</div>

    <div style="display: flex; gap: 12px; flex-wrap: wrap; margin: 12px 0">
      <router-link to="/admin/applications">报名信息</router-link>
      <router-link to="/admin/scores">成绩信息</router-link>
      <router-link to="/admin/admin-users">管理员维护</router-link>
      <router-link to="/admin/reset-pwd">学生密码重置</router-link>
      <router-link to="/admin/db-maint">数据库维护</router-link>
    </div>

    <div style="border-top: 1px solid #eee; padding-top: 12px">
      <router-view />
    </div>
  </div>
</template>
