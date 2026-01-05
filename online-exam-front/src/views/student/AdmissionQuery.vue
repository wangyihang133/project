<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import { useUserStore } from "@/stores/user";
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
  <div>
    <h3>我的录取结果（按报名分组）</h3>
    <button @click="load">刷新</button>

    <div v-if="error" style="color:#b00020; margin-top:8px">
      {{ error }}
    </div>
    <div v-if="loading" style="margin-top:8px">加载中...</div>

    <div v-if="!loading && groups.length === 0" style="margin-top:10px">
      暂无报名/成绩记录
    </div>

    <div v-if="!loading && groups.length" style="margin-top:12px">
      <div
        v-for="g in groups"
        :key="g.applicationId"
        style="border:1px solid #ddd; padding:12px; border-radius:6px; margin-bottom:12px"
      >
        <div style="display:flex; justify-content:space-between; align-items:center">
          <div>
            <b>报名ID：</b>{{ g.applicationId }}
            <span style="margin-left:10px; color:#666">
              状态：{{ g.applicationStatus ?? "-" }}
            </span>
          </div>
          <div style="color:#666">
            总分：<b>{{ g.total }}</b>
          </div>
        </div>

        <div style="margin-top:8px; color:#444; font-size:14px">
          <div>考试：{{ g.examName ?? "-" }}（{{ g.examType ?? "-" }}）</div>
          <div>专业：{{ g.major ?? g.examMajor ?? "-" }}</div>
          <div>考试时间：{{ g.examTime ?? "-" }}</div>
          <div>报名时间：{{ g.applicationTime ?? "-" }}</div>
        </div>
        
        <div style="margin-top:10px; padding:10px; background:#f7f7f7; border-radius:6px">
          <div>录取结果：<b>{{ g.admissionStatus ?? "-" }}</b></div>
          <div>分数线：{{ g.minScore != null ? g.minScore : '未设置' }}</div>
          <div>我的总分：{{ g.total }}</div>
        </div>

        <div style="margin-top:10px" v-if="g.scores && g.scores.length">
          <table style="width:100%; border-collapse:collapse">
            <thead>
              <tr style="background:#fafafa">
                <th style="border:1px solid #eee; padding:6px; text-align:left">科目</th>
                <th style="border:1px solid #eee; padding:6px; text-align:left">分数</th>
                <th style="border:1px solid #eee; padding:6px; text-align:left">录入时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in g.scores" :key="s.id">
                <td style="border:1px solid #eee; padding:6px">{{ s.subject }}</td>
                <td style="border:1px solid #eee; padding:6px">{{ s.score }}</td>
                <td style="border:1px solid #eee; padding:6px">{{ s.entryTime }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else style="margin-top:10px; color:#999">
          当前报名暂无成绩（可能尚未录入）
        </div>
      </div>
    </div>
  </div>
</template>
