package com.ept.sn.cri.backend.exception;

public class InvalidApplicationStatusException extends RuntimeException {
    public InvalidApplicationStatusException(String message) {
        super(message);
    }
}
