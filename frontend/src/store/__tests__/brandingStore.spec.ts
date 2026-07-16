import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useBrandingStore } from '../brandingStore';
import type { Branding } from '../../types';

describe('brandingStore', () => {
  const mockStyleSetProperty = vi.fn();

  beforeEach(() => {
    setActivePinia(createPinia());
    global.fetch = vi.fn();

    // Mock style setProperty on documentElement
    Object.defineProperty(document.documentElement, 'style', {
      value: {
        setProperty: mockStyleSetProperty,
      },
      writable: true,
    });
    mockStyleSetProperty.mockReset();
  });

  it('should initialize with default branding state', () => {
    const store = useBrandingStore();
    expect(store.branding.companyName).toBe('Maintenance Tool');
    expect(store.branding.themeColor).toBe('#3b82f6');
  });

  it('should load branding style variables successfully and apply to document style', async () => {
    const mockBranding: Branding = {
      companyName: 'Custom Brand',
      logoPath: '/logo.png',
      themeColor: '#ff0000',
      themeColorHover: '#cc0000',
    };

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockBranding,
    });

    const store = useBrandingStore();
    await store.loadBranding();
    expect(store.branding).toEqual(mockBranding);
    expect(mockStyleSetProperty).toHaveBeenCalledWith('--brand-primary', '#ff0000');
    expect(mockStyleSetProperty).toHaveBeenCalledWith('--brand-primary-hover', '#cc0000');
  });

  it('should gracefully handle API fetch failure', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
    });

    const store = useBrandingStore();
    await store.loadBranding();
    // state is preserved
    expect(store.branding.companyName).toBe('Maintenance Tool');
    expect(mockStyleSetProperty).not.toHaveBeenCalled();
  });
});
