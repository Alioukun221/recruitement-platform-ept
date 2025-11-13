package com.ept.sn.cri.backend.exception;

public class ApplicationNotBelongToOfferException extends RuntimeException {
    public ApplicationNotBelongToOfferException(String message) {
        super(message);
    }
}