import { createRouter, createWebHistory } from "vue-router";
import UserLogin from "../views/UserLogin.vue";
import Register from "../views/Register.vue";
import StudentHome from "../views/student/StudentHome.vue";
import AdminHome from "../views/admin/AdminHome.vue";
import RecruitHome from "../views/recruit/RecruitHome.vue";
import { useUserStore } from "../stores/user";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/login" },
    { path: "/login", component: UserLogin },
    { path: "/register", component: Register },

    { path: "/student", component: StudentHome },
    { path: "/admin", component: AdminHome },
    { path: "/recruit", component: RecruitHome },
  ],
});

router.beforeEach((to) => {
  const user = useUserStore();

  // 登录/注册放行
  if (to.path === "/login" || to.path === "/register") return true;

  // 其他页面需要登录
  if (!user.token) return "/login";

  return true;
});

export default router;
