/// <reference types="vitest" />
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// Vite-Konfiguration für Vue 3 Frontend
export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'happy-dom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov', 'clover'],
      reportsDirectory: './coverage'
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    // Schreibt Build-Output direkt in die statischen Ressourcen von Spring Boot
    outDir: '../target/classes/static',
    emptyOutDir: true // Bereinigt den Ordner vor dem Bauen
  },
  server: {
    port: 3000,
    // API-Proxy zur lokalen Spring-Boot-Instanz im Entwicklungsmodus
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/help': {
        target: 'http://localhost:5173',
        changeOrigin: true
      },
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            if (proxyRes.headers.location) {
              proxyRes.headers.location = proxyRes.headers.location.replace('http://localhost:8080', 'http://localhost:3000')
            }
          })
        }
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            if (proxyRes.headers.location) {
              proxyRes.headers.location = proxyRes.headers.location.replace('http://localhost:8080', 'http://localhost:3000')
            }
          })
        }
      }
    }
  }
})
