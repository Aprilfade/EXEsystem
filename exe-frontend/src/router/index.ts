import { createRouter, createWebHistory,} from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import MainLayout from '@/layouts/MainLayout.vue';
import { ElMessage } from "element-plus";
import { useStudentAuthStore } from '../stores/studentAuth'; // 导入学生store

const routes: Array<RouteRecordRaw> = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
    },
    // 修改后
    {
        path: '/portal',
        name: 'Portal',
        component: () => import('@/views/Portal.vue'),
        meta: { title: '系统导航' } // <-- 新增 meta.title
    },
    {
        path: '/',
        component: MainLayout,
        redirect: '/portal',
        meta: { requiresAuth: true },
        children: [
            { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { permission: 'sys:home', title: '工作台' } },
            { path: 'users', name: 'UserManagement', component: () => import('@/components/user/UserManage.vue'), meta: { permission: 'sys:user:list', title: '成员管理' } },
            { path: 'roles', name: 'RoleManagement', component: () => import('@/views/RoleManage.vue'), meta: { permission: 'sys:role:perm', title: '角色管理' } },
            { path: 'subjects', name: 'SubjectManagement', component: () => import('@/views/SubjectManage.vue'), meta: { permission: 'sys:subject:list', title: '科目管理' } },
            { path: 'knowledge-points', name: 'KnowledgePointManagement', component: () => import('@/views/KnowledgePointManage.vue'), meta: { permission: 'sys:kp:list', title: '知识点管理' } },
            { path: 'questions', name: 'QuestionManagement', component: () => import('@/views/QuestionManage.vue'), meta: { permission: 'sys:question:list', title: '题库管理' } },
            { path: 'papers', name: 'PaperManagement', component: () => import('@/views/PaperManage.vue'), meta: { permission: 'sys:paper:list', title: '试卷管理' } },
            { path: 'students', name: 'StudentManagement', component: () => import('@/views/StudentManage.vue'), meta: { permission: 'sys:student:list', title: '学生管理' } },
            { path: 'score-manage', name: 'ScoreManage', component: () => import('@/views/ScoreManage.vue'), meta: { permission: 'sys:stats:list', title: '成绩管理' } },// 暂时复用统计权限},
            { path: 'wrong-records', name: 'WrongRecordManagement', component: () => import('@/views/WrongRecordManage.vue'), meta: { permission: 'sys:wrong:list', title: '错题管理' } },
            { path: 'wrong-record-stats', name: 'WrongRecordStats', component: () => import('@/views/WrongRecordStats.vue'), meta: { permission: 'sys:wrong:list', title: '错题统计' } },
            { path: 'statistics', name: 'StatisticsDashboard', component: () => import('@/views/StatisticsDashboard.vue'), meta: { permission: 'sys:stats:list', title: '教学统计' } },
            { path: 'notifications', name: 'NotificationManagement', component: () => import('@/views/NotificationManage.vue'), meta: { permission: 'sys:notify:list', title: '通知管理' } },
            { path: 'logs/login', name: 'LoginLog', component: () => import('@/views/LoginLog.vue'), meta: { permission: 'sys:log:login', title: '登录日志' } },
// 在 children 数组中添加：
            {
                path: 'courses',
                name: 'CourseManage',
                component: () => import('@/views/CourseManage.vue'),
                meta: { permission: 'sys:course:edit', title: '课程管理' } // 记得去数据库加权限
            },
            {
                path: 'text-to-quiz',
                name: 'TextToQuiz',
                component: () => import('@/views/TextToQuiz.vue'),
                meta: { permission: 'sys:question:list', title: '智能出题' }
            },
        ]
    },
    // --- 学生端路由 ---
    {
        path: '/student/login',
        name: 'StudentLogin',
        component: () => import('@/views/StudentLogin.vue'),
    },
    {
        path: '/student',
        component: () => import('@/layouts/StudentLayout.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' },
        redirect: '/student/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'StudentDashboard',
                // 【核心修改】将这里的 component 指向我们创建的完整文件
                component: () => import('@/views/student/StudentDashboard.vue') // 原来可能是一个占位符
            },
            // === 【新增】课程列表路由 ===
            {
                path: 'courses',
                name: 'StudentCourseList',
                component: () => import('@/views/student/StudentCourseList.vue'),
                meta: { title: '课程中心' }
            },
            {
                path: 'wrong-records',
                name: 'MyWrongRecords',
                component: () => import('@/views/student/MyWrongRecords.vue')
            },
            {
                path: 'practice',
                name: 'StudentPractice',
                // 【核心修改】将 component 指向我们新创建的练习页面文件
                component: () => import('@/views/student/Practice.vue')
            },
            {
                path: 'exams',
                name: 'StudentExams',
                // 修改为指向新的试卷列表组件
                component: () => import('@/views/student/StudentExams.vue'),
                meta: { title: '模拟考试' }
            },
            // 新增 考试答题页面路由
            {
                path: 'exam/:paperId',
                name: 'StudentExamTaking',
                component: () => import('@/views/student/ExamTaking.vue'),
                meta: { title: '正在考试' }
            },
            {
                path: 'history',
                name: 'StudentHistory',
                component: () => import('@/views/student/History.vue'),
                meta: { title: '考试记录' }
            },
            {
                path: 'history/:resultId',
                name: 'StudentHistoryDetail',
                component: () => import('@/views/student/ExamResultDetail.vue'),
                meta: { title: '记录详情' }
            },
            {
                path: 'favorites',
                name: 'MyFavorites',
                component: () => import('@/views/student/MyFavorites.vue'),
                meta: { title: '我的收藏' }
            },
            {
                path: 'review',
                name: 'StudentSmartReview',
                component: () => import('@/views/student/SmartReview.vue'),
                meta: { title: '智能复习' }
            },



        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach(async (to, from, next) => {
    // --- 最终版动态标题逻辑 ---
    const pageTitle = to.meta.title as string;

    if (to.path === '/portal' && pageTitle) {
        // 特殊处理：如果是导航页，则直接使用它自己的标题，不加后缀
        document.title = pageTitle;
    } else {
        // 其他页面的通用逻辑
        let baseTitle = '试题管理综合系统'; // 默认主标题
        if (to.path.startsWith('/student/')) {
            baseTitle = '在线学习系统';
        }

        if (pageTitle) {
            document.title = `${pageTitle} - ${baseTitle}`;
        } else {
            document.title = baseTitle;
        }
    }
    // --- 标题逻辑结束 ---

    const authStore = useAuthStore();
    const studentAuthStore = useStudentAuthStore();

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    if (requiresAuth) {
        const requiredRole = to.meta.role;

        if (requiredRole === 'STUDENT') {
            // --- 学生端路由保护 ---
            if (studentAuthStore.isAuthenticated) {
                if (!studentAuthStore.student) {
                    await studentAuthStore.fetchStudentInfo();
                }
                next();
            } else {
                next({ path: '/student/login', query: { redirect: to.fullPath } });
            }
        } else {
            // --- 管理后台路由保护 (默认) ---
            if (authStore.isAuthenticated) {
                if (!authStore.user) {
                    await authStore.fetchUserInfo();
                }
                // 权限检查... (保持原有逻辑)
                const requiredPermission = to.meta.permission as string | undefined;
                if (requiredPermission && !authStore.hasPermission(requiredPermission)) {
                    ElMessage.error('您没有权限访问该页面');
                    next(from.path && from.path !== '/login' ? false : '/home');
                } else {
                    next();
                }
            } else {
                next({ path: '/login', query: { redirect: to.fullPath } });
            }
        }
    } else {
        // 不需要认证的页面直接放行
        next();
    }
});

export default router;