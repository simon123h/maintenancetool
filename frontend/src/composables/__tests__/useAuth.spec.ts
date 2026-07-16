import { describe, it, expect, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '../../store/authStore';
import { useAuth } from '../useAuth';
import type { UserAccount } from '../../types';

describe('useAuth Composable', () => {
  beforeEach(() => {
    // Initialize standard Pinia instance before each test
    setActivePinia(createPinia());
  });

  const createMockUser = (rollen: string[]): UserAccount => ({
    id: 'test-user-id',
    mailAdresse: 'test@maintenancetool.de',
    vollerName: 'Max Mustermann',
    loginName: 'maxm',
    rollen,
  });

  it('should return unauthenticated state when no user is logged in', () => {
    const store = useAuthStore();
    store.currentUser = null;

    const { currentUser, isAuthenticated, isAdmin, isManager, isTeilnehmer, hasRole } = useAuth();

    expect(currentUser.value).toBeNull();
    expect(isAuthenticated.value).toBe(false);
    expect(isAdmin.value).toBe(false);
    expect(isManager.value).toBe(false);
    expect(isTeilnehmer.value).toBe(false);
    expect(hasRole('ROLE_ADMIN')).toBe(false);
  });

  it('should identify a participant (ROLE_TEILNEHMER)', () => {
    const store = useAuthStore();
    store.currentUser = createMockUser(['ROLE_TEILNEHMER']);

    const { currentUser, isAuthenticated, isAdmin, isManager, isTeilnehmer, hasRole } = useAuth();

    expect(currentUser.value).not.toBeNull();
    expect(currentUser.value?.loginName).toBe('maxm');
    expect(isAuthenticated.value).toBe(true);
    expect(isTeilnehmer.value).toBe(true);
    expect(isAdmin.value).toBe(false);
    expect(isManager.value).toBe(false);
    expect(hasRole('ROLE_TEILNEHMER')).toBe(true);
    expect(hasRole('ROLE_ADMIN')).toBe(false);
  });

  it('should identify an admin (ROLE_ADMIN)', () => {
    const store = useAuthStore();
    store.currentUser = createMockUser(['ROLE_ADMIN', 'ROLE_TEILNEHMER']);

    const { isAuthenticated, isAdmin, isManager, isTeilnehmer, hasRole } = useAuth();

    expect(isAuthenticated.value).toBe(true);
    expect(isAdmin.value).toBe(true);
    expect(isTeilnehmer.value).toBe(true);
    expect(isManager.value).toBe(false);
    expect(hasRole('ROLE_ADMIN')).toBe(true);
    expect(hasRole('ROLE_MANAGER')).toBe(false);
  });

  it('should identify a manager (ROLE_MANAGER)', () => {
    const store = useAuthStore();
    store.currentUser = createMockUser(['ROLE_MANAGER']);

    const { isAuthenticated, isAdmin, isManager, isTeilnehmer, hasRole } = useAuth();

    expect(isAuthenticated.value).toBe(true);
    expect(isManager.value).toBe(true);
    expect(isAdmin.value).toBe(false);
    expect(isTeilnehmer.value).toBe(false);
    expect(hasRole('ROLE_MANAGER')).toBe(true);
  });
});
