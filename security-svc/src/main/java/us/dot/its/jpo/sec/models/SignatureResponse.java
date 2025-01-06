package us.dot.its.jpo.sec.models;

public class SignatureResponse {
    private String messageSigned;
    private String messageExpiry;

    public String getMessageSigned() {
        return messageSigned;
    }

    public void setMessageSigned(String messageSigned) {
        this.messageSigned = messageSigned;
    }

    public String getMessageExpiry() {
        return messageExpiry;
    }

    public void setMessageExpiry(String messageExpiry) {
        this.messageExpiry = messageExpiry;
    }
}
