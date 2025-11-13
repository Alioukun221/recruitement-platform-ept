package com.ept.sn.cri.backend.exception;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(String message) {
        super(message);
    }
}