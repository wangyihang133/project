<script setup lang="ts">
import { ref } from "vue";
import http from "@/api/http";
import "../../assets/recruit.css";

const applicationId = ref<number | null>(null);
const subject = ref("综合");
const score = ref<number>(0);

const msg = ref("");
const err = ref("");

async function submit() {
  msg.value = "";
  err.value = "";
  try {
    await http.post("/recruit/scores/entry", {
      applicationId: applicationId.value,
      subject: subject.value,
      score: score.value,
    });
    msg.value = "录入成功";
  } catch (e: any) {
    err.value = e?.response?.data || "录入失败";
  }
}
</script>

<template>
  <div>
    <h3 class="recruit-title">成绩录入</h3>
    
    <div class="recruit-card">
      <div class="recruit-form-row">
        <div class="recruit-form-group">
          <label>报名ID</label>
          <input v-model.number="applicationId" type="number" class="recruit-input" placeholder="请输入报名记录ID" />
        </div>
        <div class="recruit-form-group">
          <label>科目</label>
          <input v-model="subject" class="recruit-input" placeholder="如：综合/专业/面试" />
        </div>
        <div class="recruit-form-group">
          <label>分数</label>
          <input v-model.number="score" type="number" class="recruit-input" placeholder="请输入分数" />
        </div>
      </div>
      
      <div style="margin-top: 24px;">
        <button @click="submit" class="recruit-btn recruit-btn-lg">
          提交成绩
        </button>
      </div>
      
      <div v-if="msg" class="recruit-msg recruit-msg-success" style="margin-top: 20px;">
        {{ msg }}
      </div>
      <div v-if="err" class="recruit-msg recruit-msg-error" style="margin-top: 20px;">
        {{ err }}
      </div>
    </div>
  </div>
</template>