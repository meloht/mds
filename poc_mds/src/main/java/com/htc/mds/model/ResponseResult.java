package com.htc.mds.model;

import org.springframework.lang.Nullable;

public class ResponseResult<T> extends ResultBase {

    private T data;

    public ResponseResult(@Nullable T data, String messageCode) {
        this(data,messageCode,null);
    }

    public ResponseResult(String messageCode, String message) {
        this(null,messageCode,null);
    }

    public ResponseResult(@Nullable T data, String messageCode, String message) {
        this.setData(data);
        this.setCode(messageCode);
        this.setMessage(message);
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ResponseResult<T> Result(String messageCode){
        return Result(messageCode,messageCode);
    }
    public static <T> ResponseResult<T> Result(String messageCode,T data){

        return Result(messageCode,messageCode,data);
    }
    public static <T> ResponseResult<T> Result(String messageCode,String message){

        return Result(messageCode,message,null);
    }

    public static <T> ResponseResult<T> Result(String messageCode,String message,T data){
        ResponseResult<T> result=new ResponseResult<T>(data,messageCode,message);
        return result;
    }

}
