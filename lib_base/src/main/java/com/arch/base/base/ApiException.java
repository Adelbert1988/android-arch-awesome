package com.arch.base.base;

public class ApiException extends Exception {

    public int code;
    public String errorMessage;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.errorMessage = message;
    }
}
