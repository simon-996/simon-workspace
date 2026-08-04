import { createRouter, createWebHistory } from 'vue-router'

const HomeView = () => import('../views/HomeView.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const WorkspaceView = () => import('../views/WorkspaceView.vue')
const BlogDetailView = () => import('../views/blog/BlogDetailView.vue')
const BlogEditorView = () => import('../views/blog/BlogEditorView.vue')
const BlogListView = () => import('../views/blog/BlogListView.vue')
const PublicCourseDetailView = () => import('../views/course/PublicCourseDetailView.vue')
const PublicCourseListView = () => import('../views/course/PublicCourseListView.vue')
const PublicMarkdownView = () => import('../views/course/PublicMarkdownView.vue')
const BlogManagementView = () => import('../views/workspace/BlogManagementView.vue')
const BlogPostManagementView = () => import('../views/workspace/BlogPostManagementView.vue')
const ClassManagementView = () => import('../views/workspace/ClassManagementView.vue')
const CourseManagementView = () => import('../views/workspace/CourseManagementView.vue')
const FileCenterView = () => import('../views/workspace/FileCenterView.vue')
const GenerationHistoryView = () => import('../views/workspace/GenerationHistoryView.vue')
const SecurityManagementView = () => import('../views/workspace/SecurityManagementView.vue')
const SemesterManagementView = () => import('../views/workspace/SemesterManagementView.vue')
const SiteConfigView = () => import('../views/workspace/SiteConfigView.vue')
const StorageManagementView = () => import('../views/workspace/StorageManagementView.vue')
const TemplateManagementView = () => import('../views/workspace/TemplateManagementView.vue')
const WorkspaceHomeView = () => import('../views/workspace/WorkspaceHomeView.vue')
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
          path: 'posts',
          name: 'workspace-blog-posts',
          component: BlogPostManagementView,
          meta: {
            titleKey: 'workspace.pages.blogPosts',
            permission: 'blog:post:create',
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
      path: '/register',
      name: 'register',
      component: RegisterView,
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

  if ((to.name === 'login' || to.name === 'register') && await auth.restore()) {
    return { name: 'workspace' }
  }
})
