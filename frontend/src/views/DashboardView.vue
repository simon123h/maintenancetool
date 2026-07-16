<script setup lang="ts">
import { useAuthStore } from '../store/authStore';

const authStore = useAuthStore();
</script>

<template>
  <div class="dashboard-container">
    <div class="welcome-card">
      <h1>Willkommen im Maintenance Tool</h1>
      <p class="subtitle" v-if="authStore.currentUser">
        Hallo {{ authStore.currentUser.vollerName }}. Hier können Sie zukünftig Anwendungen verwalten, Wartungsfenster planen und Benachrichtigungen steuern.
      </p>
      <p class="subtitle" v-else>
        Bitte melden Sie sich an, um das System zu nutzen.
      </p>
    </div>

    <div class="grid-layout">
      <!-- Card for Applications -->
      <div class="card hover-effect">
        <div class="card-header">
          <i class="mdi mdi-web icon-color"></i>
          <h3>Anwendungen</h3>
        </div>
        <p>Verwalten Sie die Liste der gehosteten Web-Applikationen und deren Benutzergruppen.</p>
        <router-link to="/admin" class="action-link">Zur Verwaltung <i class="mdi mdi-arrow-right"></i></router-link>
      </div>

      <!-- Card for Maintenance Windows -->
      <div class="card hover-effect">
        <div class="card-header">
          <i class="mdi mdi-calendar-clock icon-color"></i>
          <h3>Wartungsfenster</h3>
        </div>
        <p>Planen Sie neue Wartungsfenster und steuern Sie den automatischen Versand von Benachrichtigungs-E-Mails.</p>
        <router-link to="/admin" class="action-link">Wartung planen <i class="mdi mdi-arrow-right"></i></router-link>
      </div>

      <!-- Card for Templates -->
      <div class="card hover-effect">
        <div class="card-header">
          <i class="mdi mdi-email-open-outline icon-color"></i>
          <h3>E-Mail-Templates</h3>
        </div>
        <p>Konfigurieren Sie die E-Mail-Vorlagen für Benachrichtigungen zu Wartungsankündigungen.</p>
        <router-link to="/admin" class="action-link">Vorlagen verwalten <i class="mdi mdi-arrow-right"></i></router-link>
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
  background: linear-gradient(135deg, var(--brand-primary) 0%, var(--brand-primary-hover) 100%);
  color: #ffffff;
  padding: 2.5rem;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow-md);
}

.welcome-card h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
}

.welcome-card .subtitle {
  margin-top: 0.75rem;
  margin-bottom: 0;
  font-size: 1.1rem;
  opacity: 0.9;
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
