package com.lgx.codehelper.util;

import com.lgx.codehelper.common.base.Result;
import com.lgx.codehelper.common.base.ResultStatus;
import com.lgx.codehelper.common.exception.ApiException;

/**
 * @author 13360
 * @version 1.0
 * @date 2023-12-05 10:23
 */
public class ResultUtil {

    public static Result<Object> returnOrThrow(Boolean success, String message) {
        if (Boolean.FALSE.equals(success)) {
            throw new ApiException(ResultStatus.FAIL, message);
        }
        return Result.build(ResultStatus.SUCCESS);
    }

}
