package com.zz.zmovie.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ResultStatus {
    LOGIN_SUCCESS(200001,"登录成功"),
    LOGIN_FAILURE(200002,"登录失败"),
    PWD_WRONG(200002,"密码错误"),
    LOGIN_EMPTY_ACCOUNT(200003,"无此账号"),
    USER_STATUS_BAN(200004,"该账号已被封禁！"),
    EMPTY_LABEL(200005,"需要选择类型"),
    REGISTER_SUCCESS(200011,"注册成功"),
    REGISTER_FAILURE(200012,"账号已经注册"),

    UPDATE_SUCCESS(400001,"修改成功！"),
    UPDATE_FAILURE(400002,"修改失败！"),

    FOUND(300001,"查找成功！"),
    NOT_FOUND(300002,"找不到你所寻找的信息"),
    SEARCH_ERROR(300003,"查找异常！"),

    SUCCESS(100001,"成功"),
    FAILURE(100002,"失败"),

    REDIS_SET_ADD_ERROR(500001,"redis添加set列表失败");


    private int code;
    private String message;

    private ResultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
