package com.crud.lab_springboot.projo;

import org.springframework.http.HttpStatus;

public class ResponseMessage<T> {
    private Integer code;
    private String message;
    private T data; // 泛型

    public ResponseMessage(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseMessage() {

    }

    // 封装公共的返回类型方法
    public static <T>ResponseMessage<T> success(T data){
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode(HttpStatus.OK.value());  // 200
        responseMessage.setMessage("success");  // 文本信息
        responseMessage.setData(data);  // 返回数据
        return responseMessage;
    }

    public static <T>ResponseMessage<T> success(){
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode(HttpStatus.OK.value());
        responseMessage.setMessage("success");
        responseMessage.setData(null);
        return responseMessage;
    }
    
    // 错误返回方法
    public static <T>ResponseMessage<T> error(String message){
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode(HttpStatus.BAD_REQUEST.value());  // 400
        responseMessage.setMessage(message);
        responseMessage.setData(null);
        return responseMessage;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
