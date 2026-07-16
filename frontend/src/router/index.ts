import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../store/authStore';
import { useAuth } from '../composables/useAuth';
import DashboardView from '../views/DashboardView.vue';
import AdminView from '../views/AdminView.vue';
import ApplicationsView from '../views/ApplicationsView.vue';
import ApplicationUsersView from '../views/ApplicationUsersView.vue';
import EmailTemplatesView from '../views/EmailTemplatesView.vue';
import MaintenanceWindowsView from '../views/MaintenanceWindowsView.vue';

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: DashboardView,
  },
  {
    path: '/applications',
    name: 'Applications',
    component: ApplicationsView,
  },
  {
    path: '/application-users',
    name: 'ApplicationUsers',
    component: ApplicationUsersView,
  },
  {
    path: '/email-templates',
    name: 'EmailTemplates',
    component: EmailTemplatesView,
  },
  {
    path: '/maintenance-windows',
    name: 'MaintenanceWindows',
    component: MaintenanceWindowsView,
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
