import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import AdminDashboardView from '@/views/AdminDashboardView.vue';
import CommunityManageView from '@/views/CommunityManageView.vue';
import FeedView from '@/views/FeedView.vue';
import LoginView from '@/views/LoginView.vue';
import PostDetailView from '@/views/PostDetailView.vue';
import SettingsView from '@/views/SettingsView.vue';

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/feed'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        public: true,
        layout: 'auth'
      }
    },
    {
      path: '/feed',
      name: 'feed',
      component: FeedView
    },
    {
      path: '/posts/:postId',
      name: 'post-detail',
      component: PostDetailView
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboardView
    },
    {
      path: '/community',
      name: 'community-manage',
      component: CommunityManageView
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView
    }
  ]
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  authStore.hydrate();

  if (!to.meta.public && !authStore.isAuthenticated) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    };
  }

  if (to.name === 'login' && authStore.isAuthenticated) {
    return '/feed';
  }

  return true;
});
