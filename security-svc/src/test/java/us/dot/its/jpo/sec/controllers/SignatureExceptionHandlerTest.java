package us.dot.its.jpo.sec.controllers;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
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
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(exceptionMessage);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}