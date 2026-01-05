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
