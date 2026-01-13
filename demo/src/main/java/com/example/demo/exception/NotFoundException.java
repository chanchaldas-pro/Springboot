package com.example.demo.exception;
public class NotFoundException extends RuntimeException {


    public String errorCode;

    public NotFoundException(String message,String errorCode) {

        super(message);
        this.errorCode=errorCode;
    }

    public String getErrorCode(){
        return this.errorCode;
    }
}

