package com.lgx.codehelper.common.exception;

import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.util.ThrowableUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.AccountExpiredException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * 主要异常如下：
 * 1、请求参数校验异常处理
 * 2、权限异常处理
 * 3、自定义异常处理
 * 4、其它系统异常处理
 * @author LGX_TvT <br>
 * @version 1.0 <br>
 * Create by 2022-04-05 02:15 <br>
 * @description: GlobalExceptionHandler <br>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Throwable.class)
    public Result<Object> handleException(Throwable e){
        // 打印堆栈信息
        log.error(ThrowableUtil.getStackTrace(e));
        return Result.fail(ResultStatus.FAIL, e.getMessage());
    }


    /**
     * 处理所有接口数据验证异常
     * 需要验证的参数上加上了@Valid注解，如果验证失败，它将抛出MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        // 打印堆栈信息
        log.error(ThrowableUtil.getStackTrace(e));
        List<FieldError> fieldErrors = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
        List<String> msgList = fieldErrors.stream().map(FieldError :: getDefaultMessage).collect(Collectors.toList());
        return Result.fail(ResultStatus.FAIL, String.join(ResultStatus.FAIL.getReasonPhrase(), String.join(",", msgList)));
    }

    /**
     * 处理所有接口业务异常
     */
    @ExceptionHandler(ApiException.class)
    public Result<Object> handleApiException(ApiException e){
        // 打印堆栈信息
        log.error(ThrowableUtil.getStackTrace(e));
        return Result.fail(e.getCode(), e.getMsg());
    }

}
