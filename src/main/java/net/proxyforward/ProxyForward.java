package net.proxyforward;

import net.proxyforward.config.Config;
import net.proxyforward.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProxyForward {

    public static final Logger LOGGER = LoggerFactory.getLogger("proxyforward");

    private static ProxyForward instance;

    private final Config config;

    // todo: implement crossstitch, test older version, move velocity code out of mixin classes
    public ProxyForward() {
        instance = this;
        this.config = ConfigLoader.load();

        LOGGER.info("ProxyForward initialized ({} forwarding mode)", config.forwardingMode());

        if (config.velocity() && (config.velocitySecret() == null || config.velocitySecret().isEmpty())) {
            LOGGER.warn("Velocity modern forwarding mode is enabled but no secret is set! Please set a secret in config/proxyforward.toml or using the PROXYFORWARD_VELOCITY_SECRET environment variable.");
        }
    }

    public static ProxyForward instance() {
        return instance;
    }

    public Config config() {
        return config;
    }
}
