<script setup lang="ts">
defineProps<{
  errorMsg: string | null;
  successMsg: string | null;
}>();

const emit = defineEmits<{
  (e: 'closeError'): void;
  (e: 'closeSuccess'): void;
}>();
</script>

<template>
  <div class="message-container" v-if="errorMsg || successMsg">
    <div v-if="errorMsg" class="alert alert-error">
      <i class="mdi mdi-alert-circle" style="font-size: 1.25rem"></i>
      <span>{{ errorMsg }}</span>
      <button type="button" class="close-alert" @click="emit('closeError')"><i class="mdi mdi-close"></i></button>
    </div>

    <div v-if="successMsg" class="alert alert-success">
      <i class="mdi mdi-check-circle" style="font-size: 1.25rem"></i>
      <span>{{ successMsg }}</span>
      <button type="button" class="close-alert" @click="emit('closeSuccess')"><i class="mdi mdi-close"></i></button>
    </div>
  </div>
</template>

<style scoped>
.message-container {
  padding-bottom: 0 !important;
  margin-top: 1rem;
  width: 100%;
}

.alert {
  padding: 1rem;
  border-radius: var(--border-radius-md);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  position: relative;
  font-weight: 500;
}

.alert-error {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--brand-danger-hover);
}

.alert-success {
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  color: var(--brand-secondary-hover);
}

.close-alert {
  position: absolute;
  right: 1rem;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: inherit;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
