import { createRouter, createWebHistory } from "vue-router";
import UserLogin from "../views/UserLogin.vue";
import Register from "../views/Register.vue";

import StudentHome from "../views/student/StudentHome.vue";
import StudentExamInfo from "../views/student/ExamInfo.vue";
import StudentApply from "../views/student/Apply.vue";
import StudentScore from "../views/student/ScoreQuery.vue";
import StudentAdmission from "../views/student/AdmissionQuery.vue";

import AdminHome from "../views/admin/AdminHome.vue";
import AdminApplications from "../views/admin/Applications.vue";
import AdminScores from "../views/admin/Scores.vue";
import AdminUsers from "../views/admin/AdminUsers.vue";
import AdminDbMaint from "../views/admin/DbMaint.vue";

import RecruitHome from "../views/recruit/RecruitHome.vue";
import RecruitExamInfoMgr from "../views/recruit/ExamInfoMgr.vue";
import RecruitConfirm from "../views/recruit/Confirm.vue";
import RecruitScoreline from "../views/recruit/Scoreline.vue";
import RecruitRoomAssign from "../views/recruit/RoomAssign.vue";
import RecruitScoreEntry from "../views/recruit/ScoreEntry.vue";
import RecruitResetPwd from "../views/recruit/ResetPwd.vue";

import { useUserStore } from "../stores/user";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/login" },
    { path: "/login", component: UserLogin },
    { path: "/register", component: Register },

    {
      path: "/student",
      component: StudentHome,
      children: [
        { path: "", redirect: "/student/exam-info" },
        { path: "exam-info", component: StudentExamInfo },
        { path: "apply", component: StudentApply },
        { path: "score", component: StudentScore },
        { path: "admission", component: StudentAdmission },
      ],
    },

    {
      path: "/recruit",
      component: RecruitHome,
      children: [
        { path: "", redirect: "/recruit/exam-info" },
        { path: "exam-info", component: RecruitExamInfoMgr },
        { path: "confirm", component: RecruitConfirm },
        { path: "scoreline", component: RecruitScoreline },
        { path: "room-assign", component: RecruitRoomAssign },
        { path: "score-entry", component: RecruitScoreEntry },
        { path: "reset-pwd", component: RecruitResetPwd },
      ],
    },

    {
      path: "/admin",
      component: AdminHome,
      children: [
        { path: "", redirect: "/admin/applications" },
        { path: "applications", component: AdminApplications },
        { path: "scores", component: AdminScores },
        { path: "admin-users", component: AdminUsers },
        { path: "db-maint", component: AdminDbMaint },
      ],
    },
  ],
});

router.beforeEach((to) => {
  const user = useUserStore();

  // 登录/注册放行
  if (to.path === "/login" || to.path === "/register") return true;

  // 其他页面需要登录
  if (!user.token) return "/login";

  // 角色路由保护（前端兜底；后端仍会再做权限校验）
  const path = to.path;
  if (path.startsWith("/admin")) {
    if (user.role !== "ADMIN") return "/login";
  }
  if (path.startsWith("/recruit")) {
    // 招生管理员可进；系统管理员也允许进入招生模块（全局管理）
    if (!(user.role === "RECRUIT" || user.role === "ADMIN")) return "/login";
  }
  if (path.startsWith("/student")) {
    if (user.role !== "STUDENT") return "/login";
  }

  return true;
});

export default router;
