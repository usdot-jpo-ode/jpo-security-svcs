package us.dot.its.jpo.sec.controllers;

import org.springframework.http.HttpStatus;

public class SignatureControllerException extends Throwable {
    private final HttpStatus status;

    public SignatureControllerException(String msg, HttpStatus status) {
        super(msg);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
