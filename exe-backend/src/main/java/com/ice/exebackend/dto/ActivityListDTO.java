package com.ice.exebackend.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 活动列表 DTO
 *
 * @author ice
 */
public class ActivityListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 活动列表
     */
    private List<RecentActivityDTO> activities;

    /**
     * 总数
     */
    private Integer total;

    // Getters and Setters
    public List<RecentActivityDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<RecentActivityDTO> activities) {
        this.activities = activities;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityListDTO that = (ActivityListDTO) o;
        return Objects.equals(activities, that.activities) &&
                Objects.equals(total, that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activities, total);
    }

    @Override
    public String toString() {
        return "ActivityListDTO{" +
                "activities=" + activities +
                ", total=" + total +
                '}';
    }
}
