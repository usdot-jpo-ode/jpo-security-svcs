package us.dot.its.jpo.sec.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SignatureExceptionHandlerTest {

    @Test
    void testHandleSignatureControllerException_ReturnsCorrectResponse() {
        // Arrange
        SignatureExceptionHandler handler = new SignatureExceptionHandler();
        String exceptionMessage = "Signature validation failed";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        SignatureControllerException mockException = new SignatureControllerException(exceptionMessage, status);

        // Act
        ResponseEntity<ApiError> response = handler.handleSignatureControllerException(mockException);

        // Assert
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals(exceptionMessage, response.getBody().getMessage());
    }
}