<script setup lang="ts">
import { onMounted, ref } from "vue";
import http from "@/api/http";
import { useUserStore } from "@/stores/user";

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
  <div>
    <h3>成绩查询（按报名ID，可筛选考试和时间）</h3>

    <div
      style="border:1px solid #eee; border-radius:8px; padding:12px; max-width:720px; margin-bottom:12px"
    >
      <div style="font-weight:600; margin-bottom:8px">查询条件</div>
      <div style="display:grid; grid-template-columns: 120px 1fr; gap:8px; max-width:520px">
        <div>报名ID</div>
        <input v-model="applicationId" placeholder="如：在报名记录中看到的ID" />

        <div>考试年份</div>
        <input v-model.number="examYear" type="number" placeholder="可选" />

        <div>考试类型</div>
        <input v-model="examType" placeholder="如：自主招生，留空为不限" />

        <div>成绩录入时间从</div>
        <input v-model="fromDate" type="date" />

        <div>成绩录入时间到</div>
        <input v-model="toDate" type="date" />
      </div>
      <button @click="query" style="margin-top:8px">查询成绩</button>
    </div>

    <div v-if="error" style="color:#b00020; margin-top:8px">{{ error }}</div>
    <div v-if="loading" style="margin-top:8px">加载中...</div>

    <div v-if="!loading && groups && groups.length">
      <div style="margin-top:10px">用户：{{ userStore.username || '-' }}</div>
      <div style="margin-top:8px">
        <div v-for="g in groups" :key="g.applicationId" style="border:1px solid #eee; padding:8px; margin-bottom:8px">
          <div>报名ID：<b>{{ g.applicationId }}</b> &nbsp; 总分：<b>{{ g.total }}</b></div>

          <table v-if="g.scores && g.scores.length" border="1" cellspacing="0" cellpadding="6" style="border-collapse:collapse; width:100%; max-width:720px; margin-top:8px">
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

          <div v-else style="color:#666; margin-top:8px">暂无成绩（管理员尚未录入）</div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading && data">
      <div style="margin-top:10px">报名ID：{{ data.applicationId ?? "暂无" }}</div>
      <div style="margin:6px 0">总分：<b>{{ data.total }}</b></div>

      <table v-if="data.scores && data.scores.length" border="1" cellspacing="0" cellpadding="6" style="border-collapse:collapse; width:100%; max-width:720px">
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

      <div v-else style="color:#666; margin-top:8px">暂无成绩（管理员尚未录入）</div>
    </div>
  </div>
</template>
