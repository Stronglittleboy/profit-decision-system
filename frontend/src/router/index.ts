import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import LoginView from '@/views/LoginView.vue';
import AccountSubjectView from '@/views/AccountSubjectView.vue';
import CounterpartyView from '@/views/CounterpartyView.vue';
import FactEventView from '@/views/FactEventView.vue';
import ProjectView from '@/views/ProjectView.vue';
import ContractView from '@/views/ContractView.vue';
import ReceivableView from '@/views/ReceivableView.vue';
import PayableView from '@/views/PayableView.vue';
import MainLayout from '@/layouts/MainLayout.vue';
import { isAuthenticated } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'home',
          component: HomeView
        },
        {
          path: 'account-subject',
          name: 'accountSubject',
          component: AccountSubjectView
        },
        {
          path: 'counterparty',
          name: 'counterparty',
          component: CounterpartyView
        },
        {
          path: 'fact-event',
          name: 'factEvent',
          component: FactEventView
        },
        {
          path: 'project',
          name: 'project',
          component: ProjectView
        },
        {
          path: 'contract',
          name: 'contract',
          component: ContractView
        },
        {
          path: 'receivable',
          name: 'receivable',
          component: ReceivableView
        },
        {
          path: 'payable',
          name: 'payable',
          component: PayableView
        }
      ]
    }
  ]
});

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isAuthenticated.value) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    };
  }

  if (to.path === '/login' && isAuthenticated.value) {
    return { path: '/' };
  }

  return true;
});

export default router;
