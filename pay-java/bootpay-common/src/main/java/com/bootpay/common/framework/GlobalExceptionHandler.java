package com.bootpay.common.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.ReturnMsg;


/**
 * @author futao
 * Created on 2018/9/21-15:13.
 * 异常统一处理，
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object logicExceptionHandler(HttpServletRequest request, Exception e, HttpServletResponse response) {
        //系统级异常，错误码固定为-1，提示语固定为系统繁忙，请稍后再试
        ReturnMsg result = new ReturnMsg();
        //如果是业务逻辑异常，返回具体的错误码与提示信息
        if (e instanceof TranException) {
        	TranException logicException = (TranException) e;
            result.setCode(logicException.getCode());
            result.setMessage(logicException.getMessage());
            //Validator验证框架抛出的业务逻辑异常
        }else if(e instanceof ConstraintViolationException){
        	 String message = ((ConstraintViolationException) e).getConstraintViolations().iterator().next().getConstraintDescriptor().getMessageTemplate();
             result.setCode(CodeConst.SYSTEM_ERROR_CODE);
             result.setMessage(message);
        }else {
        	result.setCode(CodeConst.SYSTEM_ERROR_CODE);
            result.setMessage("系统异常");
            //对系统级异常进行日志记录
            logger.error("系统异常:" + e.getMessage(), e);
        }
        return JSONObject.toJSON(result);
    }
}