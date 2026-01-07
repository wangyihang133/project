<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { computed, ref } from 'vue'
import { useUserStore } from './stores/user'
import http from './api/http'

const userStore = useUserStore()
const router = useRouter()

const isLoggedIn = computed(() => !!userStore.token || !!userStore.username)
const displayName = computed(() => userStore.username || '')
// 只有系统管理员(ADMIN) 才能查看所有用户登录记录、使用管理员筛选功能
const isAdmin = computed(() => userStore.role === 'ADMIN')

async function logout() {
  try {
    await http.post('/user/logout')
  } catch (e) {
    // ignore errors — still clear local state
  }
  userStore.logout()
  router.push('/login')
}

const showHistory = ref(false)
const histories = ref<any[]>([])
const filterUsername = ref('')
const startTime = ref('')
const endTime = ref('')
const historyQueryAll = ref(false)

function openHistory(all = false) {
  historyQueryAll.value = all
  // clear previous results when opening
  histories.value = []
  showHistory.value = true
}

function clearFilters() {
  startTime.value = ''
  endTime.value = ''
  filterUsername.value = ''
  historyQueryAll.value = false
}

async function queryByUsername() {
  if (!filterUsername.value) {
    alert('请输入用户名')
    return
  }
  try {
    const params = new URLSearchParams()
    params.append('username', filterUsername.value)
    if (startTime.value) params.append('startTime', startTime.value)
    if (endTime.value) params.append('endTime', endTime.value)
    const url = '/user/login-history?' + params.toString()
    const res = await http.get<any[]>(url)
    const list = res.data || []
    histories.value = list.map((it: any) => ({
      id: it.id,
      loginTime: it.loginTime || it.login_time,
      displayTime: it.loginTime || it.login_time ? new Date(it.loginTime || it.login_time).toLocaleString() : '',
      ipAddress: it.ipAddress || it.ip_address,
      userAgent: it.userAgent || it.user_agent,
      username: it.username || it.userName || ''
    }))
    showHistory.value = true
  } catch (e) {
    alert('查询失败')
  }
}

async function queryHistory(all = false) {
  try {
    const params = new URLSearchParams()
    if (all) params.append('all', 'true')
    if (startTime.value) params.append('startTime', startTime.value)
    if (endTime.value) params.append('endTime', endTime.value)
    const url = '/user/login-history' + (params.toString() ? ('?' + params.toString()) : '')
    const res = await http.get<any[]>(url)
    const list = res.data || []
    histories.value = list.map((it: any) => ({
      id: it.id,
      loginTime: it.loginTime || it.login_time,
      displayTime: it.loginTime || it.login_time ? new Date(it.loginTime || it.login_time).toLocaleString() : '',
      ipAddress: it.ipAddress || it.ip_address,
      userAgent: it.userAgent || it.user_agent,
      username: it.username || it.userName || ''
    }))
    showHistory.value = true
  } catch (e) {
    alert('查询登录记录失败')
  }
}
</script>

<template>
  <header>
    <div class="top-right" v-if="isLoggedIn">
      <span class="user-name">{{ displayName }}</span>
      <div class="top-right-actions">
        <button class="logout-btn" @click="logout">退出登录</button>
        <button class="history-btn primary" @click="openHistory(false)">查询登录记录</button>
      </div>
    </div>
  </header>

  <RouterView />

  <aside v-if="showHistory" class="history-drawer">
    <div class="history-drawer-inner">
      <div class="history-header">
        <h3 class="history-title">登录记录</h3>
        <button class="close-btn" @click="showHistory = false">关闭</button>
      </div>

      <section class="history-filter">
        <div class="history-filter-body">
          <div class="history-row">
            <label class="history-label">开始时间</label>
            <input type="datetime-local" v-model="startTime" class="history-input" />
            <label class="history-label">结束时间</label>
            <input type="datetime-local" v-model="endTime" class="history-input" />
          </div>

          <div v-if="isAdmin" class="history-row">
            <label class="history-label">管理员选项</label>
            <input type="checkbox" v-model="historyQueryAll" />
            <span class="history-tip">查询所有用户</span>
          </div>

          <div v-if="isAdmin" class="history-row">
            <input
              v-model="filterUsername"
              placeholder="按用户名筛选（管理员）"
              class="history-input history-input--full"
            />
            <button class="history-btn primary" @click="queryByUsername">按用户名查询</button>
          </div>

          <div class="history-actions">
            <button class="history-btn primary" @click="queryHistory(historyQueryAll)">查询</button>
            <button class="history-btn" @click="clearFilters">清除</button>
          </div>
        </div>
      </section>
      <table class="history-table">
        <thead>
          <tr>
            <th>时间</th>
            <th v-if="isAdmin">用户名</th>
            <th>IP</th>
            <th>Agent</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="h in histories" :key="h.id">
            <td>{{ h.displayTime }}</td>
            <td v-if="isAdmin">{{ h.username }}</td>
            <td>{{ h.ipAddress }}</td>
            <td>{{ h.userAgent }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </aside>
</template>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }
}

.top-right {
  position: absolute;
  right: 16px;
  top: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.top-right-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.user-name {
  font-size: 14px;
  color: var(--color-text);
  padding: 4px 10px;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  background-color: rgba(255, 255, 255, 0.9);
}

.logout-btn {
  background: #ffffff;
  border: 1px solid var(--color-border);
  padding: 6px 14px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background: #edf2f7;
}

.history-btn {
  background: #ffffff;
  border: 1px solid var(--color-border);
  padding: 6px 14px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
}

.history-btn.primary {
  background: #4299e1;
  color: #ffffff;
  border-color: #4299e1;
}

.history-modal {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-content {
  background: #fff;
  padding: 16px;
  border-radius: 6px;
  width: 80%;
  max-width: 800px;
}

.history-content table { width: 100%; border-collapse: collapse; margin-top: 8px }
.history-content th, .history-content td { border: 1px solid #eee; padding: 6px; font-size: 13px }
.close-btn { float: right; }

.history-drawer {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: 420px;
  background: rgba(0,0,0,0.2);
  display: flex;
  align-items: stretch;
  justify-content: flex-end;
  z-index: 1000;
}

.history-drawer-inner {
  background: rgba(255,255,255,0.96);
  width: 100%;
  padding: 18px 18px 16px;
  box-shadow: -6px 0 18px rgba(0,0,0,0.18);
  overflow: auto;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.history-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.close-btn {
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background: #ffffff;
  font-size: 12px;
  cursor: pointer;
}

.close-btn:hover {
  background: #edf2f7;
}

.history-filter {
  margin-top: 10px;
  padding: 12px;
  border-radius: 10px;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
}

.history-filter-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.history-label {
  font-size: 12px;
  color: #4a5568;
}

.history-tip {
  font-size: 12px;
  color: #718096;
}

.history-input {
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  font-size: 13px;
}

.history-input--full {
  flex: 1;
}

.history-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 4px;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 12px;
  font-size: 13px;
}

.history-table th,
.history-table td {
  border: 1px solid #e2e8f0;
  padding: 8px 10px;
}

.history-table thead {
  background-color: #f5f7fa;
}

.history-table tbody tr:nth-child(odd) {
  background-color: #fdfdfd;
}

.history-table tbody tr:hover {
  background-color: #f1f5f9;
}
</style>
