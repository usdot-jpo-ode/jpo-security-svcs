package us.dot.its.jpo.sec.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("message")
    private String msg;

    @JsonProperty("sigValidityOverride")
    private int sigValidityOverride = 0;

    public Message() {
        super();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getSigValidityOverride() {
        return sigValidityOverride;
    }

    public void setSigValidityOverride(int sigValidityOverride) {
        this.sigValidityOverride = sigValidityOverride;
    }
 }