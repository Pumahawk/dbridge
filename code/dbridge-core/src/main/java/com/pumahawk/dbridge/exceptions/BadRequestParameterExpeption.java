package com.pumahawk.dbridge.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestParameterExpeption extends ProjectException {

    public BadRequestParameterExpeption(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
    
}
