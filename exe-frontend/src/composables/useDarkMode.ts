import { ref, watch } from 'vue';

/**
 * 深色模式管理 Composable
 */
export function useDarkMode() {
    // 从 localStorage 读取主题设置，默认为浅色
    const isDark = ref<boolean>(localStorage.getItem('theme') === 'dark');

    /**
     * 切换深色模式
     */
    const toggleDarkMode = () => {
        isDark.value = !isDark.value;
    };

    /**
     * 应用主题
     */
    const applyTheme = () => {
        const html = document.documentElement;

        if (isDark.value) {
            html.classList.add('dark');
            localStorage.setItem('theme', 'dark');
        } else {
            html.classList.remove('dark');
            localStorage.setItem('theme', 'light');
        }

        // 同时更新 body 的数据属性，方便某些组件使用
        document.body.setAttribute('data-theme', isDark.value ? 'dark' : 'light');
    };

    /**
     * 设置特定主题
     */
    const setTheme = (theme: 'light' | 'dark') => {
        isDark.value = theme === 'dark';
    };

    // 监听 isDark 变化，自动应用主题
    watch(isDark, applyTheme, { immediate: true });

    return {
        isDark,
        toggleDarkMode,
        setTheme,
    };
}
