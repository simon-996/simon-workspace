import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import WorkspaceView from '../views/WorkspaceView.vue'
import ClassManagementView from '../views/workspace/ClassManagementView.vue'
import CourseManagementView from '../views/workspace/CourseManagementView.vue'
import SemesterManagementView from '../views/workspace/SemesterManagementView.vue'
import WorkspaceHomeView from '../views/workspace/WorkspaceHomeView.vue'
import { useAuthStore } from '../stores/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/workspace',
      component: WorkspaceView,
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: '',
          name: 'workspace',
          component: WorkspaceHomeView,
          meta: {
            title: '工作台总览',
          },
        },
        {
          path: 'courses',
          name: 'workspace-courses',
          component: CourseManagementView,
          meta: {
            title: '课程管理',
          },
        },
        {
          path: 'classes',
          name: 'workspace-classes',
          component: ClassManagementView,
          meta: {
            title: '班级管理',
          },
        },
        {
          path: 'semesters',
          name: 'workspace-semesters',
          component: SemesterManagementView,
          meta: {
            title: '学期管理',
          },
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth) {
    const authenticated = await auth.restore()
    if (!authenticated) {
      return {
        name: 'login',
        query: {
          redirect: to.fullPath,
        },
      }
    }
  }

  if (to.name === 'login' && await auth.restore()) {
    return { name: 'workspace' }
  }
})
