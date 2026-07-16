<script setup lang="ts">
import { useAuthStore } from '../store/authStore';

const authStore = useAuthStore();
</script>

<template>
  <div class="admin-content-box">
    <h3>Benutzerverwaltung & Berechtigungsvergabe</h3>
    <p class="subtitle">
      Vergeben Sie höhere Rollen (ROLE_ADMIN, ROLE_MANAGER) an Konten, die sich bereits im System angemeldet haben.
    </p>

    <div class="table-container" style="margin-top: 1.5rem">
      <table class="admin-table">
        <thead>
          <tr>
            <th>Voller Name</th>
            <th>E-Mail-Adresse</th>
            <th>Login-Name</th>
            <th>Aktive Rollen</th>
            <th>Rollen bearbeiten</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in authStore.users" :key="user.id">
            <td>
              <strong>{{ user.vollerName }}</strong>
              <span v-if="user.id === authStore.currentUser?.id" class="badge">Sie</span>
            </td>
            <td>{{ user.mailAdresse }}</td>
            <td>
              <code>{{ user.loginName }}</code>
            </td>
            <td>
              <span v-for="r in user.rollen" :key="r" class="badge role-badge" :class="r.toLowerCase()">
                {{ r }}
              </span>
            </td>
            <td>
              <div class="role-toggles" v-if="user.id !== authStore.currentUser?.id">
                <label class="toggle-checkbox">
                  <input
                    type="checkbox"
                    :checked="user.rollen.includes('ROLE_ADMIN')"
                    @change="authStore.toggleRole(user, 'ROLE_ADMIN')"
                  />
                  Admin
                </label>
                <label class="toggle-checkbox">
                  <input
                    type="checkbox"
                    :checked="user.rollen.includes('ROLE_MANAGER')"
                    @change="authStore.toggleRole(user, 'ROLE_MANAGER')"
                  />
                  Manager
                </label>
              </div>
              <span v-else class="text-muted"><small>Selbsteditierschutz aktiv</small></span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.admin-content-box {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  padding: 1.75rem;
  box-shadow: var(--box-shadow-md);
}

.subtitle {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
  margin-bottom: 0;
}

.table-container {
  width: 100%;
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.9rem;
}

.admin-table th,
.admin-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--border-color);
}

.admin-table th {
  background-color: #f8fafc;
  color: var(--text-secondary);
  font-weight: 600;
}

.admin-table tr:hover {
  background-color: #fafbfd;
}

.badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.15rem 0.5rem;
  border-radius: var(--border-radius-sm);
  background-color: #f1f5f9;
}

.role-badge {
  margin-right: 0.25rem;
  font-size: 0.75rem;
}

.role-badge.role_admin {
  background-color: #fee2e2;
  color: #ef4444;
}

.role-badge.role_manager {
  background-color: #ecfeff;
  color: #0891b2;
}

.role-badge.role_teilnehmer {
  background-color: #f0fdf4;
  color: #16a34a;
}

.role-toggles {
  display: flex;
  gap: 0.75rem;
}

.toggle-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
}

.toggle-checkbox input {
  width: 1rem;
  height: 1rem;
  cursor: pointer;
}

.text-muted {
  color: #64748b;
}
</style>
