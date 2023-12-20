package com.zz.zmovie.exception;

import com.zz.zmovie.utils.ResultStatus;

//自定义异常类，接受所抛出的异常
public class GlobleException extends RuntimeException {

    private ResultStatus status;

    public GlobleException(ResultStatus status){
        super();
        this.status = status;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

}
