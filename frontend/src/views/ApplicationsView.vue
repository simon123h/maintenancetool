<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAppStore } from '../store/appStore';
import { useForm } from '../composables/useForm';

interface Application {
  id?: string;
  name: string;
  url: string;
  description: string;
}

interface ApplicationUser {
  id: string;
  email: string;
  name: string;
}

const appStore = useAppStore();

const applications = ref<Application[]>([]);
const allUsers = ref<ApplicationUser[]>([]);
const mappedUsers = ref<ApplicationUser[]>([]);
const selectedApp = ref<Application | null>(null);

const canEdit = ref(true);
const isEditing = ref(false);
const activeId = ref<string | null>(null);
const userToMapId = ref<string>('');
const showAssignModal = ref(false);

const { form, reset: resetForm } = useForm<Application>({
  name: '',
  url: '',
  description: '',
});
const errors = ref<Record<string, string>>({});

const loadApplications = async () => {
  try {
    const data = await appStore.apiFetch<Application[]>('/api/applications');
    applications.value = data || [];
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Laden der Applikationen';
  }
};

const loadAllUsers = async () => {
  try {
    const data = await appStore.apiFetch<ApplicationUser[]>('/api/application-users');
    allUsers.value = data || [];
  } catch (err) {
    console.error('Failed to load application users list:', err);
  }
};

const loadMappedUsers = async (appId: string) => {
  try {
    const data = await appStore.apiFetch<ApplicationUser[]>(`/api/applications/${appId}/users`);
    mappedUsers.value = data || [];
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Laden der zugeordneten Benutzer';
  }
};

onMounted(() => {
  loadApplications();
  loadAllUsers();
});

const selectApp = async (app: Application) => {
  selectedApp.value = app;
  userToMapId.value = '';
  await loadMappedUsers(app.id!);
};

const unselectApp = () => {
  selectedApp.value = null;
  mappedUsers.value = [];
};

const showCreateForm = () => {
  resetForm({
    name: '',
    url: '',
    description: '',
  });
  activeId.value = null;
  isEditing.value = true;
};

const showEditForm = (item: Application, event: Event) => {
  event.stopPropagation(); // Prevent selection trigger
  resetForm({
    name: item.name,
    url: item.url,
    description: item.description,
  });
  activeId.value = item.id || null;
  isEditing.value = true;
};

const saveApplication = async () => {
  errors.value = {};
  if (!form.value.name.trim()) errors.value.name = 'Name ist erforderlich';
  if (!form.value.url.trim()) {
    errors.value.url = 'URL ist erforderlich';
  } else if (!form.value.url.startsWith('http://') && !form.value.url.startsWith('https://')) {
    errors.value.url = 'URL muss mit http:// oder https:// beginnen';
  }

  if (Object.keys(errors.value).length > 0) return;

  try {
    if (activeId.value) {
      const updated = await appStore.apiFetch<Application>(`/api/applications/${activeId.value}`, {
        method: 'PUT',
        body: JSON.stringify(form.value),
      });
      appStore.successMsg = 'Applikation erfolgreich aktualisiert';
      if (selectedApp.value?.id === activeId.value && updated) {
        selectedApp.value = updated;
      }
    } else {
      await appStore.apiFetch('/api/applications', {
        method: 'POST',
        body: JSON.stringify(form.value),
      });
      appStore.successMsg = 'Applikation erfolgreich erstellt';
    }
    isEditing.value = false;
    await loadApplications();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Speichern der Applikationen';
  }
};

const deleteApplication = async (id: string, event: Event) => {
  event.stopPropagation(); // Prevent selection trigger
  if (!confirm('Möchten Sie diese Applikation wirklich löschen? Sämtliche Wartungsfenster-Planungen gehen verloren.'))
    return;
  try {
    await appStore.apiFetch(`/api/applications/${id}`, {
      method: 'DELETE',
    });
    appStore.successMsg = 'Applikation erfolgreich gelöscht';
    if (selectedApp.value?.id === id) {
      unselectApp();
    }
    await loadApplications();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Löschen der Applikation';
  }
};

// Map User
const mapUser = async () => {
  if (!userToMapId.value || !selectedApp.value) return;
  try {
    await appStore.apiFetch(`/api/applications/${selectedApp.value.id}/users/${userToMapId.value}`, {
      method: 'POST',
    });
    appStore.successMsg = 'Benutzer erfolgreich zugeordnet';
    userToMapId.value = '';
    await loadMappedUsers(selectedApp.value.id!);
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler bei der Zuordnung des Benutzers';
  }
};

const mapUserAndClose = async () => {
  await mapUser();
  showAssignModal.value = false;
};

// Unmap User
const unmapUser = async (userId: string) => {
  if (!selectedApp.value) return;
  try {
    await appStore.apiFetch(`/api/applications/${selectedApp.value.id}/users/${userId}`, {
      method: 'DELETE',
    });
    appStore.successMsg = 'Zuordnung erfolgreich aufgehoben';
    await loadMappedUsers(selectedApp.value.id!);
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Aufheben der Zuordnung';
  }
};

