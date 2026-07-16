import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAppStore } from '../appStore';
import { useAuthStore } from '../authStore';

describe('appStore', () => {
  const mockLocation = { href: '' };

  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();

    mockLocation.href = '';
    Object.defineProperty(window, 'location', {
      value: mockLocation,
      writable: true,
    });

    global.fetch = vi.fn();
  });

  describe('Initialization and State', () => {
    it('should initialize with default state', () => {
      const store = useAppStore();
      expect(store.loading).toBe(true);
      expect(store.errorMsg).toBeNull();
      expect(store.successMsg).toBeNull();
    });
  });

  describe('apiFetch Utility', () => {
    it('should cleanly handle 401 unauthorized error by redirecting to /login', async () => {
      const mockFetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 401,
      });
      global.fetch = mockFetch;

      const store = useAppStore();
      const result = await store.apiFetch('/api/test');
      expect(result).toBeNull();
      expect(mockLocation.href).toBe('/login');
    });

    it('should handle 204 No Content response correctly', async () => {
      const mockFetch = vi.fn().mockResolvedValue({
        ok: true,
        status: 204,
      });
      global.fetch = mockFetch;

      const store = useAppStore();
      const result = await store.apiFetch('/api/test');
      expect(result).toBeNull();
    });

    it('should parse and return JSON content on successful response', async () => {
      const mockData = { key: 'value' };
      const mockFetch = vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => mockData,
      });
      global.fetch = mockFetch;

      const store = useAppStore();
      const result = await store.apiFetch('/api/test');
      expect(result).toEqual(mockData);
    });

    it('should throw an error with backend message when api fails', async () => {
      const mockFetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 400,
        statusText: 'Bad Request',
        json: async () => ({ message: 'Ungültige Daten' }),
      });
      global.fetch = mockFetch;

      const store = useAppStore();
      await expect(store.apiFetch('/api/test')).rejects.toThrow('Ungültige Daten');
    });
  });

  describe('initApp flow', () => {
    it('should initialize app sequence correctly for normal user', async () => {
      const appStore = useAppStore();
      const authStore = useAuthStore();

      // Spy on store loaders
      const loadProfileSpy = vi.spyOn(authStore, 'loadProfile').mockImplementation(async () => {
        authStore.currentUser = {
          id: 'u-1',
          mailAdresse: 't@ex.com',
          vollerName: 'Max',
          loginName: 'maxm',
          rollen: ['ROLE_TEILNEHMER'],
        };
      });
      const loadUsersSpy = vi.spyOn(authStore, 'loadUsers');

      await appStore.initApp();

      expect(appStore.loading).toBe(false);
      expect(loadProfileSpy).toHaveBeenCalled();
      expect(loadUsersSpy).not.toHaveBeenCalled();
    });

    it('should initialize app sequence correctly for admin user and load users', async () => {
      const appStore = useAppStore();
      const authStore = useAuthStore();

      // Spy on store loaders
      const loadProfileSpy = vi.spyOn(authStore, 'loadProfile').mockImplementation(async () => {
        authStore.currentUser = {
          id: 'u-1',
          mailAdresse: 't@ex.com',
          vollerName: 'Max',
          loginName: 'maxm',
          rollen: ['ROLE_TEILNEHMER', 'ROLE_ADMIN'],
        };
      });
      const loadUsersSpy = vi.spyOn(authStore, 'loadUsers').mockResolvedValue(undefined);

      await appStore.initApp();

      expect(appStore.loading).toBe(false);
      expect(loadUsersSpy).toHaveBeenCalled();
    });

    it('should stop initApp sequence if profile loader redirects / has no user', async () => {
      const appStore = useAppStore();
      const authStore = useAuthStore();

      vi.spyOn(authStore, 'loadProfile').mockImplementation(async () => {
        authStore.currentUser = null; // simulate failed auth
      });
      const loadUsersSpy = vi.spyOn(authStore, 'loadUsers');

      await appStore.initApp();

      expect(appStore.loading).toBe(true); // retains loading state because redirect takes over or it stops early
      expect(loadUsersSpy).not.toHaveBeenCalled();
    });
  });
});
