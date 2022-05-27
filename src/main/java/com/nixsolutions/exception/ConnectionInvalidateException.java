package com.nixsolutions.exception;

public class ConnectionInvalidateException extends RuntimeException {

    public ConnectionInvalidateException(String message, Throwable cause) {
        super(message, cause);
    }
}
