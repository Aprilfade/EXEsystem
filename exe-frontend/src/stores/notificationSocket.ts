import { defineStore } from 'pinia';
import { ElNotification } from 'element-plus';
import { useAuthStore } from './auth';
import { useStudentAuthStore } from './studentAuth';

export const useNotificationSocketStore = defineStore('notificationSocket', {
    state: () => ({
        socket: null as WebSocket | null,
        isConnected: false,
        reconnectAttempts: 0,
    }),
    actions: {
        connect() {
            if (this.isConnected) return;

            // 1. 获取 Token (优先判断学生，再判断管理员)
            const studentStore = useStudentAuthStore();
            const adminStore = useAuthStore();
            const token = studentStore.token || adminStore.token;

            if (!token) return;

            // 2. 建立连接
            // 注意：替换为你的后端 WebSocket 地址
            const wsUrl = `ws://localhost:8080/ws/notifications?token=${token}`;
            this.socket = new WebSocket(wsUrl);

            // 3. 事件监听
            this.socket.onopen = () => {
                console.log('🔔 实时通知服务已连接');
                this.isConnected = true;
                this.reconnectAttempts = 0;
            };

            this.socket.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    this.handleMessage(data);
                } catch (e) {
                    console.error('消息解析失败', e);
                }
            };

            this.socket.onclose = () => {
                this.isConnected = false;
                // 简单的重连机制
                if (this.reconnectAttempts < 5) {
                    this.reconnectAttempts++;
                    setTimeout(() => this.connect(), 3000);
                }
            };

            this.socket.onerror = (err) => {
                console.error('WebSocket Error', err);
            };
        },

        disconnect() {
            if (this.socket) {
                this.socket.close();
                this.socket = null;
                this.isConnected = false;
            }
        },

        handleMessage(data: any) {
            if (data.type === 'SYSTEM_NOTICE') {
                ElNotification({
                    title: `📢 新通知：${data.title}`,
                    // 如果内容是HTML，可以使用 dangerouslyUseHTMLString: true
                    message: data.content.length > 50 ? data.content.substring(0, 50) + '...' : data.content,
                    type: 'info',
                    duration: 5000, // 5秒后自动关闭
                    position: 'top-right'
                });
            }
            // 未来可扩展：如 EXAM_GRADED (成绩发布) 等
        }
    }
});