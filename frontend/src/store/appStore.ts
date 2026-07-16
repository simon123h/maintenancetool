import { defineStore } from 'pinia';
import { ref } from 'vue';
import { apiFetch } from './api';
import { useAuthStore } from './authStore';

export const useAppStore = defineStore('app', () => {
  const loading = ref(true);
  const errorMsg = ref<string | null>(null);
  const successMsg = ref<string | null>(null);

  const initApp = async () => {
    const authStore = useAuthStore();

    loading.value = true;
    await authStore.loadProfile();
    if (!authStore.currentUser) {
      // If the user is not logged in (or session expired), stop loading here
      // and let the 401 redirect from apiFetch handle the UI transition to /login
      return;
    }
    if (authStore.currentUser?.rollen.includes('ROLE_ADMIN')) {
      await authStore.loadUsers();
    }
    loading.value = false;
  };

  return {
    loading,
    errorMsg,
    successMsg,
    initApp,
    apiFetch, // Expose apiFetch for backward compatible test file setups
  };
});
