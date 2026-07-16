<script setup lang="ts">
import { onMounted } from 'vue';
import { useAppStore } from './store/appStore';
import { useAuthStore } from './store/authStore';
import HeaderNav from './components/HeaderNav.vue';
import AlertNotification from './components/AlertNotification.vue';

const appStore = useAppStore();
const authStore = useAuthStore();

onMounted(() => {
  appStore.initApp();
});
</script>

<template>
  <div v-if="appStore.loading" class="full-page-loading">
    <div class="spinner"></div>
    <p>Lade Anwendungsdaten...</p>
  </div>
  <div v-else class="app-container">
    <!-- Premium Navigation Header -->
    <HeaderNav :currentUser="authStore.currentUser" />

    <!-- Unified Notifications -->
    <AlertNotification
      :errorMsg="appStore.errorMsg"
      :successMsg="appStore.successMsg"
      @closeError="appStore.errorMsg = null"
      @closeSuccess="appStore.successMsg = null"
      class="container"
    />

    <!-- Main Content viewport for Vue Router -->
    <main class="container">
      <router-view></router-view>
    </main>
  </div>
</template>

<style scoped>
.full-page-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 1.5rem;
  background-color: var(--bg-app);
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-top-color: var(--brand-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
