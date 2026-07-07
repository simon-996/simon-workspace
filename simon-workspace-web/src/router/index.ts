import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import WorkspaceView from '../views/WorkspaceView.vue'
import BlogDetailView from '../views/blog/BlogDetailView.vue'
import BlogEditorView from '../views/blog/BlogEditorView.vue'
import BlogListView from '../views/blog/BlogListView.vue'
import PublicCourseDetailView from '../views/course/PublicCourseDetailView.vue'
import PublicCourseListView from '../views/course/PublicCourseListView.vue'
import PublicMarkdownView from '../views/course/PublicMarkdownView.vue'
import BlogManagementView from '../views/workspace/BlogManagementView.vue'
import ClassManagementView from '../views/workspace/ClassManagementView.vue'
import CourseManagementView from '../views/workspace/CourseManagementView.vue'
import FileCenterView from '../views/workspace/FileCenterView.vue'
import GenerationHistoryView from '../views/workspace/GenerationHistoryView.vue'
import SecurityManagementView from '../views/workspace/SecurityManagementView.vue'
import SemesterManagementView from '../views/workspace/SemesterManagementView.vue'
import SiteConfigView from '../views/workspace/SiteConfigView.vue'
import StorageManagementView from '../views/workspace/StorageManagementView.vue'
import TemplateManagementView from '../views/workspace/TemplateManagementView.vue'
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
        permission: 'workspace:view',
      },
      children: [
        {
          path: '',
          name: 'workspace',
          component: WorkspaceHomeView,
          meta: {
            titleKey: 'workspace.pages.overview',
            permission: 'workspace:view',
          },
        },
        {
          path: 'courses',
          name: 'workspace-courses',
          component: CourseManagementView,
          meta: {
            titleKey: 'workspace.pages.courses',
            permission: 'course:manage',
          },
        },
        {
          path: 'classes',
          name: 'workspace-classes',
          component: ClassManagementView,
          meta: {
            titleKey: 'workspace.pages.classes',
            permission: 'class:manage',
          },
        },
        {
          path: 'semesters',
          name: 'workspace-semesters',
          component: SemesterManagementView,
          meta: {
            titleKey: 'workspace.pages.semesters',
            permission: 'semester:manage',
          },
        },
        {
          path: 'templates',
          name: 'workspace-templates',
          component: TemplateManagementView,
          meta: {
            titleKey: 'workspace.pages.templates',
            permission: 'template:manage',
          },
        },
        {
          path: 'files',
          name: 'workspace-files',
          component: FileCenterView,
          meta: {
            titleKey: 'workspace.pages.files',
            permission: 'file:manage',
          },
        },
        {
          path: 'storage',
          name: 'workspace-storage',
          component: StorageManagementView,
          meta: {
            titleKey: 'workspace.pages.storage',
            permission: 'file:manage',
          },
        },
        {
          path: 'history',
          name: 'workspace-history',
          component: GenerationHistoryView,
          meta: {
            titleKey: 'workspace.pages.history',
            permission: 'generation:history',
          },
        },
        {
          path: 'blog',
          name: 'workspace-blog',
          component: BlogManagementView,
          meta: {
            titleKey: 'workspace.pages.blog',
            permission: 'blog:category:manage',
          },
        },
        {
          path: 'security',
          name: 'workspace-security',
          component: SecurityManagementView,
          meta: {
            titleKey: 'workspace.pages.security',
            permission: 'user:manage',
          },
        },
        {
          path: 'site',
          name: 'workspace-site',
          component: SiteConfigView,
          meta: {
            titleKey: 'workspace.pages.site',
            permission: 'site:config',
          },
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/blog',
      name: 'blog',
      component: BlogListView,
    },
    {
      path: '/courses',
      name: 'courses',
      component: PublicCourseListView,
    },
    {
      path: '/courses/:id',
      name: 'course-detail',
      component: PublicCourseDetailView,
    },
    {
      path: '/courses/:id/materials/:materialId/read',
      name: 'course-markdown',
      component: PublicMarkdownView,
    },
    {
      path: '/blog/new',
      name: 'blog-new',
      component: BlogEditorView,
      meta: {
        requiresAuth: true,
        permission: 'blog:post:create',
      },
    },
    {
      path: '/blog/:id/edit',
      name: 'blog-edit',
      component: BlogEditorView,
      meta: {
        requiresAuth: true,
        permission: 'blog:post:update',
      },
    },
    {
      path: '/blog/:id',
      name: 'blog-detail',
      component: BlogDetailView,
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

    const permission = typeof to.meta.permission === 'string' ? to.meta.permission : ''
    if (permission && !auth.hasPermission(permission)) {
      return { name: 'home' }
    }
  }

  if (to.name === 'login' && await auth.restore()) {
    return { name: 'workspace' }
  }
})
