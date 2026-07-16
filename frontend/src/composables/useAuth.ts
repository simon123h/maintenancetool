import { computed } from 'vue';
import { useAuthStore } from '../store/authStore';

export function useAuth() {
  const store = useAuthStore();

  const currentUser = computed(() => store.currentUser);

  const isAuthenticated = computed(() => !!store.currentUser);

  const isAdmin = computed(() => {
    return store.currentUser?.rollen.includes('ROLE_ADMIN') ?? false;
  });

  const isManager = computed(() => {
    return store.currentUser?.rollen.includes('ROLE_MANAGER') ?? false;
  });

  const isTeilnehmer = computed(() => {
    return store.currentUser?.rollen.includes('ROLE_TEILNEHMER') ?? false;
  });

  const hasRole = (role: string) => {
    return store.currentUser?.rollen.includes(role) ?? false;
  };

  return {
    currentUser,
    isAuthenticated,
    isAdmin,
    isManager,
    isTeilnehmer,
    hasRole,
  };
}
