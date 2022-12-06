package com.pumahawk.dbridge.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundRoute extends ProjectException {
    public NotFoundRoute() {
        super(HttpStatus.NOT_FOUND, "Route not found");
    }
}
