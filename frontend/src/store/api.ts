// Helper to read cookies
export function getCookie(name: string): string | null {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return decodeURIComponent(parts.pop()!.split(';').shift() || '');
  }
  return null;
}

// Unified Fetch Utility
export async function apiFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  if (options.headers) {
    if (options.headers instanceof Headers) {
      options.headers.forEach((val, key) => {
        headers[key] = val;
      });
    } else if (Array.isArray(options.headers)) {
      options.headers.forEach(([key, val]) => {
        headers[key] = val;
      });
    } else {
      Object.assign(headers, options.headers);
    }
  }

  const csrfToken = getCookie('XSRF-TOKEN');
  if (csrfToken && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(options.method || 'GET')) {
    headers['X-XSRF-TOKEN'] = csrfToken;
  }

  try {
    const res = await fetch(url, {
      ...options,
      headers,
    });
    if (!res.ok) {
      if (res.status === 401) {
        // If unauthorized, redirect to the login page cleanly
        window.location.href = '/login';
        return null as unknown as T;
      }
      let errData: any = {};
      try {
        errData = await res.json();
      } catch (e) {}
      const errMsg = errData.message || `HTTP Fehler ${res.status}: ${res.statusText}`;
      throw new Error(errMsg);
    }
    if (res.status === 204) {
      return null as unknown as T;
    }
    try {
      return await res.json();
    } catch (e) {
      return null as unknown as T;
    }
  } catch (err: any) {
    console.error(`API Fehler bei ${url}:`, err);
    throw err;
  }
}
