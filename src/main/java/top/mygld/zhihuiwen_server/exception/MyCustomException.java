package top.mygld.zhihuiwen_server.exception;

/**
 * 自定义异常类，用于表示特定业务异常
 */
public class MyCustomException extends RuntimeException {

    private final int code;

    public MyCustomException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
