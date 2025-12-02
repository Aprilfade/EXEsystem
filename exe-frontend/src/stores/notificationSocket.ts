import { defineStore } from 'pinia';
import { ElNotification } from 'element-plus';
import { useAuthStore } from './auth';
import { useStudentAuthStore } from './studentAuth';

export const useNotificationSocketStore = defineStore('notificationSocket', {
    state: () => ({
        socket: null as WebSocket | null,
        isConnected: false,
        reconnectAttempts: 0,
        // 添加心跳检测定时器
        heartbeatTimer: null as number | null,
        // 【新增】存储在线学生人数
        onlineStudentCount: 0,
    }),
    actions: {
        connect() {
            // 如果已经连接或正在连接，则跳过
            if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
                return;
            }

            // 1. 获取 Token (优先判断学生，再判断管理员)
            const studentStore = useStudentAuthStore();
            const adminStore = useAuthStore();
            // 确保获取到的 token 是字符串
            const token = studentStore.token || adminStore.token;

            if (!token) return;

            // 2. 动态构建 WebSocket URL
            // 读取环境变量中的 API 地址，例如 "http://localhost:8080"
            const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

            // 自动判断协议：如果是 https 则用 wss，否则用 ws
            const wsProtocol = window.location.protocol === 'https:' || apiBase.startsWith('https') ? 'wss' : 'ws';

            // 去掉 http:// 或 https:// 前缀，保留域名和端口
            const domain = apiBase.replace(/^https?:\/\//, '');

            // 拼接完整地址
            const wsUrl = `${wsProtocol}://${domain}/ws/notifications?token=${token}`;

            // console.log('Connecting to WebSocket:', wsUrl); // 调试用

            this.socket = new WebSocket(wsUrl);

            // 3. 事件监听
            this.socket.onopen = () => {
                console.log('🔔 实时通知服务已连接');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                // 开启心跳（可选，如果后端有超时断开机制）
                // this.startHeartbeat();
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
                this.socket = null;
                // 简单的重连机制
                if (this.reconnectAttempts < 5) {
                    this.reconnectAttempts++;
                    // 指数退避重连：1s, 2s, 4s...
                    const timeout = Math.pow(2, this.reconnectAttempts) * 1000;
                    console.log(`WebSocket断开，${timeout/1000}秒后尝试重连...`);
                    setTimeout(() => this.connect(), timeout);
                }
            };

            this.socket.onerror = (err) => {
                console.error('WebSocket Error', err);
                // 出错时 socket 通常会自动关闭并触发 onclose，由 onclose 处理重连
            };
        },

        disconnect() {
            if (this.socket) {
                this.socket.close();
                this.socket = null;
            }
            this.isConnected = false;
            // 重置重连次数，防止下次手动连接时无法重连
            this.reconnectAttempts = 0;
        },

        handleMessage(data: any) {
            if (data.type === 'SYSTEM_NOTICE') {
                ElNotification({
                    title: `📢 新通知：${data.title}`,
                    message: data.content.length > 50 ? data.content.substring(0, 50) + '...' : data.content,
                    type: 'info',
                    duration: 5000,
                    position: 'top-right',
                    // 点击通知跳转（可选优化）
                    // onClick: () => { router.push('/notifications') }
                });
            }
            // 【新增】处理在线人数更新消息
            else if (data.type === 'ONLINE_COUNT') {
                this.onlineStudentCount = data.count;
            }
        }
    }
});