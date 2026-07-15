package net.proxyforward.config;

import net.proxyforward.utils.JacksonUtils;
import net.proxyforward.utils.ResourceFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    private static final Path CONFIG_PATH = Path.of("config", "proxyforward.toml");

    private ConfigLoader() {
    }

    public static Config load() {
        if (Files.notExists(CONFIG_PATH)) {
            try {
                Files.createDirectories(CONFIG_PATH.getParent());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create config directory!", e);
            }
            ResourceFileUtils.copyResourceFile("config.toml", CONFIG_PATH);
        }
        final Config config = JacksonUtils.TOML_MAPPER.readValue(CONFIG_PATH.toFile(), Config.class);
        applyEnvironmentValues(config);
        return config;
    }

    private static void applyEnvironmentValues(Config config) {
        final String mode = System.getenv("PROXYFORWARD_FORWARDING_MODE");
        if (mode != null && !mode.isEmpty()) {
            config.forwardingMode(mode);
        }

        final String secret = System.getenv("PROXYFORWARD_VELOCITY_SECRET");
        if (secret != null && !secret.isEmpty()) {
            config.velocitySecret(secret);
        }
    }
}
