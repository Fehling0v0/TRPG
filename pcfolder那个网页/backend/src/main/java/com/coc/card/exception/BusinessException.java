package com.coc.card.exception;

/**
 * 业务异常：携带 HTTP 状态码与可读消息，由全局异常处理器统一转换
 */
public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    public int getStatus() {
        return status;
    }
}
