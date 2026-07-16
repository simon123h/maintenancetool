import { createApp } from 'vue';
import { createPinia } from 'pinia';
import router from './router';
import App from './App.vue';
import './style.css';
import '@mdi/font/css/materialdesignicons.css';

// Erstelle die Vue 3-Anwendung
const app = createApp(App);

// Initialisiere State Management und Routing
const pinia = createPinia();
app.use(pinia);
app.use(router);

app.mount('#app');
