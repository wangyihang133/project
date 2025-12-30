<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { computed, ref } from 'vue'
import HelloWorld from './components/HelloWorld.vue'
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
    <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="125" height="125" />

    <div class="top-right" v-if="isLoggedIn">
      <span class="user-name">{{ displayName }}</span>
      <div style="display:flex;flex-direction:column;gap:6px;align-items:flex-end">
        <button class="logout-btn" @click="logout">退出登录</button>
        <button class="history-btn" @click="openHistory(false)">查询登录记录</button>
      </div>
    </div>

    <div class="wrapper">
      <HelloWorld msg="You did it!" />

      <nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/about">About</RouterLink>
      </nav>
    </div>
  </header>

  <RouterView />

  <aside v-if="showHistory" class="history-drawer">
    <div class="history-drawer-inner">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <h3>登录记录</h3>
        <div>
          <button class="close-btn" @click="showHistory = false">关闭</button>
        </div>
      </div>

      <section style="margin-top:12px;border-bottom:1px solid #eee;padding-bottom:12px">
        <div style="display:flex;flex-direction:column;gap:8px">
          <div style="display:flex;gap:8px;align-items:center">
            <label style="font-size:12px">开始时间</label>
            <input type="datetime-local" v-model="startTime" />
            <label style="font-size:12px">结束时间</label>
            <input type="datetime-local" v-model="endTime" />
          </div>

          <div v-if="isAdmin" style="display:flex;gap:8px;align-items:center">
            <label style="font-size:12px">管理员选项</label>
            <input type="checkbox" v-model="historyQueryAll" />
            <span style="font-size:12px">查询所有用户</span>
          </div>

          <div v-if="isAdmin" style="display:flex;gap:8px;align-items:center">
            <input v-model="filterUsername" placeholder="按用户名筛选（管理员）" style="padding:6px;border:1px solid var(--color-border);border-radius:4px;flex:1" />
            <button class="history-btn" @click="queryByUsername">按用户名查询</button>
          </div>

          <div style="display:flex;gap:8px;justify-content:flex-end">
            <button class="history-btn" @click="queryHistory(historyQueryAll)">查询</button>
            <button class="history-btn" @click="clearFilters">清除</button>
          </div>
        </div>
      </section>
      <table>
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

.user-name {
  font-size: 14px;
  color: var(--color-text);
}

.logout-btn {
  background: transparent;
  border: 1px solid var(--color-border);
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.logout-btn:hover {
  background: var(--color-border);
}

.history-btn {
  background: transparent;
  border: 1px solid var(--color-border);
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
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
  background: #fff;
  width: 100%;
  padding: 16px;
  box-shadow: -4px 0 12px rgba(0,0,0,0.15);
  overflow: auto;
}
</style>
