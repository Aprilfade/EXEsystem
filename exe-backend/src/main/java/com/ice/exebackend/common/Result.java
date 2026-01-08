package com.ice.exebackend.common;

import java.util.Objects;

public class Result<T> {

    private int code;//编码 200/400
    private String msg;//成功/失败
    private Long total;//总记录数
    private T data;//数据

    // 手动添加setter和getter方法，确保编译通过
    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Long getTotal() {
        return total;
    }

    public T getData() {
        return data;
    }

    public static <T> Result<T> fail(){
        return result(400,"失败",0L,null);
    }

    /**
     * 返回一个带有自定义失败消息的Result对象
     * @param msg 自定义的失败消息
     * @return Result
     */
    public static <T> Result<T> fail(String msg) {
        return result(400, msg, 0L, null);
    }

    public static <T> Result<T> suc(){
        return result(200,"成功",0L,null);
    }

    public static <T> Result<T> suc(T data){
        return result(200,"成功",0L,data);
    }

    public static <T> Result<T> suc(T data,Long total){
        return result(200,"成功",total,data);
    }

    /**
     * Alias for suc() - returns success result with no data
     */
    public static <T> Result<T> ok() {
        return suc();
    }

    /**
     * Alias for suc(Object) - returns success result with data
     */
    public static <T> Result<T> ok(T data) {
        return suc(data);
    }

    /**
     * Alias for suc(Object, Long) - returns success result with data and total
     */
    public static <T> Result<T> ok(T data, Long total) {
        return suc(data, total);
    }

    private static <T> Result<T> result(int code,String msg,Long total,T data){
        Result<T> res = new Result<>();
        res.setData(data);
        res.setMsg(msg);
        res.setCode(code);
        res.setTotal(total);
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result<?> result = (Result<?>) o;
        return code == result.code &&
                Objects.equals(msg, result.msg) &&
                Objects.equals(total, result.total) &&
                Objects.equals(data, result.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, msg, total, data);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", total=" + total +
                ", data=" + data +
                '}';
    }
}
