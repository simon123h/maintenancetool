<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../store/authStore';
import { useAppStore } from '../store/appStore';

const authStore = useAuthStore();
const appStore = useAppStore();

const appCount = ref<number | null>(null);
const windowCount = ref<number | null>(null);
const userCount = ref<number | null>(null);

const loadStats = async () => {
  if (!authStore.currentUser) return;
  try {
    const apps = await appStore.apiFetch<any[]>('/api/applications');
    appCount.value = apps ? apps.length : 0;

    const wins = await appStore.apiFetch<any[]>('/api/maintenance-windows');
    windowCount.value = wins ? wins.length : 0;

    const users = await appStore.apiFetch<any[]>('/api/application-users');
    userCount.value = users ? users.length : 0;
  } catch (err) {
    console.error('Failed to load dashboard stats:', err);
  }
};

onMounted(() => {
  loadStats();
});
</script>

<template>
  <div class="dashboard-container">
    <div class="welcome-card">
      <h1>Willkommen im Maintenance Tool</h1>
      <p class="subtitle" v-if="authStore.currentUser">
        Hallo {{ authStore.currentUser.vollerName }}. Hier können Sie Anwendungen verwalten, Wartungsfenster planen und
        Benachrichtigungen steuern.
      </p>
      <p class="subtitle" v-else>Bitte melden Sie sich an, um das System zu nutzen.</p>
    </div>

    <!-- Quick Stats Row -->
    <div class="stats-row" v-if="authStore.currentUser">
      <div class="stat-card">
        <span class="stat-num">{{ appCount !== null ? appCount : '...' }}</span>
        <span class="stat-label">Registrierte Applikationen</span>
      </div>
      <div class="stat-card">
        <span class="stat-num">{{ windowCount !== null ? windowCount : '...' }}</span>
        <span class="stat-label">Geplante Wartungen</span>
      </div>
      <div class="stat-card">
        <span class="stat-num">{{ userCount !== null ? userCount : '...' }}</span>
        <span class="stat-label">Benachrichtigungsempfänger</span>
      </div>
    </div>

    <div class="grid-layout">
      <!-- Card for Applications -->
      <div class="card hover-effect">
        <div class="card-header">
          <i class="mdi mdi-web icon-color"></i>
          <h3>Anwendungen</h3>
        </div>
        <p>Verwalten Sie die Liste der gehosteten Web-Applikationen und ordnen Sie ihnen Endbenutzer zu.</p>
        <router-link to="/applications" class="action-link"
          >Zur Verwaltung <i class="mdi mdi-arrow-right"></i
        ></router-link>
      </div>

      <!-- Card for Maintenance Windows -->
      <div class="card hover-effect">
        <div class="card-header">
          <i class="mdi mdi-calendar-clock icon-color"></i>
          <h3>Wartungsfenster</h3>
        </div>
        <p>Planen Sie neue Wartungsfenster und steuern Sie den automatischen Versand von Benachrichtigungs-E-Mails.</p>
        <router-link to="/maintenance-windows" class="action-link"
          >Wartung planen <i class="mdi mdi-arrow-right"></i
        ></router-link>
      </div>

      <!-- Card for Templates -->
      <div class="card hover-effect">
        <div class="card-header">
          <i class="mdi mdi-email-open-outline icon-color"></i>
          <h3>E-Mail-Templates</h3>
        </div>
        <p>Konfigurieren Sie die E-Mail-Vorlagen für Benachrichtigungen zu Wartungsankündigungen.</p>
        <router-link to="/email-templates" class="action-link"
          >Vorlagen verwalten <i class="mdi mdi-arrow-right"></i
        ></router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 1rem 0;
}

.welcome-card {
  background-color: #f0f9ff;
  border: 1px solid var(--border-color);
  border-left: 4px solid var(--brand-primary);
  color: var(--text-primary);
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow-sm);
}

.welcome-card h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: var(--brand-primary);
}

.welcome-card .subtitle {
  margin-top: 0.75rem;
  margin-bottom: 0;
  font-size: 1.1rem;
  color: var(--text-secondary);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  box-shadow: var(--box-shadow-sm);
  gap: 0.5rem;
}

.stat-num {
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--brand-primary);
  line-height: 1;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.grid-layout {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  padding: 1.75rem;
  box-shadow: var(--box-shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: var(--transition-base);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.card-header h3 {
  margin: 0;
  font-size: 1.25rem;
}

.icon-color {
  font-size: 1.75rem;
  color: var(--brand-primary);
}

.action-link {
  color: var(--brand-primary);
  text-decoration: none;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: auto;
  transition: color 0.2s;
}

.action-link:hover {
  color: var(--brand-primary-hover);
}

.hover-effect:hover {
  transform: translateY(-4px);
  box-shadow: var(--box-shadow-md);
}
</style>
