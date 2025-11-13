package com.ept.sn.cri.backend.exception;

public class UnauthorizedActionException extends RuntimeException {
    public  UnauthorizedActionException(String message) {
        super(message);
    }
}
