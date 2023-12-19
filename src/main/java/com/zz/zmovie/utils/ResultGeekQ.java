package com.zz.zmovie.utils;



import lombok.Data;

import java.io.Serializable;

//返回结果信息类,封装返回给前端的数据集合。
@Data
public class ResultGeekQ<T> implements Serializable {
    private static final long serialVersionUID = 867933019328199779L;
    private T data;     //另外的信息
    int code;           //异常码
    String message;     //异常描述信息

    private ResultGeekQ(T data,ResultStatus status, String message) {
        this.data = data;
        this.code = status.getCode();
        this.message = message;
    }

    private ResultGeekQ(ResultStatus status, T data) {
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = data;
    }

    private ResultGeekQ(ResultStatus status) {
        this.code = status.getCode();
        this.message = status.getMessage();
    }

    //直接返回成功的状态信息
    public static <T> ResultGeekQ<T> build() {
        return new ResultGeekQ(ResultStatus.SUCCESS);
    }

    //携带信息的成功状态码，
    public static <T> ResultGeekQ<T> build(T data) {
        return new ResultGeekQ(ResultStatus.SUCCESS, data);
    }


    //业务处理成功：
    public static <T> ResultGeekQ<T> done(ResultStatus status) {
        return new ResultGeekQ<T>(status);
    }
    public static <T> ResultGeekQ<T> done(ResultStatus status,T data) {
        return new ResultGeekQ<T>(status,data);
    }

    //    失败/异常信息
    public static <T> ResultGeekQ<T> error(ResultStatus status) {
        return new ResultGeekQ<T>(status);
    }

    public static <T> ResultGeekQ<T> error(ResultStatus status,T data) {
        return new ResultGeekQ<T>(status,data);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    public void success(T value) {
        this.code = ResultStatus.SUCCESS.getCode();
        this.data = value;
    }
}
