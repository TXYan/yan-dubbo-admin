package com.yan.dubbo.admin.model;

import java.io.Serializable;


public class Response<T> implements Serializable {

    private static final long serialVersionUID = 1645399341200800226L;

    /**
     * 结果状态
     */
    private boolean success;

    private String message;

    private T data;

    private Response(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    private Response(boolean success) {
        this.success = success;
    }

    private Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", data=" + data +
                '}';
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(true, data);
    }

    public static <T> Response<T> fail() {
        return new Response<>(false);
    }

    public static <T> Response<T> fail(String msg) {
        return new Response<>(false, msg);
    }
}
