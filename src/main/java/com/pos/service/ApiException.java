package com.pos.service;
//send status also, 400: bad data
//params me add http status
public class ApiException extends Exception {
    public ApiException(String message) {
        super(message);
    }
}