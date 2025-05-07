package us.dot.its.jpo.sec.mock.signer;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class SignatureRequest {
    private String message;
    private String sigValidityOverride;
}
