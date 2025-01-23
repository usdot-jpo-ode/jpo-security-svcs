package us.dot.its.jpo.sec.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SignatureExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SignatureExceptionHandler.class);

    @ExceptionHandler(SignatureControllerException.class)
    public ResponseEntity<ApiError> handleSignatureControllerException(SignatureControllerException e) {
        logger.error("handleSignatureControllerException: {}", e.getMessage());
        ApiError apiError = new ApiError();
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(e.getStatus()).body(apiError);
    }
}
