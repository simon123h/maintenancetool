<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAppStore } from '../store/appStore';
import { useAuth } from '../composables/useAuth';
import { useForm } from '../composables/useForm';

interface EmailTemplate {
  id?: string;
  name: string;
  subjectPattern: string;
  bodyPattern: string;
}

const appStore = useAppStore();
const { isAdmin, isManager } = useAuth();
const templates = ref<EmailTemplate[]>();

const canEdit = computed(() => isAdmin.value || isManager.value);
const isEditing = ref(false);
const activeId = ref<string | null>(null);

const { form, errors, validate, resetForm } = useForm<EmailTemplate>({
  name: '',
  subjectPattern: '',
  bodyPattern: '',
});

const loadTemplates = async () => {
  try {
    const data = await appStore.apiFetch<EmailTemplate[]>('/api/email-templates');
    templates.value = data || [];
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Laden der Vorlagen';
  }
};

onMounted(() => {
  loadTemplates();
});

const showCreateForm = () => {
  resetForm({
    name: '',
    subjectPattern: '',
    bodyPattern: '',
  });
  activeId.value = null;
  isEditing.value = true;
};

const showEditForm = (item: EmailTemplate) => {
  resetForm({
    name: item.name,
    subjectPattern: item.subjectPattern,
    bodyPattern: item.bodyPattern,
  });
  activeId.value = item.id || null;
  isEditing.value = true;
};

const saveTemplate = async () => {
  // Simple validation
  errors.value = {};
  if (!form.value.name.trim()) errors.value.name = 'Name ist erforderlich';
  if (!form.value.subjectPattern.trim()) errors.value.subjectPattern = 'Betreff-Muster ist erforderlich';
  if (!form.value.bodyPattern.trim()) errors.value.bodyPattern = 'Inhalt-Muster ist erforderlich';

  if (Object.keys(errors.value).length > 0) return;

  try {
    if (activeId.value) {
      await appStore.apiFetch(`/api/email-templates/${activeId.value}`, {
        method: 'PUT',
        body: JSON.stringify(form.value),
      });
      appStore.successMsg = 'Vorlage erfolgreich aktualisiert';
    } else {
      await appStore.apiFetch('/api/email-templates', {
        method: 'POST',
        body: JSON.stringify(form.value),
      });
      appStore.successMsg = 'Vorlage erfolgreich erstellt';
    }
    isEditing.value = false;
    await loadTemplates();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Speichern der Vorlage';
  }
};

const deleteTemplate = async (id: string) => {
  if (!confirm('Möchten Sie diese Vorlage wirklich löschen?')) return;
  try {
    await appStore.apiFetch(`/api/email-templates/${id}`, {
      method: 'DELETE',
    });
    appStore.successMsg = 'Vorlage gelöscht';
    await loadTemplates();
  } catch (err: any) {
    appStore.errorMsg = err.message || 'Fehler beim Löschen der Vorlage';
  }
};

// Variable insertion helper
const insertVariable = (field: 'subjectPattern' | 'bodyPattern', token: string) => {
  if (field === 'subjectPattern') {
    form.value.subjectPattern += token;
  } else {
    form.value.bodyPattern += token;
  }
};
</script>

