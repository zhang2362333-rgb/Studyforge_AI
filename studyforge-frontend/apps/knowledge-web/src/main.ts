import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from '@/App.vue';
import { router } from '@/router';
import { useSessionStore } from '@/stores/session';
import '@/assets/base.css';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);

const sessionStore = useSessionStore(pinia);
sessionStore.hydrate();

app.use(router);
app.mount('#app');
