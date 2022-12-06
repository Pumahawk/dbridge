package com.pumahawk.dbridge.endpoints;

public class ErrorResponse extends BasicResponse {
    private String message;
    public ErrorResponse(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
