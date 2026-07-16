import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../store/authStore';
import DashboardView from '../views/DashboardView.vue';
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

  // Proactively fetch user profile if store is empty to ensure authentication works
  if (!authStore.currentUser) {
    try {
      await authStore.loadProfile();
    } catch (err) {
      console.error('Failed to load user profile in router guard:', err);
    }
  }

  next();
});

export default router;
