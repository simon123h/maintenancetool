import { defineStore } from 'pinia';
import { ref, watch } from 'vue';
import type { UserAccount } from '../types';
import { apiFetch } from './api';
import { useAppStore } from './appStore';

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref<UserAccount | null>(null);
  const users = ref<UserAccount[]>([]);

  // Load from localStorage on initialization
  const savedState = localStorage.getItem('maintenance_tool_auth') || localStorage.getItem('maintenance_tool_store');
  if (savedState) {
    try {
      const parsed = JSON.parse(savedState);
      if (parsed.currentUser) currentUser.value = parsed.currentUser;
      if (parsed.users) users.value = parsed.users;
    } catch (err) {
      console.error('Failed to restore auth store from localStorage:', err);
    }
  }

  // Watch state changes and save to localStorage
  watch(
    () => ({
      currentUser: currentUser.value,
      users: users.value,
    }),
    (state) => {
      localStorage.setItem('maintenance_tool_auth', JSON.stringify(state));
    },
    { deep: true }
  );

  const loadProfile = async () => {
    const appStore = useAppStore();
    try {
      currentUser.value = await apiFetch<UserAccount>('/api/users/me');
    } catch (err: any) {
      appStore.errorMsg = 'Benutzerprofil konnte nicht geladen werden: ' + err.message;
    }
  };

  const loadUsers = async () => {
    const appStore = useAppStore();
    try {
      users.value = await apiFetch<UserAccount[]>('/api/users');
    } catch (err: any) {
      appStore.errorMsg = 'Benutzerliste konnte nicht geladen werden: ' + err.message;
    }
  };

  const toggleRole = async (user: UserAccount, role: string) => {
    const appStore = useAppStore();
    const newRoles = new Set(user.rollen);
    if (newRoles.has(role)) {
      newRoles.delete(role);
    } else {
      newRoles.add(role);
    }
    if (!newRoles.has('ROLE_TEILNEHMER')) {
      newRoles.add('ROLE_TEILNEHMER');
    }
    try {
      appStore.errorMsg = null;
      appStore.successMsg = null;
      await apiFetch(`/api/users/${user.id}/roles`, {
        method: 'PUT',
        body: JSON.stringify(Array.from(newRoles)),
      });
      appStore.successMsg = `Rollen für ${user.vollerName} erfolgreich aktualisiert!`;
      await loadUsers();
      if (user.id === currentUser.value?.id) {
        await loadProfile();
      }
    } catch (err: any) {
      appStore.errorMsg = err.message;
      throw err;
    }
  };

  return {
    currentUser,
    users,
    loadProfile,
    loadUsers,
    toggleRole,
  };
});
