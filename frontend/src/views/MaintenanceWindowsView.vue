<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useAppStore } from '../store/appStore';
import { useForm } from '../composables/useForm';

interface Application {
  id: string;
  name: string;
}

interface EmailTemplate {
  id: string;
  name: string;
  subjectPattern: string;
  bodyPattern: string;
}

interface MaintenanceWindow {
  id?: string;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  applicationId: string;
  applicationName?: string;
  templateId?: string | null;
  templateName?: string | null;
  overriddenSubject?: string;
  overriddenBody?: string;
}

const appStore = useAppStore();

const windows = ref<MaintenanceWindow[]>([]);
const applications = ref<Application[]>([]);
const templates = ref<EmailTemplate[]>([]);

const canEdit = ref(true);
const isEditing = ref(false);
const activeId = ref<string | null>(null);
const sendingNotifications = ref<Record<string, boolean>>({});

const { form, errors, validate, resetForm } = useForm<MaintenanceWindow>({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  status: 'PLANNED',
  applicationId: '',
  templateId: '',
  overriddenSubject: '',
  overriddenBody: '',
});

const loadWindows = async () => {
  try {
    const data = await appStore.apiFetch<MaintenanceWindow[]>('/api/maintenance-windows');
    windows.value = data || [];
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Laden der Wartungsfenster';
  }
};

const loadApplications = async () => {
  try {
    const data = await appStore.apiFetch<Application[]>('/api/applications');
    applications.value = data || [];
  } catch (err) {
    console.error('Failed to load applications:', err);
  }
};

const loadTemplates = async () => {
  try {
    const data = await appStore.apiFetch<EmailTemplate[]>('/api/email-templates');
    templates.value = data || [];
  } catch (err) {
    console.error('Failed to load templates:', err);
  }
};

