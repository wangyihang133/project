<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import { useUserStore } from "@/stores/user";
import "../../assets/student-common.css"; // 引入通用样式
const userStore = useUserStore();

const loading = ref(false);
const groups = ref<any[]>([]);
const error = ref("");

async function load() {
  loading.value = true;
  error.value = "";
  try {
    // 方案B：传递当前登录用户的username，使用新的安全端点
    const res = await http.get("/student/scores/by-application-username", {
      params: { username: userStore.username }
    });
    // 打印返回结果，确认是数组
    console.log("scores grouped res:", res.data);
    groups.value = res.data || [];
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="student-container">
    <h3 class="student-title">我的录取结果（按报名分组）</h3>
    <button @click="load" class="student-btn" style="margin-bottom:16px">刷新</button>

    <div v-if="error" class="student-text-error">{{ error }}</div>
    <div v-if="loading" class="student-loading">加载中...</div>

    <div v-if="!loading && groups.length === 0" class="student-text-empty">暂无报名/成绩记录</div>

    <div v-if="!loading && groups.length">
      <div v-for="g in groups" :key="g.applicationId" class="student-item-card">
        <!-- 报名基础信息 -->
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px">
          <div>
            <span style="color:#2c3e50">报名ID：<b>{{ g.applicationId }}</b></span>
            <span style="margin-left:10px; color:#606266">
              状态：{{ g.applicationStatus ?? "-" }}
            </span>
          </div>
          <div style="color:#2c3e50">
            总分：<b>{{ g.total }}</b>
          </div>
        </div>

        <!-- 考试信息 -->
        <div style="margin-bottom:12px; color:#606266; font-size:14px; line-height:1.6">
          <div>考试：{{ g.examName ?? "-" }}（{{ g.examType ?? "-" }}）</div>
          <div>专业：{{ g.major ?? g.examMajor ?? "-" }}</div>
          <div>考试时间：{{ g.examTime ?? "-" }}</div>
          <div>报名时间：{{ g.applicationTime ?? "-" }}</div>
        </div>
        
        <!-- 录取结果 -->
        <div style="margin-bottom:12px; padding:12px; background:#f5f7fa; border-radius:6px">
          <div style="margin-bottom:4px">
            录取结果：<b style="color: #67c23a">{{ g.admissionStatus ?? "-" }}</b>
          </div>
          <div style="margin-bottom:4px">分数线：{{ g.minScore != null ? g.minScore : '未设置' }}</div>
          <div>我的总分：{{ g.total }}</div>
        </div>

        <!-- 成绩明细 -->
        <div v-if="g.scores && g.scores.length">
          <table class="student-table">
            <thead>
              <tr>
                <th>科目</th>
                <th>分数</th>
                <th>录入时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in g.scores" :key="s.id">
                <td>{{ s.subject }}</td>
                <td>{{ s.score }}</td>
                <td>{{ s.entryTime }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="student-text-empty" style="margin-top:8px">
          当前报名暂无成绩（可能尚未录入）
        </div>
      </div>
    </div>
  </div>
</template>