import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { getLayoutConfig, saveLayoutConfig, resetLayoutConfig } from '@/api/dashboard';

export interface DashboardModule {
    id: string;
    title: string;
    enabled: boolean;
    order: number;
    width?: number; // 宽度（24栅格）
    component?: string;
}

export interface LayoutConfig {
    layoutVersion: string;
    modules: DashboardModule[];
    theme?: string;
    compactMode?: boolean;
}

// 默认布局配置
const defaultLayoutConfig: LayoutConfig = {
    layoutVersion: '1.0',
    modules: [
        { id: 'quick-actions', title: '快捷操作', enabled: true, order: 1, width: 24 },
        { id: 'stat-cards', title: '统计卡片', enabled: true, order: 2, width: 24 },
        { id: 'todo-list', title: '待办事项', enabled: true, order: 3, width: 8 },
        { id: 'notifications', title: '系统通知', enabled: true, order: 4, width: 8 },
        { id: 'charts', title: '数据图表', enabled: true, order: 5, width: 16 },
        { id: 'activities', title: '最近活动', enabled: true, order: 6, width: 24 }
    ],
    theme: 'light',
    compactMode: false
};

// 预设布局模板
export const layoutPresets = {
    default: {
        name: '默认布局',
        config: defaultLayoutConfig
    },
    compact: {
        name: '紧凑模式',
        config: {
            ...defaultLayoutConfig,
            compactMode: true,
            modules: defaultLayoutConfig.modules.map(m => ({ ...m, width: m.width === 24 ? 24 : 12 }))
        }
    },
    analysis: {
        name: '数据分析模式',
        config: {
            ...defaultLayoutConfig,
            modules: [
                { id: 'stat-cards', title: '统计卡片', enabled: true, order: 1, width: 24 },
                { id: 'charts', title: '数据图表', enabled: true, order: 2, width: 24 },
                { id: 'todo-list', title: '待办事项', enabled: true, order: 3, width: 12 },
                { id: 'activities', title: '最近活动', enabled: true, order: 4, width: 12 },
                { id: 'quick-actions', title: '快捷操作', enabled: false, order: 5, width: 24 },
                { id: 'notifications', title: '系统通知', enabled: false, order: 6, width: 24 }
            ]
        }
    },
    management: {
        name: '管理模式',
        config: {
            ...defaultLayoutConfig,
            modules: [
                { id: 'quick-actions', title: '快捷操作', enabled: true, order: 1, width: 24 },
                { id: 'todo-list', title: '待办事项', enabled: true, order: 2, width: 12 },
                { id: 'notifications', title: '系统通知', enabled: true, order: 3, width: 12 },
                { id: 'stat-cards', title: '统计卡片', enabled: true, order: 4, width: 24 },
                { id: 'activities', title: '最近活动', enabled: true, order: 5, width: 24 },
                { id: 'charts', title: '数据图表', enabled: false, order: 6, width: 24 }
            ]
        }
    }
};

export function useLayoutConfig() {
    const currentConfig = ref<LayoutConfig>(JSON.parse(JSON.stringify(defaultLayoutConfig)));
    const isEditMode = ref(false);
    const loading = ref(false);

    // 计算属性：启用的模块列表
    const enabledModules = computed(() =>
        currentConfig.value.modules
            .filter(m => m.enabled)
            .sort((a, b) => a.order - b.order)
    );

    /**
     * 加载用户布局配置
     */
    const loadLayout = async () => {
        loading.value = true;
        try {
            const res = await getLayoutConfig();
            if (res.code === 200 && res.data) {
                const config = JSON.parse(res.data);
                currentConfig.value = config;
            } else {
                // 如果没有配置，使用默认配置
                currentConfig.value = JSON.parse(JSON.stringify(defaultLayoutConfig));
            }
        } catch (error) {
            console.error('加载布局配置失败:', error);
            currentConfig.value = JSON.parse(JSON.stringify(defaultLayoutConfig));
        } finally {
            loading.value = false;
        }
    };

    /**
     * 保存布局配置
     */
    const saveLayout = async () => {
        loading.value = true;
        try {
            const configStr = JSON.stringify(currentConfig.value);
            const res = await saveLayoutConfig(configStr);
            if (res.code === 200) {
                ElMessage.success('布局配置已保存');
                isEditMode.value = false;
                return true;
            } else {
                ElMessage.error(res.msg || '保存失败');
                return false;
            }
        } catch (error) {
            console.error('保存布局配置失败:', error);
            ElMessage.error('保存布局配置失败');
            return false;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 重置为默认布局
     */
    const resetLayout = async () => {
        loading.value = true;
        try {
            const res = await resetLayoutConfig();
            if (res.code === 200) {
                currentConfig.value = JSON.parse(JSON.stringify(defaultLayoutConfig));
                ElMessage.success('布局已重置为默认值');
                isEditMode.value = false;
                return true;
            } else {
                ElMessage.error(res.msg || '重置失败');
                return false;
            }
        } catch (error) {
            console.error('重置布局配置失败:', error);
            ElMessage.error('重置布局配置失败');
            return false;
        } finally {
            loading.value = false;
        }
    };

    /**
     * 应用预设布局
     */
    const applyPreset = (presetName: keyof typeof layoutPresets) => {
        const preset = layoutPresets[presetName];
        if (preset) {
            currentConfig.value = JSON.parse(JSON.stringify(preset.config));
            ElMessage.success(`已应用"${preset.name}"`);
        }
    };

    /**
     * 切换模块启用状态
     */
    const toggleModule = (moduleId: string) => {
        const module = currentConfig.value.modules.find(m => m.id === moduleId);
        if (module) {
            module.enabled = !module.enabled;
        }
    };

    /**
     * 更新模块宽度
     */
    const updateModuleWidth = (moduleId: string, width: number) => {
        const module = currentConfig.value.modules.find(m => m.id === moduleId);
        if (module) {
            module.width = width;
        }
    };

    /**
     * 更新模块顺序（拖拽后）
     */
    const updateModuleOrder = (modules: DashboardModule[]) => {
        modules.forEach((module, index) => {
            module.order = index + 1;
        });
        currentConfig.value.modules = modules;
    };

    return {
        currentConfig,
        isEditMode,
        loading,
        enabledModules,
        loadLayout,
        saveLayout,
        resetLayout,
        applyPreset,
        toggleModule,
        updateModuleWidth,
        updateModuleOrder
    };
}
