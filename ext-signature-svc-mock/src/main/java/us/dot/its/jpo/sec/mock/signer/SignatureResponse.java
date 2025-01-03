package us.dot.its.jpo.sec.mock.signer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignatureResponse {
    @JsonProperty("message-signed")
    private String messageSigned;
    @JsonProperty("message-expiry")
    private String messageExpiry;

    public SignatureResponse(SignatureRequest request) {

    }
}
