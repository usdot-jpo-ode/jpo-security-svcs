package us.dot.its.jpo.sec.mock.signer;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock-signer")
public class MockSignatureController {

    @PostMapping(value = "/sign", produces = "application/json")
    public SignatureResponse signTIM(SignatureRequest request) {
        return new SignatureResponse(request);
    }
}
