package us.dot.its.jpo.sec.mock.signer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class SignatureResponse {
    @JsonProperty("message-signed")
    private String messageSigned;
    @JsonProperty("message-expiry")
    private String messageExpiry;
}
