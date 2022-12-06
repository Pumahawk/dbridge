package com.pumahawk.dbridge.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProjectException extends ResponseStatusException {

    private String responseMessage;

    public ProjectException(HttpStatus status, String message, Throwable throwable) {
        super(status, message, throwable);
        setResponseMessage(message);
    }

    public ProjectException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

}
