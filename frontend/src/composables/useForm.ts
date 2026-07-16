import { ref } from 'vue';

export function useForm<T extends Record<string, any>>(initialState: T) {
  const form = ref<T>({ ...initialState });

  const reset = (newState?: T) => {
    form.value = newState ? { ...newState } : { ...initialState };
  };

  return {
    form,
    reset,
  };
}
