import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/home/pc-list',
    children: [
      {
        path: 'home/pc-list',
        name: 'PcList',
        component: () => import('../views/PcList.vue')
      },
      {
        path: 'home/pc-detail/:id',
        name: 'PcDetail',
        component: () => import('../views/PcDetail.vue')
      },
      {
        path: 'home/upload',
        name: 'Upload',
        component: () => import('../views/Upload.vue')
      },
      {
        path: 'home/ho-table',
        name: 'HoTable',
        component: () => import('../views/HoTable.vue')
      },
      {
        path: 'home/admin',
        name: 'Admin',
        component: () => import('../views/Admin.vue'),
        meta: { admin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 已登录用户访问登录/注册页 → 跳转主页
  if (to.meta.guest && userStore.isLoggedIn) {
    return next('/home/pc-list')
  }

  // 需要登录的页面
  if (!to.meta.guest && !userStore.isLoggedIn) {
    return next('/login')
  }

  // 需要 ADMIN 权限
  if (to.meta.admin && userStore.user?.role !== 'ADMIN') {
    return next('/home/pc-list')
  }

  next()
})

export default router
