<script setup lang="ts">
import { useUserStore } from "@/stores/user";
import { computed } from "vue";
import "../../assets/recruit.css";

const user = useUserStore();
const username = computed(() => user.username);
</script>

<template>
  <div class="recruit-container">
    <div class="recruit-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; margin-bottom: 24px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h2 class="recruit-title" style="color: white; border-bottom: none; margin-bottom: 8px;">招生管理系统</h2>
          <div style="opacity: 0.9;">当前用户：{{ username }}</div>
        </div>
        <div style="font-size: 14px; opacity: 0.9;">
          系统版本 v1.0
        </div>
      </div>
    </div>

    <div class="recruit-card" style="margin-bottom: 24px;">
      <h4 class="recruit-subtitle">功能导航</h4>
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-top: 16px;">
        <router-link 
          v-for="route in [
            { path: '/recruit/exam-info', name: '招考信息维护' },
            { path: '/recruit/exams', name: '考试设置' },
            { path: '/recruit/confirm', name: '现场确认' },
            { path: '/recruit/scoreline', name: '设置分数线' },
            { path: '/recruit/room-assign', name: '考场分配' },
            { path: '/recruit/score-entry', name: '成绩录入' }
          ]" 
          :to="route.path"
          style="
            display: block;
            padding: 20px;
            background: white;
            border: 1px solid #eaeaea;
            border-radius: 8px;
            text-align: center;
            text-decoration: none;
            color: #333;
            transition: all 0.2s;
            font-weight: 500;
            box-shadow: 0 2px 4px rgba(0,0,0,0.04);
          "
          :style="{ 
            borderTop: '4px solid #4a90e2',
            '&:hover': {
              transform: 'translateY(-2px)',
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
            }
          }"
          @mouseenter="(e: MouseEvent) => { (e.target as HTMLElement).style.transform = 'translateY(-2px)'; (e.target as HTMLElement).style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)' }"
          @mouseleave="(e: MouseEvent) => { (e.target as HTMLElement).style.transform = ''; (e.target as HTMLElement).style.boxShadow = '0 2px 4px rgba(0,0,0,0.04)' }"
        >
          {{ route.name }}
        </router-link>
      </div>
    </div>

    <div class="recruit-card" style="min-height: 400px;">
      <router-view />
    </div>
  </div>
</template>