// Compute unmapped users to show in assign dropdown
const assignableUsers = computed(() => {
  return allUsers.value.filter((user) => !mappedUsers.value.some((mapped) => mapped.id === user.id));
});
</script>

<template>
  <div class="view-wrapper">
    <div class="view-header">
      <div>
        <h1>Applikationen</h1>
        <p class="subtitle">Verwalten Sie Ihre gehosteten Anwendungen und ordnen Sie ihnen Benutzer zu.</p>
      </div>
      <button v-if="canEdit && !isEditing" @click="showCreateForm" class="btn btn-primary">
        <i class="mdi mdi-plus"></i> Neue Applikation
      </button>
    </div>

    <!-- Application Form -->
    <div v-if="isEditing" class="card editor-card">
      <div class="card-header">
        <h2>{{ activeId ? 'Applikation bearbeiten' : 'Neue Applikation anlegen' }}</h2>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label for="appName">Name der Applikation</label>
          <input
            id="appName"
            type="text"
            v-model="form.name"
            class="form-control"
            :class="{ 'is-invalid': errors.name }"
            placeholder="z. B. CRM-System"
          />
          <div class="invalid-feedback" v-if="errors.name">{{ errors.name }}</div>
        </div>

        <div class="form-group">
          <label for="appUrl">URL der Applikation</label>
          <input
            id="appUrl"
            type="url"
            v-model="form.url"
            class="form-control"
            :class="{ 'is-invalid': errors.url }"
            placeholder="https://crm.meinefirma.de"
          />
          <div class="invalid-feedback" v-if="errors.url">{{ errors.url }}</div>
        </div>

        <div class="form-group">
          <label for="appDescription">Beschreibung (optional)</label>
          <textarea
            id="appDescription"
            v-model="form.description"
            rows="4"
            class="form-control"
            placeholder="Kurze Beschreibung des Systems..."
          ></textarea>
        </div>

        <div class="editor-actions">
          <button @click="isEditing = false" class="btn btn-secondary">Abbrechen</button>
          <button @click="saveApplication" class="btn btn-primary">Speichern</button>
        </div>
      </div>
    </div>

    <!-- Split view Listing -->
    <div v-else class="split-view">
      <!-- Left side: Apps list -->
      <div class="apps-pane" :class="{ collapsed: selectedApp }">
        <div v-if="applications.length === 0" class="card empty-card">
          <i class="mdi mdi-application-cog empty-icon"></i>
          <h3>Keine Applikationen angelegt</h3>
          <p>Legen Sie eine neue Applikation an, um Wartungsfenster planen zu können.</p>
          <button v-if="canEdit" @click="showCreateForm" class="btn btn-primary" style="margin-top: 1rem">
            Applikation anlegen
          </button>
        </div>

        <div v-else class="card list-card">
          <div class="table-container">
            <table class="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>URL</th>
                  <th v-if="!selectedApp">Beschreibung</th>
                  <th v-if="canEdit" class="text-right" style="width: 120px">Aktionen</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="app in applications"
                  :key="app.id"
                  class="selectable-row"
                  :class="{ 'selected-row': selectedApp?.id === app.id }"
                  @click="selectApp(app)"
                >
                  <td>
                    <div class="app-row-title">
                      <i class="mdi mdi-web table-row-icon"></i>
                      <span class="font-semibold">{{ app.name }}</span>
                    </div>
                  </td>
                  <td>
                    <a :href="app.url" target="_blank" class="app-url" @click.stop>
                      {{ app.url }} <i class="mdi mdi-open-in-new" style="font-size: 0.8rem"></i>
                    </a>
                  </td>
                  <td v-if="!selectedApp">
                    <span class="preview-text text-muted">{{ app.description }}</span>
                  </td>
                  <td v-if="canEdit" class="text-right">
                    <div class="action-cell">
                      <button @click="showEditForm(app, $event)" class="action-btn text-primary" title="Bearbeiten">
                        <i class="mdi mdi-pencil-outline"></i>
                      </button>
                      <button
                        @click="deleteApplication(app.id!, $event)"
                        class="action-btn text-danger"
                        title="Löschen"
                      >
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

      <!-- Right side: Mapped Users Details -->
      <div v-if="selectedApp" class="users-pane">
        <div class="card detail-card">
          <div class="detail-header">
            <div>
              <span class="detail-subtitle">Benutzerzuordnung für:</span>
              <h2>{{ selectedApp.name }}</h2>
            </div>
            <button @click="unselectApp" class="btn-close" title="Schließen">
              <i class="mdi mdi-close"></i>
            </button>
          </div>

          <div class="detail-body">
            <!-- Add User Trigger Button -->
            <div v-if="canEdit" class="assign-trigger-row">
              <button @click="showAssignModal = true" class="btn btn-primary btn-sm btn-assign-trigger">
                <i class="mdi mdi-account-plus-outline"></i> Benutzer zuordnen
              </button>
            </div>

            <!-- Assigned Users list -->
            <div class="assigned-section">
              <h3>Zugeordnete Benutzer ({{ mappedUsers.length }})</h3>
              <div v-if="mappedUsers.length === 0" class="empty-mapping">
                <i class="mdi mdi-account-question-outline empty-mapping-icon"></i>
                <p>
                  Dieser Anwendung sind derzeit keine Benutzer zugeordnet. Wartungsmails werden für dieses System an
                  niemanden gesendet.
                </p>
              </div>
              <div v-else class="mapped-users-list">
                <div v-for="user in mappedUsers" :key="user.id" class="mapped-user-row">
                  <div class="mapped-user-info">
                    <span class="mapped-user-name">{{ user.name }}</span>
                    <span class="mapped-user-email">{{ user.email }}</span>
                  </div>
                  <button
                    v-if="canEdit"
                    @click="unmapUser(user.id)"
                    class="btn-unmap"
                    title="Benutzerzuordnung löschen"
                  >
                    <i class="mdi mdi-link-off"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- User Assignment Modal Dialog -->
    <div v-if="showAssignModal" class="modal-overlay" @click.self="showAssignModal = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3>Benutzer zuweisen</h3>
          <button @click="showAssignModal = false" class="btn-close-modal">
            <i class="mdi mdi-close"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group" style="margin-bottom: 0">
            <label for="modalAssignUser">Wählen Sie einen Benutzer für "{{ selectedApp?.name }}"</label>
            <select id="modalAssignUser" v-model="userToMapId" class="form-control">
              <option value="" disabled>Bitte Benutzer auswählen...</option>
              <option v-for="user in assignableUsers" :key="user.id" :value="user.id">
                {{ user.name }} ({{ user.email }})
              </option>
            </select>
            <p class="form-help-text" v-if="assignableUsers.length === 0">
              Alle vorhandenen Endbenutzer sind dieser Anwendung bereits zugewiesen.
            </p>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAssignModal = false" class="btn btn-secondary">Abbrechen</button>
          <button @click="mapUserAndClose" class="btn btn-primary" :disabled="!userToMapId">Zuordnen</button>
        </div>
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

