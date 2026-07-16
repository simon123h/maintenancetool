<script setup lang="ts">
import AdminUsersTab from '../components/AdminUsersTab.vue';
</script>

<template>
  <div>
    <div class="section-header">
      <h2>Administrations-Bereich</h2>
      <p class="subtitle">Systemsteuerung für Benutzerkontenberechtigungen.</p>
    </div>

    <!-- Sub-Tabs (User Management only for now) -->
    <div class="admin-subnav">
      <button class="btn-subnav active">Benutzerberechtigungen</button>
    </div>

    <AdminUsersTab />
  </div>
</template>

<style scoped>
.admin-subnav {
  display: flex;
  gap: 0.75rem;
  border-bottom: 2px solid var(--border-color);
  padding-bottom: 0.75rem;
  margin-bottom: 2.25rem;
}

.btn-subnav {
  background: none;
  border: none;
  font-family: var(--font-family);
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--text-secondary);
  padding: 0.5rem 1rem;
  cursor: pointer;
  border-radius: var(--border-radius-md);
  transition: var(--transition-base);
}

.btn-subnav.active {
  background-color: var(--brand-primary);
  color: #ffffff;
}
</style>
