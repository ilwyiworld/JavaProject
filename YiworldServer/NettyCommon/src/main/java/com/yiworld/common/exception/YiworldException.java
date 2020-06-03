package com.yiworld.common.exception;

import com.yiworld.common.enums.StatusEnum;

public class YiworldException extends GenericException {
    public YiworldException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public YiworldException(Exception e, String errorCode, String errorMessage) {
        super(e, errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public YiworldException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public YiworldException(StatusEnum statusEnum) {
        super(statusEnum.getMessage());
        this.errorMessage = statusEnum.message();
        this.errorCode = statusEnum.getCode();
    }

    public YiworldException(StatusEnum statusEnum, String message) {
        super(message);
        this.errorMessage = message;
        this.errorCode = statusEnum.getCode();
    }

    public YiworldException(Exception oriEx) {
        super(oriEx);
    }

    public YiworldException(Throwable oriEx) {
        super(oriEx);
    }

    public YiworldException(String message, Exception oriEx) {
        super(message, oriEx);
        this.errorMessage = message;
    }

    public YiworldException(String message, Throwable oriEx) {
        super(message, oriEx);
        this.errorMessage = message;
    }


    public static boolean isResetByPeer(String msg) {
        if ("Connection reset by peer".equals(msg)) {
            return true;
        }
        return false;
    }
}
