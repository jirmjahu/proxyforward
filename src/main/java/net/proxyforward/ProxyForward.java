package net.proxyforward;

import net.proxyforward.config.Config;
import net.proxyforward.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProxyForward {

    public static final Logger LOGGER = LoggerFactory.getLogger("proxyforward");

    private static ProxyForward instance;

    private final Config config;

    // todo: warn when using online mode, implement bungee forward, implement crossstitch, test older version
    public ProxyForward() {
        instance = this;
        this.config = ConfigLoader.load();

        LOGGER.info("ProxyForward initialized ({} forwarding mode)", config.forwardingMode());
    }

    public static ProxyForward instance() {
        return instance;
    }

    public Config config() {
        return config;
    }
}
