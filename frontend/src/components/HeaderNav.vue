<script setup lang="ts">
import type { UserAccount } from '../types';

defineProps<{
  currentUser: UserAccount | null;
}>();
</script>

<template>
  <header>
    <div class="nav-bar">
      <div class="brand">
        <i class="mdi mdi-server-network" style="font-size: 1.5rem; margin-right: 0.25rem"></i>
        <span>Maintenance Tool</span>
      </div>

      <nav class="nav-links">
        <router-link to="/" class="nav-btn" active-class="active" exact-active-class="active">
          Dashboard
        </router-link>

        <router-link
          v-if="currentUser?.rollen.includes('ROLE_ADMIN')"
          to="/admin"
          class="nav-btn admin-btn"
          active-class="active"
        >
          Administration
        </router-link>

        <a href="/help/" target="_blank" class="nav-btn help-link">
          Hilfe & FAQ
          <i class="mdi mdi-open-in-new external-icon" style="font-size: 0.95rem"></i>
        </a>
      </nav>

      <!-- User Profile Header Section -->
      <div class="profile-section" v-if="currentUser">
        <div class="user-avatar">
          {{ currentUser.vollerName.charAt(0).toUpperCase() }}
        </div>
        <div class="user-info">
          <span class="user-name">{{ currentUser.vollerName }}</span>
          <span class="user-roles">
            {{ currentUser.rollen.map((r) => r.replace('ROLE_', '')).join(', ') }}
          </span>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* Header & Profile section */
.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--box-shadow-sm);
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--brand-primary);
}

.nav-links {
  display: flex;
  gap: 0.5rem;
}

.nav-btn {
  background: none;
  border: none;
  font-family: var(--font-family);
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--text-secondary);
  padding: 0.5rem 0.85rem;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: var(--transition-base);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.nav-btn:hover,
.nav-btn.active {
  color: var(--brand-primary);
  background-color: rgba(59, 130, 246, 0.08);
}

.nav-btn.admin-btn {
  color: var(--text-secondary);
}

.nav-btn.admin-btn:hover,
.nav-btn.admin-btn.active {
  color: var(--brand-primary-hover);
  background-color: rgba(59, 130, 246, 0.08);
}

.help-link {
  color: var(--brand-secondary);
}

.help-link:hover {
  color: var(--brand-secondary-hover);
  background-color: rgba(16, 185, 129, 0.08);
}

.external-icon {
  opacity: 0.7;
}

.profile-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-left: 1rem;
  border-left: 1px solid var(--border-color);
}

.user-avatar {
  width: 36px;
  height: 36px;
  background-color: var(--brand-primary);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1rem;
  box-shadow: var(--box-shadow-sm);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
}

.user-roles {
  font-size: 0.75rem;
  color: var(--text-secondary);
  text-transform: uppercase;
}

@media (max-width: 768px) {
  .nav-bar {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }

  .profile-section {
    border-left: none;
    padding-left: 0;
  }
}

.custom-logo {
  height: 28px;
  max-width: 120px;
  object-fit: contain;
}
</style>
