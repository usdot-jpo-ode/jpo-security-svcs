package us.dot.its.jpo.sec.mock.signer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/mock-signer")
public class MockSignatureController {

    @PostMapping(value = "/sign", produces = "application/json")
    public SignatureResponse signTIM(@RequestBody SignatureRequest request) {
        log.debug("Received: {}", request);
        var response = new SignatureResponse.SignatureResponseBuilder()
                .messageSigned(request.getMessage() + UUID.randomUUID().toString().substring(0, 8))
                .messageExpiry(request.getSigValidityOverride()).build();
        log.debug("Returning: {}", response);
        return response;
    }
}
