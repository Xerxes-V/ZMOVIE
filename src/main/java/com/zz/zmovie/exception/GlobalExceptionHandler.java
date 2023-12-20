package com.zz.zmovie.exception;

import com.zz.zmovie.utils.ResultGeekQ;
import com.zz.zmovie.utils.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//异常捕捉
@ControllerAdvice
public class GlobalExceptionHandler {

    private static Logger logger =  LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 拦截抛出的错误，
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(value=Exception.class)
    @ResponseBody
    public ResultGeekQ<String> exceptionHandler(HttpServletRequest request , Exception e){

        e.printStackTrace();
        if(e instanceof GlobleException){
            //   内核异常
            GlobleException ex = (GlobleException)e;
            //返回结果信息
            return ResultGeekQ.error(ex.getStatus());
        }else if( e instanceof BindException){
            // 绑定异常
            BindException ex = (BindException) e  ;
            List<ObjectError> errors = ex.getAllErrors();

            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            /**
             * 打印堆栈信息
             */
            logger.error(String.format(msg, msg));
            return ResultGeekQ.error(ResultStatus.FAILURE);
        }else {
            return ResultGeekQ.error(ResultStatus.FAILURE);
        }
    }
}