/* Split view layout */
.split-view {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.apps-pane {
  flex: 1;
  transition: all 0.3s ease;
}

.users-pane {
  width: 450px;
  animation: slideIn 0.3s ease;
}

.list-card {
  padding: 0;
  overflow: hidden;
}

.empty-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  color: var(--text-muted);
  margin-bottom: 1rem;
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

.selectable-row {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.selectable-row:hover {
  background-color: var(--bg-app);
}

.selected-row {
  background-color: var(--brand-primary-light, #e0f2fe) !important;
}

.app-row-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.table-row-icon {
  font-size: 1.25rem;
  color: var(--brand-primary);
}

.font-semibold {
  font-weight: 600;
}

.app-url {
  font-size: 0.85rem;
  color: var(--text-muted);
  text-decoration: none;
  word-break: break-all;
  display: inline-block;
}

.app-url:hover {
  color: var(--brand-primary);
}

.preview-text {
  font-size: 0.85rem;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
  background-color: rgba(0, 0, 0, 0.05);
}

/* Detail Panel card styles */
.detail-card {
  position: sticky;
  top: 1.5rem;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1.25rem;
  margin-bottom: 1.5rem;
}

.detail-subtitle {
  font-size: 0.8rem;
  color: var(--text-muted);
  text-transform: uppercase;
  font-weight: 600;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.btn-close:hover {
  background-color: var(--bg-app);
}

.assign-form-group {
  margin-bottom: 2rem;
  background-color: var(--bg-app);
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.assign-form-group label {
  font-weight: 600;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.assign-input-group {
  display: flex;
  gap: 0.75rem;
}

.form-help-text {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-top: 0.5rem;
}

.assigned-section h3 {
  font-size: 1rem;
  margin-bottom: 1rem;
}

.empty-mapping {
  text-align: center;
  padding: 2.5rem 1rem;
  background-color: var(--bg-app);
  border: 1px dashed var(--border-color);
  border-radius: 8px;
  color: var(--text-muted);
}

.empty-mapping-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.empty-mapping p {
  font-size: 0.85rem;
  line-height: 1.4;
}

.mapped-users-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 2px;
}

.mapped-user-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.2s;
}

.mapped-user-row:hover {
  border-color: var(--brand-primary-light);
  box-shadow: var(--box-shadow-sm);
}

.mapped-user-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.mapped-user-name {
  font-weight: 500;
  font-size: 0.9rem;
}

.mapped-user-email {
  font-size: 0.8rem;
  color: var(--text-muted);
  font-family: monospace;
}

.btn-unmap {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 1.15rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-unmap:hover {
  color: var(--color-danger, #ef4444);
  background-color: rgba(239, 68, 68, 0.08);
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

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.assign-trigger-row {
  margin-bottom: 1.5rem;
}

.btn-assign-trigger {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.25s ease;
}

.modal-dialog {
  background: var(--bg-card);
  border-radius: var(--border-radius-lg);
  width: 500px;
  max-width: 90%;
  box-shadow: var(--box-shadow-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
}

.btn-close-modal {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.btn-close-modal:hover {
  background-color: var(--bg-app);
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-color);
  background-color: var(--bg-app);
}
</style>
