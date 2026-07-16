<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAppStore } from '../store/appStore';
import { useAuth } from '../composables/useAuth';
import { useForm } from '../composables/useForm';

interface ApplicationUser {
  id?: string;
  email: string;
  name: string;
}

const appStore = useAppStore();
const { isAdmin, isManager } = useAuth();
const users = ref<ApplicationUser[]>();
const searchQuery = ref('');

const canEdit = computed(() => isAdmin.value || isManager.value);
const isEditing = ref(false);
const activeId = ref<string | null>(null);

const { form, errors, validate, resetForm } = useForm<ApplicationUser>({
  email: '',
  name: '',
});

const loadUsers = async () => {
  try {
    const data = await appStore.apiFetch<ApplicationUser[]>('/api/application-users');
    users.value = data || [];
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Laden der Benutzer';
  }
};

onMounted(() => {
  loadUsers();
});

const filteredUsers = computed(() => {
  if (!users.value) return [];
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return users.value;
  return users.value.filter((u) => u.name.toLowerCase().includes(query) || u.email.toLowerCase().includes(query));
});

const showCreateForm = () => {
  resetForm({
    email: '',
    name: '',
  });
  activeId.value = null;
  isEditing.value = true;
};

const showEditForm = (item: ApplicationUser) => {
  resetForm({
    email: item.email,
    name: item.name,
  });
  activeId.value = item.id || null;
  isEditing.value = true;
};

const saveUser = async () => {
  errors.value = {};
  if (!form.value.name.trim()) errors.value.name = 'Name ist erforderlich';
  if (!form.value.email.trim()) {
    errors.value.email = 'E-Mail ist erforderlich';
  } else if (!form.value.email.includes('@')) {
    errors.value.email = 'E-Mail ist ungültig';
  }

  if (Object.keys(errors.value).length > 0) return;

  try {
    if (activeId.value) {
      await appStore.apiFetch(`/api/application-users/${activeId.value}`, {
        method: 'PUT',
        body: JSON.stringify(form.value),
      });
      appStore.successMsg = 'Benutzer erfolgreich aktualisiert';
    } else {
      await appStore.apiFetch('/api/application-users', {
        method: 'POST',
        body: JSON.stringify(form.value),
      });
      appStore.successMsg = 'Benutzer erfolgreich angelegt';
    }
    isEditing.value = false;
    await loadUsers();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Speichern des Benutzers';
  }
};

const deleteUser = async (id: string) => {
  if (!confirm('Möchten Sie diesen Benutzer wirklich löschen?')) return;
  try {
    await appStore.apiFetch(`/api/application-users/${id}`, {
      method: 'DELETE',
    });
    appStore.successMsg = 'Benutzer erfolgreich gelöscht';
    await loadUsers();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Löschen des Benutzers';
  }
};
</script>

<template>
  <div class="view-wrapper">
    <div class="view-header">
      <div>
        <h1>Endbenutzer</h1>
        <p class="subtitle">Verwalten Sie die Kontakte der Endbenutzer, die Wartungsmails empfangen.</p>
      </div>
      <button v-if="canEdit && !isEditing" @click="showCreateForm" class="btn btn-primary">
        <i class="mdi mdi-plus"></i> Neuer Benutzer
      </button>
    </div>

    <!-- Edit/Create Card -->
    <div v-if="isEditing" class="card editor-card">
      <div class="card-header">
        <h2>{{ activeId ? 'Benutzer bearbeiten' : 'Neuen Benutzer anlegen' }}</h2>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label for="userName">Vollständiger Name</label>
          <input
            id="userName"
            type="text"
            v-model="form.name"
            class="form-control"
            :class="{ 'is-invalid': errors.name }"
            placeholder="z. B. Max Mustermann"
          />
          <div class="invalid-feedback" v-if="errors.name">{{ errors.name }}</div>
        </div>

        <div class="form-group">
          <label for="userEmail">E-Mail-Adresse</label>
          <input
            id="userEmail"
            type="email"
            v-model="form.email"
            class="form-control"
            :class="{ 'is-invalid': errors.email }"
            placeholder="z. B. max@firma.de"
          />
          <div class="invalid-feedback" v-if="errors.email">{{ errors.email }}</div>
        </div>

        <div class="editor-actions">
          <button @click="isEditing = false" class="btn btn-secondary">Abbrechen</button>
          <button @click="saveUser" class="btn btn-primary">Speichern</button>
        </div>
      </div>
    </div>

    <!-- Users Table Listing -->
    <div v-else class="card list-card">
      <div class="search-bar">
        <i class="mdi mdi-magnify search-icon"></i>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Suchen nach Name oder E-Mail-Adresse..."
          class="search-input"
        />
      </div>

      <div class="table-container">
        <table class="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>E-Mail-Adresse</th>
              <th v-if="canEdit" class="text-right" style="width: 120px">Aktionen</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredUsers.length === 0">
              <td colspan="3" class="text-center text-muted py-4">Keine Benutzer gefunden.</td>
            </tr>
            <tr v-else v-for="user in filteredUsers" :key="user.id">
              <td>
                <div class="user-cell">
                  <div class="avatar-circle">{{ user.name.charAt(0).toUpperCase() }}</div>
                  <span class="user-fullname">{{ user.name }}</span>
                </div>
              </td>
              <td>
                <span class="email-badge">{{ user.email }}</span>
              </td>
              <td v-if="canEdit" class="text-right">
                <div class="action-cell">
                  <button @click="showEditForm(user)" class="action-btn text-primary" title="Bearbeiten">
                    <i class="mdi mdi-pencil-outline"></i>
                  </button>
                  <button @click="deleteUser(user.id!)" class="action-btn text-danger" title="Löschen">
                    <i class="mdi mdi-trash-can-outline"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-wrapper {
  padding: 2rem 0;
  animation: fadeIn 0.4s ease;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.subtitle {
  color: var(--text-muted);
  margin-top: 0.25rem;
}

.editor-card {
  max-width: 600px;
  margin: 0 auto;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.list-card {
  padding: 0;
  overflow: hidden;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--bg-app);
  gap: 0.75rem;
}

.search-icon {
  font-size: 1.25rem;
  color: var(--text-muted);
}

.search-input {
  border: none;
  background: transparent;
  width: 100%;
  font-size: 0.95rem;
  outline: none;
  color: var(--text-main);
}

.table-container {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 1rem 1.5rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.table th {
  font-weight: 600;
  color: var(--text-muted);
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background-color: var(--bg-app);
}

.table tr:last-child td {
  border-bottom: none;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--brand-primary-light, #e0f2fe);
  color: var(--brand-primary, #0284c7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
}

.user-fullname {
  font-weight: 500;
}

.email-badge {
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 0.25rem 0.5rem;
  font-size: 0.85rem;
  font-family: monospace;
}

.action-cell {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.action-btn {
  background: none;
  border: none;
  font-size: 1.15rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background-color: var(--bg-app);
}

.text-right {
  text-align: right !important;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