<template>
  <div class="view-wrapper">
    <div class="view-header">
      <div>
        <h1>E-Mail-Vorlagen</h1>
        <p class="subtitle">Verwalten Sie wiederverwendbare Benachrichtigungsmuster für Ihre Wartungsfenster.</p>
      </div>
      <button v-if="canEdit && !isEditing" @click="showCreateForm" class="btn btn-primary">
        <i class="mdi mdi-plus"></i> Neue Vorlage
      </button>
    </div>

    <!-- Template Form Card -->
    <div v-if="isEditing" class="card editor-card">
      <div class="card-header">
        <h2>{{ activeId ? 'Vorlage bearbeiten' : 'Neue Vorlage erstellen' }}</h2>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label for="templateName">Name der Vorlage</label>
          <input
            id="templateName"
            type="text"
            v-model="form.name"
            class="form-control"
            :class="{ 'is-invalid': errors.name }"
            placeholder="z. B. Standard-Wartungsankündigung"
          />
          <div class="invalid-feedback" v-if="errors.name">{{ errors.name }}</div>
        </div>

        <div class="form-group">
          <div class="label-with-helper">
            <label for="subjectPattern">Betreff-Muster</label>
            <div class="token-helper">
              <span>Token einfügen:</span>
              <button @click="insertVariable('subjectPattern', '{appName}')" class="token-btn">{appName}</button>
              <button @click="insertVariable('subjectPattern', '{title}')" class="token-btn">{title}</button>
            </div>
          </div>
          <input
            id="subjectPattern"
            type="text"
            v-model="form.subjectPattern"
            class="form-control"
            :class="{ 'is-invalid': errors.subjectPattern }"
            placeholder="z. B. Geplante Wartungsarbeiten für {appName} am {startTime}"
          />
          <div class="invalid-feedback" v-if="errors.subjectPattern">{{ errors.subjectPattern }}</div>
        </div>

        <div class="form-group">
          <div class="label-with-helper">
            <label for="bodyPattern">Inhalt-Muster (HTML/Text)</label>
            <div class="token-helper scrollable-helper">
              <span>Token einfügen:</span>
              <button @click="insertVariable('bodyPattern', '{userName}')" class="token-btn">{userName}</button>
              <button @click="insertVariable('bodyPattern', '{appName}')" class="token-btn">{appName}</button>
              <button @click="insertVariable('bodyPattern', '{appUrl}')" class="token-btn">{appUrl}</button>
              <button @click="insertVariable('bodyPattern', '{startTime}')" class="token-btn">{startTime}</button>
              <button @click="insertVariable('bodyPattern', '{endTime}')" class="token-btn">{endTime}</button>
              <button @click="insertVariable('bodyPattern', '{description}')" class="token-btn">{description}</button>
            </div>
          </div>
          <textarea
            id="bodyPattern"
            v-model="form.bodyPattern"
            rows="10"
            class="form-control code-font"
            :class="{ 'is-invalid': errors.bodyPattern }"
            placeholder="Schreiben Sie Ihre E-Mail-Nachricht..."
          ></textarea>
          <div class="invalid-feedback" v-if="errors.bodyPattern">{{ errors.bodyPattern }}</div>
        </div>

        <div class="editor-actions">
          <button @click="isEditing = false" class="btn btn-secondary">Abbrechen</button>
          <button @click="saveTemplate" class="btn btn-primary">Speichern</button>
        </div>
      </div>
    </div>

    <!-- Template Listing -->
    <div v-else class="grid">
      <div v-if="!templates || templates.length === 0" class="card empty-card">
        <i class="mdi mdi-email-open-outline empty-icon"></i>
        <h3>Keine Vorlagen vorhanden</h3>
        <p>Erstellen Sie eine E-Mail-Vorlage, um Benachrichtigungen an Ihre Anwendungsnutzer senden zu können.</p>
        <button v-if="canEdit" @click="showCreateForm" class="btn btn-primary" style="margin-top: 1rem">
          Vorlage erstellen
        </button>
      </div>

      <div v-else v-for="temp in templates" :key="temp.id" class="card template-card">
        <div class="template-header">
          <div class="template-title-group">
            <i class="mdi mdi-email-edit-outline template-icon"></i>
            <h3>{{ temp.name }}</h3>
          </div>
          <div class="actions" v-if="canEdit">
            <button @click="showEditForm(temp)" class="action-btn text-primary" title="Bearbeiten">
              <i class="mdi mdi-pencil-outline"></i>
            </button>
            <button @click="deleteTemplate(temp.id!)" class="action-btn text-danger" title="Löschen">
              <i class="mdi mdi-trash-can-outline"></i>
            </button>
          </div>
        </div>
        <div class="template-body">
          <div class="preview-line">
            <strong>Betreff:</strong>
            <span class="preview-text text-muted">{{ temp.subjectPattern }}</span>
          </div>
          <div class="preview-block">
            <strong>Inhalt:</strong>
            <div class="body-preview">{{ temp.bodyPattern }}</div>
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

.label-with-helper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.label-with-helper label {
  margin-bottom: 0;
}

.token-helper {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.scrollable-helper {
  max-width: 70%;
  overflow-x: auto;
  padding-bottom: 2px;
}

.token-btn {
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 0.15rem 0.4rem;
  font-family: monospace;
  font-size: 0.8rem;
  color: var(--brand-primary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.token-btn:hover {
  background-color: var(--brand-primary);
  color: white;
  border-color: var(--brand-primary);
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

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 1.5rem;
}

.empty-card {
  grid-column: 1 / -1;
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

.template-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.template-title-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.template-icon {
  font-size: 1.5rem;
  color: var(--brand-primary);
}

.preview-line {
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
}

.preview-text {
  margin-left: 0.5rem;
  word-break: break-all;
}

.preview-block {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.9rem;
}

.body-preview {
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 0.75rem;
  white-space: pre-wrap;
  max-height: 150px;
  overflow-y: auto;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.action-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background-color: var(--bg-app);
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
