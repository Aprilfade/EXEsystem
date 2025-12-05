import { defineStore } from 'pinia';
import { ref } from 'vue';
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

    // === 【新增】进阶玩法状态 ===
    const myCombo = ref(0); // 当前连击数
    const myScoreChange = ref(0); // 本轮实际得分（含速度和连击加成）
    // 本地模拟初始库存 (与后端 BattleRoom 构造函数保持一致)
    const myItems = ref<Record<string, number>>({ 'FOG': 1, 'HINT': 1 });
    const activeEffects = ref<string[]>([]); // 当前生效的效果，如 ['FOG']
    const excludedOptions = ref<string[]>([]); // 被排除的选项 Key，如 ['C']

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
                // 重置所有游戏状态
                myScore.value = 0;
                oppScore.value = 0;
                currentRound.value = 0;
                myCombo.value = 0;
                myItems.value = { 'FOG': 1, 'HINT': 1 };
                activeEffects.value = [];
                excludedOptions.value = [];
                break;

            case 'QUESTION':
                gameState.value = 'PLAYING';
                currentQuestion.value = msg.data;
                currentRound.value = msg.data.round;
                totalRound.value = msg.data.total;
                roundResult.value = null;
                // 新回合清除临时效果
                excludedOptions.value = [];
                // 迷雾通常持续几秒，这里可以选择由后端控制或简单地在切题时清除
                // 如果后端发送 duration，可以不在这里立刻清除，依靠 setTimeout 自动清除
                // 为了保险，切题时清除所有负面状态
                activeEffects.value = [];
                break;

            case 'ROUND_RESULT':
                gameState.value = 'ROUND_RESULT';
                roundResult.value = msg.data;
                myScore.value = msg.data.myScore;
                oppScore.value = msg.data.oppScore;
                // 【新增】更新连击和本轮得分
                myCombo.value = msg.data.combo;
                myScoreChange.value = msg.data.scoreChange;
                break;

            case 'GAME_OVER':
                gameState.value = 'GAME_OVER';
                finalResult.value = msg.data;
                break;

            case 'OPPONENT_LEFT':
                ElMessage.warning('对手已离开');
                opponent.value.name = '对手已断线';
                break;

            // 【新增】处理道具效果通知
            case 'ITEM_EFFECT':
                if (msg.data.effect === 'FOG') {
                    activeEffects.value.push('FOG');
                    ElMessage.warning({ message: '对手使用了【迷雾卡】，视线受阻！', duration: 2000 });
                    // 设置定时器自动移除效果
                    setTimeout(() => {
                        activeEffects.value = activeEffects.value.filter(e => e !== 'FOG');
                    }, msg.data.duration || 3000);
                } else if (msg.data.effect === 'HINT') {
                    if (msg.data.wrongOption) {
                        excludedOptions.value.push(msg.data.wrongOption);
                        ElMessage.success({ message: '【提示卡】生效，排除了一个错误选项', duration: 2000 });
                    }
                }
                break;

            // 【新增】处理道具使用成功回执
            case 'ITEM_USED_SUCCESS':
                const itemType = msg.data;
                if (myItems.value[itemType] > 0) {
                    myItems.value[itemType]--;
                }
                break;
        }
    };

    // 动作：开始匹配
    const startMatch = () => {
        if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
            connect();
            setTimeout(() => {
                socket.value?.send(JSON.stringify({ type: 'MATCH' }));
                gameState.value = 'MATCHING';
            }, 500);
        } else {
            socket.value.send(JSON.stringify({ type: 'MATCH' }));
            gameState.value = 'MATCHING';
        }
        opponent.value = { name: '寻找对手...', avatar: '' };
    };

    // 动作：提交答案
    const submitAnswer = (key: string) => {
        if (socket.value?.readyState === WebSocket.OPEN) {
            socket.value.send(JSON.stringify({ type: 'ANSWER', data: key }));
        }
    };

    // 【新增】动作：使用道具
    const useItem = (itemType: string) => {
        // 简单的本地校验
        if (myItems.value[itemType] <= 0) return;

        if (socket.value?.readyState === WebSocket.OPEN) {
            socket.value.send(JSON.stringify({ type: 'USE_ITEM', data: itemType }));
        }
    };

    // 动作：离开
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
        // 新增状态导出
        myCombo,
        myScoreChange,
        myItems,
        activeEffects,
        excludedOptions,
        // 方法导出
        connect,
        startMatch,
        submitAnswer,
        useItem,
        leave
    };
});