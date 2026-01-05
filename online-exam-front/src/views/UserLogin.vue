<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">用户登录</h2>

      <!-- 用户名输入框 -->
      <div class="input-group">
        <label class="input-label" for="username">用户名</label>
        <input id="username" v-model="username" @input="clearError" placeholder="请输入用户名" class="input-field" />
      </div>

      <!-- 密码输入框 -->
      <div class="input-group">
        <label class="input-label" for="password">密码</label>
        <input id="password" v-model="password" @input="clearError" type="password" placeholder="请输入密码"
          class="input-field" />
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

      <!-- 登录按钮 -->
      <button @click="login" class="login-btn" :disabled="isLoading">
        <span v-if="isLoading" class="loading-spinner"></span>
        {{ isLoading ? '登录中...' : '登录' }}
      </button>

      <!-- 注册链接 -->
      <div class="register-link">
        没有账号？<RouterLink to="/register" class="link-text">去注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import http from "../api/http";
import { useUserStore, type UserRole } from "../stores/user";

const router = useRouter();
const userStore = useUserStore();

// 响应式数据
const username = ref("");
const password = ref("");
const errorMessage = ref("");
const isLoading = ref(false); // 登录加载状态

type LoginResp = {
  token: string;
  username: string;
  role: UserRole;
};

// 登录处理
const login = async () => {
  // 表单验证
  if (!username.value.trim()) {
    errorMessage.value = "请输入用户名";
    return;
  }
  if (!password.value.trim()) {
    errorMessage.value = "请输入密码";
    return;
  }

  try {
    errorMessage.value = '';
    isLoading.value = true; // 开始加载

    const res = await http.post<LoginResp>('/user/login', {
      username: username.value.trim(),
      password: password.value.trim(),
    });

    // 登录成功处理
    userStore.setLogin(res.data);

    // 根据角色跳转对应页面
    const roleRoutes: Record<UserRole, string> = {
      STUDENT: '/student',
      ADMIN: '/admin',
      RECRUIT: '/recruit',
      "": ""
    };

    await router.push(roleRoutes[res.data.role] || '/');

  } catch (err: any) {
    // 错误处理
    const resp = err?.response;
    if (resp) {
      if (resp.status === 404) {
        errorMessage.value = '账号不存在';
        return;
      }
      if (resp.status === 401) {
        errorMessage.value = '密码错误';
        return;
      }
      if (resp.status === 500) {
        errorMessage.value = '服务器错误，请稍后重试';
        return;
      }
    }
    errorMessage.value = '登录失败，请检查网络或账号信息';
  } finally {
    isLoading.value = false; // 结束加载
  }
};

// 清除错误提示
const clearError = () => {
  if (errorMessage.value) {
    errorMessage.value = '';
  }
};
</script>

<style scoped>
/* 登录容器 - 关键：透明背景，让base.css的背景图显示出来 */
.login-container {
  /* 铺满整个屏幕 */
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  /* 居中显示登录卡片 */
  display: flex;
  align-items: center;
  justify-content: center;
  /* 背景透明，不遮挡base.css的背景图 */
  background-color: transparent;
  /* 增加轻微遮罩，提升卡片可读性，可根据需要调整透明度 */
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  /* 确保在背景图上层 */
  z-index: 10;
  /* 预留padding，适配移动端 */
  padding: 20px;
  box-sizing: border-box;
}

/* 登录卡片 - 半透明磨砂效果，融合背景图 */
.login-card {
  width: 100%;
  max-width: 360px;
  /* 半透明白色背景，既能看清内容又能透出背景图 */
  background-color: rgba(255, 255, 255, 0.9);
  /* 磨砂玻璃效果，增强融合感 */
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  /* 轻微边框，提升层次感 */
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  /* 柔和阴影，突出卡片但不突兀 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  /* 内边距优化 */
  padding: 32px 24px;
  box-sizing: border-box;
}

/* 登录标题 */
.login-title {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  text-align: center;
}

/* 输入组 */
.input-group {
  margin-bottom: 16px;
}

/* 输入框样式优化 */
.input-field {
  width: 100%;
  padding: 12px 14px;
  margin: 0;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #2d3748;
  background-color: rgba(255, 255, 255, 0.8);
  box-sizing: border-box;
  transition: all 0.2s ease;
}

/* 输入框聚焦效果 */
.input-field:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 2px rgba(66, 153, 225, 0.1);
  background-color: #ffffff;
}

/* 输入框占位符样式 */
.input-field::placeholder {
  color: #a0aec0;
}

/* 错误提示样式优化 */
.error-message {
  color: #e53e3e;
  margin: 8px 0 16px 0;
  font-size: 14px;
  padding: 10px;
  background-color: rgba(229, 62, 62, 0.08);
  border-radius: 6px;
  line-height: 1.4;
}

/* 登录按钮样式 */
.login-btn {
  width: 100%;
  margin-top: 8px;
  padding: 12px;
  background-color: #4299e1;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

/* 按钮hover效果 */
.login-btn:hover {
  background-color: #3182ce;
}

/* 按钮禁用/加载状态（可选扩展） */
.login-btn:disabled {
  background-color: #90cdf4;
  cursor: not-allowed;
}

/* 注册链接样式 */
.register-link {
  margin-top: 20px;
  font-size: 14px;
  color: #718096;
  text-align: center;
  margin-bottom: 0;
}

/* 链接文本样式 */
.link-text {
  color: #4299e1;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.link-text:hover {
  color: #3182ce;
  text-decoration: underline;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .login-card {
    padding: 24px 20px;
  }
  
  .login-title {
    font-size: 18px;
  }
}
</style>