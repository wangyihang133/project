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
    // 如果没有登录用户名，视为未查询 -> 显示为空列表而不是错误
    if (!userStore?.username) {
      groups.value = [];
      return;
    }

    // 方案B：传递当前登录用户的username，使用新的安全端点
    const res = await http.get("/student/scores/by-application-username", {
      params: { username: userStore.username }
    });
    // 打印返回结果，确认是数组
    console.log("scores grouped res:", res.data);
      const data = res.data || [];

      // 使用当前登录用户名做客户端过滤，防止后端未按用户名筛选而返回全部成绩
      const username = userStore?.username?.toString()?.trim();
      let filtered = Array.isArray(data) ? data : [];

      if (username) {
        if (filtered[0] && filtered[0].scores) {
          // 已分组结构：保留与 username 相关的组（尽可能多的字段匹配）
          filtered = filtered.filter((g: any) => {
            if (!g) return false;
            if (g.username === username) return true;
            if (g.application && (g.application.username === username || g.application.userName === username)) return true;
            if (g.scores && g.scores.some((s: any) => s.username === username || s.user?.username === username)) return true;
            return false;
          });
        } else {
          // 扁平记录：按可能的字段匹配 username
          filtered = filtered.filter((s: any) => {
            if (!s) return false;
            if (s.username === username) return true;
            if (s.user && (s.user.username === username || s.userName === username)) return true;
            if (s.application && (s.application.username === username || s.application.userName === username)) return true;
            if (s.applicantUsername === username || s.studentUsername === username) return true;
            return false;
          });
        }

        // 如果后台返回了很多记录但过滤后为空，说明后端未按用户过滤——不要展示全量数据
        if (filtered.length === 0 && Array.isArray(data) && data.length > 0) {
          groups.value = [];
          error.value = "";
          return;
        }
      }

      if (!Array.isArray(filtered) || filtered.length === 0) {
        groups.value = [];
      } else if (filtered[0] && filtered[0].scores) {
        // already grouped by backend (and possibly filtered)
        groups.value = filtered;
      } else {
        // backend returned flat list of score records -> group by applicationId
        const map = new Map<any, any>();
        filtered.forEach((s: any) => {
          const aid = s.applicationId ?? (s.application && s.application.id) ?? "unknown";
          if (!map.has(aid)) {
            map.set(aid, {
              applicationId: aid,
              applicationStatus: s.applicationStatus ?? null,
              examName: s.examName ?? null,
              examType: s.examType ?? null,
              major: s.major ?? s.examMajor ?? null,
              examTime: s.examTime ?? null,
              applicationTime: s.applicationTime ?? null,
              admissionStatus: s.admissionStatus ?? null,
              minScore: s.minScore ?? null,
              scores: [],
              total: 0,
            });
          }
          const g = map.get(aid);
          g.scores.push({ id: s.id, subject: s.subject, score: s.score, entryTime: s.entryTime });
          g.total = (Number(g.total) || 0) + (Number(s.score) || 0);
        });
        groups.value = Array.from(map.values());
      }
  } catch (e: any) {
    const status = e?.response?.status;
    // 对于常见的“无数据”响应（400/404），不要显示错误信息，改为显示空列表
    if (status === 400 || status === 404) {
      groups.value = [];
      error.value = "";
    } else {
      // 优化错误信息显示：优先使用后端返回的 message 字段
      const data = e?.response?.data;
      error.value = (data && (data.message || data)) || e?.message || "加载失败";
    }
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