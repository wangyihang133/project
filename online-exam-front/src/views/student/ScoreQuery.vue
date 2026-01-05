<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import { useUserStore } from "@/stores/user";
import "../../assets/student-common.css"; // 引入通用样式

const userStore = useUserStore();

const loading = ref(false);
const error = ref("");

// 查询条件：报名ID + 考试/时间筛选
const applicationId = ref<string>("");
const examYear = ref<number | null>(null);
const examType = ref("");
const fromDate = ref("");
const toDate = ref("");

// 支持两种显示模式：单个报名的 `data` 或 多组 `groups`
const data = ref<any>(null);
const groups = ref<any[]>([]);

async function query() {
  loading.value = true;
  error.value = "";
  data.value = null;
  groups.value = [];
  try {
    if (!applicationId.value) {
      // 空查询：按当前登录用户获取所有成绩并按 applicationId 分组
      const username = userStore?.username?.toString()?.trim();
      if (!username) {
        // 未登录：不请求后端，直接返回空
        groups.value = [];
        return;
      }

      const res = await http.get("/student/scores/by-application-username", {
        params: { username },
      });
      const resp = res.data || [];

      // 如果服务器返回扁平列表则按 applicationId 分组
      if (Array.isArray(resp) && resp.length > 0) {
        if (resp[0] && resp[0].scores) {
          // 已分组结构
          groups.value = resp;
        } else {
          // 扁平结构 -> group
          const map = new Map<any, any>();
          resp.forEach((s: any) => {
            const aid = s.applicationId ?? (s.application && s.application.id) ?? "unknown";
            if (!map.has(aid)) {
              map.set(aid, {
                applicationId: aid,
                total: 0,
                scores: [],
              });
            }
            const g = map.get(aid);
            g.scores.push({ id: s.id, subject: s.subject, score: s.score, entry_time: s.entry_time ?? s.entryTime });
            g.total = (Number(g.total) || 0) + (Number(s.score) || 0);
          });
          groups.value = Array.from(map.values());
        }
      } else {
        groups.value = [];
      }
    } else {
      // 有报名ID：正常按报名ID查询单条记录
      const res = await http.get("/student/scores/query", {
        params: {
          applicationId: applicationId.value || undefined,
          examYear: examYear.value || undefined,
          examType: examType.value || undefined,
          fromDate: fromDate.value || undefined,
          toDate: toDate.value || undefined,
        },
      });
      data.value = res.data;
    }
  } catch (e: any) {
    error.value = e?.response?.data || "加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(query);
</script>

<template>
  <div class="student-container">
    <h3 class="student-title">成绩查询（按报名ID，可筛选考试和时间）</h3>

    <!-- 查询条件 -->
    <div class="student-filter-card">
      <div style="font-weight:600; margin-bottom:12px">查询条件</div>
      <div class="student-grid">
        <div>报名ID</div>
        <input v-model="applicationId" placeholder="如：在报名记录中看到的ID" class="student-input" />

        <div>考试年份</div>
        <input v-model.number="examYear" type="number" placeholder="可选" class="student-input" />

        <div>考试类型</div>
        <input v-model="examType" placeholder="如：自主招生，留空为不限" class="student-input" />

        <div>成绩录入时间从</div>
        <input v-model="fromDate" type="date" class="student-input" />

        <div>成绩录入时间到</div>
        <input v-model="toDate" type="date" class="student-input" />
      </div>
      <button @click="query" class="student-btn">查询成绩</button>
    </div>

    <!-- 提示信息 -->
    <div v-if="error" class="student-text-error">{{ error }}</div>
    <div v-if="loading" class="student-loading">加载中...</div>

    <!-- 多组成绩展示 -->
    <div v-if="!loading && groups && groups.length">
      <div style="margin-bottom:12px; color:#606266">用户：{{ userStore.username || '-' }}</div>
      <div v-for="g in groups" :key="g.applicationId" class="student-item-card">
        <div style="margin-bottom:8px">
          <span style="color:#2c3e50">报名ID：<b>{{ g.applicationId }}</b></span>
          <span style="margin-left:16px; color:#2c3e50">总分：<b>{{ g.total }}</b></span>
        </div>

        <table v-if="g.scores && g.scores.length" class="student-table">
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
              <td>{{ s.entry_time }}</td>
            </tr>
          </tbody>
        </table>

        <div v-else class="student-text-empty">暂无成绩（管理员尚未录入）</div>
      </div>
    </div>

    <!-- 单条成绩展示 -->
    <div v-else-if="!loading && data">
      <div style="margin-bottom:8px; color:#606266">报名ID：{{ data.applicationId ?? "暂无" }}</div>
      <div style="margin-bottom:12px; color:#2c3e50">总分：<b>{{ data.total }}</b></div>

      <table v-if="data.scores && data.scores.length" class="student-table">
        <thead>
          <tr>
            <th>科目</th>
            <th>分数</th>
            <th>录入时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in data.scores" :key="s.id">
            <td>{{ s.subject }}</td>
            <td>{{ s.score }}</td>
            <td>{{ s.entry_time }}</td>
          </tr>
        </tbody>
      </table>

      <div v-else class="student-text-empty">暂无成绩（管理员尚未录入）</div>
    </div>
  </div>
</template>