package com.example.demo.exception;

public class ExternalServiceException extends RuntimeException{
    private String errorCode;

    public ExternalServiceException(String message,String errorCode){
        super(message);
        this.errorCode=errorCode;
    }

    public String getErrorCode(){
        return this.errorCode;
    }
}
