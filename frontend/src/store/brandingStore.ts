import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Branding } from '../types';
import { apiFetch } from './api';

export const useBrandingStore = defineStore('branding', () => {
  const branding = ref<Branding>({
    companyName: 'Maintenance Tool',
    logoPath: '',
    themeColor: '#3b82f6',
    themeColorHover: '#2563eb',
  });

  const loadBranding = async () => {
    try {
      const data = await apiFetch<Branding>('/api/config/branding');
      if (data) {
        branding.value = data;
        if (data.themeColor) {
          document.documentElement.style.setProperty('--brand-primary', data.themeColor);
        }
        if (data.themeColorHover) {
          document.documentElement.style.setProperty('--brand-primary-hover', data.themeColorHover);
        }
      }
    } catch (err: any) {
      console.error('Branding konnte nicht geladen werden:', err);
    }
  };

  return {
    branding,
    loadBranding,
  };
});
