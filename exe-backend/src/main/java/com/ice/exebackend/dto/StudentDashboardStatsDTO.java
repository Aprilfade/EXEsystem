package com.ice.exebackend.dto;

import lombok.Data;

@Data
public class StudentDashboardStatsDTO {
    /**
     * 累计答题总数
     */
    private long totalAnswered;

    /**
     * 平均正确率 (0-100)
     */
    private int averageAccuracy;

    /**
     * 错题总数
     */
    private long wrongRecordCount;

    /**
     * 学习时长（小时）
     */
    private int studyDurationHours;

    public long getTotalAnswered(){
        return totalAnswered;
    }
    public void setTotalAnswered(long totalAnswered){
        this.totalAnswered = totalAnswered;
    }
    public int getAverageAccuracy(){
        return averageAccuracy;
    }
    public void setAverageAccuracy(int averageAccuracy){
        this.averageAccuracy = averageAccuracy;
    }
    public long getWrongRecordCount(){
        return wrongRecordCount;
    }
    public void setWrongRecordCount(long wrongRecordCount){
        this.wrongRecordCount = wrongRecordCount;
    }
    public int getStudyDurationHours(){
        return studyDurationHours;
    }
    public void setStudyDurationHours(int studyDurationHours){
        this.studyDurationHours = studyDurationHours;
    }
}