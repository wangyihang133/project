import { defineStore } from "pinia";

export type UserRole = "STUDENT" | "ADMIN" | "RECRUIT" | "";

export const useUserStore = defineStore("user", {
  state: () => ({
    token: "",
    username: "",
    role: "" as UserRole,
  }),
  actions: {
    setLogin(payload: { token: string; username: string; role: UserRole }) {
      this.token = payload.token;
      this.username = payload.username;
      this.role = payload.role;
    },
    logout() {
      this.token = "";
      this.username = "";
      this.role = "";
    },
  },
});
