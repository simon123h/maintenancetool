import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '../authStore';
import { useAppStore } from '../appStore';
import type { UserAccount } from '../../types';

describe('authStore', () => {
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

  const createMockUser = (rollen: string[] = ['ROLE_TEILNEHMER']): UserAccount => ({
    id: 'user-1',
    mailAdresse: 'test@example.com',
    vollerName: 'Max Mustermann',
    loginName: 'maxm',
    rollen,
  });

  describe('Initialization and LocalStorage', () => {
    it('should initialize with default state', () => {
      const store = useAuthStore();
      expect(store.currentUser).toBeNull();
      expect(store.users).toEqual([]);
    });

    it('should load saved state from localStorage if present', () => {
      const savedState = {
        currentUser: createMockUser(),
        users: [createMockUser(['ROLE_ADMIN'])],
      };
      localStorage.setItem('maintenance_tool_auth', JSON.stringify(savedState));

      const store = useAuthStore();
      expect(store.currentUser).toEqual(savedState.currentUser);
      expect(store.users).toHaveLength(1);
    });

    it('should handle corrupt localStorage state gracefully', () => {
      localStorage.setItem('maintenance_tool_auth', 'invalid json{');
      const store = useAuthStore();
      expect(store.currentUser).toBeNull();
    });
  });

  describe('Actions', () => {
    it('should load user profile successfully', async () => {
      const mockUser = createMockUser();
      global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => mockUser,
      });

      const store = useAuthStore();
      const appStore = useAppStore();
      await store.loadProfile();
      expect(store.currentUser).toEqual(mockUser);
      expect(appStore.errorMsg).toBeNull();
    });

    it('should set errorMsg on appStore if loading user profile fails', async () => {
      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      });

      const store = useAuthStore();
      const appStore = useAppStore();
      await store.loadProfile();
      expect(store.currentUser).toBeNull();
      expect(appStore.errorMsg).toContain('Benutzerprofil konnte nicht geladen werden');
    });

    it('should load users successfully', async () => {
      const mockUsers = [createMockUser()];
      global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => mockUsers,
      });

      const store = useAuthStore();
      const appStore = useAppStore();
      await store.loadUsers();
      expect(store.users).toEqual(mockUsers);
      expect(appStore.errorMsg).toBeNull();
    });

    it('should set errorMsg on appStore if loading users fails', async () => {
      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      });

      const store = useAuthStore();
      const appStore = useAppStore();
      await store.loadUsers();
      expect(store.users).toEqual([]);
      expect(appStore.errorMsg).toContain('Benutzerliste konnte nicht geladen werden');
    });

    it('should toggle user roles successfully and refresh lists', async () => {
      const store = useAuthStore();
      const appStore = useAppStore();
      const user = createMockUser(['ROLE_TEILNEHMER']);
      store.currentUser = user;

      const mockFetch = vi.fn().mockImplementation((url: string) => {
        if (url.includes('/api/users/user-1/roles')) {
          return Promise.resolve({ ok: true, status: 200, json: async () => ({}) });
        }
        if (url.includes('/api/users/me')) {
          return Promise.resolve({ ok: true, status: 200, json: async () => user });
        }
        if (url.includes('/api/users')) {
          return Promise.resolve({ ok: true, status: 200, json: async () => [user] });
        }
        return Promise.reject(new Error(`Unhandled URL: ${url}`));
      });
      global.fetch = mockFetch;

      await store.toggleRole(user, 'ROLE_MANAGER');
      expect(appStore.successMsg).toContain('erfolgreich aktualisiert');
      expect(mockFetch).toHaveBeenCalledWith('/api/users/user-1/roles', expect.any(Object));
    });
  });
});
