import { defineConfig } from 'vitepress'

// Vitepress-Konfiguration für die Hilfeseite unter /help
export default defineConfig({
  title: 'Maintenance Tool Hilfe',
  description: 'Dokumentation und FAQ zum Maintenance Tool',
  
  // Schreibt die statische Seite direkt in den static/help-Ordner des Backends
  outDir: '../../target/classes/static/help',
  
  // Die Anwendung wird unter der Route /help gehostet
  base: '/help/',

  themeConfig: {
    logo: '/assets/help-logo.svg',
    nav: [
      { text: 'Startseite', link: '/' },
      { text: 'FAQ & Hilfe', link: '/faq' }
    ],
    sidebar: [
      {
        text: 'Handbuch',
        items: [
          { text: 'Einführung', link: '/' },
          { text: 'Administration', link: '/admin' }
        ]
      }
    ]
  }
})
