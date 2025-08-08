package com.ice.exebackend.common;


import lombok.Data;
@Data
public class Result {

    private int code;//编码 200/400
    private String msg;//成功/失败
    private Long total;//总记录数
    private Object data;//数据

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

    public void setData(Object data) {
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

    public Object getData() {
        return data;
    }

    public static Result fail(){
        return result(400,"失败",0L,null);
    }
    // --- 新增这个方法 ---
    /**
     * 返回一个带有自定义失败消息的Result对象
     * @param msg 自定义的失败消息
     * @return Result
     */
    public static Result fail(String msg) {
        return result(400, msg, 0L, null);
    }
    // --- 新增结束 ---
    public static Result suc(){
        return result(200,"成功",0L,null);
    }

    public static Result suc(Object data){
        return result(200,"成功",0L,data);
    }

    public static Result suc(Object data,Long total){
        return result(200,"成功",total,data);
    }

    private static Result result(int code,String msg,Long total,Object data){
        Result res = new Result();
        res.setData(data);
        res.setMsg(msg);
        res.setCode(code);
        res.setTotal(total);
        return res;
    }

}
