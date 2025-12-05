// src/stores/battle.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useStudentAuthStore } from './studentAuth';
import { ElMessage } from 'element-plus';

export const useBattleStore = defineStore('battle', () => {
    const studentStore = useStudentAuthStore();
    const socket = ref<WebSocket | null>(null);

    // 游戏状态: IDLE, MATCHING, PLAYING, ROUND_RESULT, GAME_OVER
    const gameState = ref('IDLE');

    // 核心数据
    const opponent = ref<any>({ name: '寻找对手...', avatar: '', points: 0 });
    const currentQuestion = ref<any>(null);
    const roundResult = ref<any>(null);
    const finalResult = ref<any>(null);

    // 分数与进度
    const myScore = ref(0);
    const oppScore = ref(0);
    const currentRound = ref(0);
    const totalRound = ref(0);

    // 连接 WebSocket
    const connect = () => {
        if (socket.value?.readyState === WebSocket.OPEN) return;

        const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
        const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
        const domain = apiBase.replace(/^https?:\/\//, '');
        const url = `${wsProtocol}://${domain}/ws/battle?token=${studentStore.token}`;

        socket.value = new WebSocket(url);

        socket.value.onopen = () => {
            console.log('⚔️ Battle WS Connected');
        };

        socket.value.onmessage = (event) => {
            try {
                const msg = JSON.parse(event.data);
                handleMessage(msg);
            } catch (e) {
                console.error('Battle Msg Error', e);
            }
        };

        socket.value.onclose = () => {
            console.log('Battle WS Closed');
            // 如果非正常结束且还在游戏中，可以在此处理重连逻辑
            if (gameState.value !== 'IDLE' && gameState.value !== 'GAME_OVER') {
                gameState.value = 'IDLE';
                ElMessage.warning('连接已断开');
            }
        };
    };

    // 消息处理中心
    const handleMessage = (msg: any) => {
        switch (msg.type) {
            case 'MATCH_SUCCESS':
                gameState.value = 'PLAYING';
                opponent.value = msg.data.opponent || { name: '神秘对手' };
                myScore.value = 0;
                oppScore.value = 0;
                currentRound.value = 0;
                break;

            case 'QUESTION':
                gameState.value = 'PLAYING';
                currentQuestion.value = msg.data;
                currentRound.value = msg.data.round;
                totalRound.value = msg.data.total;
                roundResult.value = null; // 清除上一轮结果
                break;

            case 'ROUND_RESULT':
                gameState.value = 'ROUND_RESULT';
                roundResult.value = msg.data;
                // 更新分数
                myScore.value = msg.data.myScore;
                oppScore.value = msg.data.oppScore;
                break;

            case 'GAME_OVER':
                gameState.value = 'GAME_OVER';
                finalResult.value = msg.data;
                break;

            case 'OPPONENT_LEFT':
                ElMessage.warning('对手已离开');
                opponent.value.name = '对手已断线';
                break;
        }
    };

    // 动作：开始匹配
    const startMatch = () => {
        if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
            connect();
            // 等待连接后发送，这里简单处理用 setTimeout，生产环境可用 Promise
            setTimeout(() => {
                socket.value?.send(JSON.stringify({ type: 'MATCH' }));
                gameState.value = 'MATCHING';
            }, 500);
        } else {
            socket.value.send(JSON.stringify({ type: 'MATCH' }));
            gameState.value = 'MATCHING';
        }

        // 重置状态
        opponent.value = { name: '寻找对手...', avatar: '' };
    };

    // 动作：提交答案
    const submitAnswer = (key: string) => {
        if (socket.value?.readyState === WebSocket.OPEN) {
            socket.value.send(JSON.stringify({ type: 'ANSWER', data: key }));
        }
    };

    // 动作：离开/取消
    const leave = () => {
        socket.value?.close();
        gameState.value = 'IDLE';
    };

    return {
        gameState,
        opponent,
        currentQuestion,
        roundResult,
        finalResult,
        myScore,
        oppScore,
        currentRound,
        totalRound,
        connect,
        startMatch,
        submitAnswer,
        leave
    };
});