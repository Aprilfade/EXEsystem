package com.ice.exebackend.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 待办事项列表 DTO
 *
 * @author ice
 */
public class TodoListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 待办事项列表
     */
    private List<TodoItemDTO> items;

    /**
     * 总数量
     */
    private Integer totalCount;

    // Getters and Setters
    public List<TodoItemDTO> getItems() {
        return items;
    }

    public void setItems(List<TodoItemDTO> items) {
        this.items = items;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoListDTO that = (TodoListDTO) o;
        return Objects.equals(items, that.items) &&
                Objects.equals(totalCount, that.totalCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, totalCount);
    }

    @Override
    public String toString() {
        return "TodoListDTO{" +
                "items=" + items +
                ", totalCount=" + totalCount +
                '}';
    }
}