onMounted(() => {
  loadWindows();
  loadApplications();
  loadTemplates();
});

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('de-DE', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const showCreateForm = () => {
  // Set default datetimes (start now + 1 day, end + 1 day + 2 hours)
  const now = new Date();
  now.setDate(now.getDate() + 1);
  now.setMinutes(0);
  const startStr = now.toISOString().slice(0, 16);
  now.setHours(now.getHours() + 2);
  const endStr = now.toISOString().slice(0, 16);

  resetForm({
    title: '',
    description: '',
    startTime: startStr,
    endTime: endStr,
    status: 'PLANNED',
    applicationId: applications.value[0]?.id || '',
    templateId: '',
    overriddenSubject: '',
    overriddenBody: '',
  });
  activeId.value = null;
  isEditing.value = true;
};

const showEditForm = (item: MaintenanceWindow) => {
  // Format dates back to ISO strings local to timezone for datetime-local input
  const startLocal = item.startTime.substring(0, 16);
  const endLocal = item.endTime.substring(0, 16);

  resetForm({
    title: item.title,
    description: item.description,
    startTime: startLocal,
    endTime: endLocal,
    status: item.status,
    applicationId: item.applicationId,
    templateId: item.templateId || '',
    overriddenSubject: item.overriddenSubject || '',
    overriddenBody: item.overriddenBody || '',
  });
  activeId.value = item.id || null;
  isEditing.value = true;
};

const saveWindow = async () => {
  errors.value = {};
  if (!form.value.title.trim()) errors.value.title = 'Titel ist erforderlich';
  if (!form.value.applicationId) errors.value.applicationId = 'Applikation ist erforderlich';
  if (!form.value.startTime) errors.value.startTime = 'Startzeitpunkt ist erforderlich';
  if (!form.value.endTime) errors.value.endTime = 'Endzeitpunkt ist erforderlich';

  if (form.value.startTime && form.value.endTime) {
    const start = new Date(form.value.startTime);
    const end = new Date(form.value.endTime);
    if (start >= end) {
      errors.value.endTime = 'Endzeitpunkt muss nach dem Startzeitpunkt liegen';
    }
  }

  if (Object.keys(errors.value).length > 0) return;

  // Clean empty templates
  const payload = { ...form.value };
  if (!payload.templateId) {
    payload.templateId = null;
  }

  try {
    if (activeId.value) {
      await appStore.apiFetch(`/api/maintenance-windows/${activeId.value}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });
      appStore.successMsg = 'Wartungsfenster erfolgreich aktualisiert';
    } else {
      await appStore.apiFetch('/api/maintenance-windows', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      appStore.successMsg = 'Wartungsfenster erfolgreich geplant';
    }
    isEditing.value = false;
    await loadWindows();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Speichern des Wartungsfensters';
  }
};

const deleteWindow = async (id: string) => {
  if (!confirm('Möchten Sie dieses Wartungsfenster wirklich löschen?')) return;
  try {
    await appStore.apiFetch(`/api/maintenance-windows/${id}`, {
      method: 'DELETE',
    });
    appStore.successMsg = 'Wartungsfenster erfolgreich gelöscht';
    await loadWindows();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Löschen des Wartungsfensters';
  }
};

// Send notifications
const notifyUsers = async (id: string) => {
  sendingNotifications.value[id] = true;
  try {
    await appStore.apiFetch(`/api/maintenance-windows/${id}/notify`, {
      method: 'POST',
    });
    appStore.successMsg = 'Benachrichtigungen wurden erfolgreich versendet!';
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Senden der Benachrichtigungen';
  } finally {
    sendingNotifications.value[id] = false;
  }
};

// Auto-fill template placeholder previews
const selectedTemplatePreview = computed(() => {
  if (!form.value.templateId) return null;
  return templates.value.find((t) => t.id === form.value.templateId) || null;
});

// Watch template select to reset subject/body placeholders
watch(
  () => form.value.templateId,
  (newId) => {
    if (!newId) {
      form.value.overriddenSubject = '';
      form.value.overriddenBody = '';
    }
  }
);
</script>

<template>
  <div class="view-wrapper">
    <div class="view-header">
      <div>
        <h1>Wartungsfenster</h1>
        <p class="subtitle">Planen Sie Wartungsarbeiten und benachrichtigen Sie betroffene Benutzer.</p>
      </div>
      <button v-if="canEdit && !isEditing" @click="showCreateForm" class="btn btn-primary">
        <i class="mdi mdi-calendar-plus"></i> Wartung planen
      </button>
    </div>

    <!-- Creation/Editing Form Card -->
    <div v-if="isEditing" class="card editor-card">
      <div class="card-header">
        <h2>{{ activeId ? 'Wartungsfenster bearbeiten' : 'Neues Wartungsfenster planen' }}</h2>
      </div>
      <div class="card-body">
        <div class="form-row">
          <div class="form-group col">
            <label for="winTitle">Titel der Wartung</label>
            <input
              id="winTitle"
              type="text"
              v-model="form.title"
              class="form-control"
              :class="{ 'is-invalid': errors.title }"
              placeholder="z. B. OS Update & DB Migration"
            />
            <div class="invalid-feedback" v-if="errors.title">{{ errors.title }}</div>
          </div>

          <div class="form-group col">
            <label for="winApp">Ziel-Applikation</label>
            <select
              id="winApp"
              v-model="form.applicationId"
              class="form-control"
              :class="{ 'is-invalid': errors.applicationId }"
            >
              <option value="" disabled>Applikation auswählen...</option>
              <option v-for="app in applications" :key="app.id" :value="app.id">
                {{ app.name }}
              </option>
            </select>
            <div class="invalid-feedback" v-if="errors.applicationId">{{ errors.applicationId }}</div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group col">
            <label for="winStart">Startzeitpunkt</label>
            <input
              id="winStart"
              type="datetime-local"
              v-model="form.startTime"
              class="form-control"
              :class="{ 'is-invalid': errors.startTime }"
            />
            <div class="invalid-feedback" v-if="errors.startTime">{{ errors.startTime }}</div>
          </div>

          <div class="form-group col">
            <label for="winEnd">Endzeitpunkt</label>
            <input
              id="winEnd"
              type="datetime-local"
              v-model="form.endTime"
              class="form-control"
              :class="{ 'is-invalid': errors.endTime }"
            />
            <div class="invalid-feedback" v-if="errors.endTime">{{ errors.endTime }}</div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group col-6">
            <label for="winStatus">Status</label>
            <select id="winStatus" v-model="form.status" class="form-control">
              <option value="PLANNED">Geplant (PLANNED)</option>
              <option value="IN_PROGRESS">Wird ausgeführt (IN_PROGRESS)</option>
              <option value="COMPLETED">Abgeschlossen (COMPLETED)</option>
              <option value="CANCELLED">Abgebrochen (CANCELLED)</option>
            </select>
          </div>

          <div class="form-group col-6">
            <label for="winTemplate">E-Mail Vorlage</label>
            <select id="winTemplate" v-model="form.templateId" class="form-control">
              <option value="">Keine Vorlage (Standard-Text)</option>
              <option v-for="t in templates" :key="t.id" :value="t.id">
                {{ t.name }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label for="winDesc">Interne Beschreibung</label>
          <textarea
            id="winDesc"
            v-model="form.description"
            rows="3"
            class="form-control"
            placeholder="z. B. Beschreibung der Hardware-Arbeiten für interne Logbücher..."
          ></textarea>
        </div>

        <!-- Overrides / Email Preview section -->
        <div class="overrides-section">
          <h3>E-Mail Benachrichtigung anpassen</h3>
          <p class="form-help-text">
            Sie können hier die Betreffzeile und den E-Mail-Inhalt individuell anpassen. Wenn leer gelassen, wird die
            ausgewählte Vorlage verwendet.
          </p>

          <div class="form-group">
            <label for="overrideSubject">Betreff (Überschreiben)</label>
            <input
              id="overrideSubject"
              type="text"
              v-model="form.overriddenSubject"
              class="form-control"
              :placeholder="
                selectedTemplatePreview
                  ? 'Standard: ' + selectedTemplatePreview.subjectPattern
                  : 'Geben Sie einen Betreff ein...'
              "
            />
          </div>

          <div class="form-group">
            <label for="overrideBody">E-Mail-Text (Überschreiben)</label>
            <textarea
              id="overrideBody"
              v-model="form.overriddenBody"
              rows="6"
              class="form-control code-font"
              :placeholder="
                selectedTemplatePreview
                  ? 'Standard:\n' + selectedTemplatePreview.bodyPattern
                  : 'Geben Sie den Text ein...'
              "
            ></textarea>
          </div>
        </div>

        <div class="editor-actions">
          <button @click="isEditing = false" class="btn btn-secondary">Abbrechen</button>
          <button @click="saveWindow" class="btn btn-primary">Speichern</button>
        </div>
      </div>
    </div>

    <!-- Windows Listing -->
    <div v-else class="windows-container">
      <div v-if="windows.length === 0" class="card empty-card">
        <i class="mdi mdi-calendar-clock empty-icon"></i>
        <h3>Keine Wartungsfenster geplant</h3>
        <p>Erstellen Sie ein neues Wartungsfenster, um mit der Koordination zu beginnen.</p>
        <button v-if="canEdit" @click="showCreateForm" class="btn btn-primary" style="margin-top: 1rem">
          Wartungsfenster planen
        </button>
      </div>

      <!-- Listing Grid -->
      <div v-else class="windows-grid">
        <div v-for="win in windows" :key="win.id" class="card window-card" :class="win.status.toLowerCase()">
          <!-- Card Header details -->
          <div class="window-card-header">
            <div class="window-title-group">
              <span class="app-badge">{{ win.applicationName }}</span>
              <span class="status-badge" :class="win.status.toLowerCase()">{{ win.status }}</span>
            </div>
            <div class="actions" v-if="canEdit">
              <button @click="showEditForm(win)" class="action-btn text-primary" title="Bearbeiten">
                <i class="mdi mdi-pencil-outline"></i>
              </button>
              <button @click="deleteWindow(win.id!)" class="action-btn text-danger" title="Löschen">
                <i class="mdi mdi-trash-can-outline"></i>
              </button>
            </div>
          </div>

          <div class="window-card-body">
            <h3>{{ win.title }}</h3>
            <p class="win-description" v-if="win.description">{{ win.description }}</p>

            <div class="time-range">
              <div class="time-point">
                <i class="mdi mdi-play-circle-outline"></i>
                <div>
                  <span class="time-label">Start</span>
                  <span class="time-value">{{ formatDateTime(win.startTime) }}</span>
                </div>
              </div>
              <div class="time-point">
                <i class="mdi mdi-stop-circle-outline"></i>
                <div>
                  <span class="time-label">Ende</span>
                  <span class="time-value">{{ formatDateTime(win.endTime) }}</span>
                </div>
              </div>
            </div>

            <!-- Email template reference -->
            <div class="template-reference" v-if="win.templateName">
              <i class="mdi mdi-email-outline"></i>
              <span
                >Vorlage: <strong>{{ win.templateName }}</strong></span
              >
            </div>
          </div>

          <!-- Bottom trigger actions -->
          <div class="window-card-footer" v-if="canEdit">
            <button
              @click="notifyUsers(win.id!)"
              class="btn btn-primary btn-block btn-notify"
              :disabled="sendingNotifications[win.id!]"
            >
              <span v-if="sendingNotifications[win.id!]" class="spinner-btn">
                <span class="spinner-icon"></span> E-Mails werden gesendet...
              </span>
              <span v-else> <i class="mdi mdi-send"></i> E-Mails an Benutzer senden </span>
            </button>
          </div>
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
  max-width: 800px;
  margin: 0 auto;
}

.form-row {
  display: flex;
  gap: 1.5rem;
}

.form-group.col {
  flex: 1;
}

.form-group.col-6 {
  flex: 0 0 calc(50% - 0.75rem);
}

.overrides-section {
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1.25rem;
  margin-top: 2rem;
  margin-bottom: 1rem;
}

.overrides-section h3 {
  font-size: 1rem;
  margin-bottom: 0.25rem;
}

.code-font {
  font-family: monospace;
  font-size: 0.9rem;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
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

/* Windows Grid layout */
.windows-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 1.5rem;
}

.window-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-left: 4px solid var(--border-color);
  transition: all 0.2s;
  height: 100%;
}

.window-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--box-shadow);
}

/* Status colors borders */
.window-card.planned {
  border-left-color: var(--brand-primary);
}
.window-card.in_progress {
  border-left-color: var(--color-warning, #f97316);
}
.window-card.completed {
  border-left-color: var(--color-success, #22c55e);
}
.window-card.cancelled {
  border-left-color: var(--text-muted);
}

.window-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.window-title-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.app-badge {
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.status-badge {
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
}

.status-badge.planned {
  background-color: var(--brand-primary-light, #e0f2fe);
  color: var(--brand-primary, #0284c7);
}

.status-badge.in_progress {
  background-color: #ffedd5;
  color: #ea580c;
}

.status-badge.completed {
  background-color: #dcfce7;
  color: #16a34a;
}

.status-badge.cancelled {
  background-color: #f3f4f6;
  color: #4b5563;
}

.window-card-body h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
}

.win-description {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin-bottom: 1.25rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
  background-color: var(--bg-app);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.time-point {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.time-point i {
  font-size: 1.25rem;
  color: var(--brand-primary);
}

.time-label {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  text-transform: uppercase;
  font-weight: 600;
}

.time-value {
  font-size: 0.9rem;
  font-weight: 500;
}

.template-reference {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.window-card-footer {
  margin-top: 1.5rem;
  border-top: 1px solid var(--border-color);
  padding-top: 1rem;
}

.btn-notify {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
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

/* Spinner for notifications */
.spinner-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.spinner-icon {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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
