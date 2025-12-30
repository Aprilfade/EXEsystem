package com.ice.exebackend.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class BattleRoomData implements Serializable {
    private String roomId;
    private String p1Id; // 玩家1的用户ID
    private String p2Id; // 玩家2的用户ID（如果是人机，这里可能是特定的机器人ID）
    // 你可以根据需要添加更多字段，比如当前题目索引、分数等


    public String getP1Id() {
        return p1Id;
    }

    public String getRoomId() {
        return roomId;
    }
    public String getP2Id() {
        return p2Id;
    }
    public void setP1Id(String p1Id) {
        this.p1Id = p1Id;
    }
    public void setP2Id(String p2Id) {
        this.p2Id = p2Id;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}