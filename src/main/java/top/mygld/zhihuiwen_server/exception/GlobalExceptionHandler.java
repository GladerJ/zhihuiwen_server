package top.mygld.zhihuiwen_server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.mygld.zhihuiwen_server.common.Result;

/**
 * 全局异常处理器，用于统一处理各类异常，并返回统一格式的结果
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获所有未处理的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        // 这里可以添加日志记录逻辑
        return new ResponseEntity<>(Result.error("服务器内部错误：" + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 捕获自定义异常
     */
    @ExceptionHandler(MyCustomException.class)
    public ResponseEntity<Result<Object>> handleMyCustomException(MyCustomException e) {
        // 如需使用自定义状态码，可直接调用构造器
        return new ResponseEntity<>(new Result<>(e.getCode(), e.getMessage(), null),
                HttpStatus.BAD_REQUEST);
    }
}
