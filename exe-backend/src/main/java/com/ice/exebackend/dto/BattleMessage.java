package com.ice.exebackend.dto;


public class BattleMessage {
    // 消息类型: MATCH(匹配), MATCH_SUCCESS(匹配成功), QUESTION(发题), ANSWER(答题),
    // RESULT(单题结果), GAME_OVER(游戏结束), OPPONENT_LEFT(对手离开)
    private String type;

    private Object data; // 负载数据

    // 辅助构造方法
    public static BattleMessage of(String type, Object data) {
        BattleMessage msg = new BattleMessage();
        msg.setType(type);
        msg.setData(data);
        return msg;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
}