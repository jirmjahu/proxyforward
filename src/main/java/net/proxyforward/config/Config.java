package net.proxyforward.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Config {

    @JsonProperty("forwarding_mode")
    private String forwardingMode = "bungee";

    @JsonProperty("velocity_secret")
    private String velocitySecret = "";

    public String forwardingMode() {
        return forwardingMode;
    }

    public void forwardingMode(String forwardingMode) {
        this.forwardingMode = forwardingMode;
    }

    public String velocitySecret() {
        return velocitySecret;
    }

    public void velocitySecret(String velocitySecret) {
        this.velocitySecret = velocitySecret;
    }

    public boolean velocity() {
        return forwardingMode.equals("velocity");
    }

    public boolean bungee() {
        return forwardingMode.equals("bungee");
    }
}
