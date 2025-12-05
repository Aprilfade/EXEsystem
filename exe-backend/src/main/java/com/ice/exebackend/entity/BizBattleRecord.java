package com.ice.exebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_battle_record")
public class BizBattleRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long playerId;      // 玩家ID
    private Long opponentId;    // 对手ID (如果是机器人，可以设为 -1 或 null)
    private String opponentName; // 冗余字段：对手名字（方便前端显示，不用联表）
    private String result;      // WIN, LOSE, DRAW
    private Integer scoreChange; // 积分变动 (e.g., 20, -10)
    private LocalDateTime createTime;

    // 手动 Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public Long getOpponentId() { return opponentId; }
    public void setOpponentId(Long opponentId) { this.opponentId = opponentId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Integer getScoreChange() { return scoreChange; }
    public void setScoreChange(Integer scoreChange) { this.scoreChange = scoreChange; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
}