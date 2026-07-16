import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../store/authStore';
import { useAuth } from '../composables/useAuth';
import DashboardView from '../views/DashboardView.vue';
import AdminView from '../views/AdminView.vue';

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: DashboardView,
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { requiresAdmin: true },
  },
  // Catch-all redirects to home
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  // Proactively fetch user profile if store is empty to ensure guard works
  if (!authStore.currentUser) {
    try {
      await authStore.loadProfile();
    } catch (err) {
      console.error('Failed to load user profile in router guard:', err);
    }
  }

  const { isAdmin } = useAuth();

  if (to.meta.requiresAdmin) {
    if (isAdmin.value) {
      next();
    } else {
      next('/');
    }
  } else {
    next();
  }
});

export default router;
