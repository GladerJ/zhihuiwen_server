package top.mygld.zhihuiwen_server.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回结果类
 *
 * @param <T> 返回的数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 返回的数据
     */
    private T data;

    /**
     * 成功时的返回
     *
     * @param data 返回的数据
     * @param <T>  返回的数据类型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 失败时的返回
     *
     * @param message 错误信息
     * @param <T>     返回的数据类型
     * @return Result
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}